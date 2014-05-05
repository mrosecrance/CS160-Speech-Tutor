package com.example.speechtutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class Statistics extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);
		
		ArrayList<String> FilesInFolder = GetFiles(Environment.getExternalStorageDirectory()+"/SpeechTutor");
		
		RecordingData recordingData = null;
        try
        {
        	File hiddenStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor/.storage");
	        if (! hiddenStorageDir.exists()){
	            if (! hiddenStorageDir.mkdirs()){
	                Log.d("SpeechTutor", "failed to create directory");
	            }
	        }
           FileInputStream fileIn = new FileInputStream(hiddenStorageDir.getPath() + "/SpeechTutorData.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           recordingData = (RecordingData) in.readObject();
           in.close();
           fileIn.close();
        }catch(IOException i)
        {
           i.printStackTrace();
        }catch(ClassNotFoundException c)
        {
           System.out.println("RecordingData class not found");
           c.printStackTrace();
        }
		
        ArrayList<String> sortedFiles = sortByTime(FilesInFolder,recordingData);
        
		GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
			    new GraphViewData(1, 2.0d)
			    , new GraphViewData(2, 1.5d)
			    , new GraphViewData(3, 2.5d)
			    , new GraphViewData(4, 1.0d)
			});
			 
			GraphView graphView = new LineGraphView(
			    this // context
			    , "GraphViewDemo" // heading
			);
			graphView.addSeries(exampleSeries); // data
			 
			LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
			layout.addView(graphView);
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
	        	String extension = files[i].getName().substring(files[i].getName().indexOf("."));
	        	if(extension.equals(".pcm")){
			        MyFiles.add(files[i].getName());
	        	}
	        }
	    }

	    return MyFiles;
	}
	
	public ArrayList<String> sortByTime(ArrayList<String> files, RecordingData recordingData){
		
		return files;
		
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
