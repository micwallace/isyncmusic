package com.isyncmusic;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;
/**
 * This is the main activity.
 */
public class PlayUrlMedia extends Activity {
	@Override	
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);  
	String relpath = this.getIntent().getStringExtra("relpath");
	PlayerCallback clb = new PlayerCallback() {

		public void playerException(Throwable arg0) {
			// TODO Auto-generated method stub
			
		}

		public void playerMetadata(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		public void playerPCMFeedBuffer(boolean arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		public void playerStarted() {
			// TODO Auto-generated method stub
			
		}

		public void playerStopped(int arg0) {
			// TODO Auto-generated method stub
			
		}  };
		URL relurl = null;
		try {
			relurl = new URL("http://192.168.1.15:8080"+relpath.replaceAll(" ", "%20"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		Log.w("isyncmusic", relurl.toString());
		MultiPlayer aacMp3Player = new MultiPlayer( clb );
		aacMp3Player.playAsync(relurl.toString());
	}
}
