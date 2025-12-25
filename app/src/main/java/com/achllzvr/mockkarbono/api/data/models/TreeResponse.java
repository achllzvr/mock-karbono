package com.achllzvr.mockkarbono.api.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TreeResponse {
    @SerializedName("trees")
    public List<TreeModel> trees;

    @SerializedName("currency")
    public String currency;
}