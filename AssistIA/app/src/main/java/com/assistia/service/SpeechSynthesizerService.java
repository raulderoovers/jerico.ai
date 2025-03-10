package com.assistia.service;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.Pair;

import com.assistia.R;
import com.assistia.contract.ISpeechSynthesizerService;
import com.assistia.contract.ISynthesizeSpeechResult;
import com.assistia.http.SynthesizeSpeechResult;
import com.assistia.listener.TextToSpeechUtteranceProgressListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpeechSynthesizerService extends UtteranceProgressListener implements ISpeechSynthesizerService {
    private static final String LOG_TAG = "AssistIA-SpeechSynthesizer"; // Define a TAG for logging

    private final Context context;
    private TextToSpeech textToSpeech;
    // TODO: improve
    private final Map<String, Pair<UtteranceProgressListener, File>> ttsCache;

    public SpeechSynthesizerService(Context context) {
        this.context = context;
        this.textToSpeech = new TextToSpeech(context, status -> {
            // TODO: properly implement languages...
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("es", "AR")); // Spanish Argentina
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(LOG_TAG, "SpeechSynthesizer: " + this.context.getString(R.string.unsupported_language));
                }
                textToSpeech.setSpeechRate(1.2f);
            }
        });
        this.textToSpeech.setOnUtteranceProgressListener(this);
        this.ttsCache = new HashMap<>();
    }

    @Override
    public boolean SynthesizeSpeech(String message, String utteranceId, UtteranceProgressListener listener) {
        Log.d(LOG_TAG, "synthesizeSpeech: Started");

        String filename = generateWavFileName();

        File audioFile = new File(this.context.getExternalFilesDir(null), filename);
        this.ttsCache.put(utteranceId, new Pair<>(listener, audioFile));
        Log.d(LOG_TAG, "synthesizeSpeech: Audio file created");

        int result = textToSpeech.synthesizeToFile(message, null, audioFile, utteranceId);
        if (result != TextToSpeech.SUCCESS) {
            Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file failed to initiate");
            return true;
        }
        Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file initiated successfully");
        return false;
    }

    private static String generateWavFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return timestamp + ".wav";
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file started for utterance id " + utteranceId);
        Pair<UtteranceProgressListener, File> pair = this.ttsCache.get(utteranceId);
        assert pair != null;
        pair.first.onStart(utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file finished for utterance id " + utteranceId);
        Pair<UtteranceProgressListener, File> pair = this.ttsCache.get(utteranceId);
        assert pair != null;
        pair.first.onDone(utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file failed for utterance id " + utteranceId);
        Pair<UtteranceProgressListener, File> pair = this.ttsCache.get(utteranceId);
        assert pair != null;
        pair.first.onError(utteranceId);
    }
}
