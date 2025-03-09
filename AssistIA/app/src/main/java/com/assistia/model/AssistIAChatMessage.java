package com.assistia.model;

import android.media.MediaPlayer;

import java.util.UUID;

public class AssistIAChatMessage extends BaseChatMessage {
    private String utteranceId;
    private MediaPlayer mediaPlayer;

    public AssistIAChatMessage(String message) {
        super(message, false);
    }

    public String getUtteranceId() {
        if (this.utteranceId == null)
            throw new NullPointerException();

        return this.utteranceId;
    }

    public void setUtteranceId(String utteranceId) {
        this.utteranceId = utteranceId;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}
