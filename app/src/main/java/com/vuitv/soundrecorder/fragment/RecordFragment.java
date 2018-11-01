package com.vuitv.soundrecorder.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.vuitv.soundrecorder.R;
import com.vuitv.soundrecorder.RecordingService;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {
    public static final String LOG_TAG = "RecordFrament";

    private FloatingActionButton btnRecod, btnPause;
    private TextView tvRecordStatus;
    private Chronometer chronometer = null;

    private boolean startRecording = true;
    private boolean pauseRecording = true;

    private int recordStartusCount = 0;
    long timePause = 0;

    public static RecordFragment newInstance(int position) {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }


    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        btnRecod = view.findViewById(R.id.btnRecord);
        btnPause = view.findViewById(R.id.btnPause);
        tvRecordStatus = view.findViewById(R.id.tvRecordStatus);
        chronometer = view.findViewById(R.id.chronometer);

        btnRecod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartRecord(startRecording);
                startRecording = !startRecording;
            }
        });

        btnPause.setVisibility(View.GONE);  //Ẩn nút tạm dừng trước khi ghi âm bắt đầu
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(pauseRecording);
                pauseRecording = !pauseRecording;
            }
        });

        return view;
    }

    private void onStartRecord(boolean start) {
        //Bắt đầu ghi âm
        Intent intent = new Intent(getActivity(), RecordingService.class);
        if (start) {
            btnRecod.setImageResource(R.drawable.ic_media_stop);
            btnPause.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //Nếu không tồn tại folder /SoundRecorder, tạo thư mục.
                folder.mkdir();
            }

            //Bắt đầu Chronometer
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    //Biểu diễn tiến trình qua textview
                    if (recordStartusCount == 0) {
                        tvRecordStatus.setText(getString(R.string.record_in_progress) + ".");
                    } else if (recordStartusCount == 1) {
                        tvRecordStatus.setText(getString(R.string.record_in_progress) + "..");
                    } else if (recordStartusCount == 2) {
                        tvRecordStatus.setText(getString(R.string.record_in_progress) + "...");
                        recordStartusCount = -1;
                    }
                    recordStartusCount++;
                }
            });

            //Bắt đầu RecordingService
            getActivity().startService(intent);
            //Giữ màn hình luôn bật khi ghi
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (!pauseRecording) {
                btnPause.setImageResource(R.drawable.ic_media_pause);
                pauseRecording = !pauseRecording;
            }
            btnRecod.setImageResource(R.drawable.ic_record_white);
            btnPause.setVisibility(View.GONE);
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            timePause = 0;
            tvRecordStatus.setText(getString(R.string.record_prompt));

            getActivity().stopService(intent);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onPauseRecord(boolean pause) {
        if (pause) {
            btnPause.setImageResource(R.drawable.ic_media_play);
            tvRecordStatus.setText(getString(R.string.resume_recording_button));
            timePause = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
        } else {
            btnPause.setImageResource(R.drawable.ic_media_pause);
            chronometer.setBase(SystemClock.elapsedRealtime() + timePause);
            chronometer.start();
        }
    }
}
