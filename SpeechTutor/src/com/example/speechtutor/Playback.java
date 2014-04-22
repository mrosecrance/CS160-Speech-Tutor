package com.example.speechtutor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class Playback extends Activity {
	
	List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
    ExpandableListView recordings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);
		recordings = (ExpandableListView)findViewById(R.id.recordingsList);
	    ArrayList<String> FilesInFolder = GetFiles(Environment.getExternalStorageDirectory()+"/SpeechTutor");
	    if(FilesInFolder == null){
	    	FilesInFolder = new ArrayList<String>();
	    }
	    recordings.setEmptyView(findViewById(R.id.empty));
		listDataHeader = FilesInFolder;
		
		    
		ExpandableListAdapter adapter= new ExpandableListAdapter(this, listDataHeader, listDataChild, recordings);
		    
		recordings.setAdapter(adapter);
		
		recordings.setOnGroupExpandListener(new OnGroupExpandListener() {
		    int previousItem = -1;

		    @Override
		    public void onGroupExpand(int groupPosition) {
		        if(groupPosition != previousItem )
		            recordings.collapseGroup(previousItem );
		        previousItem = groupPosition;
		        
		    }
		});
		 
	    
	}
	
	public ArrayList<String> GetFiles(String DirectoryPath) {
	    ArrayList<String> MyFiles = new ArrayList<String>();
	    File f = new File(DirectoryPath);

	    f.mkdirs();
	    File[] files = f.listFiles();
	    if (files.length == 0)
	        return null;
	    else {
	        for (int i=0; i<files.length; i++) {
	            MyFiles.add(files[i].getName());
	        	List<String> accordion = new ArrayList<String>();
	        	accordion.add(" ");
	        	listDataChild.put(files[i].getName(),accordion);
	        }
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

	public void navigate(View view) {
		Class classToStart = null;
		switch(view.getId()) {
		case R.id.nav_playback: 
			classToStart = Playback.class;
			break;
		case R.id.nav_record:
			classToStart = Record.class;
			break;
		}
		Intent intent = new Intent(this, classToStart);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}


}
