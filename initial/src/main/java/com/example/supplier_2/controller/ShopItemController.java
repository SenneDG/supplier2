package com.example.supplier_2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.supplier_2.domain.ShopItem;
import com.example.supplier_2.domain.ShopItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/shop-items")
public class ShopItemController {

    private final ShopItemRepository shopItemRepository;
    private static final String API_KEY = "Iw8zeveVyaPNWonPNaU0213uw3g6Ei";
    private static final Logger logger = LoggerFactory.getLogger(ShopItemController.class);

    public enum TransactionStatus {
        PREPARE,
        COMMIT,
        ROLLBACK
    }

    @Autowired
    private WebClient.Builder webClientBuilder;
    ShopItemController(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void checkTransactionState() {
        try {
            String state = new String(Files.readAllBytes(Paths.get("transaction_state.txt")));
            TransactionStatus status = TransactionStatus.valueOf(state);

            switch (status) {
                case PREPARE:
                    // The server crashed after the prepare phase but before the commit or rollback phase
                    // You'll need to check with the broker to see what the final decision was
                    WebClient webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
                    Mono<Map<String, Object>> response = webClient.get()
                            .uri("/status/last-order-status")
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
                    response.subscribe(
                            orderData -> {
                                TransactionStatus lastOrderStatus = TransactionStatus.valueOf((String) orderData.get("status"));
                                System.out.println("Last order status: " + lastOrderStatus);
                                List<LinkedHashMap<String, Object>> itemsMap = (List<LinkedHashMap<String, Object>>) orderData.get("items");
                                List<ShopItem> items = itemsMap.stream()
                                        .map(itemMap -> objectMapper.convertValue(itemMap, ShopItem.class))
                                        .collect(Collectors.toList());
                                if(lastOrderStatus == TransactionStatus.COMMIT) {
                                    System.out.println("Transaction was committed");
                                    checkoutCommit(items, API_KEY);
                                } else {
                                    System.out.println("Transaction was rolled back");
                                    checkoutRollback(items, API_KEY);
                                }
                            },
                            error -> {
                                System.err.println("Error occurred: " + error.getMessage());
                            }
                    );
                    break;
                case COMMIT:
                    // The server crashed after the commit phase
                    // You can safely assume that the transaction was committed
                    System.out.println("Transaction was committed");
                    break;
                case ROLLBACK:
                    // The server crashed after the rollback phase
                    // You can safely assume that the transaction was rolled back
                    System.out.println("Transaction was rolled back");
                    break;
            }
        } catch (IOException e) {
            logger.error("Failed to read transaction state", e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllShopItems(@RequestParam(value = "key") String apiKey) {
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        var shopItems = shopItemRepository.getAllShopItems().stream()
                .map(shopItem -> EntityModel.of(shopItem,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getShopItemById(shopItem.getId(), API_KEY)).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems(apiKey)).withRel("shop-items")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(shopItems,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems(apiKey)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ShopItem>> getShopItemById(@PathVariable UUID id, @RequestParam(value = "key") String apiKey) {
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ShopItem shopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));

        EntityModel<ShopItem> shopItemModel = EntityModel.of(shopItem,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getShopItemById(id, API_KEY)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems(API_KEY)).withRel("shop-items"));

        return new ResponseEntity<>(shopItemModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ShopItem> addShopItem(@RequestBody ShopItem shopItem, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        shopItemRepository.addShopItem(shopItem);
        return new ResponseEntity<>(shopItem, HttpStatus.OK);
    }

    @PostMapping("/prepare-checkout")
    public ResponseEntity<Void> prepareCheckout(@RequestBody List<ShopItem> shopItems, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        for (ShopItem shopItem : shopItems) {
            UUID id = shopItem.getId();
            int quantity = shopItem.getQuantity();
            ShopItem existingShopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));

            int availableQuantity = existingShopItem.getQuantity() - existingShopItem.getLockedQuantity();
            if (availableQuantity < quantity) {
                logger.info("ABORT: Not enough quantity for item " + id);
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build(); // Not enough quantity
            }

            // Lock the items here, preventing other transactions from modifying them until the commit or rollback
            logger.info("COMMIT: Item " + id + " prepared for checkout");
            existingShopItem.setLockedQuantity(existingShopItem.getLockedQuantity() + quantity);
            shopItemRepository.updateShopItem(id, existingShopItem);
        }

        persistTransactionState(TransactionStatus.PREPARE);
        return ResponseEntity.ok().build(); // Vote to commit
    }

    @PostMapping("/commit-checkout")
    public synchronized ResponseEntity<Void> checkoutCommit(@RequestBody List<ShopItem> shopItems, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        for (ShopItem shopItem : shopItems) {
            UUID id = shopItem.getId();
            int quantity = shopItem.getQuantity();
            ShopItem existingShopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));
            existingShopItem.setQuantity(existingShopItem.getQuantity() - quantity);

            // Unlock the items here, allowing other transactions to modify them
            existingShopItem.setLockedQuantity(existingShopItem.getLockedQuantity() - quantity);
            shopItemRepository.updateShopItem(id, existingShopItem);

            logger.info("COMMIT: Item " + id + " checked out");
        }

        persistTransactionState(TransactionStatus.COMMIT);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback-checkout")
    public synchronized ResponseEntity<Void> checkoutRollback(@RequestBody List<ShopItem> shopItems, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        for (ShopItem shopItem : shopItems) {
            UUID id = shopItem.getId();
            ShopItem existingShopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));

            // Unlock the items here, allowing other transactions to modify them
            existingShopItem.setLockedQuantity(0);
            shopItemRepository.updateShopItem(id, existingShopItem);

            logger.info("ROLLBACK: Item " + id + " checked out");
        }

        persistTransactionState(TransactionStatus.ROLLBACK);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopItem> updateShopItem(@PathVariable UUID id, @RequestBody ShopItem updatedShopItem, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        shopItemRepository.updateShopItem(id, updatedShopItem);
        return new ResponseEntity<>(updatedShopItem, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShopItem(@PathVariable UUID id, @RequestParam(value = "key") String apiKey){
        if (!API_KEY.equals(apiKey)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        shopItemRepository.deleteShopItem(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void persistTransactionState(TransactionStatus status) {
        try {
            Files.write(Paths.get("transaction_state.txt"), status.name().getBytes());
        } catch (IOException e) {
            logger.error("Failed to persist transaction state", e);
        }
    }
}
