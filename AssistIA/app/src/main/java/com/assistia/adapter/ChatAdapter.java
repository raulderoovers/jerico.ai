package com.assistia.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.assistia.R;
import com.assistia.contract.ISynthesizeSpeechResult;
import com.assistia.contract.ISynthesizeSpeechResultListener;
import com.assistia.model.AssistIAChatMessage;
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
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (message.isUser()) {
                convertView = inflater.inflate(R.layout.item_message_user, parent, false);
                holder = new ViewHolder();
            } else {
                convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
                AudioMessageViewHolder audioHolder = new AudioMessageViewHolder((AssistIAChatMessage) message);
                audioHolder.btnPlayPause = convertView.findViewById(R.id.btnPlayPause);
                audioHolder.btnPlayPause.setEnabled(false);
                audioHolder.ttsProgressBar = convertView.findViewById(R.id.ttsProgressBar);
                audioHolder.audioProgress = convertView.findViewById(R.id.audioProgress);
                audioHolder.audioProgress.setEnabled(false);
                audioHolder.audioSeekBar = convertView.findViewById(R.id.audioSeekBar);
                audioHolder.audioSeekBar.setEnabled(false);
                holder = audioHolder;
            }

            convertView.setTag(holder);
            holder.textMessage = convertView.findViewById(R.id.textMessage);
        }
         else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textMessage.setText(message.getMessage());
        return convertView;
    }

    private static class ViewHolder {
        TextView textMessage;
    }

    private static class AudioMessageViewHolder extends ViewHolder implements ISynthesizeSpeechResultListener {
        private ImageButton btnPlayPause;
        private SeekBar audioSeekBar;
        private ProgressBar ttsProgressBar;
        private TextView audioProgress;
        private MediaPlayer mediaPlayer;
        private Handler handler = new Handler();
        private boolean isPlaying = false;

        public AudioMessageViewHolder(AssistIAChatMessage message) {
            message.bindViewHolder(this);
        }

        public void bind(String audioFilePath) {
            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("", e.getMessage(), e);
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

            this.updateProgressText(this.mediaPlayer.getDuration());

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
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            isPlaying = true;
            updateSeekBar();
        }

        private void pauseAudio() {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            isPlaying = false;
        }

        private void resetAudio() {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            audioSeekBar.setProgress(0);
            this.updateProgressText(this.mediaPlayer.getDuration());
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

        @Override
        public void onResult(ISynthesizeSpeechResult result) {
            this.bind(result.getAudioFile().getAbsolutePath());

            this.btnPlayPause.setEnabled(true);
            this.btnPlayPause.setVisibility(View.VISIBLE);
            this.ttsProgressBar.setVisibility(View.GONE);
            this.audioProgress.setEnabled(true);
            this.audioSeekBar.setEnabled(true);
        }
    }

}
