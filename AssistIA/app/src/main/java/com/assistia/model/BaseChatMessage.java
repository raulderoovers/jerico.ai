package com.assistia.model;

public abstract class BaseChatMessage {
    private final String message;
    private final boolean isUser; // true for user (right), false for AI (left)

    protected BaseChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}
