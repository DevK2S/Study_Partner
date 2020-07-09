package com.studypartner.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.studypartner.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseActivity";

    private static final float END_SCALE = 0.7f;
    
    protected FrameLayout frameLayout;
    protected NavigationView navigationView;
    private DrawerLayout drawerLayout;
    protected ConstraintLayout contentView;
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout topAppBarLayout;
    protected MaterialToolbar topAppBar;
    protected BottomAppBar bottomAppBar;
    protected BottomNavigationView bottomNavigationView;
    protected FloatingActionButton fabMenu;
    //private LinearLayout fab_createFolderLayout,fab_addFileLayout,fab_addImageLayout;
   // private FloatingActionButton fab_createFolder,fab_addFile,fab_addImage;
    Boolean fab_Open=false;
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        /*else if (fab_Open)
        {
            closeFab();
        }*/
            else {
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
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        topAppBarLayout=findViewById(R.id.topAppBarLayout);
        topAppBar=findViewById(R.id.topAppBar);
        navigationView = findViewById(R.id.baseScreenNavigationView);
        bottomAppBar=(findViewById(R.id.bottomAppBar));
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        //fabMenu=findViewById(R.id.fab_menu);
        //fab_createFolder=findViewById(R.id.fab_createFolder);
       // fab_createFolderLayout=findViewById(R.id.fab_createFolderLayout);
        //fab_addFileLayout=findViewById(R.id.fab_addFileLayout);
       // fab_addFile=findViewById(R.id.fab_addFile);
       // fab_addImageLayout=findViewById(R.id.fab_addImageLayout);
       // fab_addImage=findViewById(R.id.fab_addImage);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //if(fab_Open)
                    //closeFab();
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                coordinatorLayout.setScaleX(offsetScale);
                coordinatorLayout.setScaleY(offsetScale);


                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = coordinatorLayout.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                coordinatorLayout.setTranslationX(xTranslation);
            }
        });

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    /*if(fab_Open)
                        closeFab();*/
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });


        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        //bottomNavigationView.setOnNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.navigationMenuHome:
                return true;
            case R.id.navigationMenuNotes:
                return true;
            case R.id.navigationMenuAttendance:
                return true;*/
            case R.id.navigationMenuReminder:
                return true;
            case R.id.navigationMenuProfile:
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            case R.id.navigationMenuLogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                finishAffinity();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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