package com.udacity.baking_app.ui.recipedetails.recipedetailslist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.udacity.baking_app.ui.recipedetails.RecipeDetailsViewModel;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class RecipeDetailsListFragment extends Fragment implements View.OnClickListener,
        IngredientsViewAdapter.ItemClickListener, StepsViewAdapter.ItemClickListener{

    private RecipeDetailsListFragment mBinding;
    private RecyclerView ingredientRecycleView;
    private RecyclerView stepRecycleView;
    private RecipeDetailsViewModel mViewModel;
    private List<RecipeModel> mRecipeModelList = new LinkedList<>();
    private RecipeModel mRecipeModelSelected;
    private List<IngredientModel> mIngredientModelList = new LinkedList<>();
    private List<StepModel> mStepModelList = new LinkedList<>();
    private StepModel mStepModel;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditorPreference;
    private Button mPreviousRecipe;
    private Button mNextRecipe;
    private int mSelectedRecipePosition;
    private String jsonResultConvertedToString;
    private  static final String JSON_KEY = "JSON_OBJECT_CONVERTED_TO_STRING";

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
        try{
            mCallback = (OnRecipeStepClickLister) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + "must implement OnRecipeStepClickLister");
        }
    }

    //Mandatory constructor for instantiating the fragment
    public RecipeDetailsListFragment(){

    }

    /**
     * Inflates the fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
       /* View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mBinding = DataBindingUtil.bind(rootView);*/

        ingredientRecycleView = (RecyclerView) rootView.findViewById(R.id.recycler_ingredient);
        stepRecycleView = (RecyclerView) rootView.findViewById(R.id.recycler_step);

        mPreviousRecipe = (Button) rootView.findViewById(R.id.btn_previous_recipe);
        mNextRecipe = (Button) rootView.findViewById(R.id.btn_next_recipe);

        mViewModel = ViewModelProviders.of(getActivity()).get(RecipeDetailsViewModel.class);

        mSharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = mSharedPreferences.edit();

        jsonResultConvertedToString = mSharedPreferences.getString(JSON_KEY, "");

        mPreviousRecipe.setOnClickListener(this);
        mNextRecipe.setOnClickListener(this);

        setupViewModel();

        Gson gson = new Gson();
        Type type = new TypeToken<List<RecipeModel>>(){}.getType();
        mRecipeModelList = gson.fromJson(jsonResultConvertedToString, type);

        mSelectedRecipePosition = mSharedPreferences.getInt("selected_recipe_position", 0);

        if (mSelectedRecipePosition == 0) {
            mPreviousRecipe.setEnabled(false);
        } else if (mSelectedRecipePosition == mRecipeModelList.size()-1) {
            mNextRecipe.setEnabled(false);
        }

        if(savedInstanceState != null){

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
        if (v == mPreviousRecipe) {
            Timber.i("mPreviousRecipe");
            if (mSelectedRecipePosition > 0) {
                newPosition = mSelectedRecipePosition - 1;
                mRecipeModelSelected = mRecipeModelList.get(newPosition);
                mViewModel.setRecipeModelSelected(mRecipeModelSelected);
                mEditorPreference.putInt("selected_recipe_position", newPosition);
                mEditorPreference.apply();
                if (newPosition == 0) {
                    mPreviousRecipe.setEnabled(false);
                } else if (newPosition == mRecipeModelList.size()-2) {
                    mNextRecipe.setEnabled(true);
                }
            }
        } else if (v == mNextRecipe) {
            Timber.i("mNextRecipe");
            if (mSelectedRecipePosition < mRecipeModelList.size() - 1) {
                newPosition = mSelectedRecipePosition + 1;
                mRecipeModelSelected = mRecipeModelList.get(newPosition);
                mViewModel.setRecipeModelSelected(mRecipeModelSelected);
                mEditorPreference.putInt("selected_recipe_position", newPosition);
                mEditorPreference.apply();
                if (newPosition == 1) {
                    mPreviousRecipe.setEnabled(true);
                } else if (newPosition == mRecipeModelList.size()-1) {
                    mNextRecipe.setEnabled(false);
                }
            }
        }
    }

    private void showIngedientsAndSteps(List<IngredientModel> ingredientModelList,
                                        List<StepModel> stepModelList) {
        int numberOfColumns = 1;

        ingredientRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        IngredientsViewAdapter ingredientsViewAdapter = new IngredientsViewAdapter(getActivity(), ingredientModelList);
        //ingredientsViewAdapter.setClickListener(this);
        ingredientRecycleView.setAdapter(ingredientsViewAdapter);

        stepRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        StepsViewAdapter stepsViewAdapter = new StepsViewAdapter(getActivity(), stepModelList);
        stepsViewAdapter.setClickListener(this);
        stepRecycleView.setAdapter(stepsViewAdapter);

        int selectedStepPosition = mSharedPreferences.getInt("selected_step_position", 0);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            showSelectedStep(selectedStepPosition);
        }

    }

    @Override
    public void onItemClick(View view, int position) {
      Timber.i("position step: "+ position);
      showSelectedStep(position);
        mEditorPreference.putInt("selected_step_position", position);
        mEditorPreference.apply();
    }

    private void showSelectedStep(int position) {

        mStepModel = mStepModelList.get(position);
        mViewModel.setStepModelSelected(mStepModel);
        mCallback.onRecipeStepSelected(position);

    }
}


