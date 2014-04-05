package com.example.speechtutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Record extends Activity {
	ToggleButton record;
	AudioRecord recorder= null;
	static final int SAMPLE_RATE = 8000;
	int bufferSize;
	byte[] buffer;
	boolean isRecording=false;
	boolean recordingInProgress=false;
	Thread recordingThread;
	String filePath;
	Chronometer chronometer = null;
	LinearLayout navBar = null;
	RelativeLayout finishRecordingBar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		
		//Set up Recording functionality
		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
	              AudioFormat.ENCODING_PCM_16BIT);
	     
	    buffer = new byte[bufferSize]; 
		
	    chronometer = (Chronometer) findViewById(R.id.chronometer);
	    navBar = (LinearLayout) findViewById(R.id.nav_bar);
	    finishRecordingBar = (RelativeLayout) findViewById(R.id.finish_recording_bar);
		
	    //Set up Record Button
		record = (ToggleButton) findViewById(R.id.record);
		record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if(isChecked){ //record
		        	recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
		  	              AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
		  	    				AudioFormat.ENCODING_PCM_16BIT)); 	
		        	recorder.startRecording();
		        	isRecording = true;
		        	if(!recordingInProgress) {
		        		chronometer.setBase(SystemClock.elapsedRealtime());
		        	}
		        	recordingInProgress=true;
		        	navBar.setVisibility(View.GONE);
		        	finishRecordingBar.setVisibility(View.VISIBLE);
		        	chronometer.start();
		            recordingThread = new Thread(new Runnable() {
		                public void run() {
		                    saveAudioDataToFile();
		                }

						
		            }, "AudioRecorder Thread");
		            recordingThread.start();
		        }else{ //stop
		        	 if (null != recorder) {
		        	        isRecording = false;
		        	        chronometer.stop();
		        	        recorder.stop();
		        	        recorder.release();
		        	        recorder = null;
		        	        recordingThread = null;
		        	    }
		        	
		        }
		    }
		});
	}
	private void saveAudioDataToFile() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("SpeechTutor", "failed to create directory");
                return;
            }
            else{
            	Log.i("SpeechTutor", "created speech directory");
            }
        }
		
		filePath=new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.pcm'").format(new Date());
		filePath= mediaStorageDir.getPath() + File.separator +   "PCM_"+filePath;
		//String filePath = "/sdcard/voice8K16bitmono.pcm";
	    short sData[] = new short[1024];

	    FileOutputStream os = null;
	    try {
	        os = new FileOutputStream(filePath);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }

	    while (isRecording) {
	        // gets the voice output from microphone to byte format

	        recorder.read(sData, 0, 1024);
	        System.out.println("Short writing to file" + sData.toString());
	        try {
	            // // writes the data to file from buffer
	            // // stores the voice buffer
	            byte bData[] = short2byte(sData);
	            os.write(bData, 0, 2048);
	            Log.d("Record", "Audio data actually saved");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    try {
	        os.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	private byte[] short2byte(short[] sData) {
	    int shortArrsize = sData.length;
	    byte[] bytes = new byte[shortArrsize * 2];
	    for (int i = 0; i < shortArrsize; i++) {
	        bytes[i * 2] = (byte) (sData[i] & 0x00FF);
	        bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
	        sData[i] = 0;
	    }
	    return bytes;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_record:
	            return true;
	        case R.id.action_playback:
	        	startActivity(new Intent(this, Playback.class));
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
		startActivity(intent);
	}
	
	public void cancelRecording(View view) {
        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recordingInProgress=false;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        chronometer.stop();
	}
	
	public void saveRecording(View view) {
		saveAudioDataToFile();
        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recordingInProgress=false;
        Toast.makeText(getApplicationContext(), "Audio Saved to "+ filePath,
                   Toast.LENGTH_SHORT).show();
	}

}
