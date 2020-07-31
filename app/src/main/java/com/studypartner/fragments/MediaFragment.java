package com.studypartner.fragments;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class MediaFragment extends Fragment {

	private String mediaPath;
	private SimpleExoPlayer player;
	private PlayerView videoPlayerView;
	private PlayerView audioPlayerView;
	private FileItem mediaFileItem;
	private PhotoView photoView;

	public MediaFragment() {}

	public static MediaFragment newInstance(String path) {

		Bundle bundle = new Bundle();
		
		MediaFragment fragment = new MediaFragment();
		bundle.putString("MediaPath", path);
		fragment.setArguments(bundle);
		
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
		
		videoPlayerView = view.findViewById(R.id.video_view);
		audioPlayerView = view.findViewById(R.id.audio_view);
		mediaPath = getArguments().getString("MediaPath");
		mediaFileItem = new FileItem(mediaPath);
		photoView = view.findViewById(R.id.photo_view);
		
		if (mediaFileItem.getType().equals(FileType.FILE_TYPE_IMAGE)) {
			videoPlayerView.setVisibility(View.GONE);
			audioPlayerView.setVisibility(View.GONE);
			photoView.setVisibility(View.VISIBLE);
			Glide.with(requireContext())
					.load(mediaPath)
					.into(photoView);
		} else {
			photoView.setVisibility(View.GONE);
			initializePlayer();
		}
	}
	
	private void initializePlayer() {
		
		MediaSource mediaSource;
		
		player = new SimpleExoPlayer.Builder(getContext()).build();
		
		DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "Media");
		
		mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaPath));
		
		player.setPlayWhenReady(false);
		player.prepare(mediaSource);
		
		if (mediaFileItem.getType().equals(FileType.FILE_TYPE_VIDEO)) {
			audioPlayerView.setVisibility(View.GONE);
			videoPlayerView.setVisibility(View.VISIBLE);
			if (videoPlayerView != null)
				videoPlayerView.setPlayer(player);
		} else {
			videoPlayerView.setVisibility(View.GONE);
			audioPlayerView.setVisibility(View.VISIBLE);
			
			GradientDrawable gradientDrawable = new GradientDrawable(
					GradientDrawable.Orientation.TOP_BOTTOM,
					new int[]{ContextCompat.getColor(requireContext(), R.color.audioColor1),
							ContextCompat.getColor(requireContext(), R.color.audioColor2),
							ContextCompat.getColor(requireContext(), R.color.audioColor3),
							ContextCompat.getColor(requireContext(), R.color.audioColor4),
							ContextCompat.getColor(requireContext(),R.color.audioColor5)});
			audioPlayerView.findViewById(R.id.audio_controller_bg).setBackground(gradientDrawable);
			
			if (audioPlayerView != null)
				audioPlayerView.setPlayer(player);

		}
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