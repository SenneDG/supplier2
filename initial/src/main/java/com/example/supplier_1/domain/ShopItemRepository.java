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
        item1.setId(ITEM1_UUID);
        item1.setName("Acer Chromebook 315 CB315-4H-C229");
        item1.setDescription("Deze Acer Chromebook 315 CB315-4H-C229 is uniek vanwege het Full HD beeldscherm dat ideaal is voor films/series en het grote formaat beeldscherm.");
        item1.setPrice(322.99);
        item1.setQuantity(12);
        item1.setImageUrl("https://media.s-bol.com/3rV108YXGAv4/EDyPGY/1200x851.jpg");

        ShopItem item2 = new ShopItem();
        item2.setId(ITEM2_UUID);
        item2.setName("Lenovo IdeaPad 1 15ALC7 82R400FCMH");
        item2.setDescription("Deze Lenovo IdeaPad 1 15ALC7 (82R400FCMH) laptop is geschikt voor het werken in Excel en uitvoeren van meerdere programma's tegelijk.");
        item2.setPrice(429.0);
        item2.setQuantity(7);
        item2.setImageUrl("https://media.s-bol.com/gPKJDKlKKjOj/grkn73/1200x1200.jpg");

        shopItems.put(item1.getId(), item1);
        shopItems.put(item2.getId(), item2);
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
    }

    public void deleteShopItem(UUID id) {
        shopItems.remove(id);
    }
}