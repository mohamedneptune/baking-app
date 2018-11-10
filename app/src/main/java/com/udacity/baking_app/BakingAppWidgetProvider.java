package com.udacity.baking_app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.udacity.baking_app.ui.recipeslist.MainActivity;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class BakingAppWidgetProvider extends AppWidgetProvider {


    static private SharedPreferences.Editor mEditorPreference;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence recipeLabel = context.getString(R.string.recipe_name);
        CharSequence ingredientLabel = context.getString(R.string.ingredient_list);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mEditorPreference = sharedPreferences.edit();

        //get values of last selected recipe from sharedPreference
        String recipeValue = sharedPreferences.getString("selected_recipe_name", "");
        String ingredientsValue = sharedPreferences.getString("selected_recipe_ingredient_list", "");

        if(recipeValue.equals("")){
            CharSequence recipe = context.getString(R.string.select_recipe);
            recipeValue = recipe.toString();
            ingredientLabel = "";
            ingredientsValue = "";
        }

        Timber.i("Recipe : " + recipeValue);


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.baking_app_widget_provider);

        views.setTextViewText(R.id.recipe_text, recipeValue);
        views.setTextViewText(R.id.ingredient_text, ingredientLabel + " "  + ingredientsValue);

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0 , intent, 0);

        views.setOnClickPendingIntent(R.id.root_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

