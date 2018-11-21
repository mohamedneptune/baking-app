package com.udacity.baking_app.ui.recipedetails.recipedetailsdescription;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.model.StepModel;
import com.udacity.baking_app.databinding.FragmentDetailsBinding;
import com.udacity.baking_app.ui.recipedetails.RecipeDetailsViewModel;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class RecipeDetailsFragment extends Fragment implements View.OnClickListener {


    private FragmentDetailsBinding mBinding;
    private RecipeDetailsViewModel mViewModel;
    private StepModel mStepModel;
    private SimpleExoPlayer mExoPlayer;
    private RecipeModel mRecipeModelSelected;
    private List<StepModel> mStepModelList = new LinkedList<>();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditorPreference;
    private int mSelectedStepPosition;
    private int mOrientation;
    private long DEFAULTPLAYERPOSITION = -100;
    private long mPlayerPosition;
    private boolean mPlayWhenReady = true;
    private static final String JSON_STEP_KEY = "JSON_STEP_OBJECT_CONVERTED_TO_STRING";
    private Bundle mSavedInstanceState;

    //Mandatory constructor for instantiating the fragment
    public RecipeDetailsFragment() {

    }

    /**
     * Inflates the fragment layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mBinding = DataBindingUtil.bind(rootView);

        mSavedInstanceState = savedInstanceState;

        mViewModel = ViewModelProviders.of(getActivity()).get(RecipeDetailsViewModel.class);

        mBinding.btnPrevious.setOnClickListener(this);
        mBinding.btnNext.setOnClickListener(this);

        mSharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = mSharedPreferences.edit();

        return rootView;
    }


    private void setupViewModel() {

        // Observe the LiveData object StepModel in the ViewModel
        mViewModel.getStepModelSelected().observe(this, new Observer<StepModel>() {
            @Override
            public void onChanged(@Nullable StepModel stepModel) {
                Timber.i("");
                mStepModel = stepModel;
                initView(stepModel);
            }
        });

        // Observe the LiveData object RecipeModel in the ViewModel
        mViewModel.getRecipeModelSelected().observe(this, new Observer<RecipeModel>() {
            @Override
            public void onChanged(@Nullable RecipeModel recipeModel) {
                Timber.i("");
                mRecipeModelSelected = recipeModel;
                mStepModelList = mRecipeModelSelected.getSteps();
            }
        });
    }

    /**
     * Init View
     */
    private void initView(StepModel stepModel){
        mBinding.playerView.setVisibility(View.GONE);
        mBinding.stepPicture.setVisibility(View.GONE);
        mBinding.stepDescription.setText(mStepModel.getDescription());

        if (!TextUtils.isEmpty(stepModel.getVideoURL())) {
            // Initialize the player.
            initializePlayer(Uri.parse(stepModel.getVideoURL()));
            mBinding.playerView.setVisibility(View.VISIBLE);
            mBinding.stepPicture.setVisibility(View.GONE);
            if (!isTablet() && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mBinding.stepDescription.setVisibility(View.GONE);
                mBinding.btnPrevious.setVisibility(View.GONE);
                mBinding.btnNext.setVisibility(View.GONE);
            } else {
                mBinding.stepDescription.setVisibility(View.VISIBLE);
                mBinding.btnPrevious.setVisibility(View.VISIBLE);
                mBinding.btnNext.setVisibility(View.VISIBLE);
            }
        } else {
            mBinding.btnPrevious.setVisibility(View.VISIBLE);
            mBinding.btnNext.setVisibility(View.VISIBLE);
            mBinding.playerView.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(stepModel.getThumbnailURL())) {
                mBinding.stepPicture.setVisibility(View.VISIBLE);
                try {
                    Picasso.with(getActivity())
                            .load(stepModel.getThumbnailURL())
                            //.placeholder(R.mipmap.ic_launcher) // can also be a drawable
                            .error(R.mipmap.ic_launcher) // will be displayed if the image cannot be loaded
                            .into(mBinding.stepPicture);
                } catch (Exception e) {
                    e.toString();
                }
            } else {
                if (isTablet()) {
                    mBinding.stepDescription.setVisibility(View.VISIBLE);
                } else if (!isTablet() && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mBinding.stepDescription.setVisibility(View.VISIBLE);
                }
            }

        }

        mSelectedStepPosition = mSharedPreferences.getInt("selected_step_position", 0);

        if (mSelectedStepPosition == 0) {
            mBinding.btnPrevious.setEnabled(false);
            mBinding.btnNext.setEnabled(true);
        } else if (mSelectedStepPosition == 6) {
            mBinding.btnPrevious.setEnabled(true);
            mBinding.btnNext.setEnabled(false);
        } else {
            mBinding.btnPrevious.setEnabled(true);
            mBinding.btnNext.setEnabled(true);
        }
    }

    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {

            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
            mBinding.playerView.setPlayer(mExoPlayer);
            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);

            mExoPlayer.setPlayWhenReady(true);

            if (mPlayerPosition > 0 && mPlayerPosition != DEFAULTPLAYERPOSITION ) {
                Toast.makeText(getContext(), "yes", Toast.LENGTH_SHORT).show();
                mExoPlayer.seekTo(mPlayerPosition);
                mExoPlayer.setPlayWhenReady(mPlayWhenReady);
            }else{
                mExoPlayer.seekTo(0);
                mExoPlayer.setPlayWhenReady(mPlayWhenReady);
            }
    }


    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("player_position", mPlayerPosition);
        outState.putBoolean("play_when_ready", mPlayWhenReady);
    }

    @Override
    public void onResume() {
        super.onResume();
        mOrientation = this.getResources().getConfiguration().orientation;

        mSelectedStepPosition = mSharedPreferences.getInt("selected_step_position", 0);

        if (mSelectedStepPosition == 0) {
            mBinding.btnPrevious.setEnabled(false);
        } else if (mSelectedStepPosition == 6) {
            mBinding.btnNext.setEnabled(false);
        }

        if (mSavedInstanceState != null) {
            mPlayerPosition = mSavedInstanceState.getLong("player_position", DEFAULTPLAYERPOSITION);
            mPlayWhenReady = mSavedInstanceState.getBoolean("play_when_ready", true);
            //LOAD stepModel into json SharedPreference
            String jsonStepConvertedToString = mSharedPreferences.getString(JSON_STEP_KEY, "");
            Gson gson = new Gson();
            Type type = new TypeToken<StepModel>() {
            }.getType();
            mStepModel = gson.fromJson(jsonStepConvertedToString, type);
            initView(mStepModel);
        }else{
            releasePlayer();
        }
        setupViewModel();
    }

    /**
     * Release the player when the activity is Paused / Stoped.
     */

    @Override
    public void onPause() {
        super.onPause();
        if (mExoPlayer != null){
            mPlayerPosition = mExoPlayer.getCurrentPosition();
            mPlayWhenReady = mExoPlayer.getPlayWhenReady();
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    /**
     * Release the player when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }



    @Override
    public void onClick(View v) {
        mSelectedStepPosition = mSharedPreferences.getInt("selected_step_position", 0);
        int newPosition = 0;
        if (v == mBinding.btnPrevious) {
            Timber.i("mBtnPrevious");
            if (mSelectedStepPosition > 0) {
                newPosition = mSelectedStepPosition - 1;
                mStepModel = mStepModelList.get(newPosition);
                mViewModel.setStepModelSelected(mStepModel);
                mEditorPreference.putInt("selected_step_position", newPosition);
                mPlayerPosition = DEFAULTPLAYERPOSITION;
                mEditorPreference.apply();
                if (newPosition == 0) {
                    mBinding.btnPrevious.setEnabled(false);
                } else if (newPosition == mStepModelList.size() - 2) {
                    mBinding.btnNext.setEnabled(true);
                }
            }
        } else if (v == mBinding.btnNext) {
            Timber.i("mBtnNext");
            if (mSelectedStepPosition < mStepModelList.size() - 1) {
                newPosition = mSelectedStepPosition + 1;
                mStepModel = mStepModelList.get(newPosition);
                mViewModel.setStepModelSelected(mStepModel);
                mEditorPreference.putInt("selected_step_position", newPosition);
                mPlayerPosition = DEFAULTPLAYERPOSITION;
                mEditorPreference.apply();
                if (newPosition == 1) {
                    mBinding.btnPrevious.setEnabled(true);
                } else if (newPosition == mStepModelList.size() - 1) {
                    mBinding.btnNext.setEnabled(false);
                }
            }
        }
    }

    public boolean isTablet(){
        return getResources().getBoolean(R.bool.isTablet);
    }
}