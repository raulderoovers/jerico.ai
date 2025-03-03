package com.assistia.http;

import android.annotation.SuppressLint;

import com.assistia.contract.IAssistantResponse;

public class AssistantResponse implements IAssistantResponse {
    private final boolean isSuccessful;
    private final String message;

    public boolean IsSuccessful() {
        return isSuccessful;
    }

    public String Message() {
        return message;
    }

    private AssistantResponse(String message, boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
        this.message = message;
    }

    @SuppressLint("DefaultLocale")
    private AssistantResponse(int statusCode) {
        this.isSuccessful = false;
        this.message = String.format("There was an error reaching the API. Status Code: %d", statusCode);
    }

    public static IAssistantResponse OkResponse(String responseText) {
        return new AssistantResponse(responseText, true);
    }

    public static IAssistantResponse HttpErrorResponse(int statusCode) {
        return new AssistantResponse(statusCode);
    }

    public static IAssistantResponse InternalErrorResponse(String responseText) {
        return new AssistantResponse(responseText, false);
    }
}
