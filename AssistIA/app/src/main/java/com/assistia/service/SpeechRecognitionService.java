package com.assistia.service;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.assistia.R;
import com.assistia.contract.ISpeechRecognitionService;

import com.assistia.exception.SpeechRecognitionServiceException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SpeechRecognitionService implements ISpeechRecognitionService {
    Context context;
    ActivityResultLauncher<Intent> activityLauncher;

    public SpeechRecognitionService(Context context, ActivityResultLauncher<Intent> activityLauncher) {
        this.context = context;
        if(activityLauncher == null){
            throw new SpeechRecognitionServiceException("activityLauncher can't be null");
        }
        this.activityLauncher = activityLauncher;
    }

    @Override
    public void run() {
        // Create Speech Recognition Intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-AR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.context.getString(R.string.speak_now));

        //Log.d(LOG_TAG, "startSpeechRecognition: Intent created");
        //StartActivityForResult startActivityForResult = new StartActivityForResult();
        //ActivityResultLauncher<Intent> activityLauncher = registerFunction.apply(startActivityForResult, processResult);

        // Launch Intent for response
        this.activityLauncher.launch(intent);
        //Log.d(LOG_TAG, "startSpeechRecognition: Recognizer launched");
    }
}
