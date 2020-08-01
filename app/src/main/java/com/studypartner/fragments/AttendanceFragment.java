package com.studypartner.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.AttendanceAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.utils.Connection;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class AttendanceFragment extends Fragment {
	
	private final String REQUIRED_ATTENDANCE_CHOSEN = "requiredAttendanceChosen";
	private final String REQUIRED_ATTENDANCE = "requiredAttendance";
	
	private ConstraintLayout mainLayout, requiredAttendanceLayout;
	
	private RecyclerView mRecyclerView;
	private BottomAppBar mBottomAppBar;
	private FloatingActionButton mfab, attendanceRequiredPercentageFabNext;
	private NavController mNavController;
	private LinearLayout mEmptyLayout;
	private Button addButton, updateButton;
	private TextView dateText, dayText, percentageAttended, attendanceComment, attendanceName, attendanceRequiredPercentageSetter;
	private CircularProgressBar attendedProgressBar, requiredProgressBar, attendanceRequiredPercentageProgressBarSetter;
	private SeekBar attendanceRequiredPercentageSeekBarSetter;
	
	private ArrayList<AttendanceItem> mAttendanceItemArrayList = new ArrayList<>();
	private AttendanceAdapter attendanceAdapter;
	private SharedPreferences sharedPreferences;
	
	private double requiredPercentage;
	
	public AttendanceFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
		
		requiredAttendanceLayout = rootView.findViewById(R.id.attendanceRequiredAttendanceConstraintLayout);
		
		attendanceName = rootView.findViewById(R.id.attendanceName);
		attendanceRequiredPercentageSetter = rootView.findViewById(R.id.attendanceRequiredPercentageSetter);
		attendanceRequiredPercentageProgressBarSetter = rootView.findViewById(R.id.attendanceRequiredProgressBarSetter);
		attendanceRequiredPercentageSeekBarSetter = rootView.findViewById(R.id.attendanceRequiredSeekBarSetter);
		attendanceRequiredPercentageFabNext = rootView.findViewById(R.id.attendanceRequiredFab);
		
		mainLayout = rootView.findViewById(R.id.attendanceMainConstraintLayout);
		
		mRecyclerView = rootView.findViewById(R.id.attendanceRecyclerView);
		mEmptyLayout = rootView.findViewById(R.id.attendanceItemEmptyAttendance);
		addButton = rootView.findViewById(R.id.attendanceAddButton);
		updateButton = rootView.findViewById(R.id.attendanceUpdateButton);
		dateText = rootView.findViewById(R.id.attendanceDate);
		dayText = rootView.findViewById(R.id.attendanceDay);
		attendanceComment = rootView.findViewById(R.id.attendanceText);
		percentageAttended = rootView.findViewById(R.id.attendancePercentageAttended);
		attendedProgressBar = rootView.findViewById(R.id.attendanceAttendedTotalProgressBar);
		requiredProgressBar = rootView.findViewById(R.id.attendanceRequiredProgressBar);
		
		sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "RequiredPercentageSelected", Context.MODE_PRIVATE);
		
		MainActivity activity = (MainActivity) requireActivity();
		mBottomAppBar = activity.mBottomAppBar;
		mfab = activity.fab;
		mNavController = NavHostFragment.findNavController(this);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				mNavController.navigate(R.id.action_nav_attendance_to_nav_home);
			}
		});
		
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addSubject();
			}
		});
		
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		mRecyclerView.setAdapter(attendanceAdapter);
		
		attendanceRequiredPercentageFabNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				requiredPercentage = attendanceRequiredPercentageSeekBarSetter.getProgress();
				sharedPreferences.edit().putBoolean(REQUIRED_ATTENDANCE_CHOSEN, true).apply();
				sharedPreferences.edit().putFloat(REQUIRED_ATTENDANCE, (float) requiredPercentage).apply();
				changeLayout();
				setTotalPercentages();
				for (AttendanceItem item : mAttendanceItemArrayList) {
					item.setRequiredPercentage(requiredPercentage);
				}
				
				attendanceAdapter.notifyDataSetChanged();
				
				SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
				SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
				Gson gson = new Gson();
				
				String json = gson.toJson(mAttendanceItemArrayList);
				
				if (!attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false) && mAttendanceItemArrayList.size() > 0) {
					attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", true);
				}
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
		});
		
		if (sharedPreferences.getBoolean(REQUIRED_ATTENDANCE_CHOSEN, false)) {
			requiredPercentage = sharedPreferences.getFloat(REQUIRED_ATTENDANCE, 75);
		} else {
			changeLayout();
		}
		
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeLayout();
				double progress = sharedPreferences.getFloat(REQUIRED_ATTENDANCE, 75);
				attendanceRequiredPercentageProgressBarSetter.setProgress((float) progress);
				DecimalFormat decimalFormat = new DecimalFormat("##.#");
				attendanceRequiredPercentageSetter.setText(decimalFormat.format(progress) + "%");
				attendanceRequiredPercentageSeekBarSetter.setProgress((int) progress);
			}
		});
		
		attendanceRequiredPercentageSeekBarSetter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				attendanceRequiredPercentageProgressBarSetter.setProgress(progress);
				DecimalFormat decimalFormat = new DecimalFormat("##.#");
				attendanceRequiredPercentageSetter.setText(decimalFormat.format(progress) + "%");
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			
			}
		});
		
		populateDataAndSetAdapter();
		
		initializeViews();
		
		return rootView;
	}
	
	private void changeLayout() {
		
		if (mainLayout.getVisibility() == View.VISIBLE) {
			Connection.checkConnection(this);
			mainLayout.animate()
					.alpha(0.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayout.setVisibility(View.GONE);
						}
					});
			
			requiredAttendanceLayout.animate()
					.alpha(1.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							requiredAttendanceLayout.setVisibility(View.VISIBLE);
						}
					});
			attendanceName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		} else {
			requiredAttendanceLayout.animate()
					.alpha(0.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							requiredAttendanceLayout.setVisibility(View.GONE);
						}
					});
			
			mainLayout.animate()
					.alpha(1.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayout.setVisibility(View.VISIBLE);
						}
					});
		}
	}
	
	private void populateDataAndSetAdapter() {
		
		final SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
		final SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
		final Gson gson = new Gson();
		
		if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
			String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
			Type type = new TypeToken<List<AttendanceItem>>() {}.getType();
			mEmptyLayout.setVisibility(View.INVISIBLE);
			mAttendanceItemArrayList = gson.fromJson(json, type);
		} else {
			mAttendanceItemArrayList = new ArrayList<>();
			mEmptyLayout.setVisibility(View.VISIBLE);
		}
		
		attendanceAdapter = new AttendanceAdapter(getContext(), mAttendanceItemArrayList, new AttendanceAdapter.AttendanceItemClickListener() {
			@Override
			public void onAttendedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onAttendedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onMissedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onMissedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void editButtonClicked(int position) {
				editSubjectName(position);
			}
			
			@Override
			public void deleteButtonClicked(int position) {
				deleteSubject(position);
			}
		});
		
		mRecyclerView.setAdapter(attendanceAdapter);
	}
	
	private void initializeViews() {
		
		Date date = new Date();
		
		SimpleDateFormat dateFormat;
		
		int day = Calendar.getInstance().get(Calendar.DATE);
		
		if (day >= 11 && day <= 13) {
			dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
		} else {
			switch (day % 10) {
				case 1:
					dateFormat = new SimpleDateFormat("dd'st' MMMM, yyyy", Locale.getDefault());
					break;
				case 2:
					dateFormat = new SimpleDateFormat("dd'nd' MMMM, yyyy", Locale.getDefault());
					break;
				case 3:
					dateFormat = new SimpleDateFormat("dd'rd' MMMM, yyyy", Locale.getDefault());
					break;
				default:
					dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
					break;
			}
		}
		
		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
		dayText.setText(dayFormat.format(date));
		dateText.setText(dateFormat.format(date));
		setTotalPercentages();
		
	}
	
	private void setTotalPercentages() {
		double totalPercentageAttended = 0;
		
		int totalClasses = 0, attendedClasses = 0;
		
		for (AttendanceItem item : mAttendanceItemArrayList) {
			attendedClasses += item.getAttendedClasses();
			totalClasses += item.getTotalClasses();
		}
		
		if (totalClasses > 0) {
			totalPercentageAttended = (double) attendedClasses * 100 / totalClasses;
			DecimalFormat decimalFormat = new DecimalFormat("##.#");
			percentageAttended.setText(decimalFormat.format(totalPercentageAttended) + "%");
		} else {
			percentageAttended.setText(getString(R.string.attendance_item_empty_percentage));
		}
		
		requiredProgressBar.setProgress((float) requiredPercentage);
		attendedProgressBar.setProgress((float) totalPercentageAttended);
		
		if (totalPercentageAttended < requiredPercentage) {
			percentageAttended.setTextColor(getResources().getColor(R.color.lowAttendanceColor, getActivity().getTheme()));
			attendedProgressBar.setProgressBarColor(getResources().getColor(R.color.lowAttendanceColor, getActivity().getTheme()));
			if (totalClasses == 0) {
				attendanceComment.setText(R.string.attendance_no_classes);
			} else {
				attendanceComment.setText(R.string.attendance_not_on_track);
			}
		} else {
			percentageAttended.setTextColor(getResources().getColor(R.color.highAttendanceColor, getActivity().getTheme()));
			attendedProgressBar.setProgressBarColor(getResources().getColor(R.color.highAttendanceColor, getActivity().getTheme()));
			attendanceComment.setText(R.string.attendance_on_track);
		}
		
		if (attendedClasses == 0) {
			percentageAttended.setTextColor(getResources().getColor(R.color.requiredAttendanceColor, getActivity().getTheme()));
		}
	}
	
	private void addSubject() {
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				if (subjectName.length() > 0) {
					
					AttendanceItem newItem = new AttendanceItem(subjectName, requiredPercentage, 0, 0);
					mEmptyLayout.setVisibility(View.INVISIBLE);
					mAttendanceItemArrayList.add(newItem);
					attendanceAdapter.notifyItemInserted(mAttendanceItemArrayList.size());
					setTotalPercentages();
					
					SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
					SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
					Gson gson = new Gson();
					
					String json = gson.toJson(mAttendanceItemArrayList);
					
					if (!attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
						attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", true);
					}
					
					attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
					attendancePreferenceEditor.apply();
				} else {
					Toast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
				}
				builder.dismiss();
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void editSubjectName(final int position) {
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		subjectNameTextInput.getEditText().setText(mAttendanceItemArrayList.get(position).getSubjectName(), TextView.BufferType.EDITABLE);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				
				if (subjectName.length() > 0) {
					mAttendanceItemArrayList.get(position).setSubjectName(subjectNameTextInput.getEditText().getText().toString());
					builder.dismiss();
					attendanceAdapter.notifyItemChanged(position);
					
					SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
					SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
					Gson gson = new Gson();
					
					String json = gson.toJson(mAttendanceItemArrayList);
					
					attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
					attendancePreferenceEditor.apply();
					
				} else {
					Toast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void deleteSubject(final int position) {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Subject");
		builder.setMessage("Are you sure you want to delete attendance record for this subject?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mAttendanceItemArrayList.remove(position);
				attendanceAdapter.notifyItemRemoved(position);
				
				setTotalPercentages();
				
				SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
				SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
				Gson gson = new Gson();
				
				String json = gson.toJson(mAttendanceItemArrayList);
				
				if (mAttendanceItemArrayList.size() == 0) {
					attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", false);
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				attendanceAdapter.notifyItemChanged(position);
			}
		});
		builder.show();
	}
}