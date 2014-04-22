package com.example.speechtutor;

import static edu.cmu.pocketsphinx.Assets.syncAssets;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
//import android.speech.RecognitionListener;
//import android.speech.RecognizerIntent;
//import android.speech.SpeechRecognizer;

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
    private static final String KEYPHRASE = "um";

	
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
		
		filePath = recognizer.getAudioStorageFilePath();

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
        			if(recordingInProgress == false) {
        				recognizer.setAudioStorageFile();
        			}
                    Log.d(TAG,"inside if isChecked");
                    switchSearch(DIGITS_SEARCH);
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
       
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        
        alert.setTitle("Enter Recording Name");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        Editable value = input.getText();
	        File from = new File(filePath);
	        File to = new File(from.getParent(), value + ".pcm");
	        from.renameTo(to);
	        Toast.makeText(getApplicationContext(), "Audio Saved to "+ to.getPath(),
	                   Toast.LENGTH_SHORT).show();
         }
        });

         alert.show();
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
        	Log.d(TAG, "Match: "+text);
        	umCount++;
        	umCounterDisplay.setText(" "+(umCount));
        } else {
        	Log.d(TAG, "Not match: "+text);
        }
        switchSearch(DIGITS_SEARCH);
	}
	
    private void switchSearch(String searchName) {
        recognizer.cancel();
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
