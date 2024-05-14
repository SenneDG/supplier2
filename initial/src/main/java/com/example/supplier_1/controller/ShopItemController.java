package com.example.supplier_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.*;

import com.example.supplier_1.domain.ShopItem;
import com.example.supplier_1.domain.ShopItemRepository;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shop-items")
public class ShopItemController {

    private final ShopItemRepository shopItemRepository;

    @Autowired
    ShopItemController(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    @GetMapping
    public CollectionModel<EntityModel<ShopItem>> getAllShopItems() {
        var shopItems = shopItemRepository.getAllShopItems().stream()
                .map(shopItem -> EntityModel.of(shopItem,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getShopItemById(shopItem.getId())).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems()).withRel("shop-items")))
                .collect(Collectors.toList());

        return CollectionModel.of(shopItems,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<ShopItem> getShopItemById(@PathVariable UUID id) {
        ShopItem shopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));

        return EntityModel.of(shopItem,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getShopItemById(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems()).withRel("shop-items"));
    }

    @PostMapping
    ShopItem addShopItem(@RequestBody ShopItem shopItem) {
        shopItemRepository.addShopItem(shopItem);
        return shopItem;
    }

    @PostMapping("/checkout")
    public synchronized EntityModel<ShopItem> checkout(@RequestParam UUID id, @RequestParam int quantity) {
        ShopItem shopItem = shopItemRepository.findShopItem(id).orElseThrow(() -> new RuntimeException("Item not found: " + id));

        if (shopItem.getQuantity() < quantity) {
            throw new RuntimeException("Not enough items in stock: " + id);
        }

        System.out.println("Checkout: " + quantity + " of " + shopItem.getName() + " (" + id + ")");

        shopItem.setQuantity(shopItem.getQuantity() - quantity);
        shopItemRepository.updateShopItem(id, shopItem);

        return EntityModel.of(shopItem,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getShopItemById(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShopItemController.class).getAllShopItems()).withRel("shop-items"));
    }

    @PutMapping("/{id}")
    ShopItem updateShopItem(@PathVariable UUID id, @RequestBody ShopItem updatedShopItem) {
        shopItemRepository.updateShopItem(id, updatedShopItem);
        return updatedShopItem;
    }

    @DeleteMapping("/{id}")
    void deleteShopItem(@PathVariable UUID id) {
        shopItemRepository.deleteShopItem(id);
    }
}