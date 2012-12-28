package com.isyncmusic;

import java.util.ArrayList;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

public class AlbumSelectionView extends SelectionListView {
	public void OnclickAction(AdapterView<?> parent, View view,
			int position, long id){
		ArrayList<SongListModel> albumlist;
	    // Fetch artists albums or all albums?
		String clickedval =(String) parent.getItemAtPosition(position).toString();
		String currentartist = global.getCurrentArtist();
	    albumlist = global.getReadIndex().getartistalbumsongs(currentartist, clickedval);	
		// pass array to global resource var used by list creator
		global.setListObjectArray(albumlist);
		// open list view
		Intent Intent3 = new Intent(AlbumSelectionView.this, SongSelectionView.class);
		Intent3.putExtra("inputtype", "objects");
		Intent3.putExtra("stage", 3);
		startActivity(Intent3);
	}
	public void OnCheckAction(String listtxt, boolean checked){
		System.out.println("Item "+listtxt+" was "+(checked?"checked":"unchecked"));
		if (listtxt.equals("All")){
			
		} else {
			if (checked){
				global.getSelectList().addalbum(listtxt, true);
			} else {
				global.getSelectList().removealbum(listtxt, true);
			}
		}
	}
	public void onBackPressed(){
		this.finish();
	}
}
