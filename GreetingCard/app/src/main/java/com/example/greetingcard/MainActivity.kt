package com.example.greetingcard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.greetingcard.ui.theme.GreetingCardTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Lalo",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var recordedText by remember { mutableStateOf("Press Record and Speak") }
    var aiResponse by remember { mutableStateOf("AI response will appear here") }
    var isRecording by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                //textToSpeech.language = Locale.getDefault()
            }
        }
    }
    textToSpeech.language = Locale.getDefault()
    textToSpeech.setSpeechRate(1.2f);
    //val selectedVoice = textToSpeech.voices.find { it.name == "es-ar-x-dub#male_1-local" }
    //if (selectedVoice != null) {
    //    textToSpeech.voice = selectedVoice
    //}

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission required!", Toast.LENGTH_LONG).show()
        }
    }

    fun sendToMistralAI(text: String) {
        val apiKey = "J3UrRCWKBlTOCm3iYVEph39mfR5IjYLj" // Replace with your actual API key
        val client = OkHttpClient()
        val json = """
            {
                "model": "mistral-small",
                "messages": [{"role": "user", "content": "$text"}]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.mistral.ai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                aiResponse = "Error: ${e.message}"
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val jsonResponse = JSONObject(body.string())
                    val resultText = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    aiResponse = resultText
                }
            }
        })
    }

    fun startRecording() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isRecording = true
        recordedText = "Listening..."

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recordedText = matches[0] // Store recognized text
                    sendToMistralAI(recordedText) // Send to AI
                }
                isRecording = false
            }

            override fun onError(error: Int) {
                recordedText = "Error recognizing speech"
                isRecording = false
            }

            override fun onEndOfSpeech() {
                isRecording = false
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    fun playResponse() {
        if (aiResponse.isNotEmpty()) {
            textToSpeech.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //Text("You said: $recordedText")
        //Text("Mistral AI: $aiResponse")

        //Spacer(modifier = Modifier.height(16.dp))

        /*Button(
            onClick = { startRecording() },
            enabled = !isRecording
        ) {
            Text(if (isRecording) "Listening..." else "Record")
        }*/

        //Spacer(modifier = Modifier.height(16.dp))

        /*Button(
            onClick = { playResponse() },
            enabled = aiResponse.isNotEmpty()
        ) {
            Text("Play Response")
        }*/
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    //GreetingCardTheme {
    //    Greeting("Android")
    //}
}