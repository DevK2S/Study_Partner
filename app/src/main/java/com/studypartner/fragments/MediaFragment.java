package com.studypartner.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.studypartner.R;


public class MediaFragment extends Fragment {

    private String mediapath;
    private SimpleExoPlayer player;
    PlayerView videoplayer;

    public MediaFragment() {
        // Required empty public constructor
    }

    public static MediaFragment newInstance(String path) {

        Bundle b = new Bundle();
        MediaFragment fragment = new MediaFragment();
        b.putString("MediaPath", path);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoplayer = view.findViewById(R.id.video_view);
        mediapath = getArguments().getString("MediaPath");
        initializePlayer();

    }

    private void initializePlayer() {

        MediaSource mediaSource;
        player = new SimpleExoPlayer.Builder(getContext()).build();

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "Media");
        mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediapath));
        player.setPlayWhenReady(false);
        player.prepare(mediaSource);
        if (videoplayer != null)
            videoplayer.setPlayer(player);
    }

    @Override
    public void onPause() {
        super.onPause();
        player.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer();
    }
}