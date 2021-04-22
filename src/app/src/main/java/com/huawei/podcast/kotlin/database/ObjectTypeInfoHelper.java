/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.huawei.podcast.kotlin.database;

import com.huawei.agconnect.cloud.database.ObjectTypeInfo;
import com.huawei.podcast.kotlin.utils.Constants;
import com.huawei.podcast.kotlin.database.model.Favourites;
import com.huawei.podcast.kotlin.database.model.PodCasts;

import java.util.Arrays;

/**
 * Definition of ObjectType Helper.
 *
 * @since 2020-11-24
 */
public class ObjectTypeInfoHelper {
    private final static int FORMAT_VERSION = Constants.ONE;
    private final static int OBJECT_TYPE_VERSION = Constants.NINETY_TWO;

    public static ObjectTypeInfo getObjectTypeInfo() {
        ObjectTypeInfo objectTypeInfo = new ObjectTypeInfo();
        objectTypeInfo.setFormatVersion(FORMAT_VERSION);
        objectTypeInfo.setObjectTypeVersion(OBJECT_TYPE_VERSION);
        objectTypeInfo.setObjectTypes(Arrays.asList(PodCasts.class, Favourites.class));
        return objectTypeInfo;
    }
}