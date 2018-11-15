package com.udacity.baking_app.ui.recipedetails;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.databinding.ActivityDetailsBinding;
import com.udacity.baking_app.ui.recipedetails.recipedetailsdescription.RecipeDetailsFragment;
import com.udacity.baking_app.ui.recipedetails.recipedetailslist.RecipeDetailsListFragment;
import com.udacity.baking_app.utils.Json;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class RecipeDetailsActivity extends AppCompatActivity implements
        RecipeDetailsListFragment.OnRecipeStepClickLister {

    private ActivityDetailsBinding mBinding;
    private RecipeDetailsViewModel mViewModel;
    private SharedPreferences.Editor mEditorPreference;
    private List<RecipeModel> mRecipeModelList = new LinkedList<>();
    private RecipeModel mRecipeModel;
    private static final int DEFAULT_SELECTED_RECIPE = -1;
    private FragmentManager mFragmentManager;
    private Bundle mSavedInstanceState;
    private String jsonResultConvertedToString;
    private static final String JSON_RECIPE_KEY = "JSON_RECIPE_OBJECT_CONVERTED_TO_STRING";
    private SharedPreferences mSharedPreferences;
    private int mSelectedRecipePosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        mSavedInstanceState = savedInstanceState;

        mViewModel = ViewModelProviders.of(this).get(RecipeDetailsViewModel.class);

        mSharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = mSharedPreferences.edit();

        // GET Recipe list from sharedPreference
        jsonResultConvertedToString = mSharedPreferences.getString(JSON_RECIPE_KEY, "");

        try {
            Type type = new TypeToken<List<RecipeModel>>() {
            }.getType();
            mRecipeModelList = Json.deSerializeList(jsonResultConvertedToString, type);
        } catch (JsonParseException e) {
            Timber.e("error " + e);
        }

        //get selected recipe position  from sharedPreference
        mSelectedRecipePosition = mSharedPreferences.getInt("selected_recipe_position", DEFAULT_SELECTED_RECIPE);

        setupViewModel();

        mRecipeModel = mRecipeModelList.get(mSelectedRecipePosition);
        mViewModel.setRecipeModelSelected(mRecipeModel);
    }

    private void setupViewModel() {
        // Observe the LiveData object in the ViewModel
        mViewModel.getRecipeModelSelected().observe(this, new Observer<RecipeModel>() {
            @Override
            public void onChanged(@Nullable RecipeModel recipeModel) {
                setTitle(recipeModel.getName());
                showFragmentRecipeDetails();
            }
        });
    }

    private void showFragmentRecipeDetails() {


        mFragmentManager = getSupportFragmentManager();

        //Only create new fragments when is no previously saved state
        if (mSavedInstanceState == null) {
            if (isTablet()) {
                RecipeDetailsListFragment recipeDetailsListFragment = new RecipeDetailsListFragment();
                RecipeDetailsFragment recipeDetailsFragment = new RecipeDetailsFragment();
                mFragmentManager.beginTransaction()
                        .add(R.id.list_container, recipeDetailsListFragment, recipeDetailsListFragment.getTag())
                        .commit();

                mFragmentManager.beginTransaction()
                        .add(R.id.details_container, recipeDetailsFragment, recipeDetailsFragment.getTag())
                        .commit();
            } else {
                RecipeDetailsListFragment recipeDetailsListFragment = new RecipeDetailsListFragment();
                mFragmentManager.beginTransaction()
                        .add(R.id.container, recipeDetailsListFragment, recipeDetailsListFragment.getTag())
                        .commit();
            }

        }
    }


    @Override
    public void onRecipeStepSelected(int position) {
        if (!isTablet()) {
            Timber.i("");
            RecipeDetailsFragment recipeDetailsFragment = new RecipeDetailsFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, recipeDetailsFragment, recipeDetailsFragment.getTag())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void backToDetailsListFragment() {
        if (!isTablet()) {
            Timber.i("");
            RecipeDetailsListFragment recipeDetailsListFragment = new RecipeDetailsListFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, recipeDetailsListFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.i("Back Button Pressed");
        switch (item.getItemId()) {
            case android.R.id.home:
                Timber.i("home on backpressed");
                int nbrBackStackEntry = getSupportFragmentManager().getBackStackEntryCount();
                if (nbrBackStackEntry > 0) {
                    getSupportFragmentManager().popBackStack();
                    backToDetailsListFragment();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isTablet(){
        return getResources().getBoolean(R.bool.isTablet);
    }
}
