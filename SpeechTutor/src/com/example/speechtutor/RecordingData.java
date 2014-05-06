package com.example.speechtutor;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordingData implements java.io.Serializable {
	
	public HashMap<String, Integer> recordingUmCount;
	public HashMap<String, Integer> recordingUhCount;
	public HashMap<String, Integer> recordingErCount;
	public HashMap<String, Integer> recordingAhCount;
	public HashMap<String, Integer> recordingLikeCount;
	public HashMap<String, Integer> recordingYouKnowCount;
	public HashMap<String, Integer> recordingFillerWordCount;
	public HashMap<String, String> recordingTime; //the length of recording
	
	public RecordingData() {
		
		recordingFillerWordCount = new HashMap<String, Integer>();
		recordingTime = new HashMap<String,String >();
		recordingUmCount = new HashMap<String, Integer>();
		recordingUhCount = new HashMap<String, Integer>();
		recordingErCount = new HashMap<String, Integer>();
		recordingAhCount = new HashMap<String, Integer>();
		recordingLikeCount = new HashMap<String, Integer>();
		recordingYouKnowCount = new HashMap<String, Integer>();
		
	}

}
