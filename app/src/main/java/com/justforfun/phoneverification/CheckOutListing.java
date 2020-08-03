package com.justforfun.phoneverification;

public class CheckOutListing {
    String name;
    int cost;
    int quantity;
    String id;

    public CheckOutListing(String name, int cost, int quantity, String id){
        this.name = name;
        this.cost = cost;
        this.quantity = quantity;
        this.id = id;
    }

    public String getName() {return name;}

    public int getCost() {return cost;}

    public int getQuantity() {return quantity;}

    public String getId() {return id;}
}
