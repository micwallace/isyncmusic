package com.isyncmusic;

import android.os.AsyncTask;
import android.util.Log;

public class CacheIndexTask extends AsyncTask<PublicResources, Integer, Long> {
	public PublicResources  global;
	@Override
	protected Long doInBackground(PublicResources... _global) {
		// TODO Auto-generated method stub
		global = _global[0];
		// create album key hashmap
		global.getReadIndex().createFastAlbumIndex();
		global.setTaskStatus(true);
		// create all songs cached array
		global.getReadIndex().genSongsArray();
		global.setSongTaskStatus(true);
		return null;
	}
	protected void onPostExecute(Long result) {
		Log.w("isyncmusic", "Index cache task finished");
    }
}