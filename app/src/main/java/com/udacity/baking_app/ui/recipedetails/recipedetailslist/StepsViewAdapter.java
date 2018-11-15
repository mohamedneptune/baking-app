package com.udacity.baking_app.ui.recipedetails.recipedetailslist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.StepModel;

import java.util.List;

public class StepsViewAdapter extends RecyclerView.Adapter<StepsViewAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private List<StepModel> mStepModelList;
    private Context mContext;

    // data is passed into the constructor
    StepsViewAdapter(Context context, List<StepModel> stepModelList) {
        this.mInflater = LayoutInflater.from(context);
        this.mStepModelList = stepModelList;
        mContext = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.stepName.setText(mStepModelList.get(position).getShortDescription());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mStepModelList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView stepName;

        ViewHolder(View itemView) {
            super(itemView);
            stepName = (TextView) itemView.findViewById(R.id.step_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    StepModel getItem(int id) {
        return mStepModelList.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}
