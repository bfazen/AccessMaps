package com.robert.maps.kml;

import org.andnav.osm.util.GeoPoint;

import android.text.Html;

import com.robert.maps.R;
import com.robert.maps.kml.constants.PoiConstants;

public class PoiPoint implements PoiConstants {

	private final int Id;
	public String Title;
//	louis.fazen 7/5/12 is changing this to an Html type rather than a String type
	public String Descr;
	public GeoPoint GeoPoint;
	public int IconId;
	public double Alt;
	public int CategoryId;
	public int PointSourceId;
	public boolean Hidden;

//	louis.fazen 7/5/12 is changing this to an Html type rather than a String type
	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint,
			int iconid, int categoryid, double alt, int sourseid, int hidden) {
		this.Id = id;
		this.Title = mTitle;
		this.Descr = mDescr;
		this.GeoPoint = mGeoPoint;
		this.IconId = iconid;
		this.Alt = alt;
		this.CategoryId = categoryid;
		this.PointSourceId = sourseid;
		this.Hidden = hidden == 1 ? true : false;
	}

	public PoiPoint(){
		this(EMPTY_ID, "", null, null, R.drawable.poi, 0, 0, 0, 0);
	}

//	louis.fazen 7/5/12 is changing this to an Html type rather than a String type
	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint, int categoryid, int iconid) {
		this(id, mTitle, mDescr, mGeoPoint, iconid, categoryid, 0, 0, 0);
	}

//	louis.fazen 7/5/12 is changing this to an Html type rather than a String type
	public PoiPoint(String mTitle, String mDescr, GeoPoint mGeoPoint, int iconid) {
		this(EMPTY_ID, mTitle, mDescr, mGeoPoint, iconid, 0, 0, 0, 0);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}

}
