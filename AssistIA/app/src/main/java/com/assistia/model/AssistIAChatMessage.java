package com.assistia.model;

import android.media.MediaPlayer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.assistia.contract.ISpeechSynthesizerService;
import com.assistia.contract.ISynthesizeSpeechResult;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AssistIAChatMessage extends BaseChatMessage {
    private static final String LOG_TAG_MASK = "AssistIA-AssistIAChatMessage-"; // Define a TAG for logging

    //private final String LOG_TAG;
    String utteranceId;
    File audioFile;
    MediaPlayer mediaPlayer;

    public String getUtteranceId() {
        if (this.utteranceId == null)
            throw new NullPointerException();

        return this.utteranceId;
    }

    public AssistIAChatMessage(ISpeechSynthesizerService speechSynthesizerService, String message) {
        super(message, false);

        //this.utteranceId = UUID.randomUUID().toString();
        //this.LOG_TAG = LOG_TAG_MASK + this.utteranceId;
        //this.mediaPlayer = new MediaPlayer();

        //speechSynthesizerService.SynthesizeSpeech(message, this.utteranceId, new MyListener());
    }

    private class MyListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {

        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}
