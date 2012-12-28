package com.isyncmusic;

import java.util.ArrayList;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ListFromObjectArray extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PublicResources global = ((PublicResources)getApplicationContext());
		final ArrayList<SongListModel> arrayobjectlist = global.getListObjectArray();
		setListAdapter(new ArrayAdapter<SongListModel>(this, R.layout.listview, arrayobjectlist));
	    
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SongListModel clickedobj = (SongListModel) parent.getItemAtPosition(position);
				String relpath = clickedobj.getRelPath().toString();
				//relpath = relpath.replaceAll("\\\\", "/");
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, relpath, duration);
				toast.show();
				// init media player view
				// get IPsocket
				String IPsocket = global.getIPAddress();
				Uri mediapath = Uri.parse("http://"+IPsocket+relpath.replaceAll(" ", "%20"));
				Intent playintent = new Intent();
				playintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				playintent.setAction(android.content.Intent.ACTION_VIEW);
				playintent.setDataAndType(mediapath, "audio/*");
				// open media in music or video application
				startActivity(playintent);
			}
		});
 
	}
 
}