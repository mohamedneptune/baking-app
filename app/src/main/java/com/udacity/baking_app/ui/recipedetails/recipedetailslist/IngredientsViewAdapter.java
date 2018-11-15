package com.udacity.baking_app.ui.recipedetails.recipedetailslist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.baking_app.R;
import com.udacity.baking_app.data.model.IngredientModel;

import java.util.List;

public class IngredientsViewAdapter extends RecyclerView.Adapter<IngredientsViewAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private List<IngredientModel> mIngredientModelList;
    private Context mContext;

    // data is passed into the constructor
    IngredientsViewAdapter(Context context, List<IngredientModel> ingredientModelList) {
        this.mInflater = LayoutInflater.from(context);
        this.mIngredientModelList = ingredientModelList;
        mContext = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.ingredientQuantity.setText(mIngredientModelList.get(position).getQuantity().toString());
        holder.ingredientMeasure.setText(mIngredientModelList.get(position).getMeasure());
        holder.ingredientName.setText(mIngredientModelList.get(position).getIngredient());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mIngredientModelList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView ingredientQuantity;
        TextView ingredientMeasure;
        TextView ingredientName;

        ViewHolder(View itemView) {
            super(itemView);
            ingredientQuantity = (TextView) itemView.findViewById(R.id.ingredient_quantity);
            ingredientMeasure = (TextView) itemView.findViewById(R.id.ingredient_mesure);
            ingredientName = (TextView) itemView.findViewById(R.id.ingredient_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    IngredientModel getItem(int id) {
        return mIngredientModelList.get(id);
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
