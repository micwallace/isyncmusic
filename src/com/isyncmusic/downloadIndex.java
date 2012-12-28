package com.isyncmusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

public class downloadIndex {
	 
    private final String PATH = "/data/data/com.isyncmusic/files/";  //put the downloaded file here
    public boolean DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
    	try {
    		File path = new File(PATH);
    		path.mkdirs();
            URL url = new URL(imageURL); //you can write here any link
            File file = new File(path, fileName);
            long startTime = System.currentTimeMillis();
            Log.d("ImageManager", "download begining");
            Log.d("ImageManager", "download url:" + url);
            Log.d("ImageManager", "downloading index");
            /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();
            /*
             * Define InputStreams to read from the URLConnection.
             */
            ucon.setConnectTimeout(20000);
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            /*
             * Read bytes to the Buffer until there is nothing more to read(-1).
             */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
            }

            /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();
            Log.d("ImageManager", "download ready in"
                            + ((System.currentTimeMillis() - startTime) / 1000)
                            + " sec");
            return true;
    } catch (IOException e) {
            Log.d("downloadIndex", "Error: " + e);
            return false;
    }
    }
}
