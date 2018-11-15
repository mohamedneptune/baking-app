package com.udacity.baking_app.ui.recipeslist;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.baking_app.common.Constants;
import com.udacity.baking_app.data.provider.api.recipe.dto.RecipeResponse;
import com.udacity.baking_app.data.provider.api.recipe.service.RetrofitApiUtils;
import com.udacity.baking_app.data.provider.api.recipe.service.RetrofitService;

import java.util.List;

import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private RetrofitService mRetrofitService;
    private final MutableLiveData<List<RecipeResponse>> recipeList = new MutableLiveData<>();


    public MainViewModel(@NonNull Application application) {
        super(application);
        mRetrofitService = RetrofitApiUtils.getRetrofitService();
    }

    public void getRecipesFromService() {
        mRetrofitService.getRecipes()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<RecipeResponse>>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError");
                    }

                    @Override
                    public void onNext(List<RecipeResponse> example) {
                        Timber.i("onNext ");
                        recipeList.postValue(example);
                    }
                });
    }

    public LiveData<List<RecipeResponse>> getRecipes() {
        return recipeList;
    }
}
