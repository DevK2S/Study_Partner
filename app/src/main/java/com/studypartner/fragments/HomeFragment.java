package com.studypartner.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.HomeAttendanceAdapter;
import com.studypartner.adapters.HomeMediaAdapter;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.models.FileItem;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements HomeMediaAdapter.HomeMediaClickListener, NotesAdapter.NotesClickListener {
	private static final String TAG = "HomeFragment";

	private File noteFolder;

	private MainActivity activity;

	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	private ArrayList<FileItem> notes = new ArrayList<>();
	private ArrayList<FileItem> docsList = new ArrayList<>();
	private ArrayList<FileItem> imagesList = new ArrayList<>();
	private ArrayList<FileItem> videosList = new ArrayList<>();

	private ArrayList<ReminderItem> reminders = new ArrayList<>();
	private CardView reminderCard, emptyReminderCard;
	private ReminderItem reminderItemToBeDisplayed;

	private ArrayList<AttendanceItem> attendances = new ArrayList<>();
	private HomeAttendanceAdapter attendanceAdapter;
	private CardView highAttendanceCard, emptyAttendanceCard;
	private RecyclerView attendanceRecyclerView;
	private RecyclerView imageRecyclerView;
	private RecyclerView videoRecyclerView;
	private RecyclerView docsRecyclerView;
	private HomeMediaAdapter imageAdapter;
	private HomeMediaAdapter videoAdapter;
	private NotesAdapter docsAdapter;

	public HomeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");

		return inflater.inflate(R.layout.fragment_home, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
		activity = (MainActivity) requireActivity();
		
		if (firebaseUser != null && firebaseUser.getEmail() != null) {
			File studyPartnerFolder = new File(String.valueOf(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(requireContext().getExternalFilesDir(null)).getParentFile()).getParentFile()).getParentFile()).getParentFile()), "StudyPartner");
			if (!studyPartnerFolder.exists()) {
				if (studyPartnerFolder.mkdirs()) {
					noteFolder = new File(studyPartnerFolder, firebaseUser.getEmail());
					if (!noteFolder.exists()) Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
				} else {
					noteFolder = new File(requireContext().getExternalFilesDir(null), firebaseUser.getEmail());
					if (!noteFolder.exists()) Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
				}
			} else {
				noteFolder = new File(studyPartnerFolder, firebaseUser.getEmail());
				if (!noteFolder.exists()) Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
			}
		} else {
			FirebaseAuth.getInstance().signOut();
			
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
			googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						activity.mNavController.navigate(R.id.nav_logout);
						activity.finishAffinity();
						activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					} else {
						activity.finishAffinity();
					}
				}
			});
		}
		
		activity.fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.mNavController.navigate(R.id.nav_notes);
			}
		});
		
		initializeReminder(view);
	}
	
	private void initializeReminder(View view) {
		
		reminderCard = view.findViewById(R.id.homeCarouselReminderCard);
		emptyReminderCard = view.findViewById(R.id.homeCarouselEmptyReminderCard);
		
		emptyReminderCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.mNavController.navigate(R.id.nav_reminder, null, activity.leftToRightBuilder.build());
			}
		});
		
		TextView reminderTitle = view.findViewById(R.id.homeCarouselReminderTitle);
		TextView reminderTime = view.findViewById(R.id.homeCarouselReminderTime);
		TextView reminderDate = view.findViewById(R.id.homeCarouselReminderDate);
		TextView reminderDay = view.findViewById(R.id.homeCarouselReminderDay);
		
		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {}.getType();
			reminders = gson.fromJson(json, type);
		} else {
			reminders = new ArrayList<>();
		}
		
		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		
		ArrayList<ReminderItem> remindersToBeRemoved = new ArrayList<>();
		
		for (int position = 0; position < reminders.size(); position++) {
			
			ReminderItem item = reminders.get(position);
			
			int year = Integer.parseInt(item.getDate().substring(6));
			int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
			int day = Integer.parseInt(item.getDate().substring(0, 2));
			int hour = Integer.parseInt(item.getTime().substring(0, 2));
			int minute = Integer.parseInt(item.getTime().substring(3, 5));
			String amOrPm = item.getTime().substring(6);
			if (amOrPm.equals("PM") && hour != 12)
				hour = hour + 12;
			calendar.set(year, month, day, hour, minute);
			if (calendar.compareTo(today) <= 0) {
				remindersToBeRemoved.add(item);
			}
		}
		
		reminders.removeAll(remindersToBeRemoved);
		
		Collections.sort(reminders, new Comparator<ReminderItem>() {
			@Override
			public int compare(ReminderItem o1, ReminderItem o2) {
				int dateCompare = o1.getDate().compareTo(o2.getDate());
				if (dateCompare == 0) {
					return o1.getTime().compareTo(o2.getTime());
				} else {
					return dateCompare;
				}
			}
		});
		
		if (reminders.size() > 0) {
			reminderItemToBeDisplayed = reminders.get(0);
		}
		
		if (reminders.size() == 0 || reminderItemToBeDisplayed == null) {
			
			emptyReminderCard.setVisibility(View.VISIBLE);
			reminderCard.setVisibility(View.INVISIBLE);
			
		} else {
			
			emptyReminderCard.setVisibility(View.INVISIBLE);
			reminderCard.setVisibility(View.VISIBLE);
			
			reminderTitle.setText(reminderItemToBeDisplayed.getTitle());
			reminderTime.setText(reminderItemToBeDisplayed.getTime());
			
			String stringDate = reminderItemToBeDisplayed.getDate();
			
			int year = Integer.parseInt(stringDate.substring(6));
			int month = Integer.parseInt(stringDate.substring(3, 5)) - 1;
			int day = Integer.parseInt(stringDate.substring(0, 2));
			
			calendar.set(year,month,day);
			
			SimpleDateFormat dateFormat;
			
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

			Date date = calendar.getTime();

			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

			reminderDate.setText(dateFormat.format(date));
			reminderDay.setText(dayFormat.format(date));

		}

		initializeAttendance(view);
		populateDataAndSetAdapter(view);
	}
	
	private void initializeAttendance(View view) {
		
		attendanceRecyclerView = view.findViewById(R.id.homeCarouselAttendanceRecyclerView);
		highAttendanceCard = view.findViewById(R.id.homeCarouselHighAttendanceCard);
		emptyAttendanceCard = view.findViewById(R.id.homeCarouselEmptyAttendanceCard);
		
		attendanceRecyclerView.setVisibility(View.INVISIBLE);
		highAttendanceCard.setVisibility(View.INVISIBLE);
		emptyAttendanceCard.setVisibility(View.INVISIBLE);
		
		CircularProgressBar totalAttendedProgressBar = view.findViewById(R.id.homeCarouselAttendanceTotalAttendedProgressBar);
		CircularProgressBar totalRequiredProgressBar = view.findViewById(R.id.homeCarouselAttendanceTotalRequiredProgressBar);
		TextView percentageAttended = view.findViewById(R.id.homeCarouselAttendanceTotalPercentageAttended);
		
		final SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
		final Gson gson = new Gson();
		
		if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
			String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
			Type type = new TypeToken<List<AttendanceItem>>() {}.getType();
			attendances = gson.fromJson(json, type);
		}
		
		if (attendances == null) {
			attendances = new ArrayList<>();
		}
		
		if (attendances.size() > 0) { // Attendance exists atleast 1
			
			double requiredPercentage = attendances.get(0).getRequiredPercentage();
			
			ArrayList<AttendanceItem> attendancesToBeRemoved = new ArrayList<>();
			
			for (AttendanceItem item : attendances) {
				if (item.getTotalClasses() == 0 || item.getAttendedPercentage() >= item.getRequiredPercentage()) { // Has not attended classes or has high attendance
					attendancesToBeRemoved.add(item);
				}
			}
			
			attendances.removeAll(attendancesToBeRemoved);
			
			if (attendances.size() == 0) { // after removing size 0 so all high attendance
				
				attendanceRecyclerView.setVisibility(View.INVISIBLE);
				highAttendanceCard.setVisibility(View.VISIBLE);
				emptyAttendanceCard.setVisibility(View.INVISIBLE);
				
				if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
					String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
					Type type = new TypeToken<List<AttendanceItem>>() {}.getType();
					attendances = gson.fromJson(json, type);
				} else {
					attendances = new ArrayList<>();
				}
				
				if (attendances == null) {
					attendances = new ArrayList<>();
				}
				
				double totalPercentageAttended;
				
				int totalClasses = 0, attendedClasses = 0;
				
				for (AttendanceItem item : attendances) {
					attendedClasses += item.getAttendedClasses();
					totalClasses += item.getTotalClasses();
				}
				
				if (totalClasses > 0) {
					totalPercentageAttended = (double) attendedClasses * 100 / totalClasses;
					DecimalFormat decimalFormat = new DecimalFormat("##.#");
					percentageAttended.setText(decimalFormat.format(totalPercentageAttended) + "%");
					totalAttendedProgressBar.setProgress((float) totalPercentageAttended);
				} else {
					TextView highAttendanceTitle = view.findViewById(R.id.homeCarouselAttendanceTotalTitle);
					TextView highAttendanceSubTitle = view.findViewById(R.id.homeCarouselAttendanceTotalSubTitle);
					
					highAttendanceTitle.setText(R.string.home_carousel_attend_classes);
					highAttendanceSubTitle.setText(R.string.home_carousel_no_classes_attended);
					totalAttendedProgressBar.setProgress((float) 0);
				}
				
				totalRequiredProgressBar.setProgress((float) requiredPercentage);
				
			} else { // low attendance item exists
				
				attendanceRecyclerView.setVisibility(View.VISIBLE);
				highAttendanceCard.setVisibility(View.INVISIBLE);
				emptyAttendanceCard.setVisibility(View.INVISIBLE);
				
				if ((reminders.size() == 0 || reminderItemToBeDisplayed == null) && attendances.size() > 1) { // has attendance, so if no reminder then remove empty reminder layout
					emptyReminderCard.setVisibility(View.GONE);
					reminderCard.setVisibility(View.GONE);
				} // else show both reminder and attendance
				
				attendanceAdapter = new HomeAttendanceAdapter(requireContext(),attendances);
				attendanceRecyclerView.setAdapter(attendanceAdapter);
				LinearLayoutManager manager = new LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false);
				attendanceRecyclerView.setLayoutManager(manager);
				attendanceRecyclerView.setItemAnimator(new DefaultItemAnimator());
			}

		} else { // no attendance item exists

			attendanceRecyclerView.setVisibility(View.INVISIBLE);
			highAttendanceCard.setVisibility(View.INVISIBLE);
			emptyAttendanceCard.setVisibility(View.VISIBLE);

		}

	}

	private void populateDataAndSetAdapter(View view) {

		imageRecyclerView = view.findViewById(R.id.homeRecyclerViewImage);
		videoRecyclerView = view.findViewById(R.id.homeRecyclerViewVideo);
		docsRecyclerView = view.findViewById(R.id.homeRecyclerViewDocs);
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);

		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);

			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);

		notes = new ArrayList<>();

		addRecursively(noteFolder);

		//notes.addAll(links);

		Collections.sort(notes, new Comparator<FileItem>() {
			@Override
			public int compare(FileItem o1, FileItem o2) {
				return o2.getDateModified().compareTo(o1.getDateModified());
			}
		});


		for (FileItem f : notes) {
			if (f.getType() == FileType.FILE_TYPE_IMAGE && imagesList.size() <= 9)
				imagesList.add(f);
			else if (f.getType() == FileType.FILE_TYPE_VIDEO && videosList.size() <= 9)
				videosList.add(f);
			else if ((f.getType() == FileType.FILE_TYPE_TEXT || f.getType() == FileType.FILE_TYPE_APPLICATION) && docsList.size() <= 9)
				docsList.add(f);
		}
		GridLayoutManager managerImage = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
		GridLayoutManager managerVideo = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
		imageRecyclerView.setLayoutManager(managerImage);
		imageAdapter = new HomeMediaAdapter(getActivity(), imagesList, new HomeMediaAdapter.HomeMediaClickListener() {
			@Override
			public void onClick(int position) {
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("HomeMedia", imagesList);
				bundle.putInt("Position", position);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_home_to_mediaActivity, bundle);
			}
		});
		imageRecyclerView.setAdapter(imageAdapter);
		videoRecyclerView.setLayoutManager(managerVideo);
		videoAdapter = new HomeMediaAdapter(getActivity(), videosList, new HomeMediaAdapter.HomeMediaClickListener() {
			@Override
			public void onClick(int position) {
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("HomeMedia", videosList);
				bundle.putInt("Position", position);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_home_to_mediaActivity, bundle);
			}
		});
		videoRecyclerView.setAdapter(videoAdapter);
		docsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		docsAdapter = new NotesAdapter(getActivity(), docsList, this, false);
		docsRecyclerView.setAdapter(docsAdapter);
	}
	
	private void addRecursively(File folder) {
		FileItem item = new FileItem(folder.getPath());
		if (folder.exists()) {
			if (folder.isDirectory()) {
				File[] files = folder.listFiles();
				if (files != null && files.length > 0) {
					for (File file : files) {
						addRecursively(file);
					}
				}
			} else {
				if (isStarred(item)) {
					item.setStarred(true);
				}
				notes.add(item);
				
			}
		}
	}
	
	private boolean isStarred (FileItem item) {
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(item.getPath())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(int position) {
		FileUtils.openFile(requireContext(), docsList.get(position));
	}

	@Override
	public void onLongClick(int position) {

	}

	@Override
	public void onOptionsClick(View view, int position) {

	}
}