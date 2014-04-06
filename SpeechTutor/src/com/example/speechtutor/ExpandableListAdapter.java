package com.example.speechtutor;
 
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    private ExpandableListView view;
    
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }
    
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData, ExpandableListView view) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.view = view;
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
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.recordingPlayback);
        
        ImageButton delete = (ImageButton) convertView
                .findViewById(R.id.delete);
        
        ToggleButton playback = (ToggleButton) convertView
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
                	Toast.makeText(_context, "Deleted"+ groupText, Toast.LENGTH_SHORT).show();
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
		        	System.out.println("PLAY");
		        }else{
		        	//TODO: STOP RECORDING
		        	System.out.println("STOP");
		        }
		    }
		        });
		
		ImageButton share_button = (ImageButton)convertView.findViewById(R.id.share);
		share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent shareIntent = new Intent();
            	final File f= new File("/sdcard/SpeechTutor/"+groupText);
            	shareIntent.setAction(Intent.ACTION_SEND);
            	shareIntent.putExtra(Intent.EXTRA_STREAM,  Uri.fromFile(f));
            	shareIntent.setDataAndType(Uri.fromFile(f), "text/plain"); 
            	_context.startActivity(Intent.createChooser(shareIntent, "Share your Speech"));
            }
        });

		
        txtListChild.setText(childText);
        return convertView;
        
    }
    
	    public void playRecording(String fileName) {
	        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpeechTutor");
	        fileName= mediaStorageDir.getPath() + File.separator + fileName;
	    	File file=  new File(fileName);
	    	int musicLength = (int)(file.length()/2);
	          short[] music = new short[musicLength];
		        Toast.makeText(_context, "Playing sound from "+ fileName, Toast.LENGTH_SHORT).show();
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
	            int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
	            		AudioFormat.ENCODING_PCM_16BIT); 

	            		AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
	            		AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM); 

	           /* AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 
	                                                   8000, 
	                                                   AudioFormat.CHANNEL_IN_MONO,
	                                                   AudioFormat.ENCODING_PCM_16BIT, 
	                                                   musicLength, 
	                                                   AudioTrack.MODE_STREAM);*/

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
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
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