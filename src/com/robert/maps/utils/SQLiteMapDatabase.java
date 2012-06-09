package com.robert.maps.utils;

import java.io.File;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import com.robert.maps.R;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class SQLiteMapDatabase {
	//louis.fazen added this ptoected Context... but no longer "final" as it is in other classes?!
	protected Context mCtx;
	private static final String SQL_CREATE_tiles = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s));";
	private static final String SQL_CREATE_info = "CREATE TABLE IF NOT EXISTS info (maxzoom Int, minzoom Int);";
	private SQLiteDatabase mDatabase;

	public void setFile(final String aFileName) throws SQLiteException {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase Constructor is called... which in turn creates an imageDB (SHOULD NOT NEED THIS!); aFileName is :" + aFileName);
		
		if (mDatabase != null)
			mDatabase.close();

		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase creates CashDatabaseHelper aFileName is :" + aFileName);
		
		mDatabase = new CashDatabaseHelper(null, aFileName).getWritableDatabase();
		Ut.d("CashDatabase: Open SQLITEDB Database");

	}

	public void setFile(final File aFile) throws SQLiteException {
		setFile(aFile.getAbsolutePath());
	}

	protected class CashDatabaseHelper extends RSQLiteOpenHelper {
		
		public CashDatabaseHelper(final Context context, final String name) {
			
			super(context, name, null, 3);
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase CashDatabaseHelper is called and name is :" + name);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_tiles);
			db.execSQL(SQL_CREATE_info);
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase CashDatabaseHelper creates db with statement tiles: " + SQL_CREATE_tiles);
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase CashDatabaseHelper creates db with statement info: " + SQL_CREATE_info);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}

	public void updateMinMaxZoom() throws SQLiteException {
		if(mDatabase != null){
			Ut.dd("Update min max");
			this.mDatabase.execSQL("DROP TABLE IF EXISTS info");
			this.mDatabase.execSQL("CREATE TABLE info As SELECT 0 As minzoom, 0 As maxzoom;");
			this.mDatabase.execSQL("UPDATE info SET minzoom = (SELECT DISTINCT z FROM tiles ORDER BY z ASC LIMIT 1);");
			this.mDatabase.execSQL("UPDATE info SET maxzoom = (SELECT DISTINCT z FROM tiles ORDER BY z DESC LIMIT 1);");
		}
	}

	public /*synchronized*/ void putTile(final int aX, final int aY, final int aZ, final byte[] aData) {
		if (this.mDatabase != null) {
			final ContentValues cv = new ContentValues();
			cv.put("x", aX);
			cv.put("y", aY);
			cv.put("z", 17 - aZ);
			cv.put("s", 0);
			cv.put("image", aData);
			this.mDatabase.insert("tiles", null, cv);
		}
	}

	public /*synchronized*/ byte[] getTile(final int aX, final int aY, final int aZ) {
		byte[] ret = null;
//louis.fazen@gmail.com  may need to change this math to account for the MapCreator SQLiteDB where there is a -1 value for zoom!!!
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase getTile is Called");
		if (this.mDatabase != null) {
			final Cursor c = this.mDatabase.rawQuery("SELECT image FROM tiles WHERE s = 0 AND x = " + aX + " AND y = "
					+ aY + " AND z = " + (17 - aZ), null);
			if (c != null) {
				if (c.moveToFirst()) {
					ret = c.getBlob(c.getColumnIndexOrThrow("image"));
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase getTile query is NOT NULL");
				}
				c.close();
			}
		}

		return ret;
	}
	
	//louis.fazen@gmail.com  added this whole class... not sure if it will work!!!
	public Bitmap getDefaultTile(final Context ctx) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase getDefaultTile is Called");
		this.mCtx = ctx;
		Bitmap defaultRet = null;
		BitmapDrawable drawable = (BitmapDrawable)mCtx.getResources().getDrawable(R.drawable.default_tile);
		
        defaultRet = drawable.getBitmap();

		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "SQLiteMapDatabase getTile query is NOT NULL");
		return defaultRet;
	}

	public int getMaxZoom() {
		int ret = 99;
		if(mDatabase != null){
			final Cursor c = this.mDatabase.rawQuery("SELECT 17-minzoom AS ret FROM info", null);
			if (c != null) {
				if (c.moveToFirst()) {
					ret = c.getInt(c.getColumnIndexOrThrow("ret"));
				}
				c.close();
			}
		};
		return ret;
	}

	public int getMinZoom() {
		int ret = 0;
		if(mDatabase != null){
			final Cursor c = this.mDatabase.rawQuery("SELECT 17-maxzoom AS ret FROM info", null);
			if (c != null) {
				if (c.moveToFirst()) {
					ret = c.getInt(c.getColumnIndexOrThrow("ret"));
				}
				c.close();
			}
		}
		return ret;
	}

	@Override
	protected void finalize() throws Throwable {
		Ut.dd("finalize: Close SQLITEDB Database database");
		if(mDatabase != null)
			mDatabase.close();
		super.finalize();
	}

	public void freeDatabases() {
		if(mDatabase != null)
			if(mDatabase.isOpen())
			{
				mDatabase.close();
				Ut.dd("Close SQLITEDB Database");
			}
	}


}
