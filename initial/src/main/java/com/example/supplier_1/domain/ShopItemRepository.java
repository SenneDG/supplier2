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
        item1.setName("KARMELIET TRIPEL 75CL");
        item1.setDescription("Karmeliet Tripel 75cl is het absolute boegbeeld van de brouwerij. Iedere fles Karmeliet Tripel wordt gemaakt op basis van een oud en historisch drie-granenrecept, bestaande uit tarwe, haver en gerst. Ook vandaag de dag zien de brouwmeesters van dit merk dit nog steeds als het ideale recept voor hun speciaalbier Naar eigen zeggen is iedere slok die je van dit tripel bier neemt een 'reis voor de zintuigen'.");
        item1.setPrice(7.75);
        item1.setQuantity(12);
        item1.setImageUrl("https://res.cloudinary.com/boozeboodcdn/image/upload/f_auto/e_trim:10/c_pad/g_south/w_140/h_455/c_limit,w_140,h_455/q_auto:best/v20210322/HD/09012.jpg");

        ShopItem item2 = new ShopItem();
        item2.setId(ITEM2_UUID);
        item2.setName("LA CHOUFFE 75CL");
        item2.setDescription("La Chouffe 75cl is een Belgisch blond bier dat bekendstaat om een verfrissend en licht kruidig karakter. La Chouffe bier is een bier van hoge gisting met nagisting op de fles. Verder heeft het een vol en rijk karakter waarin toetsen van kruiden en fruit samenkomen met het beste van hop.");
        item2.setPrice(5.25);
        item2.setQuantity(7);
        item2.setImageUrl("https://res.cloudinary.com/boozeboodcdn/image/upload/f_auto/e_trim:10/c_pad/g_south/w_140/h_455/c_limit,w_140,h_455/q_auto:best/v20210322/HD/09001.jpg");

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
        shopItem.setLockedQuantity(updatedShopItem.getLockedQuantity());
    }

    public void deleteShopItem(UUID id) {
        shopItems.remove(id);
    }
}