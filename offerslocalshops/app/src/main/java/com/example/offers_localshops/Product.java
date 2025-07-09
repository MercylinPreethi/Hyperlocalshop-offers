package com.example.offers_localshops;

public class Product {
    private String productName;
    private String offerType;
    private String offerDetail;
    private String fromDate;
    private String toDate;
    private String fromTime;
    private String toTime;
    private String shopName;

    private String shopType;

    public Product() {
        // Required for Firebase
    }

    public Product(String productName, String offerType, String shopType, String offerDetail, String fromDate, String toDate, String fromTime, String toTime, String shopName) {
        this.productName = productName;
        this.offerType = offerType;
        this.shopType = shopType;
        this.offerDetail = offerDetail;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.shopName = shopName;
    }

    public String getProductName() {
        return productName;
    }

    public String getOfferType() {
        return offerType;
    }

    public String getShopType() {
        return shopType;
    }


    public String getOfferDetail() {
        return offerDetail;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public String getShopName() {
        return shopName;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    public void setShopName(String storeName) {
        this.shopName = storeName;
    }

    public void setShopType(String shopType) {
        this.shopType = shopType;
    }
}
