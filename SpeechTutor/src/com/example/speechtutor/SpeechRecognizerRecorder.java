package com.example.speechtutor;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;


public class SpeechRecognizerRecorder {

    protected static final String TAG = SpeechRecognizerRecorder.class.getSimpleName();

    private final Config config;
    private final Decoder decoder;

    private Thread recognizerThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Collection<RecognitionListener> listeners =
        new HashSet<RecognitionListener>();

    private final int sampleRate;
    
    private File audioStorageDirectory = null;
    private String audioStorageFilePath = null;
    private FileOutputStream audioOutputStream = null;

    protected SpeechRecognizerRecorder(Config config, String outputDirectoryName) {
        sampleRate = (int) config.getFloat("-samprate");
        if (config.getFloat("-samprate") != sampleRate)
            throw new IllegalArgumentException("sampling rate must be integer");

        this.config = config;
        decoder = new Decoder(config);
	
        //Prepare for writing
    	audioStorageDirectory = new File(Environment.getExternalStorageDirectory(), outputDirectoryName);
    	if (! audioStorageDirectory.exists()){
            if (! audioStorageDirectory.mkdirs()){
                Log.d("SpeechTutor", "failed to create directory");
                return;
            }
            else{
            	Log.i("SpeechTutor", "created speech directory");
            }
        }
    }

    /**
     * Adds listener.
     */
    public void addListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes listener.
     */
    public void removeListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Starts recognition. Does nothing if recognition is active.
     *
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName) {
        if (null != recognizerThread)
            return false;
        if (null == audioOutputStream) {
        	setAudioStorageFile();
        }
    	
        Log.i(TAG, format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);
        recognizerThread = new RecognizerThread();
        recognizerThread.start();
        return true;
    }
   
    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            recognizerThread.interrupt();
            recognizerThread.join();
        } catch (InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }
                
        recognizerThread = null;
        return true;
    }

    /**
     * Stops recognition. All listeners should receive final result if there is
     * any. Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
    public boolean stop() {
        boolean result = stopRecognizerThread();
        if (result)
            Log.i(TAG, "Stop recognition");

        return result;
    }

    /**
     * Cancels recogition. Listeners do not recevie final result. Does nothing
     * if recognition is not active.
     *
     * @return true if recognition was actually canceled
     */
    public boolean cancel() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Cancel recognition");
            mainHandler.removeCallbacksAndMessages(null);
        }

        return result;
    }

    /**
     * Gets name of the currently active search.
     *
     * @return active search name or null if no search was started
     */
    public String getSearchName() {
        return decoder.getSearch();
    }

    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        decoder.setFsg(searchName, fsgModel);
    }

    /**
     * Adds searches based on JSpeech grammar.
     *
     * @param name search name
     * @param file JSGF file
     */
    public void addGrammarSearch(String name, File file) {
        Log.i(TAG, format("Load JSGF %s", file));
        decoder.setJsgfFile(name, file.getPath());
    }

    /**
     * Adds search based on N-gram language model.
     *
     * @param name search name
     * @param file N-gram model file
     */
    public void addNgramSearch(String name, File file) {
        Log.i(TAG, format("Load N-gram model %s", file));
        decoder.setLmFile(name, file.getPath());
    }

    /**
     * Adds search based on a single phrase.
     *
     * @param name search name
     * @param phrase search phrase
     */
    public void addKeywordSearch(String name, String phrase) {
        decoder.setKws(name, phrase);
    }

    private final class RecognizerThread extends Thread {
        @Override public void run() {
        	int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            AudioRecord recorder =
                new AudioRecord(AudioSource.VOICE_RECOGNITION,
                                sampleRate,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                minBufferSize);
            decoder.startUtt(null);
            recorder.startRecording();
            short[] buffer = new short[minBufferSize/2];
            boolean vadState = decoder.getVadState();

            while (!interrupted()) {
                int nread = recorder.read(buffer, 0, minBufferSize/2);

                if (-1 == nread) {
                    throw new RuntimeException("error reading audio buffer");
                } else if (nread > 0) {
                    decoder.processRaw(buffer, nread, false, false);

                    if (decoder.getVadState() != vadState) {
                        vadState = decoder.getVadState();
                        mainHandler.post(new VadStateChangeEvent(vadState));
                    }

                    final Hypothesis hypothesis = decoder.hyp();
                    if (null != hypothesis)
                        mainHandler.post(new ResultEvent(hypothesis, false));

                    byte bufferData[] = short2byte(buffer);
                    try {
                        // // writes the data to file from buffer
                        // // stores the voice buffer
                        audioOutputStream.write(bufferData, 0, bufferData.length);
                        Log.d("Record", "Audio data actually saved");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            recorder.stop();
            int nread = recorder.read(buffer, 0, buffer.length);
            recorder.release();
            decoder.processRaw(buffer, nread, false, false);
            decoder.endUtt();

            // Remove all pending notifications.
            mainHandler.removeCallbacksAndMessages(null);
            final Hypothesis hypothesis = decoder.hyp();
            if (null != hypothesis)
                mainHandler.post(new ResultEvent(hypothesis, true));
        }
    }

    private abstract class RecognitionEvent implements Runnable {
        public void run() {
            RecognitionListener[] emptyArray = new RecognitionListener[0];
            for (RecognitionListener listener : listeners.toArray(emptyArray))
                execute(listener);
        }

        protected abstract void execute(RecognitionListener listener);
    }

    private class VadStateChangeEvent extends RecognitionEvent {
        private final boolean state;

        VadStateChangeEvent(boolean state) {
            this.state = state;
        }

        @Override protected void execute(RecognitionListener listener) {
            if (state)
                listener.onBeginningOfSpeech();
            else
                listener.onEndOfSpeech();
        }
    }

    private class ResultEvent extends RecognitionEvent {
        protected final Hypothesis hypothesis;
        private final boolean finalResult;

        ResultEvent(Hypothesis hypothesis, boolean finalResult) {
            this.hypothesis = hypothesis;
            this.finalResult = finalResult;
        }

        @Override protected void execute(RecognitionListener listener) {
            if (finalResult)
                listener.onResult(hypothesis);
            else
                listener.onPartialResult(hypothesis);
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
	
	public String getAudioStorageFilePath() {
		return audioStorageFilePath;
	}
	
	public Decoder getDecoder() {
		return decoder;
	} 

    public boolean setAudioStorageFile() {
    	if(null != audioOutputStream) {
            try {
                audioOutputStream.close();
            } catch (IOException e){
                Log.d(TAG, "Could not close audioOutputStream");
            }
        }

    	audioStorageFilePath = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.pcm'", Locale.US).format(new Date());
		audioStorageFilePath = audioStorageDirectory.getPath() + File.separator +   "PCM_" + audioStorageFilePath;
	    try {
	        audioOutputStream = new FileOutputStream(audioStorageFilePath);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        return false;
	    }
	    return true;
    }

}