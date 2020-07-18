package com.studypartner.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.ContentValues.*;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.FileItem;
import com.studypartner.utils.Connection;


public class BasicNotesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "BasicNotesFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BasicNotesFragment() {
        // Required empty public constructor
    }

    /*public static BasicNotesFragment newInstance(String param1, String param2) {
        BasicNotesFragment fragment = new BasicNotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    //@Override
    /*public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d(TAG, "onCreateView: starts");

        Connection.checkConnection(this);

        View rootView = inflater.inflate(R.layout.fragment_basic_notes, container, false);
        final MainActivity activity = (MainActivity) requireActivity();
        activity.mBottomAppBar.bringToFront();
        activity.fab.bringToFront();

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: starts");
                //fab.setOnClickListener(null);
                activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
            }
        });
        TextView text=rootView.findViewById(R.id.textView);
        String FileDes=getArguments().getString("FileDes");
        text.setText(FileDes);
        return rootView;
    }
}