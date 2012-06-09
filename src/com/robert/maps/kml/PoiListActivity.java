package com.robert.maps.kml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.robert.maps.R;

public class PoiListActivity extends ListActivity {
	private PoiManager mPoiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        registerForContextMenu(getListView());
	}

	@Override
	protected void onStart() {
		mPoiManager = new PoiManager(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		mPoiManager.FreeDatabases();
		mPoiManager = null;
		super.onStop();
	}

	@Override
	protected void onResume() {
		FillData();
		super.onResume();
	}

	private void FillData() {
		Cursor c = mPoiManager.getGeoDatabase().getPoiListCursor();
		startManagingCursor(c);

		
//		louis.fazen changed this in order to get rid of the description
//        ListAdapter adapter = new SimpleCursorAdapter(this,
//                android.R.layout.simple_list_item_2, c,
//                        new String[] { "name", "descr" },
//                        new int[] { (android.R.id.text1), (android.R.id.text2) });
        
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                        new String [] {"name"},
                        new int [] {android.R.id.text1});
        	
        setListAdapter(adapter);
	}
	
//	louis.fazen trying so many things that are not working to insert the two textviews in order to preserve the html formatting... 
//	final ArrayList<TextView> items = new ArrayList<Collection extends TextView> collection();
//	if(c != null){
//		if (c.moveToFirst()) {
//			do {
//				TextView titleView = (TextView) findViewById(android.R.id.text1);
//				TextView descrView = (TextView) findViewById(android.R.id.text2);
//				String title = c.getString(2);
//				String descr = c.getString(3);
//				titleView.setText(title);
//				descrView.setText(Html.fromHtml(descr));
//				items.addAll(2, (Collection<? extends TextView>) descrView);
//				
//				Spanned nText = Html.fromHtml(c.getString(3));
//				
//			} while (c.moveToNext());
//		}
//		c.close();
//	}
//		private List<PoiPoint> doCreatePoiListFromCursor(Cursor c){
//	final ArrayList<PoiPoint> items = new ArrayList<PoiPoint>();
//	if (c != null) {
//		if (c.moveToFirst()) {
//			do {
//				items.add(new PoiPoint(c.getInt(4), c.getString(2), c.getString(3), new GeoPoint(
//						(int) (1E6 * c.getDouble(0)), (int) (1E6 * c.getDouble(1))), c.getInt(7), c.getInt(8)));
//			} while (c.moveToNext());
//		}
//		c.close();
//	}
//
//	return items;
//}
//
//	
//	int ret  = 0;
//		try {
//			final Cursor c = this.mIndexDatabase.rawQuery("SELECT minzoom FROM ListCashTables WHERE name = '"
//					+ mCashTableName + "'", null);
//			if (c != null) {
//				if (c.moveToFirst()) {
//					ret = c.getInt(c.getColumnIndexOrThrow("minzoom"));
//				}
//				c.close();
//			}
//		} catch (Exception e) {
//			Toast.makeText(mCtx, R.string.message_corruptindex, Toast.LENGTH_LONG).show();
//			e.printStackTrace();
//		}


	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poilist_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch(item.getItemId()){
		case R.id.menu_addpoi:
			final Intent PoiIntent = new Intent(this, PoiActivity.class); 
	        Bundle extras = getIntent().getExtras();
	        if(extras != null){
	        	PoiIntent.putExtra("lat", extras.getDouble("lat")).putExtra("lon", extras.getDouble("lon")).putExtra("title", extras.getString("title"));
	        }
			startActivity(PoiIntent);
			return true;
		case R.id.menu_categorylist:
			startActivity((new Intent(this, PoiCategoryListActivity.class)));
			return true;
		case R.id.menu_importpoi:
			startActivity((new Intent(this, ImportPoiActivity.class)));
			return true;
		case R.id.menu_deleteall:
			showDialog(R.id.menu_deleteall);
			return true;
		}

		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case R.id.menu_deleteall:
			return new AlertDialog.Builder(this)
				//.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.warning_delete_all_poi)
				.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mPoiManager.DeleteAllPoi();
									FillData();
								}
							}).setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

									/* User clicked Cancel so do some stuff */
								}
							}).create();
		}
		;

		return super.onCreateDialog(id);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		int pointid = (int) ((AdapterView.AdapterContextMenuInfo)menuInfo).id;
		PoiPoint poi = mPoiManager.getPoiPoint(pointid);

		menu.add(0, R.id.menu_gotopoi, 0, getText(R.string.menu_goto));
		menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
		if(poi.Hidden)
			menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
		else
			menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
		menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
		menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int pointid = (int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id;
		PoiPoint poi = mPoiManager.getPoiPoint(pointid);

		switch(item.getItemId()){
		case R.id.menu_editpoi:
			startActivity((new Intent(this, PoiActivity.class)).putExtra("pointid", pointid));
			break;
		case R.id.menu_gotopoi:
			setResult(RESULT_OK, (new Intent()).putExtra("pointid", pointid));
			finish();
			break;
		case R.id.menu_deletepoi:
			mPoiManager.deletePoi(pointid);
			FillData();
	        break;
		case R.id.menu_hide:
			poi.Hidden = true;
			mPoiManager.updatePoi(poi);
			FillData();
	        break;
		case R.id.menu_show:
			poi.Hidden = false;
			mPoiManager.updatePoi(poi);
			FillData();
	        break;
		case R.id.menu_toradar:
			try {
					Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra("name", poi.Title);
					i.putExtra("latitude",  (float)(poi.GeoPoint.getLatitudeE6() / 1000000f));
					i.putExtra("longitude", (float)(poi.GeoPoint.getLongitudeE6() / 1000000f));
					startActivity(i);
				} catch (Exception e) {
					Toast.makeText(this, R.string.message_noradar, Toast.LENGTH_LONG).show();
				}
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

}
