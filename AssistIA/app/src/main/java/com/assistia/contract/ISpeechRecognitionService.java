package com.assistia.contract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import java.util.function.Consumer;

public interface ISpeechRecognitionService {
    void Run(ActivityResultCallback<ActivityResult> processResult);
}
