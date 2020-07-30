package com.studypartner.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import androidx.fragment.app.Fragment;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Connection {
	
	public static void checkConnection (final Fragment fragment) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) fragment.requireActivity().getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragment.requireActivity())
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
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
		}
	}
	
	public static void checkConnection (final Activity activity) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity)
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
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
		}
	}
	
	public static void feedback (final Activity activity) {
		
		Connection.checkConnection(activity);
		Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
		feedbackIntent.setData(Uri.parse("mailto:"));
		feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
		feedbackIntent.putExtra(Intent.EXTRA_SUBJECT,"Feedback on Study Partner");
		activity.startActivity(Intent.createChooser(feedbackIntent,"Choose your email client"));
		
	}
	
	public static void reportBug (final Activity activity) {
		
		Connection.checkConnection(activity);
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		TelephonyManager telephonyManager = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		StatFs internalStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		long internalMemSizeInMB = (internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockCountLong()) / (1024 * 1024);
		ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		long ramSizeInMB = memoryInfo.totalMem / (1024 * 1024);
		
//		String networkType;
//
//		switch (telephonyManager.getNetworkType()) {
//			case TelephonyManager.NETWORK_TYPE_CDMA:
//				networkType = "CDMA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_EDGE:
//				networkType = "EDGE";
//				break;
//			case TelephonyManager.NETWORK_TYPE_GPRS:
//				networkType = "GPRS";
//				break;
//			case TelephonyManager.NETWORK_TYPE_GSM:
//				networkType = "GSM";
//				break;
//			case TelephonyManager.NETWORK_TYPE_IWLAN:
//				networkType = "IWLAN";
//				break;
//			case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
//				networkType = "TD SCDMA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_LTE:
//				networkType = "LTE";
//				break;
//			case TelephonyManager.NETWORK_TYPE_UMTS:
//				networkType = "UMTS";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSDPA:
//				networkType = "HSDPA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSPA:
//				networkType = "HSPA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSPAP:
//				networkType = "HSPAP";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSUPA:
//				networkType = "HSUPA";
//				break;
//			default:
//				networkType = "UNKOWN";
//				break;
//		}
//
//		String phoneType;
//		switch (telephonyManager.getPhoneType()) {
//			case TelephonyManager.PHONE_TYPE_CDMA:
//				phoneType = "CDMA";
//				break;
//			case TelephonyManager.PHONE_TYPE_GSM:
//				phoneType = "GSM";
//				break;
//			case TelephonyManager.PHONE_TYPE_SIP:
//				phoneType = "SIP";
//				break;
//			default:
//				phoneType = "NONE";
//				break;
//		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("UID: ").append(FirebaseAuth.getInstance().getCurrentUser().getUid()).append("\n");
		builder.append("EMAIL ADDRESS: ").append(FirebaseAuth.getInstance().getCurrentUser().getEmail()).append("\n");
		builder.append("EMAIL VERIFIED: ").append(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()).append("\n");
//		builder.append("SERIAL: ").append(Build.SERIAL).append("\n");
//		builder.append("APP: ").append(BuildConfig.APPLICATION_ID).append("\n");
		builder.append("MODEL: ").append(Build.MODEL).append("\n");
		builder.append("ID: ").append(Build.ID).append("\n");
		builder.append("MANUFACTURER: ").append(Build.MANUFACTURER).append("\n");
		builder.append("BRAND: ").append(Build.BRAND).append("\n");
		builder.append("SDK  ").append(Build.VERSION.SDK_INT).append("\n");
		builder.append("RELEASE: ").append(Build.VERSION.RELEASE).append("\n");
//		builder.append("NETWORK INFO: ").append(connectivityManager.getActiveNetwork()).append("\n");
		builder.append("CARRIER: ").append(telephonyManager.getNetworkOperatorName()).append("\n");
//		builder.append("PHONE TYPE: ").append(phoneType).append("\n");
//		builder.append("NETWORK TYPE: ").append(networkType).append("\n");
		builder.append("TOTAL RAM: ").append(ramSizeInMB).append(" MB").append("\n");
		builder.append("INTERNAL MEMORY AVAILABLE: ").append(internalMemSizeInMB).append(" MB").append("\n");
//		builder.append("INCREMENTAL ").append(Build.VERSION.INCREMENTAL).append("\n");
//		builder.append("BOARD: ").append(Build.BOARD).append("\n");
//		builder.append("HOST ").append(Build.HOST).append("\n");
//		builder.append("FINGERPRINT: ").append(Build.FINGERPRINT).append("\n");
//		builder.append("BOOTLOADER: ").append(Build.BOOTLOADER).append("\n");
//		builder.append("DEVICE: ").append(Build.DEVICE).append("\n");
//		builder.append("DISPLAY: ").append(Build.DISPLAY).append("\n");
//		builder.append("HARDWARE: ").append(Build.HARDWARE).append("\n");
//		builder.append("PRODUCT: ").append(Build.PRODUCT).append("\n");
		builder.append("PERMISSIONS GRANTED: ").append("\n\n");
		try {
			PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
			for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
				if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == PackageInfo.REQUESTED_PERMISSION_GRANTED) {
					builder.append(packageInfo.requestedPermissions[i]).append("\n");
				}
			}
		} catch (Exception ignored) {
		}
		builder.append("\n\n\n\n");
		
		Intent reportBugIntent = new Intent(Intent.ACTION_SENDTO);
		reportBugIntent.setData(Uri.parse("mailto:"));
		reportBugIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
		reportBugIntent.putExtra(Intent.EXTRA_SUBJECT,"Bug Report for Study Partner");
		reportBugIntent.putExtra(Intent.EXTRA_TEXT,builder.toString());
		activity.startActivity(Intent.createChooser(reportBugIntent,"Choose your email client"));
		
	}
}