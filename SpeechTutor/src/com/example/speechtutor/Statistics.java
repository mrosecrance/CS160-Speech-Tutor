package com.example.speechtutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

public class Statistics extends Activity implements OnItemSelectedListener {
	private GraphView currentGraph;
	private Spinner graphSpinner;
	private ArrayList<String> FilesInFolder;
	private RecordingData recordingData;
	
	private CheckBox checkbox_um;
	private CheckBox checkbox_uh;
	private CheckBox checkbox_ah;
	private CheckBox checkbox_er;
	private CheckBox checkbox_like;
	
	@Override
    protected void onResume() {

       super.onResume();
       this.onCreate(null);
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);	
		
		initializeCheckboxes();
		
		graphSpinner = (Spinner) findViewById(R.id.graphSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.graphs_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		graphSpinner.setAdapter(adapter);
		graphSpinner.setOnItemSelectedListener((OnItemSelectedListener) this);
		
		FilesInFolder = GetFiles(Environment.getExternalStorageDirectory()+"/SpeechTutor");
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		TextView recordingLabel = (TextView) findViewById(R.id.recordingLabel);
		if(FilesInFolder == null || FilesInFolder.size() < 2){
			layout.setVisibility(View.GONE);
			TextView no_recordings = (TextView) findViewById(R.id.need_recordings);
			no_recordings.setVisibility(View.VISIBLE);
			recordingLabel.setVisibility(View.GONE);
		}else{
			layout.setVisibility(View.VISIBLE);
			recordingLabel.setVisibility(View.VISIBLE);
		}
		
		recordingData = null;
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
       makeTotalFillerWords();
		
	}
	
	public void initializeCheckboxes(){
		checkbox_um = (CheckBox) findViewById(R.id.checkbox_um);
		checkbox_ah = (CheckBox) findViewById(R.id.checkbox_ah);
		checkbox_er = (CheckBox) findViewById(R.id.checkbox_er);
		checkbox_uh = (CheckBox) findViewById(R.id.checkbox_uh);
		checkbox_like = (CheckBox) findViewById(R.id.checkbox_like);
	}
	
	public void onCheckboxClicked(View view) {
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.removeView(currentGraph);
		makeTotalIndividual();
	}
	
	public void makeFillersPerSecond(){
		 GraphViewData[] data;
	        String[] recordingNames;
	        int maxValue = 0 ;
	        if(FilesInFolder.size() == 0){
	        	 data = new GraphViewData[1];
	        	 recordingNames = new String[1];
	        	data[0] = new GraphViewData(1,0);
	        	recordingNames[0] = "";
	            maxValue = 1;
	        }else{
				data = new GraphViewData[FilesInFolder.size()];
				recordingNames = new String[(FilesInFolder.size())];
				for(int i = 0; i < FilesInFolder.size(); i++){
					String time = recordingData.recordingTime.get(FilesInFolder.get(i));
					int minutes = Integer.parseInt(time.substring(0,2));
					int seconds = Integer.parseInt(time.substring(3,5));
					float num = minutes*60+seconds; 
					data[i] = new GraphViewData(i+1, recordingData.recordingFillerWordCount.get(FilesInFolder.get(i))/num);
					recordingNames[i] = FilesInFolder.get(i).substring(0,FilesInFolder.get(i).length()-4);
					recordingNames[i] = "";
					//if(recordingNames[i].length() > 2){
					//	recordingNames[i] = recordingNames[i].substring(0,2)+"...";
						//recordingNames[i] = "";
					//}
					if(recordingData.recordingFillerWordCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingFillerWordCount.get(FilesInFolder.get(i));
					}
				}
		        
	        }
	        makeGraph(data,recordingNames, "", maxValue,false,Color.parseColor("#63ce7c"),Color.argb(100,157, 224, 173));
	}
	public void makeTotalFillerWords(){
		 GraphViewData[] data;
	        String[] recordingNames;
	        int maxValue = 0 ;
	        if(FilesInFolder != null && FilesInFolder.size() == 0){
	        	 data = new GraphViewData[1];
	        	 recordingNames = new String[1];
	        	data[0] = new GraphViewData(1,0);
	        	recordingNames[0] = "";
	            maxValue = 1;
	        }else{
				data = new GraphViewData[FilesInFolder.size()];
				recordingNames = new String[(FilesInFolder.size())];
				for(int i = 0; i < FilesInFolder.size(); i++){
					data[i] = new GraphViewData(i+1, recordingData.recordingFillerWordCount.get(FilesInFolder.get(i)));
					//recordingNames[i] = FilesInFolder.get(i).substring(0,FilesInFolder.get(i).length()-4);
					//if(recordingNames[i].length() > 2){
					//	recordingNames[i] = recordingNames[i].substring(0,2)+"...";
					//}
					recordingNames[i] = "";
					if(recordingData.recordingFillerWordCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingFillerWordCount.get(FilesInFolder.get(i));
					}
				}
		        
	        }
	        makeGraph(data,recordingNames, "", maxValue,true,Color.parseColor("#45ada8"),Color.argb(100,118, 200, 196));
	}
	
