package com.assistia.contract;

import android.speech.tts.UtteranceProgressListener;

public interface ISpeechSynthesizerService {
    boolean SynthesizeSpeech(String message, String utteranceId, UtteranceProgressListener listener);
}
