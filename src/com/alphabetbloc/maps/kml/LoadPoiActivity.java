/**
 * 
 */
package com.alphabetbloc.maps.kml;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.openintents.filemanager.util.FileUtils;
import org.xml.sax.SAXException;

import com.robert.maps.R;
import com.robert.maps.kml.GeoDatabase;
import com.robert.maps.kml.ImportPoiActivity;
import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.XMLparser.GpxPoiParser;
import com.robert.maps.kml.XMLparser.KmlPoiParser;
import com.robert.maps.utils.Ut;
import com.robert.maps.kml.constants.PoiConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Louis.Fazen@gmail.com
 *
 */
public class LoadPoiActivity extends Activity {
    private File currentDirectory = new File("");
	private String default1;
	private PoiManager mPoiManager;
//	protected final Context mCtx;
	private GeoDatabase mGeoDatabase;
	protected ExecutorService mThreadPool = Executors.newFixedThreadPool(2);
	private PoiConstants mPoiConstants;
	
	/**
	 * 
	 */
	public LoadPoiActivity() {

//		mCtx = ctx;
//	    mGeoDatabase = new GeoDatabase(ctx);
	    
//	    SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
	    
	    String defaultFile6 = "default.kml";
		String defaultFile1 =  Ut.getRMapsImportDir(this).getAbsolutePath();
		String defaultFile2 = Uri.decode(defaultFile1);
		String defaultFile7 = defaultFile1 + "/default.kml";
//		File defaultFile3 = Ut.getRMapsImportDir(mCtx);
		final File defaultFile4 = new File(defaultFile1);
		final File defaultFile5 = new File(defaultFile2);
		
		if(OpenStreetMapViewConstants.DEBUGMODE) {
				
			Log.d("RMAPS-Me-LoadPoi", "defaultFile1: " + defaultFile1);
			Log.d("RMAPS-Me-LoadPoi", "defaultFile2: " + defaultFile2);
//			Log.d("RMAPS-Me-LoadPoi", "defaultFile3: " + defaultFile3);
			Log.d("RMAPS-Me-LoadPoi", "defaultFile4: " + defaultFile4);
			Log.d("RMAPS-Me-LoadPoi", "defaultFile5: " + defaultFile5);
			Log.d("RMAPS-Me-LoadPoi", "defaultFile7: " + defaultFile7);
		}
		
//		if(!defaultFile4.exists()){
////			Toast.makeText(this, "No such file4", Toast.LENGTH_LONG).show();
//			
//			return;
//		}
//		if(!defaultFile5.exists()){
////			Toast.makeText(this, "No such file5", Toast.LENGTH_LONG).show();
//			
//			return;
//		}

		
		
	    
//		String curdir = currentDirectory.getAbsolutePath();
//		File clickedFile = FileUtils.getFile(curdir, "Region.Mapnik.sqlitedb");
//    	Intent intent = getIntent();
//    	intent.setData(FileUtils.getUri(clickedFile));
//    	setResult(RESULT_OK, intent);
	

	/**
	 * @param args
	 */	
//	protected void doSelectFile() {
//		Intent intent = new Intent(this, FileManagerActivity.class);
//		intent.setData(Uri.parse(mFileName.getText().toString()));
//		startActivityForResult(intent, R.id.ImportBtn);
//		
//	}
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//
//		switch (requestCode) {
//		case R.id.ImportBtn:
//			if (resultCode == RESULT_OK && data != null) {
//				// obtain the filename
//				String filename = Uri.decode(data.getDataString());
//				if (filename != null) {
//
//					mFileName.setText(filename);
//				}
//
//			}
//			break;
//		}
//	}
	
//	private void doImportPOI() {
		
		//if(!defaultFile.exists()) finish();// no default file on sdcard
		
	    
	    
	    
	    
	    
	    
	    
		
//		this.mThreadPool.execute(new Runnable() {
//			public void run() {
//
//				SAXParserFactory fac = SAXParserFactory.newInstance();
//				SAXParser parser = null;
//				try {
//					parser = fac.newSAXParser();
//				} catch (ParserConfigurationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (SAXException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				if(parser != null){
//					mGeoDatabase.beginTransaction();
//					//	mPoiManager.beginTransaction();
//					Ut.dd("Start parsing file " + defaultFile5.getName());
//					try {
//
//							parser.parse(defaultFile4, new KmlPoiParser(mPoiManager, 5));
//						
//
//					//	mPoiManager.commitTransaction();
//						mGeoDatabase.commitTransaction();
//					} catch (SAXException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						mPoiManager.rollbackTransaction();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						mPoiManager.rollbackTransaction();
//					} catch (IllegalStateException e) {
//					} catch (OutOfMemoryError e) {
//						Ut.w("OutOfMemoryError");
//						mPoiManager.rollbackTransaction();
//					}
//					Ut.dd("Pois commited");
//				}
//
//				LoadPoiActivity.this.finish();
//			};
//		});

	}

}
