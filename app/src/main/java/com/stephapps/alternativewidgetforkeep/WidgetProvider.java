package com.stephapps.alternativewidgetforkeep;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.stephapps.alternativewidgetforkeep.misc.Constants;
import com.stephapps.alternativewidgetforkeep.model.Note;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WidgetProvider extends AppWidgetProvider {

   public static final String EXTRA_LIST_VIEW_ROW_NUMBER = "com.stephapps.alternatewidgetforkeep.EXTRA_LIST_VIEW_ROW_NUMBER";
   public static final String NOTE_CLICKED = "com.stephapps.alternatewidgetforkeep.NOTE_CLICKED";
   SQLiteDatabase db;
   private List<Note> mNotes = new ArrayList<Note>();

	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		String action = intent.getAction();
        Log.i("WidgetProvider","test"+action);
		if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			AppWidgetManager
					.getInstance(context)
					.updateAppWidget(
							intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
							views);
		}
        else if (action.equals(NOTE_CLICKED))
        {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_LIST_VIEW_ROW_NUMBER, 0);
            Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();

        }
	}

	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);


	}

    @Override
    public void onUpdate(Context context, AppWidgetManager
            appWidgetManager,int[] appWidgetIds) {

        if (RootTools.isAccessGiven())
        {
            String path = "/data/data/com.google.android.keep/databases/keep.db" ;

            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "777" ,path});
            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "777" , path+"-journal"});

            if (fillNoteListFromKeepDB(context, path))
                saveNotesInfosToPrefs(context);

            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "660" ,path});
            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "660" , path+"-journal"});

        }
        else
            Toast.makeText(context, "U NID ROOT", Toast.LENGTH_SHORT).show();


        final int N = appWidgetIds.length;
        for (int i = 0; i < N; ++i)
        {
            RemoteViews remoteViews = updateWidgetListView(context,
                    appWidgetIds[i]);

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(WidgetProvider.NOTE_CLICKED);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.lstViewNotes, pendingIntent);


            appWidgetManager.updateAppWidget(appWidgetIds[i],  remoteViews);

        }



        /*int[] appWidgetIds holds ids of multiple instance
         * of your widget
         * meaning you are placing more than one widgets on
         * your homescreen*/

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private boolean fillNoteListFromKeepDB(Context context, String path)
    {
        Cursor cursor = null;
        File file = context.getDatabasePath(path);
        if (file.exists())
        {

            try {
                db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                cursor = db.rawQuery("select * from list_item ", null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    do {
                        mNotes.add(new Note(cursor.getInt(5), cursor.getString(4)));
                    } while (cursor.moveToNext());

                    cursor.close();
                }

                cursor = db.rawQuery("select * from tree_entity", null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    do {
                        int id = cursor.getInt(0);
                        for (Note note : mNotes)
                        {
                            if (id==note.getId())
                            {
                                note.setColor(cursor.getString(6));
                                break;
                            }
                        }

                    } while (cursor.moveToNext());

                    cursor.close();
                }
                db.close();
            } catch (Exception e) {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
                e.printStackTrace();

                return false;
            }

            return true;
        }
        return false;
    }

    private void saveNotesInfosToPrefs(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();

        int nbNotes = mNotes.size();
        Note currentNote;
        for (int i=0;i<nbNotes;i++)
        {
            currentNote = mNotes.get(i);
            edit.putString(Constants.TEXT_NOTE+i,currentNote.getNoteText());
            edit.putInt(Constants.NOTE_ID+i, currentNote.getId());
            edit.putString(Constants.NOTE_COLOR+i, currentNote.getColor());

            edit.putInt(Constants.NB_NOTES, nbNotes);
        }
        edit.commit();
    }

    private void changeFilePermissions(String[] newPermissions)
    {
        try {
            Process process = new ProcessBuilder(newPermissions).start();
            process.waitFor();
        } catch (IOException e) { e.printStackTrace();}
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),R.layout.widget);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.lstViewNotes,
                svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.lstViewNotes, R.id.empty_view);
        return remoteViews;
    }

}