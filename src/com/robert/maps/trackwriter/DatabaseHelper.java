package com.robert.maps.trackwriter;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.robert.maps.kml.constants.PoiConstants;
import com.robert.maps.utils.RSQLiteOpenHelper;

public class DatabaseHelper extends RSQLiteOpenHelper {

	public DatabaseHelper(Context context, String name) {
		
		super(context, name, null, 1);
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "DatabaseHelper Constructor is called and name is: " + name);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PoiConstants.SQL_CREATE_trackpoints);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
