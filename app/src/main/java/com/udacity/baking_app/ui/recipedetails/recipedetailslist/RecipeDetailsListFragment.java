package com.udacity.baking_app.ui.recipedetails.recipedetailslist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.IngredientModel;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.model.StepModel;
import com.udacity.baking_app.databinding.FragmentDetailsListBinding;
import com.udacity.baking_app.ui.recipedetails.RecipeDetailsViewModel;
import com.udacity.baking_app.utils.Json;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class RecipeDetailsListFragment extends Fragment implements View.OnClickListener,
        IngredientsViewAdapter.ItemClickListener, StepsViewAdapter.ItemClickListener {

    private FragmentDetailsListBinding mBinding;
    private RecipeDetailsViewModel mViewModel;
    private List<RecipeModel> mRecipeModelList = new LinkedList<>();
    private RecipeModel mRecipeModelSelected;
    private List<IngredientModel> mIngredientModelList = new LinkedList<>();
    private List<StepModel> mStepModelList = new LinkedList<>();
    private StepModel mStepModel;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditorPreference;
    private int mSelectedRecipePosition;
    private static final String JSON_RECIPE_KEY = "JSON_RECIPE_OBJECT_CONVERTED_TO_STRING";
    private static final String JSON_STEP_KEY = "JSON_STEP_OBJECT_CONVERTED_TO_STRING";
    private long DEFAULTPLAYERPOSITION = -100;

    //Define a new interface OnRecipeStepClickLister that triggers a callback in the host activity
    OnRecipeStepClickLister mCallback;

    public interface OnRecipeStepClickLister {
        void onRecipeStepSelected(int position);
    }

    //Override onAttach to make sure that the container activity has implementd the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //this make sure that the host activity has implemented the callback interface
        //if not, it throws an exception
        try {
            mCallback = (OnRecipeStepClickLister) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "must implement OnRecipeStepClickLister");
        }
    }

    //Mandatory constructor for instantiating the fragment
    public RecipeDetailsListFragment() {

    }

    /**
     * Inflates the fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details_list, container, false);
        mBinding = DataBindingUtil.bind(rootView);

        mViewModel = ViewModelProviders.of(getActivity()).get(RecipeDetailsViewModel.class);

        mSharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = mSharedPreferences.edit();

        String jsonRecipeConvertedToString = mSharedPreferences.getString(JSON_RECIPE_KEY, "");

        mBinding.btnPreviousRecipe.setOnClickListener(this);
        mBinding.btnNextRecipe.setOnClickListener(this);


        Gson gson = new Gson();
        Type type = new TypeToken<List<RecipeModel>>() {
        }.getType();
        mRecipeModelList = gson.fromJson(jsonRecipeConvertedToString, type);

        mSelectedRecipePosition = mSharedPreferences.getInt("selected_recipe_position", 0);

        if (mSelectedRecipePosition == 0) {
            mBinding.btnPreviousRecipe.setEnabled(false);
        } else if (mSelectedRecipePosition == mRecipeModelList.size() - 1) {
            mBinding.btnNextRecipe.setEnabled(false);
        }

        if (savedInstanceState == null) {
            setupViewModel();
        } else {
            Timber.i("savedInstanceState != null");
            mRecipeModelSelected = mRecipeModelList.get(mSelectedRecipePosition);
            mIngredientModelList = mRecipeModelSelected.getIngredients();
            mStepModelList = mRecipeModelSelected.getSteps();

            showIngedientsAndSteps(mIngredientModelList, mStepModelList);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO save instance when rotate
    }

    private void setupViewModel() {
        // Observe the LiveData object in the ViewModel
        //setup the listener for the fragment A
        mViewModel.getRecipeModelSelected().observe(this, new Observer<RecipeModel>() {
            @Override
            public void onChanged(@Nullable RecipeModel recipeModel) {
                Timber.i("");
                mRecipeModelSelected = recipeModel;
                mIngredientModelList = mRecipeModelSelected.getIngredients();
                mStepModelList = mRecipeModelSelected.getSteps();

                showIngedientsAndSteps(mIngredientModelList, mStepModelList);
            }
        });
    }

    @Override
    public void onClick(View v) {
        mSelectedRecipePosition = mSharedPreferences.getInt("selected_recipe_position", 0);
        int newPosition = 0;
        if (v == mBinding.btnPreviousRecipe) {
            Timber.i("mPreviousRecipe");
            if (mSelectedRecipePosition > 0) {
                newPosition = mSelectedRecipePosition - 1;
                mRecipeModelSelected = mRecipeModelList.get(newPosition);
                mViewModel.setRecipeModelSelected(mRecipeModelSelected);
                mStepModelList = mRecipeModelSelected.getSteps();
                mEditorPreference.putInt("selected_recipe_position", newPosition);
                mEditorPreference.putInt("selected_step_position", 0);
                mEditorPreference.putLong("player_position", DEFAULTPLAYERPOSITION);
                mEditorPreference.apply();
                showSelectedStep(0);
                if (newPosition == 0) {
                    mBinding.btnPreviousRecipe.setEnabled(false);
                } else if (newPosition == mRecipeModelList.size() - 2) {
                    mBinding.btnNextRecipe.setEnabled(true);
                }
            }
        } else if (v == mBinding.btnNextRecipe) {
            Timber.i("mNextRecipe");
            if (mSelectedRecipePosition < mRecipeModelList.size() - 1) {
                newPosition = mSelectedRecipePosition + 1;
                mRecipeModelSelected = mRecipeModelList.get(newPosition);
                mViewModel.setRecipeModelSelected(mRecipeModelSelected);
                mStepModelList = mRecipeModelSelected.getSteps();
                mEditorPreference.putInt("selected_recipe_position", newPosition);
                mEditorPreference.putInt("selected_step_position", 0);
                mEditorPreference.putLong("player_position", DEFAULTPLAYERPOSITION);
                mEditorPreference.apply();
                showSelectedStep(0);
                if (newPosition == 1) {
                    mBinding.btnPreviousRecipe.setEnabled(true);
                } else if (newPosition == mRecipeModelList.size() - 1) {
                    mBinding.btnNextRecipe.setEnabled(false);
                }
            }
        }
    }

    private void showIngedientsAndSteps(List<IngredientModel> ingredientModelList,
                                        List<StepModel> stepModelList) {
        int numberOfColumns = 1;

        mBinding.recyclerIngredient.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        IngredientsViewAdapter ingredientsViewAdapter = new IngredientsViewAdapter(getActivity(), ingredientModelList);
        mBinding.recyclerIngredient.setAdapter(ingredientsViewAdapter);

        mBinding.recyclerStep.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        StepsViewAdapter stepsViewAdapter = new StepsViewAdapter(getActivity(), stepModelList);
        stepsViewAdapter.setClickListener(this);
        mBinding.recyclerStep.setAdapter(stepsViewAdapter);

        int selectedStepPosition = mSharedPreferences.getInt("selected_step_position", 0);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            showSelectedStep(selectedStepPosition);
        }

    }

    @Override
    public void onItemClick(View view, int position) {
        Timber.i("position step: " + position);
        showSelectedStep(position);
        mEditorPreference.putInt("selected_step_position", position);
        mEditorPreference.putLong("player_position", DEFAULTPLAYERPOSITION);
        mEditorPreference.apply();
    }

    private void showSelectedStep(int position) {

        mStepModel = mStepModelList.get(position);
        mViewModel.setStepModelSelected(mStepModel);

        //save stepModel into json SharedPreference
        String jsonStepModelConvertedToString = Json.serialize(mStepModel);
        mEditorPreference = mSharedPreferences.edit();
        mEditorPreference.putString(JSON_STEP_KEY, jsonStepModelConvertedToString);
        mEditorPreference.commit();


        mCallback.onRecipeStepSelected(position);

    }
}


