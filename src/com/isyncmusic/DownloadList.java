package com.isyncmusic;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DownloadList extends Activity {
	private String listtxt = "Items pending download:\r\n\r\n";
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		PublicResources global = ((PublicResources)getApplicationContext());
		final ArrayList<SongListModel> dllist = global.getSelectList().getDownloadList();
		setContentView(R.layout.selectionview);
		final TextView text = (TextView) this.findViewById(R.id.selecttext);
		text.setText(listtxt);
		// initiate loading dialog
		
		final ProgressDialog dialog = ProgressDialog.show(DownloadList.this, "", ("Processing data..."), true);
		Thread t = new Thread() {
				public void run() {
					// print out download items
					Iterator it = dllist.iterator();
					while (it.hasNext()){
						SongListModel tempobj = (SongListModel) it.next();
						listtxt+="\u00A0\u00A0\u00A0\u00A0\u00A0"+tempobj.getArtist()+" - "+tempobj.toString()+" \r\n";
						
					}
					runOnUiThread(new Runnable() {
							public void run() {
								text.setText(listtxt);
								dialog.dismiss();
							}
					});
				}
		};
		t.start();
		
		Button btnok = (Button) this.findViewById(R.id.btnOk);
		btnok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DownloadList.this.finish();
            }
        });
	}
}
