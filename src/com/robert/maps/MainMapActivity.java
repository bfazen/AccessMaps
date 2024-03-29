//TODO! Direction http://maps.google.com/maps/nav?key=ABQIAAAAzr2EBOXUKnm_jVnk0OJI7xSosDVG8KKPE1-m51RBrvYughuyMxQ-i1QfUnH94QxWIa6N4U6MouMmBA&output=js&dirflg=d&hl=en&mapclient=jsapi&q=from:%2065.366837,26.71875%20to:%2065.50,26.80
// from:%2065.366837,26.71875%20to:%2065.50,26.80
// http://maps.google.com/maps?saddr=65.366837,26.71875&daddr=65.50,26.80&output=kml
package com.robert.maps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.OpenStreetMapActivity;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileDownloader;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.StreamUtils;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.alphabetbloc.maps.kml.LoadDefaultPoi;
import com.alphabetbloc.maps.kml.LoadDefaultPoiCategories;
import com.alphabetbloc.maps.kml.LoadPoiActivity;
import com.robert.maps.kml.ImportPoiActivity;
import com.robert.maps.kml.ImportPoiActivity2;
import com.robert.maps.kml.ImportTrackActivity;
import com.robert.maps.kml.PoiActivity;
import com.robert.maps.kml.PoiListActivity;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.PoiPoint;
import com.robert.maps.kml.Track;
import com.robert.maps.kml.TrackListActivity;
import com.robert.maps.kml.XMLparser.PredefMapsParser;
import com.robert.maps.overlays.CurrentTrackOverlay;
import com.robert.maps.overlays.MyLocationOverlay;
import com.robert.maps.overlays.PoiOverlay;
import com.robert.maps.overlays.SearchResultOverlay;
import com.robert.maps.overlays.TrackOverlay;
import com.robert.maps.overlays.YandexTrafficOverlay;
//louis.fazen@gmail.com commented out the tracks and search options
//import com.robert.maps.utils.CompassView;
import com.robert.maps.utils.CrashReportHandler;
import com.robert.maps.utils.ScaleBarDrawable;
import com.robert.maps.utils.SearchSuggestionsProvider;
import com.robert.maps.utils.Ut;

public class MainMapActivity extends OpenStreetMapActivity implements OpenStreetMapConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv; //, mOsmvMinimap;
	private MyLocationOverlay mMyLocationOverlay;
	private PoiOverlay mPoiOverlay;
	private CurrentTrackOverlay mCurrentTrackOverlay;
	private TrackOverlay mTrackOverlay;
	private SearchResultOverlay mSearchResultOverlay;
	private LoadDefaultPoi mLoadDefaultPoi;
	private LoadDefaultPoiCategories mLoadDefaultPoiCategories;
	
	private LoadPoiActivity mLoadPoiActivity;
	
	private PowerManager.WakeLock myWakeLock;
	private boolean mFullScreen;
	private boolean mShowTitle;
	private String mStatusListener = "";
	
	// 	 louis.fazen@gmail.com changed this default to false 
	// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//	private boolean mAutoFollow = false;
	private Handler mCallbackHandler = new MainActivityCallbackHandler();
	// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//	private ImageView ivAutoFollow;
	//louis.fazen@gmail.com commented out the tracks and search options
	//private CompassView mCompassView;
	private SensorManager mOrientationSensorManager;
	//louis.fazen@gmail.com commented out the tracks and search options
	//private boolean mCompassEnabled;
	private boolean mDrivingDirectionUp;
	private boolean mNorthDirectionUp;
	private int mScreenOrientation;
	private float mLastSpeed, mLastBearing;
	private PoiManager mPoiManager;
	private String ACTION_SHOW_POINTS = "com.robert.maps.action.SHOW_POINTS";
	private int mMarkerIndex;

	private final SensorEventListener mListener = new SensorEventListener() {
		private int iOrientation = -1;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		public void onSensorChanged(SensorEvent event) {
			if (iOrientation < 0)
				iOrientation = ((WindowManager) MainMapActivity.this.getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay().getOrientation();
			
			//louis.fazen@gmail.com commented out the tracks and search options
//			mCompassView.setAzimuth(event.values[0] + 90 * iOrientation);
//			mCompassView.invalidate();
/*
			if (mCompassEnabled)
				if (mNorthDirectionUp)
					if (mDrivingDirectionUp == false || mLastSpeed == 0) {
						mOsmv.setBearing(updateBearing(event.values[0]) + 90 * iOrientation);
						mOsmv.invalidate();
					}
			*/
		}

	};

	private float updateBearing(float newBearing) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity updateBearing is Called");
		float dif = newBearing - mLastBearing;
		// find difference between new and current position
		if (Math.abs(dif) > 180)
			dif = 360 - dif;
		// if difference is bigger than 180 degrees,
		// it's faster to rotate in opposite direction
		if (Math.abs(dif) < 1)
			return mLastBearing;
		// if difference is less than 1 degree, leave things as is
		if (Math.abs(dif) >= 90)
			return mLastBearing = newBearing;
		// if difference is bigger than 90 degress, just update it
		mLastBearing += 90 * Math.signum(dif) * Math.pow(Math.abs(dif) / 90, 2);
		// bearing is updated proportionally to the square of the difference
		// value
		// sign of difference is paid into account
		// if difference is 90(max. possible) it is updated exactly by 90
		while (mLastBearing > 360)
			mLastBearing -= 360;
		while (mLastBearing < 0)
			mLastBearing += 360;
		// prevent bearing overrun/underrun
		return mLastBearing;
	}

	// ===========================================================
	// Constructors
	// ===========================================================

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if(OpenStreetMapViewConstants.DEBUGMODE)
    		android.os.Debug.startMethodTracing("lsd");
    	
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity onCreate Method is Called");
        super.onCreate(savedInstanceState, false); // Pass true here to actually contribute to OSM!

        if(!OpenStreetMapViewConstants.DEBUGMODE)
        	CrashReportHandler.attach(this);

        CheckNeedDataUpdate();

        mPoiManager = new PoiManager(this);
        
        
        
