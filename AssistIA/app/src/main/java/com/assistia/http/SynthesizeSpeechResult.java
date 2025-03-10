package com.assistia.http;

import com.assistia.contract.ISynthesizeSpeechResult;

import java.io.File;

public class SynthesizeSpeechResult implements ISynthesizeSpeechResult {
    private final boolean isSuccessful;
    private final File audioFile;

    public boolean isSuccessful() {
        return this.isSuccessful;
    }

    public File getAudioFile() {
        return this.audioFile;
    }

    private SynthesizeSpeechResult(File audioFile, boolean isSuccessful) {
        this.audioFile = audioFile;
        this.isSuccessful = isSuccessful;
    }

    public static ISynthesizeSpeechResult okResult(File audioFile) {
        return new SynthesizeSpeechResult(audioFile, true);
    }

    public static ISynthesizeSpeechResult errorResult() {
        return new SynthesizeSpeechResult(null, false);
    }
}
