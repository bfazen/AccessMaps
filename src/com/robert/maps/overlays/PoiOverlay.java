package com.robert.maps.overlays;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.robert.maps.R;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.PoiPoint;
import com.robert.maps.utils.Ut;

public class PoiOverlay extends OpenStreetMapViewOverlay {
	private Context mCtx;
	private PoiManager mPoiManager;
	private int mTapIndex;
	private GeoPoint mLastMapCenter;
	private int mLastZoom;
	private PoiListThread mThread;
	private RelativeLayout mT;
	private float mDensity;
	private boolean mNeedUpdateList = false;

	public int getTapIndex() {
		return mTapIndex;
	}

	public void setTapIndex(int mTapIndex) {
		this.mTapIndex = mTapIndex;
	}
	
	public void UpdateList() {
		mNeedUpdateList = true;
	}

	protected OnItemTapListener<PoiPoint> mOnItemTapListener;
	protected OnItemLongPressListener<PoiPoint> mOnItemLongPressListener;
	protected List<PoiPoint> mItemList;
	protected final Point mMarkerHotSpot;
	protected final int mMarkerWidth, mMarkerHeight;
	private boolean mCanUpdateList = true;
	protected HashMap<Integer, Drawable> mBtnMap;

	
	public PoiOverlay(Context ctx, PoiManager poiManager,
			OnItemTapListener<PoiPoint> onItemTapListener, boolean hidepoi)
	{
		mCtx = ctx;
		mPoiManager = poiManager;
		mCanUpdateList = !hidepoi;
		mTapIndex = -1;

//		TODO: need to change this back to the way it was so that the icons look good again...! Is that possible?
//		1. louis.fazen changing the individual lines old and new
//		Drawable marker = ctx.getResources().getDrawable(R.drawable.poi);
		Drawable marker = new BitmapDrawable(mCtx.getResources(), "/sdcard/rmaps/import/1.png");

		
		this.mMarkerWidth = marker.getIntrinsicWidth();
		this.mMarkerHeight = marker.getIntrinsicHeight();
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "marker.getIntrinsicWidth() == this.mMarkerWidth ==: " + this.mMarkerWidth);
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "marker.getIntrinsicHeight() == this.mMarkerHeight ==: " + this.mMarkerHeight);		
		
		mBtnMap = new HashMap<Integer, Drawable>();
		
//		2. louis.fazen changing the individual lines old and new; still need to bring in the categoryId from the name of the image
//		mBtnMap.put(new Integer(R.drawable.poi), marker);
		mBtnMap.put(new Integer(1), marker);
		
		this.mMarkerHotSpot = new Point(0, mMarkerHeight);
        
        
        
        mLastMapCenter = null;
        mLastZoom = -1;
        mThread = new PoiListThread();
        
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay about to create relative layout this.mT");
		this.mT = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.poi_descr, null);
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay about to set relative layout parameters");
		this.mT.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDensity = metrics.density;
        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay new Display Metrics created");
	}

