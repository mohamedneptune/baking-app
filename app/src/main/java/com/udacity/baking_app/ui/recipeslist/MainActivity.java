package com.udacity.baking_app.ui.recipeslist;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.udacity.baking_app.BakingAppWidgetProvider;
import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.provider.api.recipe.dto.RecipeResponse;
import com.udacity.baking_app.data.provider.api.recipe.mapper.RecipeMapper;
import com.udacity.baking_app.databinding.ActivityMainBinding;
import com.udacity.baking_app.ui.recipedetails.RecipeDetailsActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity  implements MainViewAdapter.ItemClickListener{


    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;
    private List<RecipeModel> mRecipeModelList = new LinkedList<>();
    private SharedPreferences.Editor mEditorPreference;
    private String jsonResultConvertedToString;
    private  static final String JSON_KEY = "JSON_OBJECT_CONVERTED_TO_STRING";
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.progressBar.setVisibility(View.VISIBLE);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        setupViewModel();

        mViewModel.getRecipesFromService();

        mSharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = mSharedPreferences.edit();

        jsonResultConvertedToString = mSharedPreferences.getString(JSON_KEY, "");
    }


    private void setupViewModel() {
        // Observe the LiveData object in the ViewModel

        mViewModel.getRecipes().observe(this, new Observer<List<RecipeResponse>>() {
            @Override
            public void onChanged(@Nullable List<RecipeResponse> recipeResponseList) {
                Timber.i("Updating list from LiveData in ViewModel");
                //ModelMapper
                RecipeMapper recipeMapper = new RecipeMapper();
                mRecipeModelList = new ArrayList<>();
                mRecipeModelList = recipeMapper.convertListResponseToListModel(recipeResponseList);
                setRecyclerAdapter(mRecipeModelList);

                //Save RecipeList to Json
                Gson gson = new Gson();
                jsonResultConvertedToString = gson.toJson(mRecipeModelList);
                mEditorPreference = mSharedPreferences.edit();
                mEditorPreference.putString(JSON_KEY, jsonResultConvertedToString);
                mEditorPreference.commit();

            }
        });
    }

    private void setRecyclerAdapter(List<RecipeModel> recipeModelList) {


        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        int numberOfColumns = 1;
        if (tabletSize) {
            numberOfColumns = 2;
        } else {
            numberOfColumns = 1;
        }

        mBinding.recyclerRecipe.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        MainViewAdapter mainViewAdapter = new MainViewAdapter(this, recipeModelList);
        mainViewAdapter.setClickListener(this);
        mBinding.recyclerRecipe.setAdapter(mainViewAdapter);
        mBinding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(View view, int position) {

        mEditorPreference.putInt("selected_recipe_position", position);
        mEditorPreference.putString("selected_recipe_name", mRecipeModelList.get(position).getName());

        //Initialise selectedStepPosition to 0
        mEditorPreference.putInt("selected_step_position", 0);


        //Get Recipe ingredient to show in widget
        ArrayList<String> listIngredients = new ArrayList<>();
        for(int i=0 ; i< mRecipeModelList.get(position).getIngredients().size() ; i++){
            listIngredients.add(mRecipeModelList.get(position).getIngredients().get(i).getIngredient());
        }
        String ingredientString = TextUtils.join(", ", listIngredients);
        mEditorPreference.putString("selected_recipe_ingredient_list", ingredientString);
        mEditorPreference.apply();

        // UpdateWidget
        Intent intent = new Intent(this, BakingAppWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), BakingAppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);

        // Start new Activity
        Intent intentDetails = new Intent(MainActivity.this, RecipeDetailsActivity.class);
        startActivity(intentDetails);
    }
}
