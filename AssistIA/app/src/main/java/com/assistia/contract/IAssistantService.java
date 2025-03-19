package com.assistia.contract;

import java.util.concurrent.CompletableFuture;

public interface IAssistantService {

    CompletableFuture<IAssistantResponse> sendMessageForResponse(String message, String language);

}
