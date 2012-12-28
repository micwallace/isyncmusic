package com.isyncmusic;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SyncView extends Activity {
	private PublicResources global;
	// SERVICE VARS
	LinearLayout btnStart, btnStartSelect, btnStop, btnClear;
    Messenger mService = null;
    TableLayout table;
    ProgressBar progressbar;
    TableRow currentdlrow;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	// END SERVICE VARS
    private ArrayList<SongListModel> dllist;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.synctable);
    	// setup global variable object
        global = ((PublicResources)getApplicationContext());
        // start loader
        final ProgressDialog dialog = ProgressDialog.show(SyncView.this, "", ("Processing data..."), true);
        table = ((TableLayout) this.findViewById(R.id.synctable));
        final TextView dlstat = ((TextView) this.findViewById(R.id.dlstat));
    	new Thread(){
    		public void run(){
    			// get completed downloads and iterate into table
    	    	if (global.getCompleteDL()!=null){
    	    		runOnUiThread(new Runnable() {
    					public void run() {
    						genCompletedRows(global.getCompleteDL());
    					}
    	    		});
    	    	}
    	    	// get download list and iterate into table, if there is a current service running, use its list
    	    	if (DownloadService.isRunning()){
    	    		dllist = global.getCurrentDL();
    	    	} else {
    	    		dllist = global.getSelectList().getDownloadList();
    	    	}
    	    	runOnUiThread(new Runnable() {
					public void run() {
						genCurrentRows(dllist);
						dlstat.setText("Pending:\n"+global.getSelectList().getDownloadCount()+" ("+global.convertBytes(global.getSelectList().getDownloadSize(), true)+")");
						dialog.cancel();
					}
    	    	});
    		}
    	}.start();
    	
    	// button stuff
    	btnStart = (LinearLayout)findViewById(R.id.btnStart);
    	btnStartSelect = (LinearLayout)findViewById(R.id.btnStartSelect);
        btnStop = (LinearLayout)findViewById(R.id.btnStop);
        btnClear = (LinearLayout)findViewById(R.id.btnClear);
        btnStart.setOnClickListener(btnStartListener);
        btnStartSelect.setOnClickListener(btnDLSelectListener);
        btnStop.setOnClickListener(btnStopListener);
        btnClear.setOnClickListener(new OnClickListener() {
            public void onClick(View v){
                global.clearCompletedDL();
                table.removeAllViews();
                dllist = global.getSelectList().getDownloadList();
                genCurrentRows(dllist);
            }
        });
        restoreMe(savedInstanceState);
        CheckIfServiceIsRunning();
    }
    // generate table rows; TBC functionalize further
    private void genCurrentRows(ArrayList<SongListModel> rowlist){
    	final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(this.LAYOUT_INFLATER_SERVICE);
    	Iterator<SongListModel> it = rowlist.iterator();
    	while (it.hasNext()){
    		// inflate view and set data
    		SongListModel tempobj = it.next();
    		View tr = inflater.inflate(R.layout.syncrow, null);
    		((TextView)tr.findViewById(R.id.details)).setText(tempobj.getArtist()+" - "+tempobj.toString());
    		((TextView) tr.findViewById(R.id.dl_status)).setText("Pending");
    		// set onclick listener for single download and add unique tag
    		tr.setTag(tempobj.toString());
    		tr.setOnClickListener(pendingrowlisten);
    		// add row to table
    		table.addView(tr);
    	}
    }
    private void genCompletedRows(ArrayList<SongListModel> rowlist){
    	final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(this.LAYOUT_INFLATER_SERVICE);
    	Iterator<SongListModel> it = rowlist.iterator();
    	while (it.hasNext()){
    		SongListModel tempobj = it.next();
    		View tr = inflater.inflate(R.layout.syncrow, null);
    		tr.setTag(tempobj.toString());
    		((TextView)tr.findViewById(R.id.details)).setText(tempobj.getAlbum()+" - "+tempobj.toString());
    		((TextView) tr.findViewById(R.id.dl_status)).setText("Completed");
    		// set progress to 100%
    		((TextView) tr.findViewById(R.id.dl_progtext)).setText("100%");
    		((ProgressBar) tr.findViewById(R.id.dl_progress)).setProgress(100);
    		table.addView(tr);
    	}
    }
    // download bar stuff
    private void setDownloadProgress(int progress){
    	if (progressbar!=null){
    		if (progress==100){
        		((TextView) currentdlrow.findViewById(R.id.dl_status)).setText("Completed!");
        	}
    		progressbar.setProgress(progress);
    		((TextView) currentdlrow.findViewById(R.id.dl_progtext)).setText(progress+"%");	
    	}
    }
    private void setDownloadBar(String songname){
    	if (currentdlrow!=null){
    		if (!songname.equals(currentdlrow.getTag())){
    			// set last download bar to 100% but only if its initialized and a different dl row
    			setDownloadProgress(100);
    		}
    	}
    	// set new progress bar, dlrow
    	currentdlrow = (TableRow) table.findViewWithTag(songname);
    	progressbar = (ProgressBar) currentdlrow.findViewById(R.id.dl_progress);
    	// update text of current row
    	((TextView) currentdlrow.findViewById(R.id.dl_status)).setText("Downloading");
    }
    // SERVICE STUFF
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DownloadService.MSG_SERVICE_STOPPED:
                showSyncButtons();
                String lastdlstat = (progressbar.getProgress()==100?"Completed!":"Failed!");
                ((TextView) currentdlrow.findViewById(R.id.dl_status)).setText(lastdlstat);
                // reset cancel button text 
                ((TextView) btnStop.findViewById(R.id.btnStoptxt)).setText("Stop");
                break;
            case DownloadService.MSG_SET_DL_BAR:
            	//Log.w("isyncmusic", "service message received: set download bar");
            	// move progress bar to different view
            	String viewtag = msg.getData().getString("str1");
            	setDownloadBar(viewtag);
            	break;
            case DownloadService.MSG_SET_PROGRESS:
            	//Log.w("isyncmusic", "service message received: set progress");
                // set the progress of the current download bar
            	int progress = msg.arg1;
            	setDownloadProgress(progress);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, DownloadService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private void restoreMe(Bundle state) {
        if (state!=null) {
            //textStatus.setText(state.getString("textStatus"));
            //textIntValue.setText(state.getString("textIntValue"));
            //textStrValue.setText(state.getString("textStrValue"));
        }
    }
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (DownloadService.isRunning()) {
            doBindService();
            // hide the sync buttons
    		hideSyncButtons();
        }
    }
    private void hideSyncButtons(){
    	btnStartSelect.setVisibility(LinearLayout.GONE);
    	btnStart.setVisibility(LinearLayout.GONE);
    	btnStop.setVisibility(LinearLayout.VISIBLE);
    }
    private void showSyncButtons(){
    	btnStartSelect.setVisibility(LinearLayout.VISIBLE);
    	btnStart.setVisibility(LinearLayout.VISIBLE);
    	btnStop.setVisibility(LinearLayout.GONE);
    	// also set current download text according to status
    	if (progressbar!=null){
    		if (progressbar.getProgress()!=100){
    			((TextView) currentdlrow.findViewById(R.id.dl_status)).setText("Pending");
    		}
    	}
    }
    private OnClickListener btnStartListener = new OnClickListener() {
        public void onClick(View v){
        	if (dllist.size()>0){
        		String[] sizeres = is3gOverLimit(dllist);
        		if (sizeres[0].equals("0")){
        			initDownload();
        		} else {
        			yesNoDialog("You are about to download "+global.convertBytes(Long.valueOf(sizeres[1]), true)+" over a 3G connection. This is over the recommended amount of 50Mb, proceed?", 1);
        		}
        	} else {
        		infoDialog("Nothing pending!", "There is no downloads pending, turn around and select some songs.");
        	}
        }
    };
    private OnClickListener btnStopListener = new OnClickListener() {
    	private boolean forcestop;
        public void onClick(View v){
            // service is terminated via a message according to user input
        	TextView btntxt = (TextView) v.findViewById(R.id.btnStoptxt);
        	if (btntxt.getText().equals("Force Stop")){
        		yesNoDialog("Are you sure you want to force cancel? Save some bandwidth by finishing the last download.", 0);
        	} else {
        		sendMessageToService(0);
        		Toast.makeText(getApplicationContext(), "Sync will complete after current download.", Toast.LENGTH_LONG).show();
        		btntxt.setText("Force Stop");
        	}
        }
       
    };
    // this dialog handles multiple tasks
    public void yesNoDialog(String query, final int action) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(SyncView.this);
    		builder.setMessage(query)
    				.setCancelable(true)
    				.setPositiveButton("Yes",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int id) {
    								if (action == 0){
    									// force cancel download; yes
    									sendMessageToService(1);
    									dialog.cancel();
    								} else {
    									// over download limit; user starts download
    									initDownload();
    								}
    							}
    						})
    				.setNegativeButton("No", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						if (action == 0){
    							// force cancel download; no
    							dialog.cancel();
    						} else {
    							// over download limit; don't download. Clear select list in case it has been set.
    							global.clearSelectDL();
    							dialog.cancel();
    						}
    					}
    				});
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    // single download
    private OnClickListener pendingrowlisten = new OnClickListener(){
    	SongListModel currentobj;
		public void onClick(View v) {
			// get the views unique id (filename) from clicked view
			String clickedsong = (String) v.getTag();
			// search dllist for object
			Iterator<SongListModel> it = dllist.iterator();
			while (it.hasNext()){
				currentobj = it.next();
				if (currentobj.toString().equals(clickedsong)){
					break;
				}
			}
			// pass object to dialog
			singleSongDialog(currentobj);
		}
    };
    // single download dialog
    public void singleSongDialog(SongListModel _file){
    	final SongListModel file = _file;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(_file.toString()+"\n"+_file.getArtist()+"\n"+_file.getAlbum()+" \nSize: "+global.convertBytes(Long.valueOf(_file.getSize()), true)).setCancelable(true)
		        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	singleSongDownload(file);
		                dialog.cancel();
		            }
		        })
		        .setNegativeButton("Stream", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	singleSongStream(file);
		                dialog.cancel();
		            }
		        });
		 AlertDialog alert = builder.create();
		 alert.show();
    }
    // stream download
    public void singleSongDownload(SongListModel file){
    	if (DownloadService.isRunning()){
    		infoDialog("Active Downloads", "Please wait until the current downloads are finished.");
    	} else {
    		// put in array list for service
    		ArrayList<SongListModel> selectdllist = new ArrayList<SongListModel>();
    		selectdllist.add(file);
    		// set single/selected download indicator/value in global object
    		global.setSelectDL(selectdllist);
    		// initiate download
    		initDownload();
    	}
    }
    // download single file/song
    public void singleSongStream(SongListModel file){
    	// get ip address and launch intent via media player; invoved via content type
    	String IPsocket = global.getIPAddress();
		Uri mediapath = Uri.parse("http://"+IPsocket+file.getRelPath().replaceAll(" ", "%20"));
		Intent playintent = new Intent();
		playintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		playintent.setAction(android.content.Intent.ACTION_VIEW);
		playintent.setDataAndType(mediapath, "audio/*");
		startActivity(playintent);
    }
    // selected downloads
    private OnClickListener btnDLSelectListener = new OnClickListener(){
    	SongListModel currentobj;
		public void onClick(View v) {
			// iterate through dllist, add values that checked in the list, use tag to locate checkbox
			Iterator<SongListModel> it = dllist.iterator();
			ArrayList<SongListModel> selectdllist = new ArrayList<SongListModel>();
			while (it.hasNext()){
				currentobj = it.next();
				// get the view from tag
				CheckBox currentcb = (CheckBox) table.findViewWithTag(currentobj.toString()).findViewById(R.id.synccb);
				if (currentcb.isChecked()){
					selectdllist.add(currentobj);
				}
			}
			if (selectdllist.isEmpty()){
				infoDialog("Your bad", "There is no downloads selected, either select some tracks or click Sync All");
			} else {
				// set single/selected download indicator/value in global object
        		global.setSelectDL(selectdllist);
        		// check download size
				String[] sizeres = is3gOverLimit(selectdllist);
        		if (sizeres[0].equals("0")){
        			initDownload();
        		} else {
        			yesNoDialog("You are about to download "+global.convertBytes(Long.valueOf(sizeres[1]), true)+" over a 3G connection. This is over the recommended amount of 50Mb, proceed?", 1);
        		}
			}
		}
    };
    // starts the download service. Used to initiate all types of downloads
    private void initDownload(){
    	// hide buttons
    	hideSyncButtons();
		// run service and bind
		startService(new Intent(SyncView.this, DownloadService.class));
		doBindService();
    }
    // is 3G download over limit
    public String[] is3gOverLimit(ArrayList<SongListModel> dllist){
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	if (mWifi.isConnected()) {
    		String[] result = {"0"};
    	    return result;
    	} else {
    		// get total dlsize
    		Long dlsize = Long.valueOf(0);
    		Iterator<SongListModel> it = dllist.iterator();
    		while (it.hasNext()){
    			dlsize+= Long.valueOf(it.next().getSize());
    		}
    		if (dlsize > Long.valueOf(52428800)){
    			String[] result = {"1", dlsize.toString()};
    			return result;
    		} else {
    			String[] result = {"0"};
    			return result;
    		}
    	}
    }
    // info dialog
    public void infoDialog(String _title, String _text){
    	final Dialog edialog = new Dialog(this);
	    edialog.setContentView(R.layout.infodialog);
	    edialog.setTitle(_title);
	    TextView infotxt = (TextView) edialog.findViewById(R.id.infotext);
	    infotxt.setText(_text);
	    Button infobtn = (Button) edialog.findViewById(R.id.btninfook);
	    infobtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				edialog.cancel();
           }
	    });
	    edialog.show();
    }
    private void sendMessageToService(int msgtype) {
    	Message msg = null;
        if (mIsBound) {
            if (mService != null) {
            	switch (msgtype){
            		case 0:
            			msg = Message.obtain(null, DownloadService.MSG_STOP_TASK, 0, 0);
            		break;
            		case 1:
            			msg = Message.obtain(null, DownloadService.MSG_FORCE_STOP, 0, 0);
            		break;
            	}
            	
                try {
                    //msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
    void doBindService() {
        bindService(new Intent(this, DownloadService.class), mConnection, 0);
        mIsBound = true;
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, DownloadService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }

}
