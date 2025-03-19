package com.assistia.contract;

import android.speech.tts.UtteranceProgressListener;

import com.assistia.model.LanguageInfo;

import java.io.File;

public interface ISpeechSynthesizerService {
    void synthesizeSpeech(String message, LanguageInfo languageInfo, String utteranceId, UtteranceProgressListener listener);
    File getAudioFile(String utteranceId);
}
