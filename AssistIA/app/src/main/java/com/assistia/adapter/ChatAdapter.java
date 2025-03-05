package com.assistia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.assistia.R;
import com.assistia.model.BaseChatMessage;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private final Context context;
    private final List<BaseChatMessage> messages;
    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;

    public ChatAdapter(Context context, List<BaseChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Two types: User & AI
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseChatMessage message = messages.get(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (message.isUser()) {
                convertView = inflater.inflate(R.layout.item_message_user, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            }

            holder = new ViewHolder();
            holder.textMessage = convertView.findViewById(R.id.textMessage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textMessage.setText(message.getMessage());
        return convertView;
    }

    private static class ViewHolder {
        TextView textMessage;
    }
}
