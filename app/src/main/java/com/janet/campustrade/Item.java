package com.janet.campustrade;

/**
 * Created by Janet on 18/11/2017.
 */

public class Item {
    private String itemID;
    private int categoryId;
    private String name;
    private String description;
    private String image;
    private int cost;
    private String price;
    private String quantity;

    public Item(){}

    public Item(String itemID, String name, String description, String image, String price){
        this.itemID = itemID;
        this.name = name;
        this.description = description;
        this.image = image;
        //this.cost = cost;
        this.price = price;
    }

    public Item(String itemID, String name, String description, String image, String price,
                String quantity){
        this.itemID = itemID;
        this.name = name;
        this.description = description;
        this.image = image;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId(){
        return itemID;
    }

    public void setItemId(String itemID){
        this.itemID = itemID;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
