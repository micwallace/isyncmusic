package com.isyncmusic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class InitSetup extends Activity {
	private PublicResources global;
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsedit;
	ProgressDialog loader;
	Dialog autodialog;
    private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    public boolean validateIP(String _ip){
    	Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
    	Matcher matcher = pattern.matcher(_ip);
  	  	return matcher.matches();	 
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   // setup global variable objects
       global = ((PublicResources)getApplicationContext());
       prefs = PreferenceManager.getDefaultSharedPreferences(InitSetup.this);
	   prefsedit = prefs.edit();
	   loader = new ProgressDialog(InitSetup.this);
	   autodialog = new Dialog(this);
	   // welcome dialog
	   welcomeDialog();
	}
	public void welcomeDialog(){
		final Dialog dialog = new Dialog(this);
		   dialog.setCancelable(false);
		   dialog.setContentView(R.layout.welcomedialog);
		   dialog.setTitle("Welcome");
		   dialog.show();
		   /*ImageView image = (ImageView) dialog.findViewById(R.id.image);
		   image.setImageResource(R.drawable-ldpi.android);*/
		   Button nextbtn = (Button) dialog.findViewById(R.id.btnnext);
		   nextbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
					modeDialog();
	           }
	       });
	}
	// setup type/mode dialog
	public void modeDialog(){
		final Dialog dialog = new Dialog(this);
		   dialog.setCancelable(false);
		   dialog.setContentView(R.layout.modedialog);
		   dialog.setTitle("Setup mode");
		   dialog.show();
		   Button modenextbtn = (Button) dialog.findViewById(R.id.btnmodenext);
		   Button modebackbtn = (Button) dialog.findViewById(R.id.btnmodeback);
		   modebackbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
					welcomeDialog();
				}
		   });
		   modenextbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					RadioButton autocb = (RadioButton) dialog.findViewById(R.id.autosetup);
					if (autocb.isChecked()){
						// change dialog
						dialog.dismiss();
						autoSetup();
					} else {
						// update preferences
						prefsedit.putBoolean("webservice", false);
						// change dialog
						dialog.dismiss();
						manualSetup();
					}
	           }
	       });
	}
	// setup finished dialog
	public void finishDialog(){
		final Dialog dialog = new Dialog(this);
		   dialog.setCancelable(false);
		   dialog.setContentView(R.layout.finishdialog);
		   dialog.setTitle("Setup Complete");
		   dialog.show();
		   Button nextbtn = (Button) dialog.findViewById(R.id.btnfinishsetup);
		   nextbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// finish the setup utility and return to main screen
					dialog.cancel();
					InitSetup.this.finish();
				}
		   });
	}
	// AUTOMATIC SETUP AND ASSOCIATED DIALOGS
	public void autoSetup(){
		   autodialog.setCancelable(false);
		   autodialog.setContentView(R.layout.accountdialog);
		   autodialog.setTitle("Username and password");
		   autodialog.show();
		   Button autonextbtn = (Button) autodialog.findViewById(R.id.btnaccnnext);
		   Button autobackbtn = (Button) autodialog.findViewById(R.id.btnaccnback);
		   autobackbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					autodialog.cancel();
					modeDialog();
				}
		   });
		   autonextbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String email = ((TextView) autodialog.findViewById(R.id.email)).getText().toString();
					String password = ((TextView) autodialog.findViewById(R.id.password)).getText().toString();
					prefsedit.putString("username", email);
					prefsedit.putString("password", password);
					prefsedit.commit();
					// run account check
					checkAccn();
				}
		   });
	}
	// acount setup process/check
	public void checkAccn(){
		// show loader
		showProgressDialog();
		// run async task
		AsyncTask<SharedPreferences, Void, String> checktask = new CheckTask();
		checktask.execute(prefs);
	}
	private class CheckTask extends AsyncTask<SharedPreferences, Void, String> {
		    	@Override
		    	protected String doInBackground(SharedPreferences... _prefs) {
		    		// check account connectivity
		    		WebServiceUpdate ipupdate = new WebServiceUpdate(_prefs[0]);
		    		String accnresult = ipupdate.run();
		    		
		    		return accnresult;
		    	}
		    	@Override
		    	protected void onPostExecute(String _result) {
		    		processResult(_result);
		        }
		 }
	public void processResult(String accnresult){
		// stop loader
		closeProgressDialog();
		// process result
		if (accnresult.equals("1")){
			// show final dialog
			prefsedit.putBoolean("webservice", true).commit();
			autodialog.cancel();
			finishDialog();
		} else {
			// show error dialog with string from server
			((TextView)autodialog.findViewById(R.id.errortext)).setText(accnresult);
			((LinearLayout) autodialog.findViewById(R.id.errorlayout)).setVisibility(View.VISIBLE);
		}
	}
	// loader
	private void showProgressDialog() { 
	    loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    loader.setMessage("Checking account...");
	    loader.show();
	}
	private void closeProgressDialog() {
	    if(loader != null)
	        loader.dismiss();
	}
	// MANUAL SETUP AND ASSOCIATED DIALOGS
	public void manualSetup(){
		final Dialog dialog = new Dialog(this);
		   dialog.setCancelable(false);
		   dialog.setContentView(R.layout.ipdialog);
		   dialog.setTitle("Server Details");
		   dialog.show();
		   Button mannextbtn = (Button) dialog.findViewById(R.id.btnipnext);
		   Button manbackbtn = (Button) dialog.findViewById(R.id.btnipback);
		   manbackbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
					modeDialog();
				}
		   });
		   mannextbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					TextView intipview = (TextView) dialog.findViewById(R.id.internalip);
					TextView extipview = (TextView) dialog.findViewById(R.id.externalip);
					String intip = intipview.getText().toString();
					String extip = extipview.getText().toString();
					if (validateIP(intip) || validateIP(extip)){
						// insert IP info into config
						prefsedit.putString("internalip", validateIP(intip)?intip:"");
						prefsedit.putString("externalip", validateIP(extip)?extip:"");
						prefsedit.commit();
						// select current IP
						//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InitSetup.this);
			        	//SetCurrentIP ippick = new SetCurrentIP();
			        	//String currentip = ippick.run(prefs.getString("internalip", "0.0.0.0"), prefs.getString("externalip", "0.0.0.0"), prefs.getString("serverport", "8080"));
			        	// update in global resources
			        	//global.setIPAddress(currentip);
						// return to main screen
						dialog.cancel();
						finishDialog();
					} else {
						Toast.makeText(InitSetup.this, "Please enter 1 valid IP", Toast.LENGTH_SHORT).show();
					}
	           }
	       });
	}
}
