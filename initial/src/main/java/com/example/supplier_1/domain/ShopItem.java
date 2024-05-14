package com.example.supplier_1.domain;

import java.util.Objects;
import java.util.UUID;

public class ShopItem {
    private UUID id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String imageUrl;

    // getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ShopItem shopItem = (ShopItem) obj;
        return id.equals(shopItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, quantity, imageUrl);
    }
}