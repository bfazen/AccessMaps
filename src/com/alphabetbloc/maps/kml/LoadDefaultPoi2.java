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

import com.robert.maps.kml.PoiManager;
import com.robert.maps.kml.XMLparser.GpxPoiParser;
import com.robert.maps.kml.XMLparser.KmlPoiParser;
import com.robert.maps.utils.Ut;

public class LoadDefaultPoi2 {
	protected final Context mCtx;
	private PoiManager mPoiManager;


	public LoadDefaultPoi2(Context ctx, String filename, int catid) {
		mCtx = ctx;
		if (mPoiManager == null)
			mPoiManager = new PoiManager(mCtx);
		String importPoiFilename = filename;
		int categoryId = catid;

		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "loadDefaultPoi importPoiFilename: " + importPoiFilename);
		File file = new File(importPoiFilename);
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "loadDefaultPoi new File is set: " + file);
		if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "loadDefaultPoi categoryId is set to: " + categoryId);
				
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

				if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser is set");
				if(parser != null){
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "SAXParser is Not NULL, about to call beginTransaction");
					mPoiManager.beginTransaction();
					if(OpenStreetMapViewConstants.DEBUGMODE) Log.d("RMAPS-Me-LoadPoi", "beginTransaction is Called");
					Ut.dd("Start parsing file " + file.getName());
					try {
						if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
							parser.parse(file, new KmlPoiParser(mPoiManager, categoryId));
						else if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx"))
							parser.parse(file, new GpxPoiParser(mPoiManager, categoryId));

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
			

	}

	
}
