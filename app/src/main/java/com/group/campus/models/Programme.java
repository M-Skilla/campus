package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class Programme {

    private Long id;

    private String name, abbrv;

    private College college;

    private Year year;

    @SerializedName("created_at")
    private String createdAt;
}