	public void makeTotalIndividual(){
		ArrayList<GraphViewSeries> graphs = new ArrayList<GraphViewSeries>();
		String[] recordingNames;
		GraphViewData[] data;
		int maxValue = 0;
		
		if(FilesInFolder.size() == 0){
			data = new GraphViewData[1];
	       	recordingNames = new String[1];
	       	data[0] = new GraphViewData(1,0);
	       	recordingNames[0] = "";
	        maxValue = 1;
	        makeGraph(data,recordingNames, "", maxValue,true,Color.parseColor("#45ada8"),Color.argb(100,118, 200, 196));
		}else{
			recordingNames = new String[(FilesInFolder.size())];
			for(int i = 0; i < FilesInFolder.size(); i++){
				recordingNames[i] = FilesInFolder.get(i).substring(0,FilesInFolder.get(i).length()-4);
				//if(recordingNames[i].length() > 2){
					//recordingNames[i] = recordingNames[i].substring(0,2)+"...";
				//	recordingNames[i] = "";
				//}
				recordingNames[i] = "";
			}
			if(checkbox_um.isChecked()){
					data = new GraphViewData[FilesInFolder.size()];
					for(int i = 0; i < FilesInFolder.size(); i++){
						data[i] = new GraphViewData(i+1, recordingData.recordingUmCount.get(FilesInFolder.get(i)));
						if(recordingData.recordingUmCount.get(FilesInFolder.get(i)) > maxValue){
							maxValue = recordingData.recordingUmCount.get(FilesInFolder.get(i));
						}
					}
					graphs.add(new GraphViewSeries("Um",new GraphViewSeriesStyle(Color.argb(96,210,68,65),6), data));
			}
			if(checkbox_uh.isChecked()){
				data = new GraphViewData[FilesInFolder.size()];
				for(int i = 0; i < FilesInFolder.size(); i++){
					data[i] = new GraphViewData(i+1, recordingData.recordingUhCount.get(FilesInFolder.get(i)));
					if(recordingData.recordingUhCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingUhCount.get(FilesInFolder.get(i));
					}
				}
				graphs.add(new GraphViewSeries("Uh",new GraphViewSeriesStyle(Color.argb(96,157,224,173),6), data));
		}
			if(checkbox_ah.isChecked()){
				data = new GraphViewData[FilesInFolder.size()];
				for(int i = 0; i < FilesInFolder.size(); i++){
					data[i] = new GraphViewData(i+1, recordingData.recordingAhCount.get(FilesInFolder.get(i)));
					if(recordingData.recordingAhCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingAhCount.get(FilesInFolder.get(i));
					}
				}
				graphs.add(new GraphViewSeries("Ah",new GraphViewSeriesStyle(Color.argb(96,1,201,234),6), data));
		}
			if(checkbox_er.isChecked()){
				data = new GraphViewData[FilesInFolder.size()];
				for(int i = 0; i < FilesInFolder.size(); i++){
					data[i] = new GraphViewData(i+1, recordingData.recordingErCount.get(FilesInFolder.get(i)));
					if(recordingData.recordingErCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingErCount.get(FilesInFolder.get(i));
					}
				}
				graphs.add(new GraphViewSeries("Er",new GraphViewSeriesStyle(Color.argb(96,69,173,168),6), data));
		}
			if(checkbox_like.isChecked()){
				data = new GraphViewData[FilesInFolder.size()];
				for(int i = 0; i < FilesInFolder.size(); i++){
					data[i] = new GraphViewData(i+1, recordingData.recordingLikeCount.get(FilesInFolder.get(i)));
					if(recordingData.recordingLikeCount.get(FilesInFolder.get(i)) > maxValue){
						maxValue = recordingData.recordingLikeCount.get(FilesInFolder.get(i));
					}
				}
				graphs.add(new GraphViewSeries("Like",new GraphViewSeriesStyle(Color.argb(96,255,242,0),6), data));
		}
		}
		makeMultipleGraphs(graphs,recordingNames,maxValue);
	}
		
	
	public void makeMultipleGraphs(ArrayList<GraphViewSeries> graphs, String[] recordingNames, int maxValue){
			LineGraphView graphView = new LineGraphView(
			    this
			    , ""
			);
			for(int i=0; i < graphs.size(); i++){
				graphView.addSeries(graphs.get(i));
			}
			graphView.setScalable(true);
			// optional - legend
			graphView.setScrollable(true);
			
			if(recordingNames.length > 10){
				graphView.setViewPort(recordingNames.length-10, 10);
			}
			
			
			graphView.setHorizontalLabels(recordingNames);
			graphView.setGraphViewStyle(new GraphViewStyle(Color.DKGRAY, Color.DKGRAY, Color.LTGRAY));
			graphView.getGraphViewStyle().setTextSize(40);

			  int interval;
			  if (maxValue <= 15) {
			      interval = 1; // increment of 5 between each label
			  } else if (maxValue <= 50) {
			      interval = 5; // increment of 10 between each label
			  } else {
			      interval = 10; // increment of 20 between each label
			  }
			  // search the top value of your graph, it must be a multiplier of your interval
			  int maxLabel = maxValue;
			  while (maxLabel % interval != 0) {
			      maxLabel++;
			  }
			  // set manual bounds
			  graphView.setManualYAxisBounds(maxLabel, 0);
			  // indicate number of vertical labels
			  graphView.getGraphViewStyle().setNumVerticalLabels(maxLabel / interval + 1);
			LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
			currentGraph = graphView;
			layout.addView(graphView);
	}
	
