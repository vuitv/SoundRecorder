package com.vuitv.soundrecorder.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.vuitv.soundrecorder.R;
import com.vuitv.soundrecorder.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class PlayFragment extends DialogFragment {

    private static final String LOG_TAG = "PlayFragment";
    private static final String ARG_ITEM = "recording_item";

    private TextView mFileName;
    private TextView mCurrentProgress;
    private TextView mFileLength;
    private SeekBar mSeekBar;
    private FloatingActionButton mFabPlay;

    private RecordingItem item;

    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    long min = 0;
    long sec = 0;

    public PlayFragment newInstance(RecordingItem item) {
        PlayFragment playFragment = new PlayFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ITEM, item);
        playFragment.setArguments(bundle);
        return playFragment;
    }


    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void setStyle(int style, int theme) {
        super.setStyle(style, theme);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);

        long itemDuration = item.getLength();
        min = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        sec = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(min);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_play, null);

        mFileName = view.findViewById(R.id.file_name);
        mFileLength = view.findViewById(R.id.file_length);
        mCurrentProgress = view.findViewById(R.id.current_progress);
        mFabPlay = view.findViewById(R.id.fab_play);
        mSeekBar = view.findViewById(R.id.seekBar);

        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.primary), getResources().getColor(R.color.primary));
        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                    handler.removeCallbacks(mRunnable);

                    long currentPosition = mediaPlayer.getCurrentPosition();
                    long min = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
                    long sec = TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                            - TimeUnit.MINUTES.toSeconds(min);
                    mCurrentProgress.setText(String.format("%02d:%02d", min, sec));
                    updateSeekBar();
                } else if (mediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    handler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    handler.removeCallbacks(mRunnable);
                    mediaPlayer.seekTo(seekBar.getProgress());

                    long min = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                    long sec = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(min);
                    mCurrentProgress.setText(String.format("%02d:%02d", min, sec));
                    updateSeekBar();
                }
            }
        });
        mFabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(isPlaying);
                isPlaying = !isPlaying;
            }
        });

        mFileName.setText(item.getName());
        mFileLength.setText(String.format("%02d:%02d", min, sec));

        builder.setView(view);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            stopPlaying();
        }
    }

    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            if (mediaPlayer == null) {
                startPlay();
            } else {
                resumePlaying();
            }
        } else {
            pausePlaying();
        }
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(item.getFilePath());
            mediaPlayer.prepare();
            mSeekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.seekTo(progress);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() fail");
        }
    }

    private void startPlay() {
        mFabPlay.setImageResource(R.drawable.ic_media_pause);
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(item.getFilePath());
            mediaPlayer.prepare();

            mSeekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG,"prepare() fail");
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

        updateSeekBar();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        mFabPlay.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.pause();
    }

    private void resumePlaying() {
        mFabPlay.setImageResource(R.drawable.ic_media_pause);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying() {
        mFabPlay.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        isPlaying = !isPlaying;

        mCurrentProgress.setText(mFileLength.getText());

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentPosition);

                long min = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
                long sec = TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                        - TimeUnit.MINUTES.toSeconds(min);
                mCurrentProgress.setText(String.format("%02d:%02d", min, sec));

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(mRunnable, 1000);
    }

}
