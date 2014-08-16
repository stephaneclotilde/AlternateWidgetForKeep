package com.stephapps.alternativewidgetforkeep;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.stephapps.alternativewidgetforkeep.misc.Constants;
import com.stephapps.alternativewidgetforkeep.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steph on 20/07/2014.
 */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<Note> listNotes = new ArrayList<Note>();
    private Context context = null;
    private int appWidgetId;
    private Note mCurNote;

    private static final String TAG = ListProvider.class.toString();

    public ListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int nbNotes = prefs.getInt(Constants.NB_NOTES, -1);
        for (int i=0;i<nbNotes;i++)
        {
            Note note = new Note();
            note.setNoteText(prefs.getString(Constants.TEXT_NOTE+i,null));
            note.setColor(prefs.getString(Constants.NOTE_COLOR+i,null));
            listNotes.add(note);
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
        return listNotes.size();
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
        mCurNote = listNotes.get(position);
        remoteView.setTextViewText(R.id.txtRow, mCurNote.getNoteText());
        String color = mCurNote.getColor();
        int colorInt = android.R.color.white ;
        if (color==null)                    colorInt = android.R.color.white;
        else if (color.equals("GRAY"))      colorInt = android.R.color.darker_gray;
        else if (color.equals("TEAL"))      colorInt = android.R.color.white;
        else if (color.equals("RED"))       colorInt = android.R.color.holo_red_light;
        else if (color.equals("BLUE"))      colorInt = android.R.color.holo_blue_bright;
        else if (color.equals("ORANGE"))    colorInt = android.R.color.holo_orange_light;
        else if (color.equals("YELLOW"))    colorInt = android.R.color.holo_purple;
        remoteView.setInt(R.id.txtRow, "setBackgroundColor", context.getResources().getColor(colorInt));
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