//		String defaultKMLPath =  Ut.getRMapsImportDir(this).getAbsolutePath() + "/default.kml";
//		final File defaultKML = new File(defaultKMLPath);
//		
//		if(OpenStreetMapViewConstants.DEBUGMODE) {
//			Log.d("RMAPS-Me-LoadPoi", "defaultFilePath: " + defaultKMLPath);
//			//Log.d("RMAPS-Me-LoadPoi", "defaultFile2: " + defaultKML);
//		}
//		        
        
        mOrientationSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
       	final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

 		final RelativeLayout rl = new RelativeLayout(this);
        OpenStreetMapRendererInfo RendererInfo = new OpenStreetMapRendererInfo(getResources(), "");
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity newRendererInfo is created");
		
        this.mOsmv = new OpenStreetMapView(this, RendererInfo);
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity OpenStreetMapView is Created with RendererInfo as parameter");
        this.mOsmv.setMainActivityCallbackHandler(mCallbackHandler);
//        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity calls OpenStreetMapView.SetMainActivityCallBackHandler");
        
        rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        registerForContextMenu(mOsmv);
        


        /* SingleLocation-Overlay */
        {
	        /* Create a static Overlay showing a single location. (Gets updated in onLocationChanged(Location loc)! */
        	if(RendererInfo.YANDEX_TRAFFIC_ON == 1)
        		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));

	        this.mMyLocationOverlay = new MyLocationOverlay(this);
	        this.mOsmv.getOverlays().add(mMyLocationOverlay);

	        this.mSearchResultOverlay = new SearchResultOverlay(this);
	        this.mOsmv.getOverlays().add(mSearchResultOverlay);
        }

        /* Itemized Overlay */
		{
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity onCreate TrackOverlay about to be added ");
        	
			this.mTrackOverlay = new TrackOverlay(this, mPoiManager);
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity Osmv getoverlays(trackoverlay) about to be added ");
        	
			this.mOsmv.getOverlays().add(this.mTrackOverlay);

			this.mCurrentTrackOverlay = new CurrentTrackOverlay(this, mPoiManager, mOsmv);
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity Osmv getoverlays(currenttrackoverlay) about to be added ");
        	
			this.mOsmv.getOverlays().add(this.mCurrentTrackOverlay);

//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity Osmv getoverlays(poioverlay) about to be added ");
        	
			this.mPoiOverlay = new PoiOverlay(this, mPoiManager, null, pref.getBoolean("pref_hidepoi", false));
			this.mOsmv.getOverlays().add(this.mPoiOverlay);
		}

        {
        	final int sideBottom = Integer.parseInt(pref.getString("pref_zoomctrl", "1"));

            /* Compass */
        	/*louis.fazen@gmail.com louisDEBUG got rid of compass by commenting out the following two blocks of code:
        	 * 		if(DEBUGMODE) Log.i(DEBUGTAG, "Compass Mode is activated");
				
        	mCompassView = new CompassView(this, sideBottom == 2 ? false : true);
	        mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);
	        */
	        
        	/* Create RelativeLayoutParams, that position in in the top right corner. */
	        /*
        	final RelativeLayout.LayoutParams compassParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        compassParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        compassParams.addRule(!(sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
	        rl.addView(mCompassView, compassParams);
	        */
	        
	        // 	louis.fazen@gmail.com got rid of this button by commenting it out
	        /* AutoFollow */

        	
//	        ivAutoFollow = new ImageView(this);
//	        ivAutoFollow.setImageResource(R.drawable.autofollow);
//	        ivAutoFollow.setVisibility(ImageView.INVISIBLE);
	 
	        
	        /* Create RelativeLayoutParams, that position in in the top right corner. */
	        
	     
//	        final RelativeLayout.LayoutParams followParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//	        followParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//	        followParams.addRule(!(sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
//	        rl.addView(ivAutoFollow, followParams);

	        
//	        ivAutoFollow.setOnClickListener(new OnClickListener(){
//				// @Override
//				public void onClick(View v) {
//					setAutoFollow(true);
//					mSearchResultOverlay.Clear();
//					setLastKnownLocation();
//				}
//	        });
	         
	      
	        
	        /* ZoomControls */
	        if(sideBottom > 0){
		        /* Create a ImageView with a zoomIn-Icon. */
		        final ImageView ivZoomIn = new ImageView(this);
		        ivZoomIn.setImageResource(R.drawable.zoom_in);
		        /* Create RelativeLayoutParams, that position in in the top right corner. */
		        final RelativeLayout.LayoutParams zoominParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		        zoominParams.addRule((sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
		        rl.addView(ivZoomIn, zoominParams);

		        ivZoomIn.setOnClickListener(new OnClickListener(){
					// @Override
					public void onClick(View v) {
						MainMapActivity.this.mOsmv.zoomIn();
						setTitle();

						if(MainMapActivity.this.mOsmv.getZoomLevel() > 16 && MainMapActivity.this.mOsmv.getRenderer().YANDEX_TRAFFIC_ON == 1)
							Toast.makeText(MainMapActivity.this, R.string.no_traffic, Toast.LENGTH_SHORT).show();
					}
		        });
		        ivZoomIn.setOnLongClickListener(new OnLongClickListener(){
					// @Override
					public boolean onLongClick(View v) {
						
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainMapActivity.this);
						//louis.fazen@gmail.com wants to change boolean prefs for zoommaxlevel to 0
						final int zoom = Integer.parseInt(pref.getString("pref_zoommaxlevel", "17"));
						if (zoom > 0) {
							MainMapActivity.this.mOsmv.setZoomLevel(zoom - 1);
							setTitle();
							
						}
						return true;
					}
		        });

		        

		        /* Create a ImageView with a zoomOut-Icon. */
		        final ImageView ivZoomOut = new ImageView(this);
		        ivZoomOut.setImageResource(R.drawable.zoom_out);
		        ivZoomOut.setId(R.id.whatsnew);

		        /* Create RelativeLayoutParams, that position in in the top left corner. */
		        final RelativeLayout.LayoutParams zoomoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		        zoomoutParams.addRule((sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
		        rl.addView(ivZoomOut, zoomoutParams);

		        ivZoomOut.setOnClickListener(new OnClickListener(){
					// @Override
					public void onClick(View v) {
				       //louis.fazen mLoadPoiActivity = new LoadPoiActivity();
						MainMapActivity.this.mOsmv.zoomOut();
						setTitle();
					}
		        });
		        ivZoomOut.setOnLongClickListener(new OnLongClickListener(){
					// @Override
					public boolean onLongClick(View v) {
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainMapActivity.this);
						//louis.fazen@gmail.com wants to change this prefs to not set which may be 0
						final int zoom = Integer.parseInt(pref.getString("pref_zoomminlevel", "10"));
						if (zoom > 0) {
							MainMapActivity.this.mOsmv.setZoomLevel(zoom - 1);
							setTitle();
						}
						return true;
					}
		        });

	        };

	        /*ScaleBarView*/
	        if(pref.getBoolean("pref_showscalebar", true)){
		        final ImageView ivZoomOut2 = new ImageView(this);
		        final ScaleBarDrawable dr = new ScaleBarDrawable(this, mOsmv, Integer.parseInt(pref.getString("pref_units", "0")));
		        ivZoomOut2.setImageDrawable(dr);
		        final RelativeLayout.LayoutParams scaleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		        scaleParams.addRule(RelativeLayout.RIGHT_OF, R.id.whatsnew);
		        scaleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		        rl.addView(ivZoomOut2, scaleParams);
	        };
        }
        
        //louis.fazen@gmail.com changed boolean prefs for driving direction
		mDrivingDirectionUp = pref.getBoolean("pref_drivingdirectionup", false);
		
		mNorthDirectionUp = pref.getBoolean("pref_northdirectionup", true);

		mScreenOrientation = Integer.parseInt(pref.getString("pref_screen_orientation", "-1"));
		this.setRequestedOrientation(mScreenOrientation);
		
		//louis.fazen@gmail.com changed boolean prefs for statusbar
     	mFullScreen = pref.getBoolean("pref_showstatusbar", false);
		if (mFullScreen)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		else
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//louis.fazen@gmail.com changed boolean prefs for showTitlebar
		mShowTitle = pref.getBoolean("pref_showtitle", false);
		if (mShowTitle)
	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		else
			requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(rl);

        if(mShowTitle)
	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_title);

		restoreUIState();

        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();

        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent);
        }
        else if(ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction))
        	ActionShowPoints(queryIntent);

        if(OpenStreetMapViewConstants.DEBUGMODE)
        	android.os.Debug.stopMethodTracing();
