package com.example.speechtutor;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordingData implements java.io.Serializable {
	public HashMap<String, Integer> recordingUmsCount;
	public HashMap<String, Integer> recordingUhsCount;
	public HashMap<String, Integer> recordingFillerWordCount;
	public HashMap<String, String> recordingTime; //the length of recording
	public RecordingData() {
		recordingFillerWordCount = new HashMap<String, Integer>();
		recordingTime = new HashMap<String,String >();
	}

}
