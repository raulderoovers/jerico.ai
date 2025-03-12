package com.assistia.service.mock;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import com.assistia.contract.ISpeechRecognitionService;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MockSpeechRecognitionService implements ISpeechRecognitionService {
    @Override
    public void Run(ActivityResultCallback<ActivityResult> processResult) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Intent intent = new Intent();
            //intent.setData(Uri.parse("Hello!"));
            intent.setData(Uri.parse("Hello darkness, my old friend. I've come to talk with you again. Because a vision softly creeping. Left its seeds while I was sleeping"));
            return new ActivityResult(Activity.RESULT_OK, intent);
        }).thenAccept(processResult::onActivityResult);
    }
}
