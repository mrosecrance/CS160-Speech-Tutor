package com.example.speechtutor;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class Statistics extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.statistics, menu);
		return true;
	}
	
	public void navigate(View view) {
		Class classToStart = null;
		switch(view.getId()) {
		case R.id.nav_playback: 
			classToStart = Playback.class;
			break;
		case R.id.nav_record:
			classToStart = Record.class;
			break;
		case R.id.nav_statistics:
			classToStart = Statistics.class;
			break;
		case R.id.nav_settings:
			classToStart = Settings.class;
			break;
		}
		Intent intent = new Intent(this, classToStart);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

}
