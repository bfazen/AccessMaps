package com.robert.maps.kml.constants;

import com.robert.maps.R;

public interface PoiConstants {
	public static final int EMPTY_ID = -777;
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final String EMPTY = "";
	public static final String ONE_SPACE = " ";

	public static final String LON = "lon";
	public static final String LAT = "lat";
	public static final String NAME = "name";
	public static final String DESCR = "descr";
	public static final String ALT = "alt";
	public static final String CATEGORYID = "categoryid";
	public static final String POINTSOURCEID = "pointsourceid";
	public static final String HIDDEN = "hidden";
	public static final String ICONID = "iconid";
	public static final String MINZOOM = "minzoom";
	public static final String SHOW = "show";
	public static final String TRACKID = "trackid";
	public static final String SPEED = "speed";
	public static final String DATE = "date";
	public static final String CNT = "cnt";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String ACTIVITY = "activity";

	public static final String POINTS = "points";
	public static final String CATEGORY = "category";
	public static final String TRACKS = "tracks";
	public static final String TRACKPOINTS = "trackpoints";
	public static final String DATA = "data";
	public static final String GEODATA_FILENAME = "/geodata.db";
	public static final String TRACK = "Track";

	public static final String UPDATE_POINTS = "pointid = @1";
	public static final String UPDATE_CATEGORY = "categoryid = @1";
	public static final String UPDATE_TRACKS = "trackid = @1";

	public static final String STAT_GET_POI_LIST = "SELECT lat, lon, name, descr, pointid, pointid _id, pointid ID FROM points ORDER BY lat, lon";
//	louis.fazen@gmail thought about adding this in... but on second thought, maybe unnecessary...!
//	public static final String GET_POI_SUM_LIST = "SELECT name, descr FROM points ORDER BY categoryid";
	
	public static final String STAT_PoiListNotHidden = "SELECT poi.lat, poi.lon, poi.name, poi.descr, poi.pointid, poi.pointid _id, poi.pointid ID, poi.categoryid, cat.iconid FROM points poi LEFT JOIN category cat ON cat.categoryid = poi.categoryid WHERE poi.hidden = 0 AND cat.hidden = 0 "
		+"AND cat.minzoom <= @1"
		+ " AND poi.lon BETWEEN @2 AND @3"
		+ " AND poi.lat BETWEEN @4 AND @5"
		+ " ORDER BY lat, lon";
	public static final String STAT_PoiCategoryList = "SELECT name, categoryid _id FROM category ORDER BY name";
	public static final String STAT_ActivityList = "SELECT name, activityid _id FROM activity ORDER BY activityid";
	public static final String STAT_getPoi = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE pointid = @1";
	public static final String STAT_deletePoi = "DELETE FROM points WHERE pointid = @1";
	public static final String STAT_deletePoiByCat = "DELETE FROM points WHERE categoryid = @1";
	public static final String STAT_deletePoiCategory = "DELETE FROM category WHERE categoryid = @1";
	public static final String STAT_getPoiCategory = "SELECT name, categoryid, hidden, iconid, minzoom FROM category WHERE categoryid = @1";
	public static final String STAT_DeleteAllPoi = "DELETE FROM points";
	public static final String STAT_getTrackList = "SELECT tracks.name, activity.name || ', ' || strftime('%%d/%%m/%%Y %%H:%%M:%%S', date, 'unixepoch', 'localtime') As title2, descr, trackid _id, cnt, TIME('2011-01-01', duration || ' seconds') as duration, round(distance/1000, 2) AS distance0, CASE WHEN show=1 THEN "
			+ R.drawable.btn_check_buttonless_on
			+ " ELSE "
			+ R.drawable.btn_check_buttonless_off
			+ " END as image, IFNULL(duration, -1) As NeedStatUpdate, '%s' as units, round(distance/1000/1.609344, 2) AS distance1 FROM tracks LEFT JOIN activity ON activity.activityid = tracks.activity ORDER BY trackid DESC;";
	public static final String STAT_getTrackChecked = "SELECT name, descr, show, trackid, cnt, distance, duration, categoryid, activity, date FROM tracks WHERE show = 1 LIMIT 1";
	public static final String STAT_getTrack = "SELECT name, descr, show, cnt, distance, duration, categoryid, activity, date FROM tracks WHERE trackid = @1";
	public static final String STAT_getTrackPoints = "SELECT lat, lon, alt, speed, date FROM trackpoints WHERE trackid = @1 ORDER BY id";
	public static final String STAT_setTrackChecked_1 = "UPDATE tracks SET show = 1 - show * 1 WHERE trackid = @1";
	public static final String STAT_setTrackChecked_2 = "UPDATE tracks SET show = 0 WHERE trackid <> @1";
	public static final String STAT_deleteTrack_1 = "DELETE FROM trackpoints WHERE trackid = @1";
	public static final String STAT_deleteTrack_2 = "DELETE FROM tracks WHERE trackid = @1";
	public static final String STAT_saveTrackFromWriter = "SELECT lat, lon, alt, speed, date FROM trackpoints ORDER BY id;";
	public static final String STAT_CLEAR_TRACKPOINTS = "DELETE FROM 'trackpoints';";

