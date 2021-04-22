/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.huawei.podcast.java.database.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;


import java.util.Date;

/**
 * Definition of ObjectType Favourites.
 *
 * @since 2021-03-30
 */

public class Favourites extends CloudDBZoneObject {
    @PrimaryKey
    private String podcast_id;

    private String url;

    private String user_id;

    private String user_name;

    private String podcast_title;

    @NotNull
    @DefaultValue(booleanValue = false)
    private Boolean isFavourite;

    private Integer actual_podcast_id;

    public Favourites() {
        this.isFavourite = false;

    }

    public void setPodcast_id(String podcast_id) {
        this.podcast_id = podcast_id;
    }

    public String getPodcast_id() {
        return podcast_id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setPodcast_title(String podcast_title) {
        this.podcast_title = podcast_title;
    }

    public String getPodcast_title() {
        return podcast_title;
    }

    public void setIsFavourite(Boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public Boolean getIsFavourite() {
        return isFavourite;
    }

    public void setActual_podcast_id(Integer actual_podcast_id) {
        this.actual_podcast_id = actual_podcast_id;
    }

    public Integer getActual_podcast_id() {
        return actual_podcast_id;
    }

}