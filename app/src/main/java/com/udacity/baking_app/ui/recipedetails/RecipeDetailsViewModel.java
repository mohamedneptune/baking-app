package com.udacity.baking_app.ui.recipedetails;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.udacity.baking_app.data.model.IngredientModel;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.model.StepModel;
import com.udacity.baking_app.data.provider.api.recipe.dto.RecipeResponse;
import com.udacity.baking_app.data.provider.api.recipe.service.RetrofitApiUtils;
import com.udacity.baking_app.data.provider.api.recipe.service.RetrofitService;

import java.util.LinkedList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RecipeDetailsViewModel extends AndroidViewModel {

    private MutableLiveData<RecipeModel> recipeModelSelected = new MutableLiveData<>();
    private MutableLiveData<StepModel> stepModelSelected = new MutableLiveData<>();

    public RecipeDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    //Get and Set RecipeSelected
    public LiveData<RecipeModel> getRecipeModelSelected() {
        return recipeModelSelected;
    }

    public void setRecipeModelSelected(RecipeModel recipeModel){
        recipeModelSelected.postValue(recipeModel);
    }

    //Get and Set StepSelected
    public LiveData<StepModel> getStepModelSelected() {
        return stepModelSelected;
    }

    public void setStepModelSelected(StepModel stepModel){
        stepModelSelected.postValue(stepModel);
    }
}
