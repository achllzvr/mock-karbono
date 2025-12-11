package com.achllzvr.mockkarbono.ui.adapters;

/**
 * MarketItem model class for the Marketplace RecyclerView
 */
public class MarketItem {
    private String title;
    private String description;
    private String price;
    private int imageResId;

    public MarketItem(String title, String description, String price, int imageResId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}