	public void makeGraph(GraphViewData[] data, String[] recordingNames, String title, int maxValue, boolean integerize, int color1, int color2){
		GraphViewSeries numFillers = new GraphViewSeries("Number of Fillers",new GraphViewSeriesStyle(color1, 3),data);
		 
		GraphView graphView = new LineGraphView(
		    this // context
		    , title // heading
		);
		graphView.addSeries(numFillers); // data
		graphView.setScrollable(true);
		//graphView.setScalable(true);
		
		if(data.length > 10){
			graphView.setViewPort(data.length-10, 10);
		}
		
		
		((LineGraphView) graphView).setDrawBackground(true);
		((LineGraphView) graphView).setBackgroundColor(color2);
		graphView.setGraphViewStyle(new GraphViewStyle(Color.DKGRAY, Color.DKGRAY, Color.LTGRAY));

		graphView.getGraphViewStyle().setTextSize(40);
		if(integerize){
		  
				  int interval;
				  if (maxValue <= 15) {
				      interval = 1; // increment of 5 between each label
				  } else if (maxValue <= 50) {
				      interval = 5; // increment of 10 between each label
				  } else {
				      interval = 10; // increment of 20 between each label
				  }
				  // search the top value of your graph, it must be a multiplier of your interval
				  int maxLabel = maxValue;
				  while (maxLabel % interval != 0) {
				      maxLabel++;
				  }
				  // set manual bounds
				  graphView.setManualYAxisBounds(maxLabel, 0);
				  // indicate number of vertical labels
				  graphView.getGraphViewStyle().setNumVerticalLabels(maxLabel / interval + 1);
		}
		graphView.getGraphViewStyle().setVerticalLabelsWidth(100);
		graphView.setHorizontalLabels(recordingNames);
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		currentGraph = graphView;
		layout.addView(graphView);
	}
	
	public ArrayList<String> GetFiles(String DirectoryPath) {
	    ArrayList<String> MyFiles = new ArrayList<String>();
	    File f = new File(DirectoryPath);

	    f.mkdirs();
	    File[] files = f.listFiles();
	    Arrays.sort(files, new Comparator<File>(){
	        public int compare(File f1, File f2)
	        {
	            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
	        } });
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.statistics, menu);
		return true;
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			   long id) {
			  
			  graphSpinner.setSelection(position);
			  if(FilesInFolder == null || FilesInFolder.size() < 2){
				  return;
			  }
			  String selState = (String) graphSpinner.getSelectedItem();
			  System.out.println(selState);
			  if(selState.equals("Total Filler Words per Recording")){
				  LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
				  layout.removeView(currentGraph);
				  LinearLayout checkboxes = (LinearLayout) findViewById(R.id.individualCheckboxes);
				  checkboxes.setVisibility(View.GONE);
				  makeTotalFillerWords();
				  
			  }else if(selState.equals("Filler Words per Second")){
				  LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
				  layout.removeView(currentGraph);
				  LinearLayout checkboxes = (LinearLayout) findViewById(R.id.individualCheckboxes);
				  checkboxes.setVisibility(View.GONE);
				  makeFillersPerSecond();
			  }else if(selState.equals("Individual Filler Words per Recording")){
				  LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
				  layout.removeView(currentGraph);
				  LinearLayout checkboxes = (LinearLayout) findViewById(R.id.individualCheckboxes);
				  checkboxes.setVisibility(View.VISIBLE);
				  makeTotalIndividual();
			  }
			 }

			  @Override
			 public void onNothingSelected(AdapterView<?> arg0) {
			  // TODO Auto-generated method stub

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