//        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity end of onCreate Method except for loadPOI");
//      ImportPoiActivity mImportPoiActivity = new ImportPoiActivity();
//      mImportPoiActivity.loadPOI();
      
        if (this.CheckNeedPoiUpdate()){
        mLoadDefaultPoiCategories = new LoadDefaultPoiCategories(this);
        mLoadDefaultPoiCategories.start();
        }
//   louis.fazenDeleted the following line even though it did in fact work!   
//        mLoadDefaultPoi = new LoadDefaultPoi(this);
        
//      louis.fazen@gmail.com.... the following worked!  x1
//		startActivity((new Intent(this, ImportPoiActivity2.class)));
		
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity end of onCreate Method");
    }

	@Override
	protected void onDestroy() {
		try {
			mOsmv.freeDatabases();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			mPoiManager.FreeDatabases();
//			 if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "mPoiManager successfully FreeDatabases on Destroy");
		} catch (Exception e) {
			// TODO! Auto-generated catch block
//			 if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "mPoiManager could not FreeDatabases on Destroy");
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private void ActionShowPoints(Intent queryIntent) {
		final ArrayList<String> locations = queryIntent.getStringArrayListExtra("locations");
		if(!locations.isEmpty()){
			Ut.dd("Intent: "+ACTION_SHOW_POINTS+" locations: "+locations.toString());
			String [] fields = locations.get(0).split(";");
			String locns = "", title = "", descr = "";
			if(fields.length>0) locns = fields[0];
			if(fields.length>1) title = fields[1];
			if(fields.length>2) descr = fields[2];

			GeoPoint point = GeoPoint.fromDoubleString(locns);
			mPoiOverlay.setGpsStatusGeoPoint(point, title, descr);
			// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//			setAutoFollow(false);
			mOsmv.setMapCenter(point);
		}
	}

	private void CheckNeedDataUpdate() {
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity CheckNeedDataUpdate is Called");
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		final int versionDataUpdate = settings.getInt("versionDataUpdate", 0);
		Ut.dd("versionDataUpdate="+versionDataUpdate);

		if(versionDataUpdate < 3){
			Ut.dd("Upgrade app data to v.3");
		}

		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putInt("versionDataUpdate", 3);
		editor.commit();
	}

		private boolean CheckNeedPoiUpdate() {
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainMapActivity", "MainMapActivity CheckNeedPoiUpdate is Called");
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		final float needPoiUpdate = settings.getFloat("needPoiUpdate", 0);

		String importDir = Ut.getRMapsImportDir(this).getAbsolutePath();
		File currentDirectory = new File(importDir);	
		boolean toUpdate = false;		
		long updatedDate = currentDirectory.lastModified();
		
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainMapActivity", "CheckNeedPoiUpdate last update is: " + needPoiUpdate + "and current is:" + updatedDate);
		
		if (updatedDate > needPoiUpdate) {
			toUpdate = true;
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainMapActivity", "CheckNeedPoiUpdate Need to update is: " + toUpdate);
			SharedPreferences uiState = getPreferences(0);
			SharedPreferences.Editor editor = uiState.edit();
			editor.putFloat("needPoiUpdate", updatedDate);
			editor.commit();
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainMapActivity", "CheckNeedPoiUpdate has now updated the SettingsValue");
			
		}
		
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainMapActivity", "CheckNeedPoiUpdate SettingsValue was: " + needPoiUpdate + "and is now set to" + settings.getFloat("needPoiUpdate", 0));

		return toUpdate;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_addpoi:
			GeoPoint point = this.mOsmv.getTouchDownPoint();
			startActivityForResult((new Intent(this, PoiActivity.class))
					.putExtra("lat", point.getLatitude()).putExtra("lon", point.getLongitude()).putExtra("title", "POI"), R.id.menu_addpoi);
			break;
		case R.id.menu_editpoi:
			startActivityForResult((new Intent(this, PoiActivity.class)).putExtra("pointid", mPoiOverlay.getPoiPoint(mMarkerIndex).getId()), R.id.menu_editpoi);
			mOsmv.invalidate();
			break;
		case R.id.menu_deletepoi:
			mPoiManager.deletePoi(mPoiOverlay.getPoiPoint(mMarkerIndex).getId());
			mPoiOverlay.UpdateList();
			mOsmv.invalidate();
			break;
		case R.id.menu_hide:
			final PoiPoint poi = mPoiOverlay.getPoiPoint(mMarkerIndex);
			poi.Hidden = true;
			mPoiManager.updatePoi(poi);
			mPoiOverlay.UpdateList();
			mOsmv.invalidate();
			break;
		case R.id.menu_toradar:
			final PoiPoint poi1 = mPoiOverlay.getPoiPoint(mMarkerIndex);
			try {
					Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra("name", poi1.Title);
					i.putExtra("latitude",  (float)(poi1.GeoPoint.getLatitudeE6() / 1000000f));
					i.putExtra("longitude", (float)(poi1.GeoPoint.getLongitudeE6() / 1000000f));
					startActivity(i);
				} catch (Exception e) {
					Toast.makeText(this, R.string.message_noradar, Toast.LENGTH_LONG).show();
				}
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(mOsmv.canCreateContextMenu()){
			mMarkerIndex = mPoiOverlay.getMarkerAtPoint(mOsmv.mTouchDownX, mOsmv.mTouchDownY, mOsmv);
			if(mMarkerIndex >= 0){
				menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
				menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
				menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
				menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));
			} else {
				menu.add(0, R.id.menu_addpoi, 0, getText(R.string.menu_addpoi));
			}
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_option_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		
		//louis.fazen@gmail.com commented out the gpsstatus option
		/*
		case (R.id.gpsstatus):
			try {
				startActivity(new Intent("com.eclipsim.gpsstatus.VIEW"));
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this,
						R.string.message_nogpsstatus,
						Toast.LENGTH_LONG).show();
				try {
					startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
							.parse("market://search?q=pname:com.eclipsim.gpsstatus2")));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			return true;
		*/
		case (R.id.poilist):
			final GeoPoint point = mOsmv.getMapCenter();
			startActivityForResult((new Intent(this, PoiListActivity.class)).putExtra("lat", point.getLatitude()).putExtra("lon", point.getLongitude()).putExtra("title", "POI"), R.id.poilist);
			return true;
		//louis.fazen@gmail.com commented out the tracks options
		/*
		case (R.id.tracks):
			startActivityForResult(new Intent(this, TrackListActivity.class), R.id.tracks);
			return true;
		*/
		case (R.id.search):
			onSearchRequested();
			return true;
		
		// 	 louis.fazen@gmail.com commented out the settings menu 5-2-2012 17:43
//		case (R.id.settings):
//			startActivityForResult(new Intent(this, MainPreferences.class), R.id.settings_activity_closed);
//			return true;
			//louis.fazen@gmail.com commented out the about options
			/*
		case (R.id.about):
			showDialog(R.id.about);
			return true;
			*/
//		 	 louis.fazen@gmail.com commented out the mapselector menu 5-2-2012 17:43
//		case (R.id.mapselector):
//			return true;

		//louis.fazen@gmail.com commented out the compass options
		/*
		case (R.id.compass):
			mCompassEnabled = !mCompassEnabled;
			mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);
			if(mCompassEnabled)
				mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
			else
				mOrientationSensorManager.unregisterListener(mListener);
			return true;
		*/
			
		case (R.id.mylocation):
			// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//			setAutoFollow(true);
			setLastKnownLocation();
			return true;
		default:
 			OpenStreetMapRendererInfo RendererInfo = getRendererInfo(getResources(), getPreferences(Activity.MODE_PRIVATE), (String)item.getTitleCondensed());
			mOsmv.setRenderer(RendererInfo);

			this.mOsmv.getOverlays().clear();
			if(RendererInfo.YANDEX_TRAFFIC_ON == 1){
	       		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));
			}
	        if(mTrackOverlay != null)
	        	this.mOsmv.getOverlays().add(mTrackOverlay);
	        if(mCurrentTrackOverlay != null)
	        	this.mOsmv.getOverlays().add(mCurrentTrackOverlay);
	        if(mPoiOverlay != null)
	        	this.mOsmv.getOverlays().add(mPoiOverlay);
	        this.mOsmv.getOverlays().add(mMyLocationOverlay);
	        this.mOsmv.getOverlays().add(mSearchResultOverlay);

	        setTitle();

			return true;
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity onCreateDialog Method is Called");
		switch (id) {
		
		// louis.fazen@gmail.com commented out many of the sections that seemed irrelevant
		case R.id.add_yandex_bookmark:
			return new AlertDialog.Builder(this)
				.setTitle(R.string.ya_dialog_title)
				.setMessage(R.string.ya_dialog_message)
				.setPositiveButton(R.string.ya_dialog_button_caption, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Browser.saveBookmark(MainMapActivity.this, "��������� ������", "m.yandex.ru");
						}
				}).create();
				
			
		case R.id.whatsnew:
			return new AlertDialog.Builder(this) //.setIcon( R.drawable.alert_dialog_icon)
					.setTitle(R.string.about_dialog_whats_new)
					.setMessage(R.string.whats_new_dialog_text)
					.setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							/* User clicked Cancel so do some stuff */
						}
					}).create();
		//louis.fazen@gmail.com also commented this section out
		/*
		case R.id.about:
			return new AlertDialog.Builder(this) //.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.menu_about)
					.setMessage(getText(R.string.app_name) + " v." + Ut.getAppVersion(this) + "\n\n"
							+ getText(R.string.about_dialog_text))
					.setPositiveButton(R.string.about_dialog_whats_new, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							showDialog(R.id.whatsnew);
						}
					}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						
			*/				/* User clicked Cancel so do some stuff */ /*
						}
					}).create();
			*/
		case R.id.error:
			return new AlertDialog.Builder(this) //.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.error_title)
			.setMessage(getText(R.string.error_text))
			.setPositiveButton(R.string.error_send, new DialogInterface.OnClickListener() {
				@SuppressWarnings("static-access")
				public void onClick(DialogInterface dialog, int whichButton) {

					SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
					String text =  settings.getString("error", "");
					String subj = "RMaps error: ";
					try {
						final String[] lines = text.split("\n", 2);
						final Pattern p = Pattern.compile("[.][\\w]+[:| |\\t|\\n]");
						final Matcher m = p.matcher(lines[0]+"\n");
						if (m.find())
							subj += m.group().replace(".", "").replace(":", "").replace("\n", "")+" at ";
						final Pattern p2 = Pattern.compile("[.][\\w]+[(][\\w| |\\t]*[)]");
						final Matcher m2 = p2.matcher(lines[1]);
						if (m2.find())
							subj += m2.group().substring(2);
					} catch (Exception e) {
					}

					final Build b = new Build();
					final Build.VERSION v = new Build.VERSION();
					text = "Your message:"
						+"\n\nRMaps: "+Ut.getAppVersion(MainMapActivity.this)
						+"\nAndroid: "+v.RELEASE
						+"\nDevice: "+b.BOARD+" "+b.BRAND+" "+b.DEVICE+" "+b.MANUFACTURER+" "+b.MODEL+" "+b.PRODUCT
						+"\n\n"+text;

					startActivity(Ut.SendMail(subj, text));

					SharedPreferences uiState = getPreferences(0);
					SharedPreferences.Editor editor = uiState.edit();
					editor.putString("error", "");
					editor.commit();

				}
			}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					SharedPreferences uiState = getPreferences(0);
					SharedPreferences.Editor editor = uiState.edit();
					editor.putString("error", "");
					editor.commit();
				}
			}).create();

		}
		return null;
	}

	private void setLastKnownLocation() {
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "MainMapActivity setLastKnownLocation Method is Called");
		final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final Location loc1 = lm.getLastKnownLocation("gps");
		final Location loc2 = lm.getLastKnownLocation("network");

		boolean boolGpsEnabled = lm.isProviderEnabled(GPS);
		boolean boolNetworkEnabled = lm.isProviderEnabled(NETWORK);
		String str = "";
		Location loc = null;

		if(loc1 == null && loc2 != null)
			loc = loc2;
		else if (loc1 != null && loc2 == null)
			loc = loc1;
		else if (loc1 == null && loc2 == null)
			loc = null;
		else
			loc = loc1.getTime() > loc2.getTime() ? loc1 : loc2;

		if(boolGpsEnabled){}
		else if(boolNetworkEnabled)
			str = getString(R.string.message_gpsdisabled);
		else if(loc == null)
			str = getString(R.string.message_locationunavailable);
		else
			str = getString(R.string.message_lastknownlocation);

		if(str.length() > 0)
			Toast.makeText(this, str, Toast.LENGTH_LONG).show();

		if (loc != null)
			this.mOsmv
					.getController()
					.animateTo(
							TypeConverter.locationToGeoPoint(loc),
							OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
							OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
							OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
	}

	// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//	private void setAutoFollow(boolean autoFollow) {
