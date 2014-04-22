package com.example.speechtutor;

import static edu.cmu.pocketsphinx.Assets.syncAssets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
//import android.speech.RecognitionListener;
//import android.speech.RecognizerIntent;
//import android.speech.SpeechRecognizer;
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

public class Record extends Activity implements
RecognitionListener {

	//SpeechRecognizerRecorder mSpeechRecognizer;
	CountDownTimer mtimer;
	//Button umFinderButton;
	int partialumCount;
	int umCount;
	boolean umBoolean = true; 
	ToggleButton recognizerButton;
	SpeechRecognizerRecorder recognizer;
	static final int SAMPLE_RATE = 44100;
	int bufferSize;
	byte[] buffer;
	boolean isRecording=false;
	boolean recordingInProgress=false;
	String filePath;
	public Chronometer chronometer;
	LinearLayout navBar = null;
	RelativeLayout finishRecordingBar = null;
	TextView umCounterDisplay;
    private String TAG = "record";
    
    
	private File appDir;

    private static final String KWS_SEARCH_NAME = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "chicken";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


		try {
			Log.d(TAG,"before trying to sync assets");
			appDir = syncAssets(getApplicationContext());
		} catch (IOException e) {
			throw new RuntimeException("failed to synchronize assets", e);
		}
		

		Log.d(TAG,"before recognizer instantiaiton");
		recognizer = SpeechRecognizerRecorderSetup.defaultSetup()
                .setAcousticModel(new File(appDir, "models/hmm/en-us-semi"))
                .setDictionary(new File(appDir, "models/lm/cmu07a.dic"))
                .setRawLogDir(appDir)
                .setKeywordThreshold(1e-5f)
                .setAudioStorageDirectory("SpeechTutor")
                .getRecognizer();
		Log.d(TAG,"after recognizer instantiaiton");

        recognizer.addListener(this);
        // Create keyword-activation search.
        recognizer.addKeywordSearch(KWS_SEARCH_NAME, KEYPHRASE);
        // Create grammar-based searches.
        File menuGrammar = new File(appDir, "models/grammar/menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
        File digitsGrammar = new File(appDir, "models/grammar/digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
        File languageModel = new File(appDir, "models/lm/weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);


        Log.d(TAG,"addlistener this");

        navBar = (LinearLayout) findViewById(R.id.nav_bar);
        finishRecordingBar = (RelativeLayout) findViewById(R.id.finish_recording_bar);
        
        Log.d(TAG,"before contentview set");

        chronometer = (Chronometer) findViewById(R.id.chronometer);

        umCounterDisplay = (TextView) findViewById(R.id.fillerword_counter);
        
        Log.d(TAG,"after contentview set");
        
        Log.d(TAG,"before recognizer button is created");

        //Set up Record Button
        recognizerButton = (ToggleButton) findViewById(R.id.record);
        recognizerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG,"before if isChecked");

        		if(isChecked){ //record
                    Log.d(TAG,"inside if isChecked");
                    switchSearch(KWS_SEARCH_NAME);
                    Log.d(TAG,"startListening for ums");

        			isRecording = true;
        			if(!recordingInProgress) {
        				System.out.println(chronometer);
        				chronometer.setBase(SystemClock.elapsedRealtime());
        			}
                    Log.d(TAG,"after settingBase ");

        			recordingInProgress=true;
        			navBar.setVisibility(View.GONE);
        			finishRecordingBar.setVisibility(View.VISIBLE);
        			chronometer.start();

        		}else{ //stop
                    Log.d(TAG,"Not isChecked");

        			if (null != recognizer) {
        				isRecording = false;
        				chronometer.stop();
        				recognizer.stop();
        				//		        	        recorder.release();
        			}

        		}
                Log.d(TAG,"completed onChecked Changed");

        	}
        });
        Log.d(TAG,"after recognizer button is created");


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
        //filePath isn't being created in this class... add a public method to SpeechRecognizerRecorder
        File recording = new File(filePath);
        recording.delete();
        Log.d("Record", "Deleted recording exists: "+recording.exists());
	}

	public void saveRecording(View view) {
//		saveAudioDataToFile();
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
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		String text = hypothesis.getHypstr();
        Log.d(getClass().getSimpleName(), "on partial: " + text);
        if (text.equals(KEYPHRASE)){
        	umCount++;
        	umCounterDisplay.setText(" "+(umCount));
            switchSearch(KWS_SEARCH_NAME);

        }
		
	}
    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }

	@Override
	public void onResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
//		String text = hypothesis.getHypstr();
//        Log.d(getClass().getSimpleName(), "on result: " + text);
//        if (text.equals(KEYPHRASE)){
//        	umCount++;
//        	umCounterDisplay.setText(" "+(umCount));
//            switchSearch(KWS_SEARCH_NAME);
//
//        }
		
	}
	
}
