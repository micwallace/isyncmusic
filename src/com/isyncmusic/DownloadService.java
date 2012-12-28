package com.isyncmusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadService extends Service {
	private NotificationManager nm;
	private Notification notification;
	private AsyncTask<ArrayList<SongListModel>, Integer, Long> dltask;
    private static boolean isRunning = false;
    private static boolean cancelled = false;
    private static boolean forcecancelled = false;
    private String currentdl = null;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SERVICE_STOPPED = 3;
    static final int MSG_SET_DL_BAR = 4;
    static final int MSG_SET_PROGRESS = 5;
    static final int MSG_STOP_TASK = 6;
    static final int MSG_FORCE_STOP = 7; // cancel the current download and all tasks TBC
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    @Override
    public void onCreate() {
        super.onCreate();
        cancelled = false;
        forcecancelled = false;
        Log.i("MyService", "Service Started.");
        // show notification
        showNotification();
        // setup global variable object and get download list
        PublicResources global = ((PublicResources)getApplicationContext());
        ArrayList<SongListModel> dllist = global.getSelectList().getDownloadList();
        // set current download list for use by sync view when service is running, it prevents changes of the pending downloads during a sync (for a few reasons DONT CHANGE)
        global.setCurrentDL(dllist);
        // check for single/selected download object, if present, set dllist to this single value arraylist
        if (global.isSetSelectDL()){
        	dllist = global.getSelectDL();
        }
        // create and execute async download task
        dltask = new DownloadTask(global);
    	dltask.execute(dllist);
    	isRunning = true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    // ACTIVITY MESSAGES
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                // when client binds, we'll send the current download
                if (currentdl!=null){
                	sendDataToUI(0);
                }
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_STOP_TASK:
            	cancelled=true;
            	break;
            /*case MSG_SET_INT_VALUE:
                incrementby = msg.arg1;
                break;*/
            case MSG_FORCE_STOP:
            	forcecancelled = true;
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    private void sendDataToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
            	// if input is 0, we'll send the current dl instead of dl progress
            	if (intvaluetosend==-1){
            		// let activity know the service has stopped
            		mClients.get(i).send(Message.obtain(null, MSG_SERVICE_STOPPED));
            	} else if (intvaluetosend==0){
                	//Send current dl
                	Bundle b = new Bundle();
                	b.putString("str1", currentdl);
                	Message msg = Message.obtain(null, MSG_SET_DL_BAR);
                	msg.setData(b);
                	mClients.get(i).send(msg);
                } else {
                	// Send progress int
                	mClients.get(i).send(Message.obtain(null, MSG_SET_PROGRESS, intvaluetosend, 0));
                }
            } catch (RemoteException e) {
            	Log.w("", "client disconnected from download service");
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
    // NOTIFICATION
    public void resultNotify(int _result){
    	// show the result notification
    	if (nm==null){
    		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	} else {
    		nm.cancel(R.layout.synctable); // Cancel the persistent notification.
    	}
    	// Set the icon, scrolling text and timestamp
    	CharSequence result = (_result==0?"Sync failed!":"Successfully synced "+_result+" songs");
        Notification resnotify = new Notification((_result==0?android.R.drawable.stat_notify_error:android.R.drawable.stat_notify_sync), result, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        Intent notifyintent = new Intent(this, SyncView.class);
        notifyintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent cintent = PendingIntent.getActivity(this, 0, notifyintent, 0);
        // Set the info for the views that show in the notification panel.
        resnotify.setLatestEventInfo(this, "ISyncMusic", result, cintent);
        resnotify.flags = Notification.FLAG_AUTO_CANCEL;
        // Send the notification.
        nm.notify(R.layout.syncrow, resnotify);
        // stop service
        stopSelf();
    }
    private void showNotification() {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Syncronising music...";
        // Set the icon, scrolling text and timestamp
        notification = new Notification(android.R.drawable.stat_sys_download, text, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        Intent notifyintent = new Intent(this, SyncView.class);
        notifyintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent cintent = PendingIntent.getActivity(this, 0, notifyintent, 0);
        notification.contentIntent = cintent;
        // set as ongoing
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        // init layout
        notification.contentView = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.downloadnotify);
        notification.contentView.setImageViewResource(R.id.notify_icon, android.R.drawable.stat_sys_download);
        notification.contentView.setTextViewText(R.id.notify_title, "ISyncMusic");
        notification.contentView.setTextViewText(R.id.notify_text, "Syncronizing music..");
        notification.contentView.setTextViewText(R.id.notify_progtext, "0%");
        notification.contentView.setProgressBar(R.id.notify_progress, 100, 0, true);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nm.notify(R.layout.synctable, notification);
    }
    private void updateNotifyProgress(int progress) {
    	notification.contentView.setTextViewText(R.id.notify_progtext, progress+"%");
    	notification.contentView.setProgressBar(R.id.notify_progress, 100, progress, false);
        // inform the notification of updates
        nm.notify(R.layout.synctable, notification);
    }
    private void updateNotifyText(String _text){
    	Log.w("download task", "new notification text: "+_text);
    	// init layout
    	notification.contentView.setTextViewText(R.id.notify_progtext, "0%");
    	notification.contentView.setProgressBar(R.id.notify_progress, 100, 0, false);
    	notification.contentView.setTextViewText(R.id.notify_text, _text);
        // inform the notification of updates
        nm.notify(R.layout.synctable, notification);
    }
    // DL STATUS HANDLING; updates notification and send progress updates to UI
    private void setCurrentDownload(SongListModel _cdlobject){
    	currentdl = _cdlobject.toString();
    	// set current download in notification; TBC
    	updateNotifyText("Downloading: "+_cdlobject.getArtist()+" - "+currentdl);
    	// send current download to UI activity
    	sendDataToUI(0);
    }
    private void setDownloadProgress(int _progress){
    	// update notification
    	updateNotifyProgress(_progress);
    	// send update to connected clients
    	sendDataToUI(_progress);
    }
    // MISC SEVICE STUFF
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Download service", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }
    public static boolean isRunning(){
        return isRunning;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //if (timer != null) {timer.cancel();}
        //counter=0;
        //dltask.cancel(true);
        //nm.cancel(R.layout.synctable); // Cancel the persistent notification; async task now fires this
        Log.i("MyService", "Service Stopped.");
        // send message to activity
        sendDataToUI(-1);
        isRunning = false;
    }
    // DOWNLOAD TASK
    public class DownloadTask extends AsyncTask<ArrayList<SongListModel>,Integer,Long> {
    	private PublicResources global;
    	private final String ROOTPATH = Environment.getExternalStorageDirectory()+"/Music/isyncedmusic/";  //put the downloaded file here
    	private String weburl;
    	public DownloadTask(PublicResources context) {
    		// setup global variable object
            global = context;
            weburl = "http://"+global.getIPAddress();
            Log.w("DownloadTask", "Url site: "+weburl);
        }
    	@Override
    	protected Long doInBackground(ArrayList<SongListModel>... _songs) {
    		ArrayList<SongListModel> songs = _songs[0];
    		int filecount = 0;
    		// download each file
    		Iterator<SongListModel> it = songs.iterator(); 
    		while (it.hasNext()){
    			SongListModel tempobj = it.next();
    			String path = tempobj.getRelPath();
    			String encpath = path.replaceAll(" ", "%20");
    			File file = new File(ROOTPATH+path);
    			// sets current download string, updates notification and sends msg to connected UIs
    			setCurrentDownload(tempobj);
    			try {
    				URL url = new URL(weburl+encpath); // file link
    				Log.w("dl", "Url address: "+url.toString());
    				// get parent and write dirs
    				File dir = new File(file.getParent());
    				dir.mkdirs();
    				Log.d("DownloadManager", "download begining");
    				Log.d("DownloadManager", "download url:" + url);
    				// open connection and set timeout
    				URLConnection ucon = url.openConnection();
    				ucon.setConnectTimeout(20000);
    				// this will be useful so that you can show a typical 0-100% progress bar
    				int fileLength = Integer.valueOf(tempobj.getSize());
    				//Define InputStreams to read from the URLConnection and a file output stream
    				InputStream is = ucon.getInputStream();
    				BufferedInputStream bis = new BufferedInputStream(is);
    				FileOutputStream fos = new FileOutputStream(file);
    				// Read bytes to the Buffer until there is nothing more to read(-1).
    				byte data[] = new byte[1024];
    				long total = 0;
    				int progress = 0;
    				int count;
    				Date interestingDate = new Date();
    				while ((count = bis.read(data)) != -1) {
    					total += count;
    					// publishing progress every 300ms (prevent ui from crashing from fast download lol)
    					if (((new Date()).getTime() - interestingDate.getTime()) > 300){
    						progress = (int) (total * 100 / fileLength);
    						publishProgress(progress);
    						interestingDate = new Date();
    					}
    					// write to output
    					fos.write(data, 0, count);
    					// check for force stop
    					if (forcecancelled){
    						fos.flush();
    	    				fos.close();
    	    				bis.close();
    						if (file.exists()){
    	    					file.delete();
    	    				}
    	    				// clear single/select download list
    	        			global.clearSelectDL();
    	    				return Long.valueOf(filecount);
    	    			}
    				}
    				// finalize progress
    				publishProgress(100);
    				fos.flush();
    				fos.close();
    				bis.close();
    				// increment file counter
    				filecount++;
    				it.remove(); // remove from current download list
    				Log.d("Download Task", "Download completed!");	
    			} catch (IOException e) {
    				Log.d("DownloadTask", "Error: " + e);
    				if (file.exists()){
    					file.delete();
    				}
    				return Long.valueOf(0);
    			}
    			// update global completed download list
    			global.putCompletedDL(tempobj);
    			if (cancelled){
    				// clear single/select download list
        			global.clearSelectDL();
    				return Long.valueOf(filecount);
    			}
    		}
    		// clear single download list
			global.clearSelectDL();
    		return Long.valueOf(filecount);
    	}
    	@Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // run progress update function in service
            setDownloadProgress(progress[0]);
        }
        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            if (result.intValue() > 0){
            	// run success notification function in service
            	resultNotify(result.intValue());
            } else {
            	// run error notification function in service
            	resultNotify(0);
            }
        }		
    }
}
