package com.achllzvr.mockkarbono.api.data.models;

import com.google.gson.annotations.SerializedName;

public class BlogPost {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("excerpt")
    public String excerpt;

    @SerializedName("content")
    public String content;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("created_at")
    public String createdAt;
}