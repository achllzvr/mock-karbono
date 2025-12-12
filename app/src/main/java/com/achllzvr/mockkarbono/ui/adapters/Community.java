package com.achllzvr.mockkarbono.ui.adapters;

/**
 * MarketItem model class for the Marketplace RecyclerView
 */
public class Community {
    private String title;
    private String description;
    private int imageResId;

    public Community(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
