package com.example.speechtutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Record extends Activity implements RecognitionListener {
	
	SpeechRecognizer mSpeechRecognizer;
	CountDownTimer mtimer;
	Button umFinderButton;
	int partialumCount;
	int umCount;
	boolean umBoolean = true; 
	ToggleButton record;
	AudioRecord recorder= null;
	static final int SAMPLE_RATE = 44100;
	int bufferSize;
	byte[] buffer;
	boolean isRecording=false;
	boolean recordingInProgress=false;
	Thread recordingThread;
	String filePath;
	Chronometer chronometer = null;
	LinearLayout navBar = null;
	RelativeLayout finishRecordingBar = null;
	TextView umCounterDisplay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		
	    //umCounterDisplay = (TextView) findViewById(R.id.textView1);
	   // umCounterDisplay.setText(0);
		// set up Speech to Text Recognizer 
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(this);
		
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
		
		// Set up Um Button
		umFinderButton = (Button) findViewById(R.id.umbutton);

		umFinderButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (umBoolean) {
					
					Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
					i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
					i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.speechtotext");
					i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(5000));
					mSpeechRecognizer.startListening(i);
					umFinderButton.setText("Stop um finder");
					umBoolean = false;
				}
				else {
					mSpeechRecognizer.stopListening();
					umCount = 0;
					umBoolean = true;
					umFinderButton.setText("Count your ums!");
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
	
	public void cancelRecording(View view) {
        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recordingInProgress=false;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        chronometer.stop();
        File recording = new File(filePath);
        recording.delete();
        Log.d("Record", "Deleted recording exists: "+recording.exists());
	}
	
	public void saveRecording(View view) {
		saveAudioDataToFile();
        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recordingInProgress=false;
        Toast.makeText(getApplicationContext(), "Audio Saved to "+ filePath,
                   Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBufferReceived(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		System.out.println("SUP");
		/*if(!umBoolean){
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
			i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
			i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.speechtotext");
			i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(5000000));
			mSpeechRecognizer.startListening(i);
			System.out.println("restarting");
			//umFinderButton.setText("Stop um finder");
		}*/
		
	}
	@Override
	public void onError(int arg0) {
		// TODO Auto-generated method stub
		if(!umBoolean){
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
			i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
			i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.speechtotext");
			i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(5000));
			mSpeechRecognizer.startListening(i);
			System.out.println("restarting cause error and idk why");
			//umFinderButton.setText("Stop um finder");
		}
		
	}
	@Override
	public void onEvent(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPartialResults(Bundle arg0) {
		// TODO Auto-generated method stub
		umCounterDisplay = (TextView) findViewById(R.id.textView1);
		partialumCount = 0;
	    ArrayList<String> speechResults = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (speechResults.size() > 0) {
			
			
			String str = speechResults.get(0);
		    Pattern p = Pattern.compile("um");
		    Matcher m = p.matcher(str);
		    
		    while (m.find()){
		    	partialumCount += 1;
		    }
		   umCounterDisplay.setText(" "+(umCount+partialumCount));

			System.out.println("what do you know: " + speechResults.get(0));
		} else {
			System.out.println("what do you know!!!!!!!!!!");

		}
		//umCount = 0;
		
		
	}
	@Override
	public void onReadyForSpeech(Bundle arg0) {
		// TODO Auto-generated method stub
		if(mtimer != null) {
            mtimer.cancel();
        }
	}
	

	
	@Override
	public void onResults(Bundle arg0) {
		// TODO Auto-generated method stub
		if(mtimer != null){
            mtimer.cancel();
        }
		partialumCount = 0;
		ArrayList<String> speechResults = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (speechResults.size() > 0) {
			
			
			String str = speechResults.get(0);
		    Pattern p = Pattern.compile("um");
		    Matcher m = p.matcher(str);
		    
		    while (m.find()){
		    	partialumCount += 1;
		    }
		   umCounterDisplay.setText(" "+(umCount+partialumCount));
		   umCount = umCount+partialumCount;
			System.out.println("what do you know: " + speechResults.get(0));
		} else {
			System.out.println("what do you know!!!!!!!!!!");

		}
		if(!umBoolean){
			final Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
			i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
			i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.speechtotext");
			i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(5000000));
			mSpeechRecognizer.startListening(i);
			if(mtimer == null) {
                mtimer = new CountDownTimer(2000, 500) {
                    @Override
                    public void onTick(long l) {
                    }

                    @Override
                    public void onFinish() {
                       
                        mSpeechRecognizer.cancel();
                        mSpeechRecognizer.startListening(i);
                    }
                };
            }
            mtimer.start();
			System.out.println("restarting");
			//umFinderButton.setText("Stop um finder");
		}

		
	}
	@Override
	public void onRmsChanged(float arg0) {
		// TODO Auto-generated method stub
		
	}

}
