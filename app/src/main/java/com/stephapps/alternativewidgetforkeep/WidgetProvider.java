package com.stephapps.alternativewidgetforkeep;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WidgetProvider extends AppWidgetProvider {

    SQLiteDatabase db;
    private Set<String> mTextNotes;

	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			AppWidgetManager
					.getInstance(context)
					.updateAppWidget(
							intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
							views);
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

            File file = context.getDatabasePath(path);
            if (file.exists()) {

                try {
                    db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                    Cursor cursor = db.rawQuery("select * from text_search_list_items ", null);

                    if (cursor != null) {
                        mTextNotes = new HashSet<String>();
                        cursor.moveToFirst();
                        do {
                            mTextNotes.add(cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                }catch(Exception e) {e.printStackTrace();}


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putStringSet("textNotes", mTextNotes);
                edit.commit();
            }

            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "660" ,path});
            changeFilePermissions(new String[]{"su" , "-c" , "chmod", "660" , path+"-journal"});

        }
        else
            Toast.makeText(context, "U NID ROOT", Toast.LENGTH_SHORT).show();


        final int N = appWidgetIds.length;
        for (int i = 0; i < N; ++i) {
            RemoteViews remoteViews = updateWidgetListView(context,
                    appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i],
                    remoteViews);
        }



        /*int[] appWidgetIds holds ids of multiple instance
         * of your widget
         * meaning you are placing more than one widgets on
         * your homescreen*/

        super.onUpdate(context, appWidgetManager, appWidgetIds);
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