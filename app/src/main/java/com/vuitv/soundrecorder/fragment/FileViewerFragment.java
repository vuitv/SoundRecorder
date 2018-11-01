package com.vuitv.soundrecorder.fragment;


import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vuitv.soundrecorder.R;
import com.vuitv.soundrecorder.adapter.FileViewerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class FileViewerFragment extends Fragment {

    private static final String LOG_TAG = "FileViewerFragment";
    private TextView emptyView;
    private RecyclerView recyclerView;
    private FileViewerAdapter adapter;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment fragment = new FileViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }


    public FileViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new FileViewerAdapter(getActivity(), layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    FileObserver observer = new FileObserver(Environment.getExternalStorageDirectory().toString() + "/SoundRecorder") {
        @Override
        public void onEvent(int event, @Nullable String path) {
            if (event == FileObserver.DELETE) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/SoundRecorder" + path + "]";

                adapter.removeOutOfApp(filePath);
            }
        }
    };

}
