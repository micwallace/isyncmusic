package com.isyncmusic;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ConfigActivity extends PreferenceActivity {
	OnSharedPreferenceChangeListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConfigActivity.this);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				if (key.equals("syncfreq")){
					String newvalue = prefs.getString("syncfreq", "0");
					if (newvalue.equals("0")){
						// cancel alarm
						cancelAlarm();
						Toast.makeText(getApplicationContext(), "Auto-Sync disabled", Toast.LENGTH_SHORT).show();
					} else {
						cancelAlarm();
						setRecurringAlarm(Integer.valueOf(newvalue));
						Toast.makeText(getApplicationContext(), "Auto-Sync Scheduled", Toast.LENGTH_SHORT).show();
					}
				} else if (key.equals("password")){
					
				}
			}
			private void setRecurringAlarm(int dayincrement) {
				Context context = getApplicationContext();
				// get current time for first sync
			    Calendar updateTime = Calendar.getInstance();
			    // update first run increment; according to user input TBC
			    //updateTime.roll(Calendar.DAY_OF_YEAR, dayincrement);
			    // TESTING
			    updateTime.roll(Calendar.MINUTE, 1);
			    // set pending intent
			    Intent autosync = new Intent(context, ScheduledSync.class);
			    PendingIntent pendingsync = PendingIntent.getBroadcast(context, 0, autosync, PendingIntent.FLAG_CANCEL_CURRENT);
			    AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			    alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), (AlarmManager.INTERVAL_FIFTEEN_MINUTES*dayincrement), pendingsync);
			    System.out.println("alarm scheduled");
			}
			private void cancelAlarm(){
				Context context = getApplicationContext();
				// setup matching intent
				Intent autosync = new Intent(context, ScheduledSync.class);
			    PendingIntent pendingsync = PendingIntent.getBroadcast(context, 0, autosync, PendingIntent.FLAG_CANCEL_CURRENT);
			    AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			    // stop alarm
			    alarms.cancel(pendingsync);
			}
		};
		
		prefs.registerOnSharedPreferenceChangeListener(listener);
		addPreferencesFromResource(R.layout.settingsview);
	}
	
	
}
