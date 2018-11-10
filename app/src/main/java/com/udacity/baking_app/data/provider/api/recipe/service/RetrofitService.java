package com.udacity.baking_app.data.provider.api.recipe.service;





import com.udacity.baking_app.common.Constants;
import com.udacity.baking_app.data.provider.api.recipe.dto.RecipeResponse;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;


public interface RetrofitService {

    @GET(Constants.URL_RECIPE)
    Observable<List<RecipeResponse>> getRecipes();




}