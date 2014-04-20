package com.example.speechtutor;

import static edu.cmu.pocketsphinx.Decoder.defaultConfig;
import static edu.cmu.pocketsphinx.Decoder.fileConfig;

import java.io.File;

import edu.cmu.pocketsphinx.Config;


public class SpeechRecognizerRecorderSetup {

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    private final Config config;
    private String audioStorageDirectory = "PocketSphinxTemp";

    /**
     * Creates new speech recognizer builder with default configuration.
     */
    public static SpeechRecognizerRecorderSetup defaultSetup() {
        return new SpeechRecognizerRecorderSetup(defaultConfig());
    }

    /**
     * Creates new speech recognizer builder from configuration file.
     * Configuration file should consist of lines containing key-value pairs.
     *
     * @param configFile configuration file
     */
    public static SpeechRecognizerRecorderSetup setupFromFile(File configFile) {
        return new SpeechRecognizerRecorderSetup(fileConfig(configFile.getPath()));
    }

    private SpeechRecognizerRecorderSetup(Config config) {
        this.config = config;
    }

    public SpeechRecognizerRecorder getRecognizer() {
        return new SpeechRecognizerRecorder(config, audioStorageDirectory);
    }

    public SpeechRecognizerRecorderSetup setAcousticModel(File model) {
        return setString("-hmm", model.getPath());
    }

    public SpeechRecognizerRecorderSetup setDictionary(File dictionary) {
        return setString("-dict", dictionary.getPath());
    }

    public SpeechRecognizerRecorderSetup setSampleRate(int rate) {
        return setFloat("-samprate", rate);
    }

    public SpeechRecognizerRecorderSetup setRawLogDir(File dir) {
        return setString("-rawlogdir", dir.getPath());
    }

    public SpeechRecognizerRecorderSetup setKeywordThreshold(float threshold) {
        return setFloat("-kws_threshold", threshold);
    }

    public SpeechRecognizerRecorderSetup setBoolean(String key, boolean value) {
        config.setBoolean(key, value);
        return this;
    }

    public SpeechRecognizerRecorderSetup setInteger(String key, int value) {
        config.setInt(key, value);
        return this;
    }

    public SpeechRecognizerRecorderSetup setFloat(String key, float value) {
        config.setFloat(key, value);
        return this;
    }

    public SpeechRecognizerRecorderSetup setString(String key, String value) {
        config.setString(key, value);
        return this;
    }
    
    public SpeechRecognizerRecorderSetup setAudioStorageDirectory(String audioStorageDirectory) {
    	this.audioStorageDirectory = audioStorageDirectory;
    	return this;
    }
}
