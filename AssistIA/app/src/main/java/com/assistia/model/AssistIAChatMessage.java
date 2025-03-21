package com.assistia.model;

import android.app.Activity;
import android.media.MediaPlayer;
import android.speech.tts.UtteranceProgressListener;

import com.assistia.contract.ISpeechSynthesizerService;
import com.assistia.contract.ISynthesizeSpeechResultListener;
import com.assistia.http.SynthesizeSpeechResult;

import java.io.File;
import java.util.UUID;

public class AssistIAChatMessage extends BaseChatMessage {
    private static final String LOG_TAG_MASK = "AssistIA-AssistIAChatMessage-"; // Define a TAG for logging

    //private final String LOG_TAG;
    String utteranceId;
    File audioFile;
    MediaPlayer mediaPlayer;
    ISynthesizeSpeechResultListener resultListener;

    public String getUtteranceId() {
        if (this.utteranceId == null)
            throw new NullPointerException();

        return this.utteranceId;
    }

    private final Activity activity;
    private final ISpeechSynthesizerService speechSynthesizerService;

    public AssistIAChatMessage(Activity activity, ISpeechSynthesizerService speechSynthesizerService, String message, LanguageInfo languageInfo) {
        super(message, false);

        this.activity = activity;
        this.speechSynthesizerService = speechSynthesizerService;
        this.utteranceId = UUID.randomUUID().toString();

        this.speechSynthesizerService.synthesizeSpeech(message, languageInfo, this.utteranceId, new SpeechSynthesizerUtteranceProgressListener(this));
    }

    public void bindViewHolder(ISynthesizeSpeechResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public void onDone(String utteranceId) {
        File audioFile = this.speechSynthesizerService.getAudioFile(utteranceId);
        this.activity.runOnUiThread(() -> this.resultListener.onResult(SynthesizeSpeechResult.okResult(audioFile)));
    }

    private static class SpeechSynthesizerUtteranceProgressListener extends UtteranceProgressListener {
        AssistIAChatMessage message;
        private SpeechSynthesizerUtteranceProgressListener(AssistIAChatMessage message) {
            this.message = message;
        }

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            this.message.onDone(utteranceId);
        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}
