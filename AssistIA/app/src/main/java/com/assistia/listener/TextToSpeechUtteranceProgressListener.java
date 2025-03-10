package com.assistia.listener;

import android.media.MediaPlayer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;

import com.assistia.model.AssistIAChatMessage;
import com.assistia.model.BaseChatMessage;

import java.util.Optional;

public class TextToSpeechUtteranceProgressListener extends UtteranceProgressListener {
    @Override
    public void onStart(String utteranceId) {
        // Speech synthesis started
    }

    @Override
    public void onDone(String utteranceId) {
        /*
        // Speech synthesis complete, start playback
        runOnUiThread(() -> {
            // TODO: improve
            Optional<BaseChatMessage> messageOpt = messages.stream().filter(x -> x instanceof AssistIAChatMessage && ((AssistIAChatMessage)x).getUtteranceId().equals(utteranceId)).findFirst();
            AssistIAChatMessage aiMessage = (AssistIAChatMessage)messageOpt.get();
            boolean ok = false;
            try {
                // TODO: add to latest message with play and pause icons!
                mediaPlayerForLastMessage = new MediaPlayer();
                mediaPlayerForLastMessage.setDataSource(audioFile.getAbsolutePath());
                mediaPlayerForLastMessage.prepare();
                ok = true;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Error converting text to speech: " + e.getMessage());
            }

            // Hide loading indicator and restore content
            enableRecordControl();
            hideLoadingIndicator();
            if (ok && lastSynthesizeSucceeded) {
                aiMessage.setMediaPlayer(mediaPlayerForLastMessage);
                enablePlaybackControls();

                if(speakAsap) {
                    // TODO: replace with full custom method
                    btnPlay.callOnClick();
                }
            }
        });
        */
    }

    @Override
    public void onError(String utteranceId) {
        /*
        // Handle errors if needed
        runOnUiThread(() -> {
            // Hide loading indicator and restore content
            progressSection.setVisibility(View.GONE);
            mainContent.setAlpha(1.0f);
            mainContent.setEnabled(true);
        });
        */
    }
}