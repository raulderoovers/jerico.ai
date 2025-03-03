package com.assistia.service;

import com.assistia.contract.IAssistantResponse;
import com.assistia.contract.IAssistantService;
import com.assistia.http.AssistantResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MistralAIService implements IAssistantService {
    private final MediaType mediaTypeJson = MediaType.get("application/json; charset=utf-8");

    String apiUrl;
    String apiKey;
    OkHttpClient client;

    public MistralAIService(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    @Override
    public CompletableFuture<IAssistantResponse> SendMessageForResponse(String message) {
        Request request = createRequest(message);
        return CompletableFuture.supplyAsync(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseText = parseResponse(response.body().string());
                    return AssistantResponse.OkResponse(responseText);
                } else {
                    return AssistantResponse.HttpErrorResponse(response.code());
                }
            } catch (Exception e) {
                return AssistantResponse.InternalErrorResponse(e.getMessage());
            }
        });
    }

    private Request createRequest(String message) {
        String payloadMask = "{\n" +
                "    \"model\": \"mistral-small\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]\n" +
                "}";
        String payload = String.format(payloadMask, message);
        RequestBody body = RequestBody.create(payload, mediaTypeJson);
        return new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", String.format("Bearer %s", apiKey))
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private String parseResponse(String body) throws JSONException {
        JSONObject jsonResponse = new JSONObject(body);
        return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
