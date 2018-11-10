package com.udacity.baking_app.data.provider.api.recipe.service;


import com.udacity.baking_app.common.Constants;

public class RetrofitApiUtils {

    public static RetrofitService getRetrofitService() {
        return RetrofitClient.getClient(Constants.BASE_SERVER).create(RetrofitService.class);
    }

}
