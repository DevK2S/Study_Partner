package com.studypartner.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Connection {
	private static final String TAG = "Connection";
	
	public static void checkConnection (final Fragment fragment) {
		Log.d(TAG, "checkConnection: internet check");
		
		ConnectivityManager connectivityManager = (ConnectivityManager) fragment.requireActivity().getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragment.requireActivity())
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							fragment.requireActivity().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Toast.makeText(fragment.requireActivity(), "Some functions might not work properly without internet", Toast.LENGTH_SHORT).show();
						}
					})
					.setNeutralButton("Reload", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkConnection(fragment.requireActivity());
						}
					});
			alertDialog.show();
		} else {
			Log.d(TAG, "checkConnection: internet connected");
		}
	}
	
	public static void checkConnection (final Activity activity) {
		Log.d(TAG, "checkConnection: internet check");
		
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity)
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Toast.makeText(activity, "Some functions might not work properly without internet", Toast.LENGTH_SHORT).show();
						}
					})
					.setNeutralButton("Reload", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkConnection(activity);
						}
					});
			alertDialog.show();
		} else {
			Log.d(TAG, "checkConnection: internet connected");
		}
	}
}
