package com.studypartner.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.AlertReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;


public class ReminderDialogFragment extends DialogFragment {

    private TextInputLayout TitleLayout;
    private TextInputLayout ContentLayout;
    private TextInputEditText TitleEditText;
    private TextInputEditText ContentEditText;
    private TextView DateEditText;
    private TextView TimeEditText;
    private NavController mNavController;
    private DatePickerDialog DatePicker;
    private TimePickerDialog TimePicker;
    private FloatingActionButton okButton;
    String time;
    String date;
    Boolean edit = false;
    private ArrayList<ReminderItem> mReminderList = new ArrayList<>();

    public ReminderDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminder_dialog, container, false);
        int positionToEdit = -1;
        if (getArguments() != null) {
            edit = true;
            positionToEdit = Integer.parseInt(String.valueOf(getArguments().getString("ItemPosition")));
        }

        TitleEditText = rootView.findViewById(R.id.titleEditText);
        ContentEditText = rootView.findViewById(R.id.contentEditText);
        DateEditText = rootView.findViewById(R.id.Date);
        TimeEditText = rootView.findViewById(R.id.editTextTime);
        okButton = rootView.findViewById(R.id.okButton);
        mNavController = NavHostFragment.findNavController(this);
        final Calendar cldr = Calendar.getInstance();
        if (edit) {
            editShow(positionToEdit);
        } else {
            TimeEditText.setText("00:00 AM");
            DateEditText.setText("Pick a Date");
        }

        final SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
        final Gson gson = new Gson();
        final SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();

        if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
            String json = reminderPreference.getString("REMINDER_ITEMS", "");
            Type type = new TypeToken<ArrayList<ReminderItem>>() {
            }.getType();
            mReminderList = gson.fromJson(json, type);
        } else {
            mReminderList = new ArrayList<>();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                dismiss();
                mNavController.navigate(R.id.action_reminderDialogFragment_to_nav_reminder);
            }
        });

        DateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                DatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker datePicker, int i, int i1, int i2) {
                        String spm = "";
                        String spd = "";
                        if (i1 < 9)
                            spm = "0";
                        if (i2 < 10)
                            spd = "0";
                        date = spd + Integer.toString(i2) + "-" + spm + Integer.toString(i1 + 1) + "-" + Integer.toString(i);
                        DateEditText.setText(date);
                    }
                }, year, month, day);
                //updateDisplay();
                DatePicker.setTitle("Pick a Date");
                DatePicker.show();
            }
        });

        TimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minute = cldr.get(Calendar.MINUTE);
                TimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker timePicker, int i, int i1) {
                        String ampm = " AM";
                        String sph = "";
                        String spm = "";
                        if (i > 12) {
                            i = i - 12;
                            ampm = " PM";
                        }
                        if (i < 10)
                            sph = "0";
                        if (i1 < 10)
                            spm = "0";
                        time = sph + i + ":" + spm + i1 + ampm;
                        TimeEditText.setText(time);
                    }
                }, hour, minute, false);

                TimePicker.show();
            }
        });
        final int finalPositionToEdit = positionToEdit;
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TEST", "ok button");
                int notifyId;
                // ReminderItem item = new ReminderItem( String.valueOf(TitleEditText.getText()),String.valueOf(ContentEditText.getText()),time,date);
                if (edit) {
                    mReminderList.get(finalPositionToEdit).Edit(String.valueOf(TitleEditText.getText()), String.valueOf(ContentEditText.getText()), String.valueOf(DateEditText.getText()), String.valueOf(TimeEditText.getText()));
                    notifyId = mReminderList.get(finalPositionToEdit).getnotifyId();
                } else {
                    ReminderItem item = new ReminderItem(String.valueOf(TitleEditText.getText()), String.valueOf(ContentEditText.getText()), time, date);
                    notifyId = item.getnotifyId();
                    mReminderList.add(item);
                    createNotificaiton(item);
                }
                Log.d("TEST", String.valueOf(notifyId));
                String json = gson.toJson(mReminderList);
                if (!reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
                    reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", true);
                }

                reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
                reminderPreferenceEditor.apply();
                //dismiss();
                mNavController.navigate(R.id.action_reminderDialogFragment_to_nav_reminder);
            }
        });
        return rootView;
    }

    public void editShow(int position) {
        final SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
        final Gson gson = new Gson();
        final SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();

        String json = reminderPreference.getString("REMINDER_ITEMS", "");
        Type type = new TypeToken<ArrayList<ReminderItem>>() {
        }.getType();
        mReminderList = gson.fromJson(json, type);
        ReminderItem editItem = mReminderList.get(position);
        TitleEditText.setText(editItem.getTitle());
        ContentEditText.setText(editItem.getDes());
        DateEditText.setText(editItem.getDate());
        TimeEditText.setText(editItem.getTime());

    }

    public void createNotificaiton(ReminderItem item) {
        Calendar c = Calendar.getInstance();
        int year = Integer.parseInt(item.getDate().substring(6));
        int month = Integer.parseInt(item.getDate().substring(3, 5));
        int day = Integer.parseInt(item.getDate().substring(0, 2));
        int hour = Integer.parseInt(item.getTime().substring(0, 2));
        int minute = Integer.parseInt(item.getTime().substring(3, 5));
        String AMPM = item.getTime().substring(6);
        if (AMPM.equals("PM"))
            hour = hour + 12;
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlertReceiver.class);
        Log.d(TAG, "createNotificaiton: item " + item.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable("Item", item);
        intent.putExtra("Item", bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        Log.d("Test", String.valueOf(c.getTime()));
    }
}