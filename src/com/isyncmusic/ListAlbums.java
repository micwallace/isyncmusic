package com.isyncmusic;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListAlbums extends ListViewFromArray {
	public void OnclickAction(){
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ArrayList<SongListModel> songlist;
			    // Fetch artists albums or all albums?
				String clickedval =(String) parent.getItemAtPosition(position);
				String currentartist = global.getCurrentArtist();
			    if(clickedval!="All"){
			    	if (currentartist=="All"){
			    		songlist = global.getReadIndex().getalbumsongsfast(currentartist, clickedval);
			    	} else {
			    	songlist = global.getReadIndex().getartistalbumsongs(currentartist, clickedval);	
			    	}
			    } else {
			    	// if current artist is all, get all songs, otherwise get current artist songs
			    	if (currentartist!="All"){
			    		songlist = global.getReadIndex().getartistsongs(currentartist);
			    	} else {
			    		//TODO: collect this data in an async task that starts after processing albums list
			    		indexstatus = global.getSongTaskstatus();
			    		if (!indexstatus){
			    			ProgressDialog dialog = ProgressDialog.show(ListAlbums.this, "","Pre-caching data...", true);
			    			while (indexstatus==false){
			    				try {
			    					Thread.sleep(500);
			    				} catch (InterruptedException e) {
			    					// TODO Auto-generated catch block
									e.printStackTrace();
			    				}
			    				indexstatus = global.getSongTaskstatus();
			    				Log.w("isyncmusic", "Async song task status: "+indexstatus);
			    			}
			    			dialog.dismiss();
			    		}
			    		songlist = global.getReadIndex().getallsongs();
			    	}
			    }
				// NOW REDUNDANT, PASSING VIA BUNDLE pass array to global resource var used by list creator
				global.setListObjectArray(songlist);
				// open list view
				Intent intent = new Intent(ListAlbums.this, ListFromObjectArray.class);
				// add values bundle
				startActivity(intent);
			}
		});
	}
}
