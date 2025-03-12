package com.assistia.contract;

import android.speech.tts.UtteranceProgressListener;

import java.io.File;

public interface ISpeechSynthesizerService {
    boolean SynthesizeSpeech(String message, String utteranceId, UtteranceProgressListener listener);
    File GetAudioFile(String utteranceId);
}
