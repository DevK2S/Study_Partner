package com.studypartner.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.studypartner.R;
import com.studypartner.adapters.MediaAdapter;
import com.studypartner.fragments.MediaFragment;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    private static final String TAG = "Media ";
    ViewPager2 viewPager;
    MediaAdapter mediaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Media Activity");
        setContentView(R.layout.activity_media);
        viewPager = findViewById(R.id.viewPager2);
        Intent intent = getIntent();
        String path = intent.getStringExtra("Media");
        File file = new File(path);
        File parentFile = file.getParentFile();
        MediaData(parentFile, file);
    }

    public void MediaData(File parent, File child) {
        ArrayList<String> mediafiles = new ArrayList<>();
        File[] files = parent.listFiles();
        int value = 0;
        for (File f : files) {
            FileItem newFile = new FileItem(f.getPath());

            if (newFile.getType() == FileType.FILE_TYPE_VIDEO) {
                mediafiles.add(newFile.getPath());
                if (f.getName().equals(child.getName()))
                    value = mediafiles.size() - 1;
            }
        }
        mediaAdapter = new MediaAdapter(getSupportFragmentManager(), getLifecycle());
        for (String s : mediafiles) {
            mediaAdapter.addFragment(MediaFragment.newInstance(s));
        }

        viewPager.setAdapter(mediaAdapter);
        viewPager.setCurrentItem(value, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                try {
                    (mediaAdapter.createFragment(position - 1)).isHidden();
                    (mediaAdapter.createFragment(position)).isVisible();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


    }
}