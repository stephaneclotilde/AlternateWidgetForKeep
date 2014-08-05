package com.stephapps.alternativewidgetforkeep;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Steph on 20/07/2014.
 */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<String> listItemList = new ArrayList<String>();
    private Context context = null;
    private int appWidgetId;



    public ListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() {

        Log.i("ListProvider","test");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> notesList = prefs.getStringSet("textNotes", null);
        if (notesList!=null) {
            int nbNotes = notesList.size();
            for (String s : notesList) {
                Log.i("ListProvider", "" + s);
                listItemList.add(s);
            }
        }
        else
        {
            for (int i = 0; i < 30; i++) {
                String listItem = "Heading" + i;
                listItemList.add(listItem);
            }
        }

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position)
    {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.list_row);
        String listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.txtRow, listItem);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}