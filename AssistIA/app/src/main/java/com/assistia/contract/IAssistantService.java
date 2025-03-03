package com.assistia.contract;

import java.util.concurrent.CompletableFuture;

public interface IAssistantService {

    CompletableFuture<IAssistantResponse> SendMessageForResponse(String message);

}
