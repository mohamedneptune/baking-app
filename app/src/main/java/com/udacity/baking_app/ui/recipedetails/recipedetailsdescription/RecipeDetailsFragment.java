package com.udacity.baking_app.ui.recipedetails.recipedetailsdescription;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.RecipeModel;
import com.udacity.baking_app.data.model.StepModel;
import com.udacity.baking_app.ui.recipedetails.RecipeDetailsViewModel;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class RecipeDetailsFragment extends Fragment implements View.OnClickListener {


    private RecipeDetailsViewModel mViewModel;
    private StepModel mStepModel;
    private TextView mStepDescription;
    private SimpleExoPlayerView mSimpleExoPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private ImageView mStepPicture;
    private Button mBtnNext;
    private Button mBtnPrevious;
    private RecipeModel mRecipeModelSelected;
    private List<StepModel> mStepModelList = new LinkedList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor mEditorPreference;
    private int mSelectedStepPosition;
    private int mOrientation;
    private boolean mTabletSize;

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

        mViewModel = ViewModelProviders.of(getActivity()).get(RecipeDetailsViewModel.class);

        mStepDescription = (TextView) rootView.findViewById(R.id.step_description);
        mSimpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.playerView);
        mStepPicture = (ImageView) rootView.findViewById(R.id.step_picture);
        mBtnPrevious = (Button) rootView.findViewById(R.id.btn_previous);
        mBtnNext = (Button) rootView.findViewById(R.id.btn_next);


        mBtnPrevious.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);

        sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = sharedPreferences.edit();

        mSelectedStepPosition = sharedPreferences.getInt("selected_step_position", 0);

        if (mSelectedStepPosition == 0) {
            mBtnPrevious.setEnabled(false);
        } else if (mSelectedStepPosition == 6) {
            mBtnNext.setEnabled(false);
        }

        setupViewModel();

        if (savedInstanceState != null) {

        }

        mOrientation = this.getResources().getConfiguration().orientation;
        mTabletSize = getResources().getBoolean(R.bool.isTablet);

        /*if(mTabletSize){
            mStepDescription.setVisibility(View.VISIBLE);
        } else if (!mTabletSize && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mStepDescription.setVisibility(View.VISIBLE);
        }*/

        return rootView;

    }

    private void setupViewModel() {
        // Observe the LiveData object in the ViewModel

        //setup the listener for the fragment A
        mViewModel.getStepModelSelected().observe(this, new Observer<StepModel>() {
            @Override
            public void onChanged(@Nullable StepModel stepModel) {
                Timber.i("");
                mStepModel = stepModel;

                releasePlayer();

                mSimpleExoPlayerView.setVisibility(View.GONE);
                mStepPicture.setVisibility(View.GONE);
                mStepDescription.setText(mStepModel.getDescription());


                if (!mStepModel.getVideoURL().equals("")) {
                    // Initialize the player.
                    initializePlayer(Uri.parse(mStepModel.getVideoURL()));
                    mSimpleExoPlayerView.setVisibility(View.VISIBLE);
                    mStepPicture.setVisibility(View.GONE);
                    if (!mTabletSize && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mStepDescription.setVisibility(View.GONE);
                        mBtnPrevious.setVisibility(View.GONE);
                        mBtnNext.setVisibility(View.GONE);
                    }else{
                        mStepDescription.setVisibility(View.VISIBLE);
                        mBtnPrevious.setVisibility(View.VISIBLE);
                        mBtnNext.setVisibility(View.VISIBLE);
                    }
                } else {
                    mBtnPrevious.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    mSimpleExoPlayerView.setVisibility(View.GONE);
                    if (!mStepModel.getThumbnailURL().equals("")) {
                        mStepPicture.setVisibility(View.VISIBLE);
                        try {
                            Picasso.with(getActivity())
                                    .load(mStepModel.getThumbnailURL())
                                    //.placeholder(R.mipmap.ic_launcher) // can also be a drawable
                                    .error(R.mipmap.ic_launcher) // will be displayed if the image cannot be loaded
                                    .into(mStepPicture);
                        } catch (Exception e) {
                            e.toString();
                        }
                    } else {
                        if (mTabletSize) {
                            mStepDescription.setVisibility(View.VISIBLE);
                        } else if (!mTabletSize && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            mStepDescription.setVisibility(View.VISIBLE);
                        }
                    }

                }

                mSelectedStepPosition = sharedPreferences.getInt("selected_step_position", 0);

                if (mSelectedStepPosition == 0) {
                    mBtnPrevious.setEnabled(false);
                    mBtnNext.setEnabled(true);
                } else if (mSelectedStepPosition == 6) {
                    mBtnPrevious.setEnabled(true);
                    mBtnNext.setEnabled(false);
                } else {
                    mBtnPrevious.setEnabled(true);
                    mBtnNext.setEnabled(true);
                }

            }
        });

        //Observe on change RecipeStep
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
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
            mSimpleExoPlayerView.setPlayer(mExoPlayer);
            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
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

    /**
     * Release the player when the activity is Paused / Stoped.
     */

    @Override
    public void onPause() {
        super.onPause();

        if (Integer.valueOf(android.os.Build.VERSION.SDK) < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Integer.valueOf(android.os.Build.VERSION.SDK) > 24
                || Integer.valueOf(android.os.Build.VERSION.SDK) == 24) {
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
        mSelectedStepPosition = sharedPreferences.getInt("selected_step_position", 0);
        int newPosition = 0;
        if (v == mBtnPrevious) {
            Timber.i("mBtnPrevious");
            if (mSelectedStepPosition > 0) {
                newPosition = mSelectedStepPosition - 1;
                mStepModel = mStepModelList.get(newPosition);
                mViewModel.setStepModelSelected(mStepModel);
                mEditorPreference.putInt("selected_step_position", newPosition);
                mEditorPreference.apply();
                if (newPosition == 0) {
                    mBtnPrevious.setEnabled(false);
                } else if (newPosition == mStepModelList.size()-2) {
                    mBtnNext.setEnabled(true);
                }
            }
        } else if (v == mBtnNext) {
            Timber.i("mBtnNext");
            if (mSelectedStepPosition < mStepModelList.size() - 1) {
                newPosition = mSelectedStepPosition + 1;
                mStepModel = mStepModelList.get(newPosition);
                mViewModel.setStepModelSelected(mStepModel);
                mEditorPreference.putInt("selected_step_position", newPosition);
                mEditorPreference.apply();
                if (newPosition == 1) {
                    mBtnPrevious.setEnabled(true);
                } else if (newPosition == mStepModelList.size() - 1) {
                    mBtnNext.setEnabled(false);
                }
            }
        }
    }
}