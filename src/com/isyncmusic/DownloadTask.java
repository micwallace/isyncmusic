package com.isyncmusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadTask extends AsyncTask<String,Integer,Long> {
	private PublicResources global;
	private final String ROOTPATH = Environment.getExternalStorageDirectory()+"/Music/isyncedmusic/";  //put the downloaded file here
	private String weburl;
	public DownloadTask(Context context) {
		// setup global variable object
        global = ((PublicResources)context);
        weburl = "http://"+global.getIPAddress();
        Log.w("DownloadTask", "Url site: ");
    }
	@Override
	protected Long doInBackground(ArrayList<SongListModel>... _songs) {
		ArrayList<SongListModel> song = _songs[0];
		int filecount = 0;
		// download each file
		Iterator<E> it = song.iterator(); 
		while (it.hasNext()){
			// update notification text
			
			File file = new File(ROOTPATH+path);
			try {
				URL url = new URL(weburl+path); // file link
				// get parent and write dirs
				File dir = new File(file.getParent());
				dir.mkdirs();
				Log.d("DownloadManager", "download begining");
				Log.d("DownloadManager", "download url:" + url);
				/* Open a connection to that URL. */
				URLConnection ucon = url.openConnection();
				// this will be useful so that you can show a typical 0-100% progress bar
				int fileLength = ucon.getContentLength();
				//Define InputStreams to read from the URLConnection and a file output stream
				ucon.setConnectTimeout(20000);
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				FileOutputStream fos = new FileOutputStream(file);
				// Read bytes to the Buffer until there is nothing more to read(-1).
				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = bis.read(data)) != -1) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / fileLength));
					// write to output
					fos.write(data, 0, count);
				}
				fos.flush();
				fos.close();
				bis.close();
				// increment file counter
				filecount++;
				Log.d("Download Task", "Download completed!");
			} catch (IOException e) {
				Log.d("DownloadTask", "Error: " + e);
				if (file.exists()){
					file.delete();
				}
				return Long.valueOf(0);
			}	
		}
		return Long.valueOf(filecount);
	}
	@Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // run progress update function in service
    }
    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        if (result.intValue() > 0){
        	// run success notification function in service
        } else {
        	// run error notification function in service
        }
    }		
}
