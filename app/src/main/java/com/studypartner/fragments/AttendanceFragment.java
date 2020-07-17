package com.studypartner.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.AttendanceAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.utils.Connection;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceFragment extends Fragment {
	private static final String TAG = "AttendanceFragment";
	
	private final String REQUIRED_ATTENDANCE_CHOSEN = "requiredAttendanceChosen";
	private final String REQUIRED_ATTENDANCE = "requiredAttendance";
	
	private DatabaseReference mDatabaseReference;
	
	private ConstraintLayout mainLayout, requiredAttendanceLayout;
	private ProgressBar loadingProgressBar;
	
	private RecyclerView mRecyclerView;
	private BottomAppBar mBottomAppBar;
	private FloatingActionButton mfab, attendanceRequiredPercentageFabNext;
	private NavController mNavController;
	private LinearLayout mEmptyLayout;
	private Button addButton, updateButton;
	private TextView dateText, dayText, percentageAttended, attendanceComment, attendanceName, attendanceRequiredPercentageSetter;
	private CircularProgressBar attendedProgressBar, requiredProgressBar, attendanceRequiredPercentageProgressBarSetter;
	private SeekBar attendanceRequiredPercentageSeekBarSetter;
	
	private ArrayList<AttendanceItem> mAttendanceItemArrayList;
	private AttendanceAdapter attendanceAdapter;
	private SharedPreferences sharedPreferences;
	
	private double requiredPercentage;
	
	public AttendanceFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
		
		Log.d(TAG, "onCreateView: creating or using database at attendance");
		mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("attendance").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
		
		requiredAttendanceLayout = rootView.findViewById(R.id.attendanceRequiredAttendanceConstraintLayout);
		
		attendanceName = rootView.findViewById(R.id.attendanceName);
		attendanceRequiredPercentageSetter = rootView.findViewById(R.id.attendanceRequiredPercentageSetter);
		attendanceRequiredPercentageProgressBarSetter = rootView.findViewById(R.id.attendanceRequiredProgressBarSetter);
		attendanceRequiredPercentageSeekBarSetter = rootView.findViewById(R.id.attendanceRequiredSeekBarSetter);
		attendanceRequiredPercentageFabNext = rootView.findViewById(R.id.attendanceRequiredFab);
		
		mainLayout = rootView.findViewById(R.id.attendanceMainConstraintLayout);
		
		loadingProgressBar = rootView.findViewById(R.id.attendanceLoadingProgressBar);
		
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
		
		sharedPreferences = requireActivity().getSharedPreferences("RequiredPercentageSelected", Context.MODE_PRIVATE);
		
		loadingProgressBar.setVisibility(View.VISIBLE);
		mainLayout.setEnabled(false);
		updateButton.setEnabled(false);
		addButton.setEnabled(false);
		
		mAttendanceItemArrayList = new ArrayList<>();
		
		mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.d(TAG, "onDataChange: starts");
				if (snapshot.exists()) {
					mEmptyLayout.setVisibility(View.INVISIBLE);
					for (DataSnapshot attendance : snapshot.getChildren()) {
						Log.d(TAG, "onDataChange: adding attendance records");
						AttendanceItem attendanceItem = attendance.getValue(AttendanceItem.class);
						Log.d(TAG, "onDataChange: id " + attendanceItem.getId() + " sub name " + attendanceItem.getSubjectName());
						mAttendanceItemArrayList.add(attendanceItem);
						attendanceAdapter.notifyItemInserted(mAttendanceItemArrayList.size());
					}
					setTotalPercentages();
				} else {
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				loadingProgressBar.setVisibility(View.INVISIBLE);
				mainLayout.setEnabled(true);
				updateButton.setEnabled(true);
				addButton.setEnabled(true);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				
			}
		});
		
		MainActivity activity = (MainActivity) requireActivity();
		mBottomAppBar = activity.mBottomAppBar;
		mfab = activity.fab;
		mNavController = NavHostFragment.findNavController(this);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				mBottomAppBar.performShow();
				mfab.show();
				mNavController.navigate(R.id.action_nav_attendance_to_nav_home);
			}
		});
		
		attendanceAdapter = new AttendanceAdapter(getContext(), mAttendanceItemArrayList, new AttendanceAdapter.AttendanceItemClickListener() {
			@Override
			public void onAttendedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).setValue(mAttendanceItemArrayList.get(position)).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: attended classes changed");
						} else {
							Log.d(TAG, "onComplete: attended classes could not be changed");
						}
					}
				});
			}
			
			@Override
			public void onAttendedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).setValue(mAttendanceItemArrayList.get(position)).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: attended classes changed");
						} else {
							Log.d(TAG, "onComplete: attended classes could not be changed");
						}
					}
				});
			}
			
			@Override
			public void onMissedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).setValue(mAttendanceItemArrayList.get(position)).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: missed classes changed");
						} else {
							Log.d(TAG, "onComplete: missed classes could not be changed");
						}
					}
				});
			}
			
			@Override
			public void onMissedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).setValue(mAttendanceItemArrayList.get(position)).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: missed classes changed");
						} else {
							Log.d(TAG, "onComplete: missed classes could not be changed");
						}
					}
				});
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
					
					mDatabaseReference.child(item.getId()).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: required percentage changed");
								attendanceAdapter.notifyDataSetChanged();
							} else {
								Log.d(TAG, "onComplete: required percentage could not be changed");
							}
						}
					});
				}
				
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
		
		initializeViews();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume: bringing bottom app bar to front");
		mBottomAppBar.bringToFront();
		mfab.hide();
		super.onResume();
	}
	
	private void changeLayout() {
		if (mainLayout.getVisibility() == View.VISIBLE) {
			mainLayout.setVisibility(View.GONE);
			requiredAttendanceLayout.setVisibility(View.VISIBLE);
			attendanceName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		} else {
			mainLayout.setVisibility(View.VISIBLE);
			requiredAttendanceLayout.setVisibility(View.GONE);
		}
	}
	
	private void initializeViews() {
		
		Date date = new Date();
		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
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
		Log.d(TAG, "addSubject: add subject clicked");
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: cancel pressed while adding subject");
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: ok pressed while adding subject");
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				if (subjectName.length() > 0) {
					Log.d(TAG, "onClick: adding new subject");
					
					AttendanceItem newItem = new AttendanceItem(subjectName, requiredPercentage, 0, 0);
					mEmptyLayout.setVisibility(View.GONE);
					mAttendanceItemArrayList.add(newItem);
					attendanceAdapter.notifyItemInserted(mAttendanceItemArrayList.size());
					setTotalPercentages();
					
					DatabaseReference databaseReference = mDatabaseReference.child(newItem.getId());
					databaseReference.setValue(newItem);
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
		Log.d(TAG, "editSubjectName: edit button clicked");
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		subjectNameTextInput.getEditText().setText(mAttendanceItemArrayList.get(position).getSubjectName(), TextView.BufferType.EDITABLE);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: cancel pressed while changing subject");
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: ok pressed while changing subject");
				
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				
				if (subjectName.length() > 0) {
					mAttendanceItemArrayList.get(position).setSubjectName(subjectNameTextInput.getEditText().getText().toString());
					builder.dismiss();
					mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).setValue(mAttendanceItemArrayList.get(position)).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: subject name changed");
								attendanceAdapter.notifyItemChanged(position);
							} else {
								Log.d(TAG, "onComplete: subject name could not be changed");
							}
						}
					});
				} else {
					Toast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void deleteSubject(final int position) {
		Log.d(TAG, "deleteSubject: delete button clicked");
		mBottomAppBar.performShow();
		loadingProgressBar.setVisibility(View.VISIBLE);
		mainLayout.setEnabled(false);
		updateButton.setEnabled(false);
		addButton.setEnabled(false);
		
		mDatabaseReference.child(mAttendanceItemArrayList.get(position).getId()).removeValue(new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
				if (error == null) {
					Log.d(TAG, "onComplete: data deleted");
					
					attendanceAdapter.notifyItemRemoved(position);
					
					mAttendanceItemArrayList.remove(position);
					
					setTotalPercentages();
					
					loadingProgressBar.setVisibility(View.INVISIBLE);
					mainLayout.setEnabled(true);
					updateButton.setEnabled(true);
					addButton.setEnabled(true);
					
					if (mAttendanceItemArrayList.size() == 0) {
						mEmptyLayout.setVisibility(View.VISIBLE);
					}
					
				} else {
					Log.d(TAG, "onComplete: data could not be deleted");
					loadingProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
}