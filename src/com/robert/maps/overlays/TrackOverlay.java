package com.robert.maps.overlays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robert.maps.MainMapActivity;
import com.robert.maps.R;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.Track;
import com.robert.maps.utils.Ut;

public class TrackOverlay extends OpenStreetMapViewOverlay {
	private Paint mPaint;
	private int mLastZoom;
	private Path mPath;
	private Track mTrack;
	private Point mBaseCoords;
	private GeoPoint mBaseLocation;
	private PoiManager mPoiManager;
	private TrackThread mThread;
	private boolean mThreadRunned = false;
	private OpenStreetMapView mOsmv;
	private Handler mMainMapActivityCallbackHandler;
	private boolean mStopDraw = false;

	protected ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor();

	private class TrackThread extends Thread {

		@Override
		public void run() {
			Ut.d("run TrackThread");

			mPath = null;

			if(mTrack == null){
				mTrack = mPoiManager.getTrackChecked();
				if(mTrack == null){
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay Constructor mPoiManager.getTrackChecked is NULL ");
					mThreadRunned = false;
					mStopDraw = true;
					return;
				}
				Ut.d("Track loaded");
			}

			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay Constructor is about to call OSMView to getProjection! ");
			final OpenStreetMapViewProjection pj = mOsmv.getProjection();
			mPath = pj.toPixelsTrackPoints(mTrack.getPoints(), mBaseCoords, mBaseLocation);

			Ut.d("TrackOverlay maped");
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay Constructor says track mapped");
			
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay: OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID: " +  OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID);
			Message.obtain(mMainMapActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();
			
			mThreadRunned = false;
		}
	}

	public TrackOverlay(MainMapActivity mainMapActivity, PoiManager poiManager) {
		mTrack = null;
		mPoiManager = poiManager;
		mBaseCoords = new Point();
		mBaseLocation = new GeoPoint(0, 0);
		mLastZoom = -1;
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay about to create new TrackThread");
		mThread = new TrackThread();
		mThread.setName("Track thread");


		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(4);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(mainMapActivity.getResources().getColor(R.color.track));
	}

	public void setStopDraw(boolean stopdraw){
		mStopDraw = stopdraw;
	}

	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		if(mStopDraw) return;

		if (!mThreadRunned && (mTrack == null || mLastZoom != osmv.getZoomLevel())) {
			mPath = null;
			mLastZoom = osmv.getZoomLevel();
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay onDraw is about to call on OSMView to get its handler value! ");
			mMainMapActivityCallbackHandler = osmv.getHandler();
			mOsmv = osmv;
			//mThread.run();
			Ut.d("mThreadExecutor.execute "+mThread.isAlive());
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay onDraw should execute: "+mThread.isAlive());
			mThreadRunned = true;
			mThreadExecutor.execute(mThread);
			return;
		}

		if(mPath == null)
			return;

		Ut.d("Draw track");
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me", "TrackOverlay onDraw is about to call OSMView to getProjection! ");
		final OpenStreetMapViewProjection pj = osmv.getProjection();
		final Point screenCoords = new Point();

		pj.toPixels(mBaseLocation, screenCoords);

		//final long startMs = System.currentTimeMillis();

		if(screenCoords.x != mBaseCoords.x && screenCoords.y != mBaseCoords.y){
			c.save();
			c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
			c.drawPath(mPath, mPaint);
			c.restore();
		} else
			c.drawPath(mPath, mPaint);
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
	}

	public void clearTrack(){
		mTrack = null;
	}

}
