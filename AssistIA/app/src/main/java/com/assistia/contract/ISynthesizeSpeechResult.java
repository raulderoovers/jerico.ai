package com.assistia.contract;

import java.io.File;

public interface ISynthesizeSpeechResult {
    boolean isSuccessful();
    File getAudioFile();
}