//		setAutoFollow(autoFollow, false);
//	}
//
//	private void setAutoFollow(boolean autoFollow, final boolean supressToast) {
//		if (mAutoFollow != autoFollow) {
//			mAutoFollow = autoFollow;
//
//			if (autoFollow) {
//				ivAutoFollow.setVisibility(ImageView.INVISIBLE);
//				if(!supressToast)
//					Toast.makeText(this, R.string.auto_follow_enabled, Toast.LENGTH_SHORT).show();
//			} else {
//				ivAutoFollow.setVisibility(ImageView.VISIBLE);
//				if(!supressToast)
//					Toast.makeText(this, R.string.auto_follow_disabled, Toast.LENGTH_SHORT).show();
//			}
//		}
//	}

	@Override
	public void onLocationChanged(Location loc) {
		this.mMyLocationOverlay.setLocation(loc);

		mLastSpeed = loc.getSpeed();
		
		// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//		if (mAutoFollow) {
//			if (mDrivingDirectionUp)
//				if (loc.getSpeed() > 0.5)
//					this.mOsmv.setBearing(loc.getBearing());
//
//			this.mOsmv.getController().animateTo(
//					TypeConverter.locationToGeoPoint(loc),
//					OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
//					OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
//					OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
//		} else
//			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainActivity onLocationChanged calls to invalidate OSMView");
		
			this.mOsmv.invalidate();
	}

	@Override
	public void onLocationLost() {
		// Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		int cnt = extras.getInt("satellites", Integer.MIN_VALUE);
		mStatusListener = provider+ " " + status + " " + (cnt >= 0 ? cnt : 0);
		setTitle();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity onPrepareOptionsMenu is Called");
// 	 louis.fazen@gmail.com commented out the settings menu 5-2-2012 17:43
//		Menu submenu = menu.findItem(R.id.mapselector).getSubMenu();
//		submenu.clear();
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//
//		File folder = Ut.getRMapsMapsDir(this);
//		if (folder.exists()) {
//			File[] files = folder.listFiles();
//			if (files != null)
//				for (int i = 0; i < files.length; i++) {
//					if (files[i].getName().toLowerCase().endsWith(".mnm")
//							|| files[i].getName().toLowerCase()
//									.endsWith(".tar")
//							|| files[i].getName().toLowerCase().endsWith(
//									".sqlitedb")) {
//						String name = Ut.FileName2ID(files[i].getName());
//						if (pref.getBoolean("pref_usermaps_" + name + "_enabled", false)) {
//							MenuItem item = submenu.add(pref.getString("pref_usermaps_" + name + "_name", files[i]
//									.getName()));
//							item.setTitleCondensed("usermap_" + name);
//						}
//					}
//				}
//		}
//
//		final SAXParserFactory fac = SAXParserFactory.newInstance();
//		SAXParser parser = null;
//		try {
//			parser = fac.newSAXParser();
//			if(parser != null){
//				final InputStream in = getResources().openRawResource(R.raw.predefmaps);
//				parser.parse(in, new PredefMapsParser(submenu, pref));
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putString("MapName", mOsmv.getRenderer().ID);
		editor.putInt("Latitude", mOsmv.getMapCenterLatitudeE6());
		editor.putInt("Longitude", mOsmv.getMapCenterLongitudeE6());
		editor.putInt("ZoomLevel", mOsmv.getZoomLevel());

		
		//louis.fazen@gmail.com commented out the tracks and search options
		//editor.putBoolean("CompassEnabled", mCompassEnabled);
		// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//		editor.putBoolean("AutoFollow", mAutoFollow);
		editor.putString("app_version", Ut.getAppVersion(this));
		if(mPoiOverlay != null)
			editor.putInt("curShowPoiId", mPoiOverlay.getTapIndex());
		mSearchResultOverlay.toPref(editor);
		editor.commit();

		if (myWakeLock != null) {
			myWakeLock.release();
		}

		mOrientationSensorManager.unregisterListener(mListener);
		
//		louis.fazen is adding this one to see fi it works at all 8/5/12
		mPoiManager.FreeDatabases();
		
		super.onPause();
	}


	
	@Override
	protected void onResume() {
		super.onResume();

    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (pref.getBoolean("pref_keepscreenon", true)) {
			myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "RMaps");
			myWakeLock.acquire();
		} else {
			myWakeLock = null;
		}

	    if (this.CheckNeedPoiUpdate()){
            mLoadDefaultPoiCategories = new LoadDefaultPoiCategories(this);
            mLoadDefaultPoiCategories.start();
            }
		//louis.fazen@gmail.com commented out the tracks and search options
		/*
		if(mCompassEnabled)
			mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		 */
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "mTrackOverlayabout to check on setStopDraw(false)");
		
		if(mTrackOverlay != null){
			mTrackOverlay.setStopDraw(false);
		}

	}

	@Override
	protected void onRestart() {
		if(mTrackOverlay != null)
			mTrackOverlay.clearTrack();
//		if(mCurrentTrackOverlay != null)
//			mCurrentTrackOverlay.clearTrack();
	    if (this.CheckNeedPoiUpdate()){
            mLoadDefaultPoiCategories = new LoadDefaultPoiCategories(this);
            mLoadDefaultPoiCategories.start();
            }
		super.onRestart();
	}

	private OpenStreetMapRendererInfo getRendererInfo(final Resources aRes, final SharedPreferences aPref, final String aName){
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity getRendererInfo Method is Called and aName is: " + aName);
		OpenStreetMapRendererInfo RendererInfo = new OpenStreetMapRendererInfo(aRes, aName);
		RendererInfo.LoadFromResources(aName, PreferenceManager.getDefaultSharedPreferences(this));
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity getRendererInfo Method Calls LoadFromRecourses and aName is: " + aName);


		return RendererInfo;
	}

	private void restoreUIState() {
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity Restore UI State Method is Called");
		//louis.fazen@gmail.com reset these from mapnik to Region_Mapnik_sqlitedb
		OpenStreetMapRendererInfo RendererInfo = getRendererInfo(getResources(), settings, settings.getString("MapName", "Region_Mapnik_sqlitedb"));
		if(!mOsmv.setRenderer(RendererInfo))
			mOsmv.setRenderer(getRendererInfo(getResources(), settings, "Region_Mapnik_sqlitedb"));

		this.mOsmv.getOverlays().clear();
		if(RendererInfo.YANDEX_TRAFFIC_ON == 1){
       		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));
		}
        if(mTrackOverlay != null)
