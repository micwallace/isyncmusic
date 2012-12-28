package com.isyncmusic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class SongSelectionView extends SelectionListView {
	public void OnclickAction(AdapterView<?> parent, View view,
			int position, long id){
		ListItem clickedobj =(ListItem) parent.getItemAtPosition(position);
		String relpath = clickedobj.getRelPath();
		//relpath = relpath.replaceAll("\\\\", "/");
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, relpath, duration);
		toast.show();
		// get IPsocket
		String IPsocket = global.getIPAddress();
		Uri mediapath = Uri.parse("http://"+IPsocket+relpath.replaceAll(" ", "%20"));
		Intent playintent = new Intent();
		playintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		playintent.setAction(android.content.Intent.ACTION_VIEW);
		playintent.setDataAndType(mediapath, "audio/*");
		startActivity(playintent);
	}
	public void OnCheckAction(String listtxt, boolean checked){
		System.out.println("Item "+listtxt+" was "+(checked?"checked":"unchecked"));
		if (checked){
			global.getSelectList().addSong(listtxt, true);
		} else {
			global.getSelectList().removeSong(listtxt, true);
		}
	}
	public void onBackPressed(){
		this.finish();
	}
}

