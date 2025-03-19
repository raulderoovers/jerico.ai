package com.assistia.listener;

import android.view.View;
import android.widget.AdapterView;

import com.assistia.contract.ILanguageChangeListener;

public class LanguageSpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    // TODO: ensure we need this...
    private int selection = -1;
    private final ILanguageChangeListener languageChangeListener; // For selection logic

    public LanguageSpinnerOnItemSelectedListener(ILanguageChangeListener languageChangeListener) {
        this.languageChangeListener = languageChangeListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (this.selection == position) return;

        this.selection = position;
        languageChangeListener.onLanguageChanged(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

}
