package com.isyncmusic;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScheduledSync extends BroadcastReceiver {
	private PublicResources global;
	private SharedPreferences prefs;
	@Override
    public void onReceive(Context context, Intent intent) {
        Log.d("iSyncMusic AutoSync", "Auto syncronization started.");
        // setup global variable object
     	global = ((PublicResources) context.getApplicationContext());
     	prefs = global.getPrefs();
        // pick ip address
     	Log.d("iSyncMusic AutoSync", "Setting IP socket.");
     	SetCurrentIP ippick = new SetCurrentIP();
		String currentip = ippick.run(prefs.getString("internalip", "0.0.0.0"), prefs.getString("externalip", "0.0.0.0"), prefs.getString("serverport", "0.0.0.0"));
		// if result is 0.0.0.0 update ip's if web service is enabled, otherwise just display error notification and exit
		if (prefs.getBoolean("webservice", false) && currentip.equals("0.0.0.0")) {
				WebServiceUpdate wsupdate = new WebServiceUpdate(prefs);
				// if webservice fails set dummy ip (internal IP)
				wsupdate.run();
		}
		// try a second time/ try after wsupdate
		currentip = ippick.run(prefs.getString("internalip", "0.0.0.0"), prefs.getString("externalip", "0.0.0.0"), prefs.getString("serverport", "0.0.0.0"));			
		// update in global resources
		global.setIPAddress(currentip.equals("0.0.0.0") ? prefs.getString("internalip", "0.0.0.0")+ ":"+ prefs.getString("port", "8080") : currentip);
		// proceed if theres a server connection
		if (!currentip.equals("0.0.0.0")){
			// download index
			Log.d("iSyncMusic AutoSync", "Downloading Index");
			String IPsocket = global.getIPAddress();
			downloadIndex dlindex = new downloadIndex();
			boolean dlresult = dlindex.DownloadFromUrl("http://" + IPsocket + "/iasindex.json", "ismsindex.json");
			// proceed if download succeeded
			if (dlresult){
				// read new index and collate with the users selections
				Log.d("iSyncMusic AutoSync", "Processing Index");
				global.getReadIndex().readIndex();
				global.getSelectList().collateWithNewIndex();
				// get download list
				ArrayList<SongListModel> dllist = global.getSelectList().getDownloadList();
				// check if theres pending downloads
				if (dllist.size()>0){
        			// downloads pending, set dllist in global vars and start the download service
					Log.d("iSyncMusic AutoSync", "Starting service");
					global.setCurrentDL(dllist);
					context.startService(new Intent(context, DownloadService.class));
				} else { 
        			// no download pending, display no downloads notification
					Log.d("iSyncMusic AutoSync", "Nothing to download: finished");
				}
			} else {
				// display error notification
				Log.d("iSyncMusic AutoSync", "Index download failed: finished");
			}
		} else {
			// display error notification
			Log.d("iSyncMusic AutoSync", "Could not connect to server: finished");
		}
    }
}
