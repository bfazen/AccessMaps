package com.alphabetbloc.maps.kml;

import java.io.File;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.openintents.filemanager.util.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.robert.maps.kml.PoiCategory;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.utils.Ut;

public class LoadDefaultPoiCategories extends Thread {

	private File currentDirectory;
	boolean cancel;

	private Context mCtx;

	private PoiCategory mPoiCategory;
	private PoiManager mPoiManager;
	private final String importDir;
	private int mId;
	
	public String mTitle;
	public boolean mHidden;
	public int mIconId;
	public int mMinZoom;

	
	// // Cupcake-specific methods
	// static Method formatter_formatFileSize;

	public LoadDefaultPoiCategories(Context context) {
		super("Directory Scanner");
		this.mCtx = context;
		importDir = Ut.getRMapsImportDir(mCtx).getAbsolutePath();
		currentDirectory = new File(importDir);		
		
		if(mPoiManager == null)
			mPoiManager = new PoiManager(mCtx);
	}
	
//	private boolean needPoiUpdate(){
//		boolean toUpdate = false;
//		long updated = currentDirectory.lastModified();
//		if (updated > IMPORT_UPDATED) {
//			toUpdate = true;
//		}
//		return toUpdate;
//	}
	
	
	

	private void clearData() {
		// Remove all references so we don't delay the garbage collection.
		mCtx = null;
	}

	public void run() {
		Ut.d("Scanning directory " + currentDirectory);

		File[] files = currentDirectory.listFiles();

		int totalCount = 0;

		if (cancel) {
			Ut.d("Scan aborted");
			clearData();
			return;
		}

		if (files == null) {
			Ut.d("Returned null - inaccessible directory?");
			totalCount = 0;
		} else {
			totalCount = files.length;
		}

		Ut.d("Counting files... (total count=" + totalCount + ")");

		
		if (files != null) {
			for (File currentFile : files) {
				if (cancel) {
					// Abort!
					Ut.d("Scan aborted while checking files");
					clearData();
					return;
				}
				

				String ext = FileUtils.getExtension(currentFile.getName());
				if (ext.equalsIgnoreCase(".kml")) {
					String fileName = currentFile.getName();
					String loadPoiPath = importDir + "/" + fileName;
//					Add the POI categories based on the filename in the format of: ID_PoiName.xml
					int dot = fileName.lastIndexOf(".");
					int und = fileName.indexOf("_");
					int und_title = und + 1;  
					

					if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-LoadPoi", "LoadDefaultPoiCategories filename is: " + fileName);
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-LoadPoi", "LoadDefaultPoiCategories dot is: " + dot);
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.e("RMAPS-Me-LoadPoi", "LoadDefaultPoiCategories und is: " + und);
					
					mTitle = fileName.substring(und_title, dot);
					mId = Integer.valueOf(fileName.substring(0, und));

					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "LoadDefaultPoiCategories Called and mTitle is: " + mTitle);
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "LoadDefaultPoiCategories Called and mId is: " + mId);
					
		        	mPoiCategory = new PoiCategory(mId, mTitle, false, mId, 13);
					mPoiManager.loadPoiCategory(mPoiCategory);
					
//					First delete all the POI from the previously associated category, then add the individual new POI from the file to the associated POIcatgeory
					mPoiManager.deletePoiByCategory(mId);
					LoadDefaultPoi2 mLoadDefaultPoi2 = new LoadDefaultPoi2(mCtx, loadPoiPath, mId);
					
				}

			}
		}
		
		clearData();
		mPoiManager.FreeDatabases();
	}

}
