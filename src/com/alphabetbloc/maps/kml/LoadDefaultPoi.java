package com.alphabetbloc.maps.kml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.openintents.filemanager.util.FileUtils;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

import com.robert.maps.kml.ImportPoiActivity;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.XMLparser.GpxPoiParser;
import com.robert.maps.kml.XMLparser.KmlPoiParser;
import com.robert.maps.utils.Ut;

public class LoadDefaultPoi {
	protected final Context mCtx;
	EditText mFileName;
	Spinner mSpinner;
	private PoiManager mPoiManager;
	private String importPoiFilename;
	private ImportPoiActivity mImportPoiActivity;
	private String poiName;
	private File currentDirectory;

	public LoadDefaultPoi(Context ctx) {
		mCtx = ctx;
		if (mPoiManager == null)
			mPoiManager = new PoiManager(mCtx);
		
		String importDir = Ut.getRMapsImportDir(mCtx).getAbsolutePath();
		currentDirectory = new File(importDir);
		
		//use the ImportPoiActivity Preferences to store the values? was working before adding the LoadDefaultPoiCategories
//		SharedPreferences uiState = mImportPoiActivity.getPreferences(Activity.MODE_PRIVATE);
//		SharedPreferences.Editor editor = uiState.edit();
//		editor.putString(poiName, mFileName.getText().toString());
//		editor.commit();
		
		File[] files = currentDirectory.listFiles();

		int totalCount = 0;

		if (files == null) {
			Ut.d("Returned null - inaccessible directory?");
			totalCount = 0;
		} else {
			totalCount = files.length;
		}

		Ut.d("Counting files... (total count=" + totalCount + ")");

		
		
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "LoadDefaultPoi Called");
		importPoiFilename = "/sdcard/rmaps/import/default.kml";

		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "importPoiFilename: " + importPoiFilename);
		File file = new File(importPoiFilename);
		
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "doImportPoi new File is set: " + file);


				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "threadpool.execute.run() is Called");
				int CategoryId = 1;
				
				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "CategoryID is set");
				
				SAXParserFactory fac = SAXParserFactory.newInstance();
				SAXParser parser = null;
				try {
					parser = fac.newSAXParser();
				} catch (ParserConfigurationException e) {
					// TODO! Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO! Auto-generated catch block
					e.printStackTrace();
				}

				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser is set");
				if(parser != null){
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser is Not NULL, about to call beginTransaction");
					mPoiManager.beginTransaction();
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "beginTransaction is Called");
					Ut.dd("Start parsing file " + file.getName());
					try {
						if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
							parser.parse(file, new KmlPoiParser(mPoiManager, CategoryId));
						else if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx"))
							parser.parse(file, new GpxPoiParser(mPoiManager, CategoryId));

						mPoiManager.commitTransaction();
					} catch (SAXException e) {
						// TODO! Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IOException e) {
						// TODO! Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IllegalStateException e) {
					} catch (OutOfMemoryError e) {
						Ut.w("OutOfMemoryError");
						mPoiManager.rollbackTransaction();
					}
					Ut.dd("Pois commited");
				}


	}

	
}
