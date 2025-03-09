package com.assistia.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assistia.R;
import com.assistia.model.BaseChatMessage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.os.Handler;

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

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (message.isUser()) {
                convertView = inflater.inflate(R.layout.item_message_user, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            }

            if (message.isUser()) {
                ViewHolder holder = new ViewHolder();
                holder.textMessage = convertView.findViewById(R.id.textMessage);
                convertView.setTag(holder);
            } else {
                AudioMessageViewHolder holder = new AudioMessageViewHolder(convertView);
                holder.textMessage = convertView.findViewById(R.id.textMessage);
                convertView.setTag(holder);
            }
        } else {
            if (message.isUser()) {
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.textMessage.setText(message.getMessage());
            } else {
                AudioMessageViewHolder holder = (AudioMessageViewHolder) convertView.getTag();
                holder.textMessage.setText(message.getMessage());
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView textMessage;
    }

    private class AudioMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageButton btnPlayPause;
        private SeekBar audioSeekBar;
        private TextView audioProgress;
        private MediaPlayer mediaPlayer;
        private Handler handler = new Handler();
        private TextView textMessage;
        private boolean isPlaying = false;

        public AudioMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
            audioProgress = itemView.findViewById(R.id.audioProgress);
        }

        public void bind(String audioFilePath) {
            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Set SeekBar Max Value
            audioSeekBar.setMax(mediaPlayer.getDuration());

            // Play/Pause Button Logic
            btnPlayPause.setOnClickListener(v -> {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    playAudio();
                }
            });

            // SeekBar Change Listener
            audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                    updateProgressText(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            // Update SeekBar and Progress Text Continuously
            mediaPlayer.setOnPreparedListener(mp -> updateSeekBar());
            mediaPlayer.setOnCompletionListener(mp -> resetAudio());
        }

        private void playAudio() {
            mediaPlayer.start();
            //btnPlayPause.setImageResource(R.drawable.ic_mic_pause);
            isPlaying = true;
            updateSeekBar();
        }

        private void pauseAudio() {
            mediaPlayer.pause();
            //btnPlayPause.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        }

        private void resetAudio() {
            //btnPlayPause.setImageResource(R.drawable.ic_play);
            audioSeekBar.setProgress(0);
            audioProgress.setText(R.string.default_time_progress);
            isPlaying = false;
        }

        private void updateSeekBar() {
            handler.postDelayed(() -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    updateProgressText(mediaPlayer.getCurrentPosition());
                    updateSeekBar();
                }
            }, 500);
        }

        private void updateProgressText(int progress) {
            int seconds = (progress / 1000) % 60;
            int minutes = (progress / 1000) / 60;
            audioProgress.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }
    }

}
