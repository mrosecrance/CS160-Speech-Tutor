package com.example.speechtutor;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Playback extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);
		
		ListView recordings;
	    ArrayList<String> FilesInFolder = GetFiles("/sdcard/SpeechTutor");
	    recordings = (ListView)findViewById(R.id.recordingsList);

	    recordings.setAdapter(new ArrayAdapter<String>(this,
	        android.R.layout.simple_list_item_1, FilesInFolder));

	   /* lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            // Clicking on items
	         }
	    });*/
	}
	
	public ArrayList<String> GetFiles(String DirectoryPath) {
	    ArrayList<String> MyFiles = new ArrayList<String>();
	    File f = new File(DirectoryPath);

	    f.mkdirs();
	    File[] files = f.listFiles();
	    if (files.length == 0)
	        return null;
	    else {
	        for (int i=0; i<files.length; i++) 
	            MyFiles.add(files[i].getName());
	    }

	    return MyFiles;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.playback, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_record:
	        	startActivity(new Intent(this, Record.class));
	            return true;
	        case R.id.action_playback:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}
