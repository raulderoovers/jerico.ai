package com.assistia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.assistia.adapter.ChatAdapter;
import com.assistia.contract.IAssistantService;
import com.assistia.contract.ISpeechRecognitionService;
import com.assistia.contract.ISpeechSynthesizerService;
import com.assistia.model.AssistIAChatMessage;
import com.assistia.model.BaseChatMessage;
import com.assistia.model.UserChatMessage;
import com.assistia.service.MistralAIService;
import com.assistia.service.SpeechSynthesizerService;
import com.assistia.service.mock.MockSpeechRecognitionService;
import com.assistia.service.mock.MockSpeechSynthesizerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AssistIA-MainActivity";
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swtSpeakAsap;
    ImageButton btnTapToRecord;
    ListView lvwChat;
    LinearLayout mainContent;
    LinearLayout progressSection;

    boolean speakAsap = false;
    String speakNowMessage;

    ISpeechRecognitionService speechRecognitionService;
    IAssistantService assistantService;
    ISpeechSynthesizerService speechSynthesizerService;
    ChatAdapter chatAdapter;
    List<BaseChatMessage> messages;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            String msg = getString(R.string.speech_recognition_not_supported);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, msg);
            return;
        }

        Log.d(LOG_TAG, "onCreate: Activity started");

        this.swtSpeakAsap = findViewById(R.id.swt_speak_asap);
        this.swtSpeakAsap.setOnCheckedChangeListener((buttonView, isChecked) -> speakAsap = isChecked);

        this.btnTapToRecord = findViewById(R.id.btn_tap_to_record);
        this.btnTapToRecord.setOnClickListener(v -> startSpeechRecognition());

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        this.lvwChat = findViewById(R.id.lvw_chat);
        this.lvwChat.setAdapter(chatAdapter);
        this.lvwChat.post(() -> lvwChat.setSelection(chatAdapter.getCount() - 1));

        this.mainContent = findViewById(R.id.main_content);
        this.progressSection = findViewById(R.id.progress_section);

        this.speakNowMessage = getString(R.string.speak_now);

        //this.speechRecognitionService = new SpeechRecognitionService(this, this::registerForActivityResult);
        this.speechRecognitionService = new MockSpeechRecognitionService();

        String apiUrl = BuildConfig.ASSISTANT_SERVICE_URL;
        String apiKey = BuildConfig.ASSISTANT_SERVICE_KEY;
        this.assistantService = new MistralAIService(apiUrl, apiKey);

        //this.speechSynthesizerService = new SpeechSynthesizerService(this);
        this.speechSynthesizerService = new MockSpeechSynthesizerService(this);

        Log.d(LOG_TAG, "onCreate: Started OK!");
    }

    private void showLoadingIndicator() {
        this.progressSection.setVisibility(View.VISIBLE);
        this.mainContent.setAlpha(0.5f);
        this.mainContent.setEnabled(false);
    }

    private void hideLoadingIndicator() {
        progressSection.setVisibility(View.GONE);
        mainContent.setAlpha(1.0f);
        mainContent.setEnabled(true);
    }

    private void startSpeechRecognition() {
        Log.d(LOG_TAG, "startSpeechRecognition: Started");
        this.btnTapToRecord.setEnabled(false);

        Log.d(LOG_TAG, "startSpeechRecognition: Calling Speech Recognition Service");
        this.speechRecognitionService.Run(this::processResult);
    }

    private void processResult(ActivityResult result) {
        Log.d(LOG_TAG, "processResult: Started");

        Pair<Boolean, String> parseResult = parseMessage(result);
        // Check Speech Recognition result
        if (!parseResult.first) {
            // Unsuccessful
            Log.d(LOG_TAG, "processResult: Unsuccessful");
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.something_went_wrong_try_again_later, Toast.LENGTH_SHORT).show();
                this.btnTapToRecord.setEnabled(true);
            });
            return;
        }
        Log.d(LOG_TAG, "processResult: Successful");

        String message = parseResult.second;
        Log.d(LOG_TAG,"processResult: Recognized: " + message);
        runOnUiThread(() -> {
            messages.add(new UserChatMessage(message));
            chatAdapter.notifyDataSetChanged();
            showLoadingIndicator();
        });

        Log.d(LOG_TAG, "processResult: Sending request...");
        this.assistantService.sendMessageForResponse(message).thenAccept(response -> {
            Log.d(LOG_TAG,  "processResult: Got response!");
            if (!response.isSuccessful()) {
                Log.d(LOG_TAG, "processResult: Sending request failed: " + response.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.something_went_wrong_try_again_later, Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                    this.btnTapToRecord.setEnabled(true);
                });
                return;
            }

            String responseMessage = response.getMessage();
            AssistIAChatMessage iaMessage = new AssistIAChatMessage(this, this.speechSynthesizerService, responseMessage);
            runOnUiThread(() -> {
                Log.d(LOG_TAG, "processResult: Sending request succeeded: " + responseMessage);
                messages.add(iaMessage);
                chatAdapter.notifyDataSetChanged();
                hideLoadingIndicator();
                this.btnTapToRecord.setEnabled(true);
            });
            Log.d(LOG_TAG, "processResult: Invoking `synthesizeSpeech");
        });
    }

    private Pair<Boolean, String> parseMessage(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) {
            return new Pair<>(false, null);
        }
        Intent data = result.getData();
        if (data == null) {
            return new Pair<>(false, null);
        }
        String dataString = data.getDataString();
        if (dataString == null) {
            ArrayList<String> dataStrings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (dataStrings == null || dataStrings.isEmpty()) {
                return new Pair<>(false, null);
            }
            String message = dataStrings.get(0);
            return new Pair<>(true, message);
        }
        return new Pair<>(true, dataString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: what to destroy?
    }
}