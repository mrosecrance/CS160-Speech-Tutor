package com.example.speechtutor;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	private Handler threadHandler;
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    private ExpandableListView view;
    private AudioTrack playing;
    ToggleButton playback;
    private int position;
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        threadHandler = new Handler(){
      	  @Override
      	  public void handleMessage(Message msg) {
      		synchronized(this){
	      	    if(msg.what==1){
	      	    	playback.setChecked(false);
	      	    	position = 0;
	      	    }
	      	    super.handleMessage(msg);
	      	  }
      	  }
      	};
        
    }
    
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData, ExpandableListView view) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.view = view;
        threadHandler = new Handler(){
      	  @Override
      	  public void handleMessage(Message msg) {
      		synchronized(this){
	      	    if(msg.what==1){
	      	    	playback.setChecked(false);
	      	    }
	      	    super.handleMessage(msg);
	      	  }
      	  }
      	};
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	
        final String childText = (String) getChild(groupPosition, childPosition);
        final String groupText = (String) getGroup(groupPosition);
        final int pos = groupPosition;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.playback_accordion, null);
        }
        if (playback != null){
        	playback.setChecked(false);
        	position = 0;
        }
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.recordingPlayback);
        
        ImageView delete = (ImageView) convertView
                .findViewById(R.id.delete);
        
        ImageView prev = (ImageView) convertView.findViewById(R.id.beginning);
        ImageView end = (ImageView) convertView.findViewById(R.id.end);
        
        prev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	 playback.setChecked(false);
                }
            
        });
        
        end.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	 playback.setChecked(false);
                }
            
        });
        
       
        playback = (ToggleButton) convertView
                .findViewById(R.id.playback);
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println("Delete clicked" + groupText);
                File file = new File("/sdcard/SpeechTutor/"+groupText);
                boolean deleted = file.delete();
                if(deleted){
                	_listDataHeader.remove(groupText);
                	_listDataChild.remove(groupText);
                	Toast.makeText(_context, "Deleted "+ groupText, Toast.LENGTH_SHORT).show();
                	view.collapseGroup(pos);
                	
                	notifyDataSetChanged();
                }
            }
        });
        
        
        
		playback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if(isChecked){ //play
		        	playRecording(groupText);
		        	if(playing != null){
		        		playing.flush();
			        	playing.stop();
			        	playing.release();
			        	playing = null;
		        	}
		        	System.out.println("PLAY");
		        }else{
		        	//TODO: STOP RECORDING
		        	if(playing != null){
		        		position = playing.getPlaybackHeadPosition();
		        		playing.flush();
			        	playing.stop();
			        	playing.release();
			        	playing = null;
		        	}
		        }
		    }
		        });
		
		ImageView share_button = (ImageView)convertView.findViewById(R.id.share);
		share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent shareIntent = new Intent();
            	final File f= new File("/sdcard/SpeechTutor/"+groupText);
            	shareIntent.setAction(Intent.ACTION_SEND);
            	shareIntent.putExtra(Intent.EXTRA_STREAM,  Uri.fromFile(f));
            	shareIntent.setType("text/plain"); 
            	_context.startActivity(Intent.createChooser(shareIntent, "Share your Speech"));
            }
        });

		
        txtListChild.setText(childText);
        return convertView;
        
    }
    
	    public void playRecording(String fileName) {
	    	 File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor");
             final String fName= mediaStorageDir.getPath() + File.separator + fileName;
             //Toast.makeText(_context, "Playing sound from "+ fName, Toast.LENGTH_SHORT).show();
                 
                 new Thread(new Runnable() {
         			
         			@Override
         			public void run() {
         				File file=  new File(fName);
         	             final int musicLength = (int) file.length();
         	             final byte[] music = new byte[musicLength];
         	                    
         	               try {
         	                 InputStream is = new FileInputStream(file);
         	                 is.read(music);
         	                 is.close();
         	                } catch (IOException e) {
         	                        //nothing
         	                }
         	             int sampleRate = 16000;
         				 int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                         AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
                         playing = audioTrack;
		                 playing.play();
		                 playing.write(music, 0, musicLength);
		                 System.out.println(position);
		                 if(position != 0){
		                	 playing.setPlaybackHeadPosition(position);
		                 }
		                 Message msg = threadHandler.obtainMessage();
						 msg.what = 1;
						 threadHandler.sendMessage(msg);
		                 
         			}
                 }).start();
	    }
	   
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.recordinglist, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.recording);
        
        //lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        // set filler word count
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
        TextView lblListFillerWordCount = (TextView) convertView
                .findViewById(R.id.ums);
        TextView lblListTime = (TextView) convertView.findViewById(R.id.time);

        if (recordingData != null) {
    		String writeOut = ((TextView) convertView
                    .findViewById(R.id.recording)).getText().toString();
        	

        	if(recordingData.recordingFillerWordCount.containsKey(writeOut)){
        		Integer tmpStr = recordingData.recordingFillerWordCount.get(writeOut);
        		lblListFillerWordCount.setText(tmpStr.toString());
        	}
        	
        	if(recordingData.recordingTime != null){
	        	if(recordingData.recordingTime.containsKey(writeOut)){
	        	
	            		lblListTime.setText(recordingData.recordingTime.get(writeOut));
	        		
	        		
	        	}
        	}
        }
        else {
            lblListFillerWordCount.setText("78");

        }
        
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}