//	louis.fazen 7/5/12 is changing this to an Html type rather than a String type
	public void setGpsStatusGeoPoint(final GeoPoint geopoint, final String title, final String descr) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay calls setGpsStatusGeoPoint");
		PoiPoint poi = new PoiPoint(title, descr, geopoint, R.drawable.poi_satttelite);
		if(mItemList == null){
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "setGpsStatusGeoPoint mItemList == null");	
			mItemList = new ArrayList<PoiPoint>();
		}
		else{
			mItemList.clear();
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "setGpsStatusGeoPoint mItemList != null?");
//			louis.fazen added this, but I am not sure if the else works in this way... may need to change it back to get rid of the {} around the else statement, only nec. for the log.w
		}
		mItemList.add(poi);
		mCanUpdateList = false;
	}

	@Override
	public void onDraw(Canvas c, OpenStreetMapView mapView) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay calls getProjection which calls OSMViewProjection");
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();

		if (mCanUpdateList){
	        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay mCanUpdateList is true");
			boolean looseCenter = false;
			GeoPoint center = mapView.getMapCenter();
			GeoPoint lefttop = pj.fromPixels(0, 0);
			double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());
			double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());

			if (mLastMapCenter == null || mLastZoom != mapView.getZoomLevel())
				looseCenter = true;
			else if(0.7 * deltaX < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude()) || 0.7 * deltaY < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude()))
				looseCenter = true;

			if(looseCenter || mNeedUpdateList){
		        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay mCanUpdateList is true, looseCenter or mNeedUpdateList is true");
				mLastMapCenter = center;
		        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "LooseCenter mLastMapCenter.mLatitudeE6: " + mLastMapCenter.getLatitude() + "Longitude: " + mLastMapCenter.getLongitude());
			
				mLastZoom = mapView.getZoomLevel();
				mNeedUpdateList = false;

				mThread.setParams(1.5*deltaX, 1.5*deltaY);
				mThread.run();
			}
		}

		if (this.mItemList != null) {
	        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "this.Itemlist != null; about to draw all  of the non-selected items");
			/*
			 * Draw in backward cycle, so the items with the least index are on
			 * the front.
			 */
			for (int i = this.mItemList.size() - 1; i >= 0; i--) {
		        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay this.Itemlist != null for i: " + i);
				if (i != mTapIndex) {
			        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay i == mTapIndex ==: " + i);
					PoiPoint item = this.mItemList.get(i);
					pj.toPixels(item.GeoPoint, curScreenCoords);
					

					c.save();
					c.rotate(mapView.getBearing(), curScreenCoords.x,
							curScreenCoords.y);

			        if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay about to call onDrawItem for item i: " + i);
					onDrawItem(c, i, curScreenCoords);

					c.restore();
				}
			}
	        if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-PoiOverlay", "About to call IF statement for the selected item...");
	        
			if (mTapIndex >= 0 && mTapIndex < this.mItemList.size()) {
		        if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-PoiOverlay", "Item SELECTED... About to draw the selected item! item: " + mTapIndex);
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay mTapIndex >= 0 but < this.mItemList.size()");

				
				PoiPoint item = this.mItemList.get(mTapIndex);
				pj.toPixels(item.GeoPoint, curScreenCoords);

				c.save();
				c.rotate(mapView.getBearing(), curScreenCoords.x,
						curScreenCoords.y);
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "PoiOverlay about to call onDrawItem for item mTapIndex: " + mTapIndex);
				onDrawItem(c, mTapIndex, curScreenCoords);

				c.restore();
			}
			else {
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-PoiOverlay", "NoItem was SELECTED?... Index: " + mTapIndex);
			}
		}
	}

