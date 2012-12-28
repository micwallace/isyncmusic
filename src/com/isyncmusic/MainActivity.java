package com.isyncmusic;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	protected static final Context MainActivity = null;
	public PublicResources global;
	private SharedPreferences prefs;
	private boolean ctrunning = true;
	private boolean uncachedidx = false;

	// to stop activity firing onCreate on screen rotation
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// re-render view
		setContentView(R.layout.main);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// bring up main layout
		setContentView(R.layout.main);
		// setup global variable object
		global = ((PublicResources) getApplicationContext());
		// temp write music dir
		File musicdir = new File(Environment.getExternalStorageDirectory()
				+ "/Music/isyncedmusic/");
		if (!musicdir.exists()) {
			musicdir.mkdirs();
			Log.w("isyncmusic", "Created music dir");
		}
		// check for previous config
		prefs = global.getPrefs();
		if (prefs.getString("externalip", "").equals("")) {
			// config not exist; load default values and start config dialog
			PreferenceManager.setDefaultValues(this, R.layout.settingsview,
					false);
			Intent configint = new Intent(MainActivity.this, InitSetup.class);
			startActivity(configint);
		} else {
			// run startup task; selects current ip and updates socket
			StartupTask starttask = new StartupTask();
			starttask.execute(global);
			// add onclick listener to connection bar
			this.findViewById(R.id.conbar).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					checkConnection();
				}});
		}
	}

	private class StartupTask extends AsyncTask<PublicResources, Void, String> {
		@Override
		protected String doInBackground(PublicResources... _global) {
			PublicResources global = _global[0];
			SharedPreferences prefs = global.getPrefs();
			SetCurrentIP ippick = new SetCurrentIP();
			String currentip = ippick.run(
					prefs.getString("internalip", "0.0.0.0"),
					prefs.getString("externalip", "0.0.0.0"),
					prefs.getString("serverport", "0.0.0.0"));
			// if result is 0.0.0.0 update ip's if web service is enabled,
			// otherwise just set dummy ip (intip) and return
			if (prefs.getBoolean("webservice", false) && currentip.equals("0.0.0.0")) {
				runOnUiThread(new Runnable() {
					public void run() {
						updateStatusText("Updating IP addresses from webservice");
					}
				});
				WebServiceUpdate wsupdate = new WebServiceUpdate(prefs);
				// if webservice fails set dummy ip (internal IP)
				if (!wsupdate.run().equals("1")) {
					global.setIPAddress(prefs
							.getString("internalip", "0.0.0.0")
							+ ":"
							+ prefs.getString("port", "8080"));
					return "0";
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							updateStatusText("Checking server connection..");
						}
					});
					// webservice success, rerun ip picker
					currentip = ippick.run(
							prefs.getString("internalip", "0.0.0.0"),
							prefs.getString("externalip", "0.0.0.0"),
							prefs.getString("serverport", "0.0.0.0"));
				}
			}
			// update in global resources
			global.setIPAddress(currentip.equals("0.0.0.0") ? prefs.getString(
					"internalip", "0.0.0.0")
					+ ":"
					+ prefs.getString("port", "8080") : currentip);
			// delay the return by two second for fast connections, so the user can see its "checking server connection..."
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return currentip;
		}

		protected void onPostExecute(String _result) {
			updateServerStatus(_result);
		}
	}

	public void updateStatusText(String _status) {
		TextView statustxt = (TextView) this.findViewById(R.id.constat);
		statustxt.setText(_status);
	}

	public void updateServerStatus(String _result) {
		TextView statustxt = (TextView) this.findViewById(R.id.constat);
		ImageView statusicon = (ImageView) this.findViewById(R.id.conicon);
		// hide loader
		this.findViewById(R.id.conloader).setVisibility(View.GONE);
		if (_result.equals("0")) {
			statustxt.setText("The Webservice failed to update IPs");
			statusicon.setImageResource(R.drawable.stoppedicon);
		} else if (_result.equals("0.0.0.0")) {
			statustxt.setText("Could not connect to your computer");
			statusicon.setImageResource(R.drawable.stoppedicon);
		} else if (_result.contains(prefs.getString("externalip", ""))) {
			statustxt.setText("Connected via Internet");
			statusicon.setImageResource(R.drawable.okicon);
		} else {
			statustxt.setText("Connected via Local network");
			statusicon.setImageResource(R.drawable.okicon);
		}
		statusicon.setVisibility(View.VISIBLE);
		// make button "clickable" again
		ctrunning = false;
	}

	public void startStreamer(View _view) {
		// initiated by static xml button link
		// ask to download new index if it has not been read and exists
		File ismsindex = new File(
				"/data/data/com.isyncmusic/files/ismsindex.json");
		if (ismsindex.exists()) {
			if (global.getReadIndex().isIndexRead()) {
				initUI(false, 1);
			} else {
				yesNoDialog("Download a new index from the server?", 1);
			}
		} else {
			initUI(true, 1);
		}
	}

	public void startSyncSet(View _view) {
		File ismsindex = new File(
				"/data/data/com.isyncmusic/files/ismsindex.json");
		if (ismsindex.exists()) {
			if (global.getReadIndex().isIndexRead()) {
				initUI(false, 2);
			} else {
				yesNoDialog("Download a new index from the server?", 2);
			}
		} else {
			initUI(true, 2);
		}
	}

	public void yesNoDialog(String query, final int action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(query)
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (action == 1) {
									initUI(true, 1);
								} else {
									initUI(true, 2);
								}
								dialog.cancel();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (action == 1) {
							initUI(false, 1);
						} else {
							initUI(false, 2);
						}
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void infoDialog(String _title, String _text) {
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

	public void initSyncUI(View _view){
		Intent syncintent = new Intent(MainActivity.this, SyncView.class);
		startActivity(syncintent);
    }

	public void showSettings(View _view) {
		Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
		startActivity(intent);
	}
	// runs another startuptask to determine correct IP socket & check connection
	public void checkConnection(){
		if (!ctrunning){
			ctrunning = true;
			// hide stat image and show the loader
			this.findViewById(R.id.conicon).setVisibility(View.GONE);
			this.findViewById(R.id.conloader).setVisibility(View.VISIBLE);
			((TextView) this.findViewById(R.id.constat)).setText("Checking Server Connection...");
			// run startup task; selects current ip and updates socket
			StartupTask starttask = new StartupTask();
			starttask.execute(global);
		}
	}
	// runs initnewindexUI to open SyncView
	public void getNewSongs(View _view){
		initUI(true , 3);
	}
	// (download new index if specified and then collate with users selection) and gather the required data (depending on op) and open the specified UI
	public void initUI(final boolean download , final int UIint){
		// initiate loading dialog
				final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,
						"", (download ? "Downloading Index. Please wait..."
								: "Processing Index..."), true);
				Thread t = new Thread() {
					public void run() {
						Looper.prepare();
						boolean dlresult;
						// Download index from server if download = true
						if (download) {
							String IPsocket = global.getIPAddress();
							if (IPsocket.equals("0")) {
								while (IPsocket.equals("0")) {
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									IPsocket = global.getIPAddress();
									Log.w("isyncmusic", "Calculating IP");
								}
							}
							downloadIndex dlindex = new downloadIndex();
							dlresult = dlindex.DownloadFromUrl("http://" + IPsocket
									+ "/iasindex.json", "ismsindex.json");
						} else {
							dlresult = true;
						}
						// read index and get array
						if (dlresult) {
							// dialog control; update text
							if (download) {
								runOnUiThread(new Runnable() {
									public void run() {
										dialog.setMessage("Processing Index...");
									}
								});
							}
							// read index
							if (download || !global.getReadIndex().isIndexRead()) {
								global.getReadIndex().readIndex();
							}
							// if new index is downloaded, collate into selection list
							if (download) {
								global.getSelectList().collateWithNewIndex();
							}
							// Get data according to target UI
							switch(UIint){
							case(1): case(2):
								ArrayList<String> artistlist = global.getReadIndex().getallartists();
								// add all to if target is streamer UI
								if (UIint == 1){
									artistlist.add(0, "All");
								}
								// pass array to global resource var used by list creator
								global.setListArray(artistlist);
								break;
							case(3):
								
							}
							// dialog control
							runOnUiThread(new Runnable() {
								public void run() {
									dialog.dismiss();
								}
							});
							// Open activity according to target UI
							Intent intent = null;
							switch(UIint){
							case(1):
								// open stream view
								intent = new Intent(MainActivity.this, ListViewFromArray.class);
								break;
							case(2):
								// open select view
								intent = new Intent(MainActivity.this, SelectionListView.class);
								intent.putExtra("inputtype", "strings");
								intent.putExtra("stage", 1);
								break;
							case(3):
								// open sync view
								intent = new Intent(MainActivity.this, SyncView.class);	
							}
							startActivity(intent);
							// e--
							// start caching task if accessing select or stream lists
							if (UIint == 1 || UIint == 2){
								if (download || (global.getTaskstatus() == false || uncachedidx )) {
								CacheIndexTask task = (CacheIndexTask) new CacheIndexTask()
										.execute(global);
								uncachedidx = false;
								}
							} else if (download) {
								uncachedidx = true;
							}
						} else {
							// dialog control
							runOnUiThread(new Runnable() {
								public void run() {
									dialog.dismiss();
									infoDialog("Server timeout", "The connection to the server failed");
								}
							});
						}

					}
				};
				t.start();
	}
}