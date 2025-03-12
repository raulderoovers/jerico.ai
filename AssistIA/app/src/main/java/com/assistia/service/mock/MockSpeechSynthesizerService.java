package com.assistia.service.mock;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.assistia.R;
import com.assistia.contract.ISpeechSynthesizerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MockSpeechSynthesizerService implements ISpeechSynthesizerService {
    private static final String LOG_TAG = "AssistIA-MockSpeechSynthesizerService";
    private final Context context;

    public MockSpeechSynthesizerService(Context context) {
        this.context = context;
    }
    @Override
    public boolean SynthesizeSpeech(String message, String utteranceId, UtteranceProgressListener listener) {
        CompletableFuture.supplyAsync(() -> {
            listener.onStart(utteranceId);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return new RuntimeException(e);
            }
            return null;
        }).thenAccept(exception -> {
            if (exception == null) {
                listener.onDone(utteranceId);
                return;
            }
            listener.onError(utteranceId);
        });
        return true;
    }

    @Override
    public File GetAudioFile(String utteranceId) {
        return copyRawResourceToFile(R.raw.audio, this.context);
    }

    private File copyRawResourceToFile(int resourceId, Context context) {
        File outputFile = new File(context.getFilesDir(), "audio.wav");

        try (InputStream inputStream = context.getResources().openRawResource(resourceId);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return outputFile;
        } catch (IOException e) {
            Log.e(LOG_TAG, "copyRawResourceToFile: Failed with error: " + e.getMessage());
            return null;
        }
    }
}
