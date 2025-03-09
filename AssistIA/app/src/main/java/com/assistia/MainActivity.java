package com.assistia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

import com.assistia.adapter.ChatAdapter;
import com.assistia.contract.IAssistantService;
import com.assistia.model.AssistIAChatMessage;
import com.assistia.model.BaseChatMessage;
import com.assistia.model.UserChatMessage;
import com.assistia.service.MistralAIService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AssistIA-MainActivity"; // Define a TAG for logging
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swtSpeakAsap;
    ImageButton btnTapToRecord;
    ImageButton btnPlay;
    ImageButton btnPause;
    ImageButton btnStop;
    ListView lvwChat;
    LinearLayout mainContent;
    LinearLayout progressSection;

    boolean lastSynthesizeSucceeded = false;
    boolean speakAsap = false;
    String speakNowMessage;

    IAssistantService assistantService;
    MediaPlayer mediaPlayerForLastMessage;
    File audioFile;
    TextToSpeech textToSpeech;
    ChatAdapter chatAdapter;
    ActivityResultLauncher<Intent> someActivityLauncher;
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

        Log.d(LOG_TAG, "onCreate: Activity started");

        this.swtSpeakAsap = findViewById(R.id.swt_speak_asap);
        this.swtSpeakAsap.setOnCheckedChangeListener((buttonView, isChecked) -> speakAsap = isChecked);

        this.btnTapToRecord = findViewById(R.id.btn_tap_to_record);
        this.btnTapToRecord.setOnClickListener(v -> startSpeechRecognition());

        this.btnPlay = findViewById(R.id.btn_play);
        this.btnPlay.setOnClickListener(v -> {
            disableRecordControl();
            mediaPlayerForLastMessage.start();
        });

        this.btnPause = findViewById(R.id.btn_pause);
        this.btnPause.setOnClickListener(v -> {
            mediaPlayerForLastMessage.pause();
            enableRecordControl();
        });

        this.btnStop = findViewById(R.id.btn_stop);
        this.btnStop.setOnClickListener(v -> {
            mediaPlayerForLastMessage.stop();
            mediaPlayerForLastMessage.reset();
            try {
                mediaPlayerForLastMessage.setDataSource(audioFile.getAbsolutePath());
                mediaPlayerForLastMessage.prepare();
            } catch (Exception e) {
                // TODO: do something...
            }

            enableRecordControl();
        });

        disablePlaybackControls();

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        this.lvwChat = findViewById(R.id.lvw_chat);
        this.lvwChat.setAdapter(chatAdapter);
        this.lvwChat.post(() -> lvwChat.setSelection(chatAdapter.getCount() - 1));

        this.mainContent = findViewById(R.id.main_content);
        this.progressSection = findViewById(R.id.progress_section);

        this.textToSpeech = new TextToSpeech(this, status -> {
            // TODO: properly implement languages...
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("es", "AR")); // Spanish Argentina
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Idioma no soportado", Toast.LENGTH_SHORT).show();
                }
                textToSpeech.setSpeechRate(1.2f);
            }
        });
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speech synthesis started
            }

            @Override
            public void onDone(String utteranceId) {
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
            }

            @Override
            public void onError(String utteranceId) {
                // Handle errors if needed
                runOnUiThread(() -> {
                    // Hide loading indicator and restore content
                    progressSection.setVisibility(View.GONE);
                    mainContent.setAlpha(1.0f);
                    mainContent.setEnabled(true);
                });
            }
        });

        StartActivityForResult startActivityForResult = new StartActivityForResult();
        this.someActivityLauncher = registerForActivityResult(startActivityForResult, this::processResult);

        this.speakNowMessage = getString(R.string.speak_now);

        String apiUrl = BuildConfig.ASSISTANT_SERVICE_URL;
        String apiKey = BuildConfig.ASSISTANT_SERVICE_KEY;
        this.assistantService = new MistralAIService(apiUrl, apiKey);

        Log.d(LOG_TAG, "onCreate: Started OK!");
    }

    private void enablePlaybackControls() {
        setPlaybackControlsEnabledStatus(true);
    }

    private void hideLoadingIndicator() {
        progressSection.setVisibility(View.GONE);
        mainContent.setAlpha(1.0f);
        mainContent.setEnabled(true);
    }

    // TODO: refactor! this should be a pipeline
    // 1. Disable controls
    // 2. Start Speech Recognition Intent
    //    a. Ok:
    //       1. lock ui and show loading indicator
    //       2. send http request
    //          a. Ok: send text to speech intent
    //             1. Ok: add message including audio, enable all controls, hide loading indicator and trigger play if enabled
    //             2. Error: log & display error, enable tap to record, enable ui and hide loading indicator
    //          b. Error: log & display error, enable tap to record, enable ui and hide loading indicator
    //    b. Error: log & display error, enable tap to record
    private void startSpeechRecognition() {
        // Disable buttons until action finishes
        Log.d(LOG_TAG, "startSpeechRecognition: Started");
        disableControls();

        // TODO: this should be on startup...
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            String msg = getString(R.string.speech_recognition_not_supported);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Speech Recognition Intent
        Intent intent = createRecognizerIntent();
        Log.d(LOG_TAG, "startSpeechRecognition: Intent created");

        // Launch Intent for response
        this.someActivityLauncher.launch(intent);
        Log.d(LOG_TAG, "startSpeechRecognition: Recognizer launched");
    }

    private void disableControls() {
        setControlsEnabledStatus(false);
    }

    private void disablePlaybackControls() {
        setPlaybackControlsEnabledStatus(false);
    }

    private void setControlsEnabledStatus(boolean enabled) {
        setRecordControlEnabledStatus(enabled);
        setPlaybackControlsEnabledStatus(enabled);
    }

    private void setRecordControlEnabledStatus(boolean enabled) {
        this.btnTapToRecord.setEnabled(enabled);
    }

    private void setPlaybackControlsEnabledStatus(boolean enabled) {
        this.btnPlay.setEnabled(enabled);
        this.btnPause.setEnabled(enabled);
        this.btnStop.setEnabled(enabled);
    }

    private Intent createRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-AR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.speakNowMessage);
        return intent;
    }

    private void processResult(ActivityResult result) {
        Log.d(LOG_TAG, "processResult: Started");

        Pair<Boolean, String> parseResult = parseMessage(result);
        // Check Speech Recognition result
        if (!parseResult.first) {
            // Unsuccessful
            Log.d(LOG_TAG, "processResult: Unsuccessful");
            // TODO: toast
            enableRecordControl();
            return;
        }
        Log.d(LOG_TAG, "processResult: Successful");

        String message = parseResult.second;
        Log.d(LOG_TAG,"processResult: Recognized: " + message);
        runOnUiThread(() -> {
            messages.add(new UserChatMessage(message));
            chatAdapter.notifyDataSetChanged();
        });

        showLoadingIndicator();

        Log.d(LOG_TAG, "processResult: Sending request...");
        this.assistantService.sendMessageForResponse(message).thenAccept(response -> {
            Log.d(LOG_TAG,  "Got response!");
            // TODO: improve messages and move to strings
            if (!response.isSuccessful()) {
                Log.d(LOG_TAG, "processResult: Sending request failed: " + response);
                runOnUiThread(() -> Toast.makeText(this, "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show());
                enableRecordControl();
                return;
            }

            String responseMessage = response.getMessage();
            AssistIAChatMessage iaMessage = new AssistIAChatMessage(responseMessage);
            runOnUiThread(() -> {
                Log.d(LOG_TAG, "processResult: Response: " + responseMessage);
                messages.add(iaMessage);
                chatAdapter.notifyDataSetChanged();
            });
            Log.d(LOG_TAG, "processResult: Invoking `synthesizeSpeech");
            this.lastSynthesizeSucceeded = synthesizeSpeech(iaMessage);
        });
    }

    private void showLoadingIndicator() {
        this.progressSection.setVisibility(View.VISIBLE);
        this.mainContent.setAlpha(0.5f);
        this.mainContent.setEnabled(false);
    }

    private void enableRecordControl() {
        setRecordControlEnabledStatus(true);
    }

    private void disableRecordControl() {
        setRecordControlEnabledStatus(false);
    }

    private Pair<Boolean, String> parseMessage(ActivityResult result) {
        // TODO: improve messages and move to strings
        if (result.getResultCode() != Activity.RESULT_OK) {
            Toast.makeText(this, "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show();
            return new Pair<>(false, null);
        }
        Intent data = result.getData();
        if (data == null) {
            Toast.makeText(this, "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show();
            return new Pair<>(false, null);
        }
        ArrayList<String> dataStrings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (dataStrings == null || dataStrings.isEmpty()) {
            Toast.makeText(this, "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show();
            return new Pair<>(false, null);
        }
        String message = dataStrings.get(0);
        return new Pair<>(true, message);
    }

    private boolean synthesizeSpeech(AssistIAChatMessage message) {
        Log.d(LOG_TAG, "synthesizeSpeech: Started");

        String filename = generateWavFileName();
        String utteranceId = UUID.randomUUID().toString();
        message.setUtteranceId(utteranceId);
        audioFile = new File(getExternalFilesDir(null), filename);
        Log.d(LOG_TAG, "synthesizeSpeech: Audio file created");

        int result = textToSpeech.synthesizeToFile(message.getMessage(), null, audioFile, utteranceId);
        if (result != TextToSpeech.SUCCESS) {
            Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file failed");
            runOnUiThread(() -> Toast.makeText(this, "Error synthesizing speech", Toast.LENGTH_SHORT).show());
            return false;
        }
        Log.d(LOG_TAG, "synthesizeSpeech: Synthesize to file succeeded");
        return true;
    }

    public static String generateWavFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return timestamp + ".wav";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerForLastMessage != null) {
            mediaPlayerForLastMessage.release();
            mediaPlayerForLastMessage = null;
        }
    }
}