package com.example.offers_localshops;

public class Product {
    private String productName;
    private String offerDetail;
    private String offerType;
    private String fromDate;
    private String toDate;
    private String fromTime;
    private String toTime;

    public Product() {
        // Required for Firebase
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOfferDetail() {
        return offerDetail;
    }
    public void setOfferDetail(String offerDetail) {
        this.offerDetail = offerDetail;
    }

    public String getOfferType() {
        return offerType;
    }
    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    public String getFromDate() {
        return fromDate;
    }
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getFromTime() {
        return fromTime;
    }
    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }
    public void setToTime(String toTime) {
        this.toTime = toTime;
    }
}
