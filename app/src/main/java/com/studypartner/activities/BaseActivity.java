package com.studypartner.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.navigation.NavigationView;
import com.studypartner.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseActivity";

    private static final float END_SCALE = 0.7f;
    protected FrameLayout frameLayout;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ConstraintLayout contentView;

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_activity);

        //checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
        frameLayout= findViewById(R.id.baseScreenFrameLayout);
        drawerLayout = findViewById(R.id.baseScreenDrawerLayout);
        contentView = findViewById(R.id.baseScreenConstraintLayout);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });

        findViewById(R.id.baseScreenMenuImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        navigationView = findViewById(R.id.baseScreenNavigationView);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.navigationMenuHome);

    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigationMenuHome:
                return true;
            case R.id.navigationMenuNotes:
                return true;
            case R.id.navigationMenuAttendance:
                return true;
            case R.id.navigationMenuReminder:
                return true;
            case R.id.navigationMenuProfile:
                return true;
            case R.id.navigationMenuLogout:
                return true;
            case R.id.navigationMenuSettings:
                return true;
            case R.id.navigationMenuDarkMode:
                return true;
            default:
                return false;
        }
    }
}