	public static final String SQL_CREATE_points = "CREATE TABLE 'points' (pointid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,descr VARCHAR,lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',alt FLOAT DEFAULT '0',hidden INTEGER DEFAULT '0',categoryid INTEGER,pointsourceid INTEGER,iconid INTEGER DEFAULT NULL);";
	public static final String SQL_CREATE_category = "CREATE TABLE 'category' (categoryid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, hidden INTEGER DEFAULT '0', iconid INTEGER DEFAULT NULL, minzoom INTEGER DEFAULT '14');";
	public static final String SQL_CREATE_pointsource = "CREATE TABLE IF NOT EXISTS 'pointsource' (pointsourceid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR);";
	public static final String SQL_CREATE_tracks = "CREATE TABLE IF NOT EXISTS 'tracks' (trackid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date DATETIME, show INTEGER, cnt INTEGER, duration INTEGER, distance INTEGER, categoryid INTEGER, activity INTEGER);";
	public static final String SQL_CREATE_trackpoints = "CREATE TABLE IF NOT EXISTS 'trackpoints' (trackid INTEGER NOT NULL, id INTEGER NOT NULL PRIMARY KEY UNIQUE, lat FLOAT, lon FLOAT, alt FLOAT, speed FLOAT, date DATETIME);";
	public static final String SQL_CREATE_activity = "CREATE TABLE 'activity' (activityid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR);";
	public static final String SQL_CREATE_drop_activity = "DROP TABLE IF EXISTS 'activity';";
	public static final String SQL_CREATE_insert_activity = "INSERT INTO 'activity' (activityid, name) VALUES (%d, '%s');";

//	louis.fazen added this db, but it is not yet implemented...
	public static final String SQL_CREATE_poiicons = "CREATE TABLE 'poiicons' (iconid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, image BLOB);";

	//louis.fazen@gmail.com changed this, but will need to change again to make it more generalizeable 
	//TODO!: change this category table so that instead of iconid you have a blob, and then import the blobs from the file system
	//They can be assigned an auto-increment value for categoryid, but the import process should have a way to pull in the values from the SDCard... 
	//or even better would be from a google account, and then they could be updated?  the name should be brought in by the string name from the kml file, and it 
	//should be associated with an icon blob that has the same name from the SDCard
	public static final String SQL_ADD_category = "INSERT INTO 'category' (categoryid, name, hidden, iconid) VALUES (0, 'My POI', 0, "
			+ R.drawable.poiblue + ");";

	public static final String SQL_UPDATE_1_1 = "DROP TABLE IF EXISTS 'points_45392250'; ";
	public static final String SQL_UPDATE_1_2 = "CREATE TABLE 'points_45392250' AS SELECT * FROM 'points';";
	public static final String SQL_UPDATE_1_3 = "DROP TABLE 'points';";
	public static final String SQL_UPDATE_1_5 = "INSERT INTO 'points' (pointid, name, descr, lat, lon, alt, hidden, categoryid, pointsourceid, iconid) SELECT pointid, name, descr, lat, lon, alt, hidden, categoryid, pointsourceid, "
			+ R.drawable.poi + " FROM 'points_45392250';";
	public static final String SQL_UPDATE_1_6 = "DROP TABLE 'points_45392250';";

	public static final String SQL_UPDATE_1_7 = "DROP TABLE IF EXISTS 'category_46134312'; ";
	public static final String SQL_UPDATE_1_8 = "CREATE TABLE 'category_46134312' AS SELECT * FROM 'category';";
	public static final String SQL_UPDATE_1_9 = "DROP TABLE 'category';";
	public static final String SQL_UPDATE_1_11 = "INSERT INTO 'category' (categoryid, name) SELECT categoryid, name FROM 'category_46134312';";
	public static final String SQL_UPDATE_1_12 = "DROP TABLE 'category_46134312';";

	public static final String SQL_UPDATE_2_7 = "DROP TABLE IF EXISTS 'category_46134313'; ";
	public static final String SQL_UPDATE_2_8 = "CREATE TABLE 'category_46134313' AS SELECT * FROM 'category';";
	public static final String SQL_UPDATE_2_9 = "DROP TABLE 'category';";
	public static final String SQL_UPDATE_2_11 = "INSERT INTO 'category' (categoryid, name, hidden, iconid) SELECT categoryid, name, hidden, iconid FROM 'category_46134313';";
	public static final String SQL_UPDATE_2_12 = "DROP TABLE 'category_46134313';";

	public static final String SQL_UPDATE_6_1 = "DROP TABLE IF EXISTS 'tracks_46134313'; ";
	public static final String SQL_UPDATE_6_2 = "CREATE TABLE 'tracks_46134313' AS SELECT * FROM 'tracks'; ";
	public static final String SQL_UPDATE_6_3 = "DROP TABLE IF EXISTS 'tracks'; ";
	public static final String SQL_UPDATE_6_4 = "INSERT INTO 'tracks' (trackid, name, descr, date, show, cnt, duration, distance, categoryid, activity) SELECT trackid, name, descr, date, show, (SELECT COUNT(*) FROM trackpoints WHERE trackid = tracks_46134313.trackid), null, null, null, 0 FROM 'tracks_46134313';";
	public static final String SQL_UPDATE_6_5 = "DROP TABLE 'tracks_46134313';";
	//(trackid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date DATETIME
	//, show INTEGER, cnt INTEGER, duration INTEGER, distance INTEGER, categoryid INTEGER, activity INTEGER);";
}
