package com.example.speechtutor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

public class Playback extends Activity {
	
	List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);
		
		final ExpandableListView recordings;
		//final ListView recordings;
		recordings = (ExpandableListView)findViewById(R.id.recordingsList);
	    ArrayList<String> FilesInFolder = GetFiles("/sdcard/SpeechTutor");
	    //recordings = (ExpandableListView)findViewById(R.id.recordingsList);
	    
	    listDataHeader = FilesInFolder;
	    
	    
	    ExpandableListAdapter adapter= new ExpandableListAdapter(this, listDataHeader, listDataChild);
	    
	    recordings.setAdapter(adapter);
	 // Listview on child click listener
        recordings.setOnChildClickListener(new OnChildClickListener() {
 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                        listDataHeader.get(groupPosition)).get(
                                        childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
	    
	    /*recordings.setAdapter(new ArrayAdapter<String>(this,
	        android.R.layout.simple_list_item_1, FilesInFolder));*/

	    recordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	String fileName=(String)(recordings.getItemAtPosition(position));
	        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor");
	        fileName= mediaStorageDir.getPath() + File.separator + fileName;
	    	File file=  new File(Environment.getExternalStorageDirectory(),fileName);
	    	int musicLength = (int)(file.length()/2);
	          short[] music = new short[musicLength];
		        Toast.makeText(getApplicationContext(), "Playing sound from "+ fileName, Toast.LENGTH_SHORT).show();
	          try {
	            InputStream is = new FileInputStream(file);
	            BufferedInputStream bis = new BufferedInputStream(is);
	            DataInputStream dis = new DataInputStream(bis);
	            int i = 0;
	            while (dis.available() > 0) {
	              music[i] = dis.readShort();
	              i++;
	            }
	            dis.close();     

	            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 
	                                                   8000, 
	                                                   AudioFormat.CHANNEL_IN_MONO,
	                                                   AudioFormat.ENCODING_PCM_16BIT, 
	                                                   musicLength, 
	                                                   AudioTrack.MODE_STREAM);

	            audioTrack.play();
	            audioTrack.write(music, 0, musicLength);

	         } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	        	accordion.add("Delete");
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

}
