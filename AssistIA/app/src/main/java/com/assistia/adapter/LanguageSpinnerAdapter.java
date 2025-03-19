package com.assistia.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.assistia.model.LanguageInfo;
import com.assistia.model.Settings;

public class LanguageSpinnerAdapter extends BaseAdapter {
    private final Context context;

    public LanguageSpinnerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return Settings.Languages.size();
    }

    @Override
    public Object getItem(int position) {
        return Settings.Languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        LanguageInfo languageInfo = Settings.Languages.get(position);
        assert languageInfo != null;
        imageView.setImageResource(languageInfo.getFlagResource());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(80, 60));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
