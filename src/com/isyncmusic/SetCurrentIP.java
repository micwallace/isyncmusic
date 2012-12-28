package com.isyncmusic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class SetCurrentIP {
	public String run(String _intip, String _extip, String _port){
		Log.w("isyncmusic", "IP picker started ");
		String currentip = "0.0.0.0";
		if (testCon(_intip+":"+_port)){
			currentip = _intip+":"+_port;
			Log.w("isyncmusic", "IP socket: "+currentip);
			return currentip;
		} else if (testCon(_extip+":"+_port)){
			currentip = _extip+":"+_port;
			Log.w("isyncmusic", "IP socket: "+currentip);
			return currentip;
		} else {
			Log.w("isyncmusic", "IP socket (no con): "+currentip);
			return currentip;
		}
	}
	public boolean testCon(String socket) {
	    String strUrl = "http://"+socket+"/iasindex.json";
	    try {
	        URL url = new URL(strUrl);
	        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
	        urlConn.setConnectTimeout(4000);
	        urlConn.connect();
	        return true;
	    } catch (SocketTimeoutException e) {
	        System.err.println("Error creating HTTP connection");
	        return false;
	    } catch (IOException e) {
	    	System.err.println("Error creating HTTP connection");
	    	return false;
		}
	}

}