//	louis.fazen@gmail.com added this method
	protected Drawable getIconImage(int index){

		final PoiPoint focusedItem = mItemList.get(index);
		final String importDir = Ut.getRMapsImportDir(mCtx).getAbsolutePath();
		final String iconPath = importDir + "/" + focusedItem.IconId +".png";
		final Drawable picIconImage = new BitmapDrawable(mCtx.getResources(), iconPath);
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onDrawItem.getIconImage is called, iconPath: " + iconPath);
		return picIconImage;	
	}
	
	
	protected void onDrawItem(Canvas c, int index, Point screenCoords) {
		final PoiPoint focusedItem = mItemList.get(index);
		final Drawable picIcon = this.getIconImage(index); 
		int iconHeight = picIcon.getIntrinsicHeight();
		int iconWidth = picIcon.getIntrinsicHeight();
//		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onDrawItem is called for item index: " + index);
//		final String importDir = Ut.getRMapsImportDir(mCtx).getAbsolutePath();
//		final String iconPath = importDir + "/" + focusedItem.IconId +".png";
//		final Drawable picIcon = new BitmapDrawable(mCtx.getResources(), iconPath);
		

		if (index == mTapIndex) {
			
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-PoiOverlay", "onDrawItem is drawing the selected item poi_title and description!");
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onDrawItem index == mTapIndex: " + index + " == " + mTapIndex);
			final ImageView pic = (ImageView) mT.findViewById(R.id.pic);
			final TextView title = (TextView) mT.findViewById(R.id.poi_title);
			
//			louis.fazen 7/5/12 is changing this to an WebView type rather than a String? or textview?  type x2
			final TextView descr = (TextView) mT.findViewById(R.id.descr);
			final TextView coord = (TextView) mT.findViewById(R.id.coord);

			
//			3. louis.fazen wants to change this as well... maybe not on second thought... 
//			pic.setImageResource(focusedItem.IconId);	
			pic.setImageDrawable(picIcon);
			title.setText(focusedItem.Title);

//			louis.fazen is changing this to load data
//			descr.setText(focusedItem.Descr);
			descr.setText(Html.fromHtml(focusedItem.Descr));
//			descr.loadData(focusedItem.Descr, "text/html; charset=UTF-8", null);
			
			
			coord.setText(Ut.formatGeoPoint(focusedItem.GeoPoint));


			
			mT.measure(0, 0);
			
//			louis.fazen@gmail.com wants to change the following line to reflect the exact size of the image itself... but that does not work
//			mT.layout(0, 0, picIcon.getIntrinsicWidth(), picIcon.getIntrinsicHeight());
			mT.layout(0, 0, mT.getMeasuredWidth(), mT.getMeasuredHeight());

			c.save();
			
//			louis.fazen: this sets the icon to be at the same x as the poipoint (geopoint) by passing the geopoint and getting it in pixel form... 
			c.translate(screenCoords.x, screenCoords.y - pic.getMeasuredHeight() - pic.getTop());
			mT.draw(c);
			c.restore();
			
		} else {

//			louis.fazen@gmail.com is changing the following lines to reflect the image height and width;
//		final int left = screenCoords.x - this.mMarkerHotSpot.x;
//		final int right = left + this.mMarkerWidth;
//		final int top = screenCoords.y - this.mMarkerHotSpot.y;
//		final int bottom = top + this.mMarkerHeight;

		final int left = screenCoords.x;
		final int right = left + iconWidth;
		final int top = screenCoords.y - iconHeight;
		final int bottom = top + iconHeight;
		
		
		
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onDrawItem index != mTapIndex: " + index + " != " + mTapIndex);
		
		Integer key = new Integer(focusedItem.IconId);
		Drawable marker = null;
		
		if(mBtnMap.containsKey(key)){
			Log.w("RMAPS-Me-PoiOverlay", "onDrawItem (index != mTapIndex) but Hashmap contains key: " + key);
			marker = mBtnMap.get(key);
		}

		else {
			try{
				
//				4. louis.fazen changed the following line...
//				marker = mCtx.getResources().getDrawable(focusedItem.IconId);
//				marker = new BitmapDrawable(mCtx.getResources(), iconPath);
				marker = picIcon;
				Log.w("RMAPS-Me-PoiOverlay", "onDrawItem (index != mTapIndex) Hashmap did not contain key, Setting marker = picIcon");
				
			} catch (Exception e) {
				marker = mCtx.getResources().getDrawable(R.drawable.poi);
				Log.w("RMAPS-Me-PoiOverlay", "onDrawItem (index != mTapIndex) Final Exception, setting icon to R.Drawable.poi");
			}
			mBtnMap.put(key, marker);
			Log.w("RMAPS-Me-PoiOverlay", "onDrawItem (index != mTapIndex) adding the key-drawable pair to the Hasmap");
		}

		marker.setBounds(left, top, right, bottom);

		marker.draw(c);
		

		if(OpenStreetMapViewConstants.DEBUGMODE){
			final int pxUp = 2;
			final int left2 = (int)(screenCoords.x + mDensity*(5 - pxUp));
//			louis.fazen is changing this from 38 to 48 to Width
			final int right2 = (int)(screenCoords.x + mDensity*(iconWidth + pxUp));
			
			final int top2 = (int)(screenCoords.y - iconHeight - mDensity*(pxUp));
//			louis.fazen is changing this from 33 to 61 to Height
			final int bottom2 = (int)(top2 + mDensity*(iconHeight + pxUp));
			Paint p = new Paint();
			c.drawLine(left2, top2, right2, bottom2, p);
			c.drawLine(right2, top2, left2, bottom2, p);
			
			c.drawLine(screenCoords.x - 5, screenCoords.y - 5, screenCoords.x + 5, screenCoords.y + 5, p);
			c.drawLine(screenCoords.x - 5, screenCoords.y + 5, screenCoords.x + 5, screenCoords.y - 5, p);
			}
		}
	}

	public PoiPoint getPoiPoint(final int index){
		return this.mItemList.get(index);
	}

	public int getMarkerAtPoint(final int eventX, final int eventY, OpenStreetMapView mapView){
		if(this.mItemList != null){
			if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "getMarkerAtPoint calls getProjection which calls OSMViewProjection");
			final OpenStreetMapViewProjection pj = mapView.getProjection();

			final Rect curMarkerBounds = new Rect();
			final Point mCurScreenCoords = new Point();
			

			 
			for(int i = 0; i < this.mItemList.size(); i++){
				final PoiPoint mItem = this.mItemList.get(i);
				pj.toPixels(mItem.GeoPoint, mapView.getBearing(), mCurScreenCoords);

//				louis.fazen needs to change this to reflect the size of the image used
				final int pxUp = 2;
				final int left = (int)(mCurScreenCoords.x + mDensity*(5 - pxUp));
				

//				louis.fazen is changing this from 38 to 48 to Width
				final int right = (int)(mCurScreenCoords.x + mDensity*(this.getIconImage(i).getIntrinsicWidth() + pxUp));
				final int top = (int)(mCurScreenCoords.y - this.getIconImage(i).getIntrinsicHeight() - mDensity*(pxUp));
				
//				louis.fazen is changing this from 33 to 61 to Height
				final int bottom = (int)(top + mDensity*(this.getIconImage(i).getIntrinsicHeight() + pxUp));

				curMarkerBounds.set(left, top, right, bottom);
				if(curMarkerBounds.contains(eventX, eventY))
					return i;
			}
		}

		return -1;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, OpenStreetMapView mapView) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onSingleTapUp is called, about to call getMarkerAtPoint");
		
		final int index = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (index >= 0)
			if (onTap(index))
				return true;

		return super.onSingleTapUp(event, mapView);
	}

	@Override
	public boolean onLongPress(MotionEvent event, OpenStreetMapView mapView) {
		final int index = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (index >= 0)
			if (onLongLongPress(index))
				return true;

		return super.onLongPress(event, mapView);
	}

	private boolean onLongLongPress(int index) {
		return false;
//		if(this.mOnItemLongPressListener != null)
//			return this.mOnItemLongPressListener.onItemLongPress(index, this.mItemList.get(index));
//		else
//			return false;
	}

	protected boolean onTap(int index) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onTap is called, about to call onItemTap");
		if(mTapIndex == index)
			mTapIndex = -1;
		else
			mTapIndex = index;

		if(this.mOnItemTapListener != null)
			return this.mOnItemTapListener.onItemTap(index, this.mItemList.get(index));
		else
			return false;
	}

	@SuppressWarnings("hiding")
	public static interface OnItemTapListener<PoiPoint>{
		public boolean onItemTap(final int aIndex, final PoiPoint aItem);
	}

	@SuppressWarnings("hiding")
	public static interface OnItemLongPressListener<PoiPoint>{
		public boolean onItemLongPress(final int aIndex, final PoiPoint aItem);
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.w("RMAPS-Me-PoiOverlay", "onDrawFinished is called");
	}



	private class PoiListThread extends Thread {
		private double mdeltaX;
		private double mdeltaY;

		public void setParams(double deltaX, double deltaY){
			mdeltaX = deltaX;
			mdeltaY = deltaY;
		}

		@Override
		public void run() {
			mItemList = mPoiManager.getPoiListNotHidden(mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);

			super.run();
		}

	}


}


