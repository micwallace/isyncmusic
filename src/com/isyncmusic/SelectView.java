package com.isyncmusic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SelectView extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		String finaltxt = getIntent().getExtras().get("finaltxt").toString();
		setContentView(R.layout.selectionview);
		TextView text = (TextView) this.findViewById(R.id.selecttext);
		text.setText(finaltxt);
		Button btnok = (Button) this.findViewById(R.id.btnOk);
		btnok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SelectView.this.finish();
            }
        });
	}
}
