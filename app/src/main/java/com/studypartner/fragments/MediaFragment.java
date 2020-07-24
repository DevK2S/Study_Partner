package com.studypartner.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class MediaFragment extends Fragment {

	private String mediapath;
	private SimpleExoPlayer player;
	PlayerView mediaplayerView;
	FileItem mediaFileItem;
	PhotoView photoView;

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
		mediaplayerView = view.findViewById(R.id.video_view);
		mediapath = getArguments().getString("MediaPath");
		mediaFileItem = new FileItem(mediapath);
		photoView = view.findViewById(R.id.photo_view);
		if (mediaFileItem.getType().equals(FileType.FILE_TYPE_IMAGE)) {
			mediaplayerView.setVisibility(View.GONE);
			photoView.setVisibility(View.VISIBLE);
			Picasso.get().load(mediapath).into(photoView);
		} else {
			photoView.setVisibility(View.GONE);
			mediaplayerView.setVisibility(View.VISIBLE);
			initializePlayer();
		}
	}
	
	private void initializePlayer() {
		MediaSource mediaSource;
		player = new SimpleExoPlayer.Builder(getContext()).build();
		DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "Media");
		mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediapath));
		player.setPlayWhenReady(false);
		player.prepare(mediaSource);
		if (mediaplayerView != null)
			mediaplayerView.setPlayer(player);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (player != null)
			player.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mediaFileItem.getType().equals(FileType.FILE_TYPE_IMAGE))
			initializePlayer();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (player != null)
			player.release();
	}
}