package com.robert.maps.kml;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.util.FileUtils;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.robert.maps.R;
import com.robert.maps.kml.XMLparser.GpxPoiParser;
import com.robert.maps.kml.XMLparser.KmlPoiParser;
import com.robert.maps.utils.Ut;

public class ImportPoiActivity2 extends Activity {

	EditText mFileName;
	Spinner mSpinner;
	private PoiManager mPoiManager;
	private String defaultKml;
	private boolean defaultPOI;
	private String importPoiFilename;
	
	private ProgressDialog dlgWait;
	protected ExecutorService mThreadPool = Executors.newFixedThreadPool(2);

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		if (mPoiManager == null)
			mPoiManager = new PoiManager(this);
		
		
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "loadPoi Called");
		importPoiFilename = "/sdcard/rmaps/import/default.kml";

		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "importPoiFilename is now set: " + importPoiFilename);
		File file = new File(importPoiFilename);

				int CategoryId = 1;
				SAXParserFactory fac = SAXParserFactory.newInstance();
				SAXParser parser = null;
				try {
					parser = fac.newSAXParser();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser all set");
				
				if(parser != null){
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser NOT NULL about to call POiManager.beginTrasaction");
					
					mPoiManager.beginTransaction();

					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "we have now begun a transaction!");
					
					Ut.dd("Start parsing file " + file.getName());
					try {
						if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
							parser.parse(file, new KmlPoiParser(mPoiManager, CategoryId));
						else if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx"))
							parser.parse(file, new GpxPoiParser(mPoiManager, CategoryId));

						mPoiManager.commitTransaction();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IllegalStateException e) {
					} catch (OutOfMemoryError e) {
						Ut.w("OutOfMemoryError");
						mPoiManager.rollbackTransaction();
					}
					Ut.dd("Pois commited");
				}


				
			
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "about to dismiss!");

				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "dismissed about to finish!");
				ImportPoiActivity2.this.finish();
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "we have called finish already!");

	}


	@Override
	protected void onDestroy() {
		mPoiManager.FreeDatabases();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("IMPORT_POI_FILENAME", mFileName.toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putString("IMPORT_POI_FILENAME", mFileName.getText().toString());
		editor.commit();
		super.onPause();
	}

}
