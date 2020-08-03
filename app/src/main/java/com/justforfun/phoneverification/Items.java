package com.justforfun.phoneverification;

public class Items {
    private String itemName;
    private String itemType;
    private String itemID;
    private long itemCost;
    private boolean itemAvialable;
    private int itemCount; //for local counting

    public Items(){

    }

    public Items(boolean itemAvialable, long itemCost, String itemID, String itemName, String itemType){
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemID = itemID;
        this.itemCost = itemCost;
        this.itemAvialable = itemAvialable;
    }

    public String getItemName() {return itemName;}

    public void setItemName(String itemName) {this.itemName = itemName;}

    public String getItemType() {return itemType;}

    public void setItemType(String itemType) {this.itemType = itemType;}

    public String getItemID() {return itemID;}

    public void setItemID(String id) {this.itemID = id;}

    public long getItemCost() {return itemCost;}

    public void setItemCost(long itemCost) {this.itemCost = itemCost;}

    public boolean getItemAvialable() {return itemAvialable;}

    public void setItemAvialable(boolean itemAvialable) {this.itemAvialable = itemAvialable;}

    public int getItemCount() {return itemCount;}

    public void setItemCount(int itemCount) {this.itemCount = itemCount;}
}
