package com.studypartner.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.utils.FileUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutUsFragment extends Fragment {
	
	public AboutUsFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				MainActivity activity = (MainActivity) requireActivity();
				activity.mNavController.navigate(R.id.action_nav_about_us_to_nav_home);
			}
		});
		
		return inflater.inflate(R.layout.fragment_about_us, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		view.findViewById(R.id.teamCard1Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/Kayvee08");
			}
		});
		
		view.findViewById(R.id.teamCard1LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/karanveer-singh-102153174");
			}
		});
		
		view.findViewById(R.id.teamCard2Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/Saket5");
			}
		});
		
		view.findViewById(R.id.teamCard2LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/saketagar");
			}
		});
		
		view.findViewById(R.id.teamCard3Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/krayong");
			}
		});
		
		view.findViewById(R.id.teamCard3LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/karangourisaria");
			}
		});
		
		view.findViewById(R.id.aboutUsContactUs).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
				feedbackIntent.setData(Uri.parse("mailto:"));
				feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
				requireActivity().startActivity(Intent.createChooser(feedbackIntent, "Choose your email client"));
			}
		});
	}
}