//        	if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity Osmv get overlays (trackoverlay) about to be called ");
		
        	this.mOsmv.getOverlays().add(mTrackOverlay);
        if(mCurrentTrackOverlay != null)
//        	if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity Osmv get overlays(currenttrackoverlay) about to be called ");
        	this.mOsmv.getOverlays().add(mCurrentTrackOverlay);
        if(mPoiOverlay != null)
//        	if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity Osmv get overlays(poioverlay) about to be called ");
    	
        	this.mOsmv.getOverlays().add(mPoiOverlay);
        this.mOsmv.getOverlays().add(mMyLocationOverlay);
        this.mOsmv.getOverlays().add(mSearchResultOverlay);

		mOsmv.setZoomLevel(settings.getInt("ZoomLevel", 0));
		mOsmv.setMapCenter(settings.getInt("Latitude", 0), settings.getInt("Longitude", 0));

		//louis.fazen@gmail.com commented out the tracks and search options
		//mCompassEnabled = settings.getBoolean("CompassEnabled", false);
		//mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);

		// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//		mAutoFollow = settings.getBoolean("AutoFollow", true);
//		ivAutoFollow.setVisibility(mAutoFollow ? ImageView.INVISIBLE : View.VISIBLE);

		setTitle();

		if(mPoiOverlay != null)
			mPoiOverlay.setTapIndex(settings.getInt("curShowPoiId", -1));

		mSearchResultOverlay.fromPref(settings);

		if(settings.getString("error", "").length() > 0){
			showDialog(R.id.error);
		}

		if(!settings.getString("app_version", "").equalsIgnoreCase(Ut.getAppVersion(this)))
			showDialog(R.id.whatsnew);

		if (settings.getBoolean("add_yandex_bookmark", true))
			if (getResources().getConfiguration().locale.toString()
					.equalsIgnoreCase("ru_RU")) {
				SharedPreferences uiState = getPreferences(0);
				SharedPreferences.Editor editor = uiState.edit();
				editor.putBoolean("add_yandex_bookmark", false);
				editor.commit();

				Message msg = Message.obtain(mCallbackHandler,
						R.id.add_yandex_bookmark);
				mCallbackHandler.sendMessageDelayed(msg, 2000);
			}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity onActivityResult Method is Called... Will Invalidate the view");
		switch(requestCode){
		case R.id.menu_addpoi:
		case R.id.menu_editpoi:
			mPoiOverlay.UpdateList();
			mOsmv.invalidate();
			break;
		case R.id.poilist:
			if(resultCode == RESULT_OK){
				PoiPoint point = mPoiManager.getPoiPoint(data.getIntExtra("pointid", PoiPoint.EMPTY_ID()));
				if(point != null){
					// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//					setAutoFollow(false);
					mPoiOverlay.UpdateList();
					mOsmv.setMapCenter(point.GeoPoint);
				}
			} else {
				mPoiOverlay.UpdateList();
				mOsmv.invalidate();
			}
			break;
			
			//louis.fazen@gmail.com commented out the tracks button
			/*
		case R.id.tracks:
			if(resultCode == RESULT_OK){
				Track track = mPoiManager.getTrack(data.getIntExtra("trackid", PoiPoint.EMPTY_ID()));
				if(track != null){
					setAutoFollow(false);
					mOsmv.setMapCenter(track.getBeginGeoPoint());
				}
			}
			break;
			*/
		case R.id.settings_activity_closed:
			finish();
			startActivity(new Intent(this, this.getClass()));
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setTitle(){
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainMapActivity setTitle called and mOSMv.getRenderer().NAME: " +  mOsmv.getRenderer().NAME);
		
		final TextView leftText = (TextView) findViewById(R.id.left_text);
		if(leftText != null)
			leftText.setText(mOsmv.getRenderer().NAME);

		final TextView rightText = (TextView) findViewById(R.id.right_text);
		if(rightText != null){
			rightText.setText(mStatusListener + " " + (1+mOsmv.getZoomLevel()));
		}
	}

	private class MainActivityCallbackHandler extends Handler{
		@Override
		public void handleMessage(final Message msg) {
			final int what = msg.what;
			switch(what){
				case R.id.user_moved_map:
//					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainActivityCallBackHandler: R.id.user_moved_map: " +  R.id.user_moved_map);
					// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//					setAutoFollow(false);
					break;
				case R.id.set_title:
//					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainActivityCallBackHandler: R.id.set_title: " +  R.id.set_title);
					setTitle();
					break;
				case R.id.add_yandex_bookmark:
//					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainActivityCallBackHandler: R.id.add_yandex_bookmark: " +  R.id.add_yandex_bookmark);
					showDialog(R.id.add_yandex_bookmark);
					break;
				case OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE:
//					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-MainActivity", "MainActivityCallBackHandler: OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE: " +  OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE);
					if(msg.obj != null)
						Toast.makeText(MainMapActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
			}
		}
	}

	@Override
	public boolean onSearchRequested() {
        startSearch("", false, null, false);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(intent);
        }
	}

	private void doSearchQuery(Intent queryIntent) {
		mSearchResultOverlay.Clear();
		
		this.mOsmv.invalidate();

		final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);

        // Record the query string in the recent queries suggestions provider.
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(queryString, null);

		InputStream in = null;
		OutputStream out = null;

		try {
			URL url = new URL(
					"http://ajax.googleapis.com/ajax/services/search/local?v=1.0&sll="
							+ this.mOsmv.getMapCenter().toDoubleString()
							+ "&q=" + URLEncoder.encode(queryString, "UTF-8")
							+ "");
			Ut.dd(url.toString());
			in = new BufferedInputStream(url.openStream(), StreamUtils.IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();

			String str = dataStream.toString();
			JSONObject json = new JSONObject(str);
			//Ut.dd(json.toString(4)); //
			JSONArray results = (JSONArray) ((JSONObject) json.get("responseData")).get("results");
			//Ut.dd("results.length="+results.length());
			if(results.length() == 0){
				Toast.makeText(this, R.string.no_items, Toast.LENGTH_SHORT).show();
				return;
			}
			JSONObject res = results.getJSONObject(0);
			//Ut.dd(res.toString(4));
			//Toast.makeText(this, res.getString("titleNoFormatting"), Toast.LENGTH_LONG).show();
			final String address = res.getString("addressLines").replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ").replace("  ", " ");
			//Toast.makeText(this, address, Toast.LENGTH_LONG).show();
			//Toast.makeText(this, ((JSONObject) json.get("addressLines")).toString(), Toast.LENGTH_LONG).show();

			// 	 louis.fazen@gmail.com commented out all the autofollow stuff
//			setAutoFollow(false, true);
			this.mSearchResultOverlay.setLocation(new GeoPoint((int)(res.getDouble("lat")* 1E6), (int)(res.getDouble("lng")* 1E6)), address);
			this.mOsmv.setZoomLevel((int) (2 * res.getInt("accuracy")));
			this.mOsmv.getController().animateTo(new GeoPoint((int)(res.getDouble("lat")* 1E6), (int)(res.getDouble("lng")* 1E6)), OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);

			setTitle();

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.no_inet_conn, Toast.LENGTH_LONG).show();
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
		}
	}



}
