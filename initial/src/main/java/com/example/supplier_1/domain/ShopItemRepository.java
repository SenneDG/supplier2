package com.example.supplier_1.domain;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.*;

@Component
public class ShopItemRepository {

    private final Map<UUID, ShopItem> shopItems = new HashMap<>();

    private static final UUID ITEM1_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
    private static final UUID ITEM2_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174004");

    @PostConstruct
    public void init() {
        ShopItem item1 = new ShopItem();
        item3.setId(ITEM3_UUID);
        item3.setName("COCA-COLA 33CL");
        item3.setDescription("Coca-Cola 33cl is een verfrissende en iconische frisdrank die bekendstaat om zijn unieke smaak en bruisende karakter. Perfect voor elke gelegenheid en heerlijk koel geserveerd.");
        item3.setPrice(1.50);
        item3.setQuantity(20);
        item3.setImageUrl("https://goedkoopdrank.be/wp-content/uploads/2023/06/GD60.jpg");
        
        ShopItem item2 = new ShopItem();
        item4.setId(ITEM4_UUID);
        item4.setName("SPA BLÅ 50CL");
        item4.setDescription("Spa Blå 50cl is een verfrissend natuurlijk mineraalwater uit de Belgische Ardennen. Het staat bekend om zijn zuiverheid en zachte smaak, ideaal om op elk moment van de dag van te genieten.");
        item4.setPrice(1.00);
        item4.setQuantity(30);
        item4.setImageUrl("https://www.dirckiii.nl/media/catalog/product/cache/d55535bea25a3d0676d7fc260feb4497/8/f/8f1294769bfdbd1447ca63d781f13f0c4bfae269_5410013126737_Spa_Reine_Sport.jpg");
        
        ShopItem item5 = new ShopItem();
        item5.setId(ITEM5_UUID);
        item5.setName("ORANGINA 25CL");
        item5.setDescription("Orangina 25cl is een verfrissende frisdrank met een unieke mix van sinaasappelsap en echte vruchtvlees. Het staat bekend om zijn licht bruisende karakter en heerlijke, natuurlijke smaak. Perfect voor een verfrissende pauze.");
        item5.setPrice(1.75);
        item5.setQuantity(15);
        item5.setImageUrl("https://media.carrefour.fr/medias/0dcdea4544113db486517310cc4ede47/p_540x540/03249760013319-h1l1-s12.jpg");

        shopItems.put(item1.getId(), item1);
        shopItems.put(item2.getId(), item2);
        shopItems.put(item2.getId(), item3);
    }

    public Optional<ShopItem> findShopItem(UUID id) {
        return Optional.ofNullable(shopItems.get(id));
    }

    public Collection<ShopItem> getAllShopItems() {
        return shopItems.values();
    }

    public void addShopItem(ShopItem shopItem) {
        shopItems.put(shopItem.getId(), shopItem);
    }

    public void updateShopItem(UUID id, ShopItem updatedShopItem) {
        if (!shopItems.containsKey(id)) {
            throw new IllegalArgumentException("Item with id " + id + " does not exist.");
        }

        ShopItem shopItem = shopItems.get(id);
        shopItem.setName(updatedShopItem.getName());
        shopItem.setDescription(updatedShopItem.getDescription());
        shopItem.setPrice(updatedShopItem.getPrice());
        shopItem.setQuantity(updatedShopItem.getQuantity());
        shopItem.setImageUrl(updatedShopItem.getImageUrl());
        shopItem.setLockedQuantity(updatedShopItem.getLockedQuantity());
    }

    public void deleteShopItem(UUID id) {
        shopItems.remove(id);
    }
}
