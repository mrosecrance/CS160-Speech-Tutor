package com.example.speechtutor;

import static edu.cmu.pocketsphinx.Assets.syncAssets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
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


public class Record extends Activity implements
RecognitionListener,OnSharedPreferenceChangeListener  {

	//SpeechRecognizerRecorder mSpeechRecognizer;
	CountDownTimer mtimer;
	//Button umFinderButton;
	int partialumCount;
	int fillerWordCount;
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
    private long lastPause;
    
    private File appDir;
    private int umCount;
    private int uhCount;
    private int erCount;
    private int ahCount;
    private int likeCount;
    private int youKnowCount;
    
    private static final String KWS_SEARCH_NAME = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "um";
    private static final Map<String, Boolean> FILLER_WORDS = new HashMap<String, Boolean>();

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        
        FILLER_WORDS.put("uh", sharedPrefs.getBoolean("detectUh", true));
        FILLER_WORDS.put("uhh", false);
        FILLER_WORDS.put("um", sharedPrefs.getBoolean("detectUm", true));
        FILLER_WORDS.put("umm", false);
        FILLER_WORDS.put("er", sharedPrefs.getBoolean("detectEr", true));
        FILLER_WORDS.put("err", false);
        FILLER_WORDS.put("ah", sharedPrefs.getBoolean("detectAh", true));
        FILLER_WORDS.put("you know",false);
        FILLER_WORDS.put("like", sharedPrefs.getBoolean("detectLike", false));

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
                .setKeywordThreshold(200)
                .setAudioStorageDirectory("SpeechTutor")
                .getRecognizer();
        
		Log.d(TAG,"after recognizer instantiaiton");
		
		filePath = recognizer.getAudioStorageFilePath();

        recognizer.addListener(this);
        // Create keyword-activation search.
        File fillers = new File(appDir, "models/grammar/menu.gram");
        recognizer.addKeywordSearch(KWS_SEARCH_NAME, fillers.getPath());
        // Create grammar-based searches.
        //File menuGrammar = new File(appDir, "models/grammar/menu.gram");
        //recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
        File digitsGrammar = new File(appDir, "models/grammar/digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
        //digitsGrammar.File languageModel = new File(appDir, "models/lm/weather.dmp");
        //recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);


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
        				filePath = recognizer.getAudioStorageFilePath();
        			}
                    Log.d(TAG,"inside if isChecked");
                    switchSearch(KWS_SEARCH_NAME);
                    Log.d(TAG,"startListening for ums");

        			isRecording = true;
        			if(!recordingInProgress) {
        				chronometer.setBase(SystemClock.elapsedRealtime());
        			}else{
        				chronometer.setBase(chronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
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
        				lastPause = SystemClock.elapsedRealtime();
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
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if(key.equals("detectUh")){
    		
    		 FILLER_WORDS.put("uh", sharedPreferences.getBoolean("detectUh", true));
    	}else if(key.equals("detectUm")){
    		FILLER_WORDS.put("um", sharedPreferences.getBoolean("detectUm", true));
    	}else if(key.equals("detectEr")){
    		FILLER_WORDS.put("er", sharedPreferences.getBoolean("detectEr", true));
    	}else if(key.equals("detectAh")){
    		FILLER_WORDS.put("ah", sharedPreferences.getBoolean("detectAh", true));
    	}else if(key.equals("detectLike")){
    		FILLER_WORDS.put("like", sharedPreferences.getBoolean("detectLike", false));
    	}else{
    		FILLER_WORDS.put("you know", false);
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
	
	public void cancelRecording(View view) {
        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recognizerButton.setChecked(false);
        recordingInProgress=false;
        fillerWordCount=0;
    	umCounterDisplay.setText(""+(fillerWordCount));
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        chronometer.stop();
        //filePath isn't being created in this class... add a public method to SpeechRecognizerRecorder
        File recording = new File(filePath);
        Log.w(TAG, "Trying to delete recording");
        recording.delete();
        Log.d("Record", "Deleted recording exists: "+recording.exists());
	}

	public void saveRecording(View view) {
//		saveAudioDataToFile();
        Log.d("Record", "SaveRecording method reached: ");

        navBar.setVisibility(View.VISIBLE);
        finishRecordingBar.setVisibility(View.GONE);
        recognizerButton.setChecked(false);
        recordingInProgress=false;
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Recording Name:");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        Editable value = input.getText();
        String whereToSaveFileName = value.toString()+".pcm";
	        File from = new File(filePath);
	        File to = new File(from.getParent(), value + ".pcm");
	        int count = 1;
	        String name = value.toString();
	        while(to.exists()){
	        	to = new File(from.getParent(),value + "(" + Integer.toString(count)+")"+".pcm");
	        	name = value.toString() + "(" + Integer.toString(count)+")";
	        	count++;
	        }
	        
	        from.renameTo(to);
	        Toast.makeText(getApplicationContext(), "Audio Saved to "+ to.getPath(),
	                   Toast.LENGTH_SHORT).show();
	
         	umCounterDisplay.setText(" "+(fillerWordCount));
         	String time = (String) chronometer.getText();
            saveRecordingData(name+".pcm",time, fillerWordCount, umCount, uhCount, erCount, ahCount, likeCount, youKnowCount);
            umCount = 0;
            uhCount = 0;
            erCount = 0;
            ahCount = 0;
            likeCount = 0;
            youKnowCount = 0; 
            fillerWordCount=0;
            umCounterDisplay.setText(" "+(fillerWordCount));
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.stop();
             
             
         }
        });
        AlertDialog.Builder saveAlert = new AlertDialog.Builder(this);
        
        saveAlert.setTitle("Would you like to name your recording?");

        saveAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
	        alert.show();
         }
        });
        saveAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Toast.makeText(getApplicationContext(), "Audio Saved to "+ filePath,
                        Toast.LENGTH_SHORT).show();
            	File from = new File(filePath);
            	Integer umCountToSave = fillerWordCount;
            	String time = (String) chronometer.getText();
            	saveRecordingData(from.getName(),time, fillerWordCount, umCount, uhCount, erCount, ahCount, likeCount, youKnowCount);
            	umCount = 0;
            	uhCount = 0;
            	erCount = 0;
            	ahCount = 0;
            	likeCount = 0;
            	youKnowCount = 0; 
            	fillerWordCount=0;
            	umCounterDisplay.setText(" "+(fillerWordCount));
            	chronometer.setBase(SystemClock.elapsedRealtime());
            	chronometer.start();
            	chronometer.stop();
            }
          });
         saveAlert.show();
        
        
        
        
	}
	
	  public void saveRecordingData(String recordingName, String time, int fillerWordCount1, int umCount1, int uhCount1, int erCount1, int ahCount1, int likeCount1, int youKnowCount1){
		    // Get fillerword data
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
		        //   i.printStackTrace();
		        }catch(ClassNotFoundException c)
		        {
		           c.printStackTrace();
		        }
		        //write fillerword data
		        if (recordingData == null) {
		         recordingData = new RecordingData();
		        }
		        recordingData.recordingFillerWordCount.put(recordingName, fillerWordCount);
		        recordingData.recordingTime.put(recordingName, time);
		        recordingData.recordingUmCount.put(recordingName, umCount1);
		        recordingData.recordingUhCount.put(recordingName, uhCount1);
		        recordingData.recordingErCount.put(recordingName, erCount1);
		        recordingData.recordingAhCount.put(recordingName, ahCount1);
		        recordingData.recordingLikeCount.put(recordingName, likeCount1);
		        recordingData.recordingYouKnowCount.put(recordingName, youKnowCount1);
		        
		        try
		        {
		            File hiddenStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor/.storage");
		          if (! hiddenStorageDir.exists()){
		              if (! hiddenStorageDir.mkdirs()){
		                  Log.d("SpeechTutor", "failed to create directory");
		              }
		          }                 
		           FileOutputStream fileOut =
		           new FileOutputStream(hiddenStorageDir.getPath() + "/SpeechTutorData.ser", false);
		           ObjectOutputStream out = new ObjectOutputStream(fileOut);
		           out.writeObject(recordingData);
		           out.close();
		           fileOut.close();
		        }catch(IOException i)
		        {
		            i.printStackTrace();
		        }
		        
		        Log.d("Record", "Number of Filler words saved"+fillerWordCount);
		        Log.d("Record", "Number of umCount words saved"+umCount);
		        Log.d("Record", "Number of uhCount words saved"+uhCount);
		        Log.d("Record", "Number of erCount words saved"+erCount);
		        Log.d("Record", "Number of ahCount words saved"+ahCount);
		        Log.d("Record", "Number of likeCount words saved"+likeCount);
		        Log.d("Record", "Number of youKnowCount words saved"+youKnowCount);


		  }
	  
	  private void individualFillerWordUpdate(String text) {
		    
		    if (text.toLowerCase().equals("um")) {
		      umCount++;  
		    } 
		  
		    else if (text.toLowerCase().equals("uh")) {
		      uhCount++;
		    }
		    
		    else if (text.toLowerCase().equals("er")) {
		      erCount++;
		    }
		    
		    else if (text.toLowerCase().equals("ah")) {
		      ahCount++;
		    }
		    
		    else if (text.toLowerCase().equals("like")) {
		      likeCount++;
		    }
		    
		    else if (text.toLowerCase().equals("youKnow")) {
		      youKnowCount++;
		    }
		  
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
		String[] splitText = hypothesis.getHypstr().split(" ");
		String text = splitText[splitText.length-1];
        Log.d(getClass().getSimpleName(), "on partial: " + text);
        Log.d(getClass().getSimpleName(), "on partial: " + FILLER_WORDS.get(text));
        if (FILLER_WORDS.containsKey(text) && FILLER_WORDS.get(text)) {
        	Log.d(TAG, "Match: "+text);
        	fillerWordCount++;
            individualFillerWordUpdate(text);
        	umCounterDisplay.setText(" "+(fillerWordCount));
        } else {
        	Log.d(TAG, "Not match: "+text);
        }
        switchSearch(KWS_SEARCH_NAME);
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
