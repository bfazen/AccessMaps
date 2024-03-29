package com.robert.maps.kml;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.robert.maps.R;
import com.robert.maps.kml.constants.PoiConstants;
import com.robert.maps.utils.RSQLiteOpenHelper;
import com.robert.maps.utils.Ut;

public class GeoDatabase implements PoiConstants{
	protected final Context mCtx;
	private SQLiteDatabase mDatabase;
	protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public GeoDatabase(Context ctx) {
		super();
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "GeoDatabase Constructor is called");
		mCtx = ctx;
		mDatabase = getDatabase();
	}

	public void addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
			final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			this.mDatabase.insert(POINTS, null, cv);
		}
	}

	public void updatePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
			final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(POINTS, cv, UPDATE_POINTS, args);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if(mDatabase != null){
			if(mDatabase.isOpen()){
				mDatabase.close();
				mDatabase = null;
			}
		}
		super.finalize();
	}

	public Cursor getPoiListCursor() {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_GET_POI_LIST, null);
		}

		return null;
	}
	
//	louis.fazen@gmail.com added this method
//	public Cursor getPoiSummaryListCursor() {
//		if (isDatabaseReady()) {
//			// �� ������ ������� �����
//			return mDatabase.rawQuery(GET_POI_SUM_LIST, null);
//		}
//
//		return null;
//	}
	

	public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(zoom + 1),Double.toString(left),Double.toString(right),Double.toString(bottom),Double.toString(top)};
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_PoiListNotHidden, args);
		}

		return null;
	}

	public Cursor getPoiCategoryListCursor() {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_PoiCategoryList, null);
		}

		return null;
	}

	public Cursor getActivityListCursor() {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_ActivityList, null);
		}

		return null;
	}

	public Cursor getPoi(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_getPoi, args);
		}

		return null;
	}

	public void deletePoi(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {new Double(id)};
			mDatabase.execSQL(STAT_deletePoi, args);
		}
	}
	
	public void deletePoiByCategory(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {new Double(id)};
			mDatabase.execSQL(STAT_deletePoiByCat, args);
		}
	}

	public void deletePoiCategory(final int id) {
		if (isDatabaseReady()) { // predef category My POI never delete louis.fazen@gmail.com changed this so you could delete it!
			final Double[] args = {new Double(id)};
			mDatabase.execSQL(STAT_deletePoiCategory, args);
		}
	}

	private boolean isDatabaseReady() {
		boolean ret = true;

		if(mDatabase == null)
			mDatabase = getDatabase();

		if(mDatabase == null)
			ret = false;
		else if(!mDatabase.isOpen())
			mDatabase = getDatabase();

		if(ret == false)
			try {
				Toast.makeText(mCtx, mCtx.getText(R.string.message_geodata_notavailable), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		return ret;
	}

	public void FreeDatabases(){
		if(mDatabase != null){
			if(mDatabase.isOpen()){
				mDatabase.close();
				mDatabase = null;
			}
		}
	}

	protected SQLiteDatabase getDatabase() {
		File folder = Ut.getRMapsMainDir(mCtx, DATA);
		if(!folder.exists()) // no sdcard
			return null;

		SQLiteDatabase db = new GeoDatabaseHelper(mCtx, folder.getAbsolutePath() + GEODATA_FILENAME).getWritableDatabase();

		return db;
	}

	protected class GeoDatabaseHelper extends RSQLiteOpenHelper {
		public GeoDatabaseHelper(final Context context, final String name) {
			super(context, name, null, 18);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(PoiConstants.SQL_CREATE_points);
			db.execSQL(PoiConstants.SQL_CREATE_pointsource);
			db.execSQL(PoiConstants.SQL_CREATE_category);
			db.execSQL(PoiConstants.SQL_ADD_category);
			db.execSQL(PoiConstants.SQL_CREATE_tracks);
			db.execSQL(PoiConstants.SQL_CREATE_trackpoints);
			LoadActivityListFromResource(db);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
//			Ut.dd("Upgrade data.db from ver." + oldVersion + " to ver."
//					+ newVersion);

			if (oldVersion < 2) {
				db.execSQL(PoiConstants.SQL_UPDATE_1_1);
				db.execSQL(PoiConstants.SQL_UPDATE_1_2);
				db.execSQL(PoiConstants.SQL_UPDATE_1_3);
				db.execSQL(PoiConstants.SQL_CREATE_points);
				db.execSQL(PoiConstants.SQL_UPDATE_1_5);
				db.execSQL(PoiConstants.SQL_UPDATE_1_6);
				db.execSQL(PoiConstants.SQL_UPDATE_1_7);
				db.execSQL(PoiConstants.SQL_UPDATE_1_8);
				db.execSQL(PoiConstants.SQL_UPDATE_1_9);
				db.execSQL(PoiConstants.SQL_CREATE_category);
				db.execSQL(PoiConstants.SQL_ADD_category);
				//db.execSQL(PoiConstants.SQL_UPDATE_1_11);
				//db.execSQL(PoiConstants.SQL_UPDATE_1_12);
			}
			if (oldVersion < 3) {
				db.execSQL(PoiConstants.SQL_UPDATE_2_7);
				db.execSQL(PoiConstants.SQL_UPDATE_2_8);
				db.execSQL(PoiConstants.SQL_UPDATE_2_9);
				db.execSQL(PoiConstants.SQL_CREATE_category);
				db.execSQL(PoiConstants.SQL_UPDATE_2_11);
				db.execSQL(PoiConstants.SQL_UPDATE_2_12);
			}
			if (oldVersion < 5) {
				db.execSQL(PoiConstants.SQL_CREATE_tracks);
				db.execSQL(PoiConstants.SQL_CREATE_trackpoints);
			}
			if (oldVersion < 18) {
				db.execSQL(PoiConstants.SQL_UPDATE_6_1);
				db.execSQL(PoiConstants.SQL_UPDATE_6_2);
				db.execSQL(PoiConstants.SQL_UPDATE_6_3);
				db.execSQL(PoiConstants.SQL_CREATE_tracks);
				db.execSQL(PoiConstants.SQL_UPDATE_6_4);
				db.execSQL(PoiConstants.SQL_UPDATE_6_5);
				LoadActivityListFromResource(db);
			}
		}

	}

	public Cursor getPoiCategory(final int id) {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoiCategory, args);
		}

		return null;
	}

	public void LoadActivityListFromResource(final SQLiteDatabase db) {
		db.execSQL(PoiConstants.SQL_CREATE_drop_activity);
		db.execSQL(PoiConstants.SQL_CREATE_activity);
    	String[] act = mCtx.getResources().getStringArray(R.array.track_activity);
    	for(int i = 0; i < act.length; i++){
    		db.execSQL(String.format(PoiConstants.SQL_CREATE_insert_activity, i, act[i]));
    	}
	}


	
	public void addPoiCategory(final String title, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			this.mDatabase.insert(CATEGORY, null, cv);
		}
	}
	
	public void loadPoiCategory(final int id, final String title, final int hidden, final int iconid, final int minzoom) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(CATEGORYID, id);
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			cv.put(MINZOOM, minzoom);
			this.mDatabase.insert(CATEGORY, null, cv);
		}
	}

	public void updatePoiCategory(final int id, final String title, final int hidden, final int iconid, final int minzoom) {
		if (isDatabaseReady()) {
	        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "GeoDatabase updatePoiCategory is about to add some fields!");
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			cv.put(MINZOOM, minzoom);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(CATEGORY, cv, UPDATE_CATEGORY, args);
		}
	}

	public void DeleteAllPoi() {
		if (isDatabaseReady()) {
			mDatabase.execSQL(STAT_DeleteAllPoi);
		}
	}

	public void beginTransaction(){
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "GeoDatabase BeginTransaction is about to be called");
		mDatabase.beginTransaction();
	}

	public void rollbackTransaction(){
		mDatabase.endTransaction();
	}

	public void commitTransaction(){
		mDatabase.setTransactionSuccessful();
		mDatabase.endTransaction();
	}

	public Cursor getTrackListCursor(final String units) {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			return mDatabase.rawQuery(String.format(STAT_getTrackList, units), null);
		}

		return null;
	}

	public long addTrack(final String name, final String descr, final int show, final int cnt, final double distance,
			final double duration, final int category, final int activity, final Date date) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date.getTime()/1000);
			newId = this.mDatabase.insert(TRACKS, null, cv);
		}

		return newId;
	}

	public void updateTrack(final int id, final String name, final String descr, final int show, final int cnt, final double distance, final double duration, final int category, final int activity, final Date date) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date.getTime()/1000);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);
		}
	}

	public void addTrackPoint(final long trackid, final double lat,
			final double lon, final double alt, final double speed,
			final Date date) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(TRACKID, trackid);
			cv.put(LAT, lat);
			cv.put(LON, lon);
			cv.put(ALT, alt);
			cv.put(SPEED, speed);
			cv.put(DATE, date.getTime()/1000);
			this.mDatabase.insert(TRACKPOINTS, null, cv);
		}
	}

	public Cursor getTrackChecked() {
		if (isDatabaseReady()) {
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_getTrackChecked, null);
		}

		return null;
	}

	public Cursor getTrack(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_getTrack, args);
		}

		return null;
	}

	public Cursor getTrackPoints(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			// �� ������ ������� �����
			return mDatabase.rawQuery(STAT_getTrackPoints, args);
		}

		return null;
	}

	public void setTrackChecked(final int id){
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_setTrackChecked_1, args);
			mDatabase.execSQL(STAT_setTrackChecked_2, args);
		}
	}

	public void deleteTrack(final int id) {
		if (isDatabaseReady()) {
			beginTransaction();
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_deleteTrack_1, args);
			mDatabase.execSQL(STAT_deleteTrack_2, args);
			commitTransaction();
		}
	}

	public int saveTrackFromWriter(final SQLiteDatabase db){
		int res = 0;
		if (isDatabaseReady()) {
			final Cursor c = db.rawQuery(STAT_saveTrackFromWriter, null);
			if(c != null){
				if(c.getCount() > 1){
					beginTransaction();

					res = c.getCount();
					long newId = -1;

					final ContentValues cv = new ContentValues();
					cv.put(NAME, TRACK);
					cv.put(SHOW, 0);
					cv.put(ACTIVITY, 0);
					cv.put(CATEGORYID, 0);
					newId = mDatabase.insert(TRACKS, null, cv);
					res = (int) newId;

					cv.put(NAME, TRACK+ONE_SPACE+newId);
					if (c.moveToFirst()) {
						cv.put(DATE, c.getInt(4));
					}
					final String[] args = {Long.toString(newId)};
					mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);

					if (c.moveToFirst()) {
						do {
							cv.clear();
							cv.put(TRACKID, newId);
							cv.put(LAT, c.getDouble(0));
							cv.put(LON, c.getDouble(1));
							cv.put(ALT, c.getDouble(2));
							cv.put(SPEED, c.getDouble(3));
							cv.put(DATE, c.getInt(4));
							mDatabase.insert(TRACKPOINTS, null, cv);
						} while (c.moveToNext());
					}

					commitTransaction();
				}
				c.close();

				db.execSQL(STAT_CLEAR_TRACKPOINTS);
			}

		}

		return res;
	}


}
