package com.udacity.baking_app.data.provider.api.recipe.mapper;

import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.provider.api.recipe.dto.RecipeResponse;

import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

public class RecipeMapper {

    public List<RecipeModel> convertListResponseToListModel(List<RecipeResponse> recipeResponseList) {
        ModelMapper modelMapper = new ModelMapper();
        List<RecipeModel> recipeModels = new ArrayList<>();
        RecipeModel recipeModel;
        for(int i = 0; i < recipeResponseList.size() ; i++){
            recipeModel = modelMapper.map(recipeResponseList.get(i), RecipeModel.class);
            recipeModels.add(recipeModel);
        }

        return recipeModels;
    }
}
