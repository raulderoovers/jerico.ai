package com.assistia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
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

import com.assistia.contract.IAssistantResponse;
import com.assistia.contract.IAssistantService;
import com.assistia.service.MistralAIService;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swtSpeakAsap;
    ImageButton btnTapToRecord;
    ImageButton btnPlay;
    ImageButton btnPause;
    ImageButton btnStop;
    TextView txtResponse;
    LinearLayout mainContent;
    LinearLayout progressSection;

    boolean speakAsap = false;

    IAssistantService assistantService;
    MediaPlayer mediaPlayer;
    File audioFile;
    TextToSpeech textToSpeech;
    ActivityResultLauncher<Intent> someActivityLauncher;

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

        this.swtSpeakAsap = findViewById(R.id.swt_speak_asap);
        this.swtSpeakAsap.setOnCheckedChangeListener((buttonView, isChecked) -> speakAsap = isChecked);

        this.btnTapToRecord = findViewById(R.id.btn_tap_to_record);
        this.btnTapToRecord.setOnClickListener(v -> startSpeechRecognition());

        this.btnPlay = findViewById(R.id.btn_play);
        this.btnPlay.setOnClickListener(v -> mediaPlayer.start());

        this.btnPause = findViewById(R.id.btn_pause);
        this.btnPause.setOnClickListener(v -> mediaPlayer.pause());

        this.btnStop = findViewById(R.id.btn_stop);
        this.btnStop.setOnClickListener(v -> mediaPlayer.stop());

        this.txtResponse = findViewById(R.id.txt_response);

        this.mainContent = findViewById(R.id.main_content);
        this.progressSection = findViewById(R.id.progress_section);

        this.mediaPlayer = new MediaPlayer();

        this.textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("es", "AR")); // Spanish Argentina
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Idioma no soportado", Toast.LENGTH_SHORT).show();
                }
                textToSpeech.setSpeechRate(1.2f);
            }
        });

        StartActivityForResult startActivityForResult = new StartActivityForResult();
        this.someActivityLauncher = registerForActivityResult(startActivityForResult, this::processResult);

        String apiUrl = BuildConfig.ASSISTANT_SERVICE_URL;
        String apiKey = BuildConfig.ASSISTANT_SERVICE_KEY;
        this.assistantService = new MistralAIService(apiUrl, apiKey);
    }

    private void startSpeechRecognition() {
        // TODO: this should be on startup...
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            String msg = getString(R.string.speech_recognition_not_supported);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }

        String speakNowMessage = getString(R.string.speak_now);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-AR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, speakNowMessage);
        someActivityLauncher.launch(intent);
    }

    private void processResult(ActivityResult result) {
        Pair<Boolean, String> parseResult = parseMessage(result);
        if (!parseResult.first) {
            return;
        }

        String message = parseResult.second;
        Toast.makeText(this, "Recognized: " + message, Toast.LENGTH_SHORT).show();

        this.progressSection.setVisibility(View.VISIBLE);
        this.mainContent.setAlpha(0.5f);
        this.mainContent.setEnabled(false);

        Toast.makeText(this, "Sending request...", Toast.LENGTH_SHORT).show();
        this.assistantService.SendMessageForResponse(message).thenAccept(response -> {
            runOnUiThread(() -> Toast.makeText(this, "Got response!", Toast.LENGTH_SHORT).show());
            // TODO: improve messages and move to strings
            if (!response.IsSuccessful()) {
                runOnUiThread(() -> Toast.makeText(this, "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show());
                return;
            }

            String responseMessage = response.Message();
            runOnUiThread(() -> {
                Toast.makeText(this, "Response: " + responseMessage, Toast.LENGTH_SHORT).show();
                txtResponse.setText(responseMessage);
            });
            synthesizeSpeech(responseMessage);
        });
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

    private void synthesizeSpeech(String message) {
        audioFile = new File(getExternalFilesDir(null), "speech_output.wav");
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speech synthesis started
            }

            @Override
            public void onDone(String utteranceId) {

                // Speech synthesis complete, start playback
                runOnUiThread(() -> {
                    // Hide loading indicator and restore content
                    progressSection.setVisibility(View.GONE);
                    mainContent.setAlpha(1.0f);
                    mainContent.setEnabled(true);
                    try {
                        mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (speakAsap) {
                        mediaPlayer.start();
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
        int result = textToSpeech.synthesizeToFile(message, null, audioFile, "TTS_FILE");

        if (result != TextToSpeech.SUCCESS) {
            runOnUiThread(() -> Toast.makeText(this, "Error synthesizing speech", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}