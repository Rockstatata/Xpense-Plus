package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.activity.OnBackPressedCallback;

import com.example.myapplication.views.fragments.DashboardPage;
import com.example.myapplication.views.fragments.Discover;
import com.example.myapplication.views.fragments.Handshake;
import com.example.myapplication.views.fragments.Profile;
import com.example.myapplication.R;
import com.example.myapplication.views.fragments.TransactionsPage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//                if (currentFragment instanceof DashboardPage) {
//                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
//                } else if (currentFragment instanceof TransactionsPage) {
//                    bottomNavigationView.setSelectedItemId(R.id.nav_transactions);
//                } else if (currentFragment instanceof Profile) {
//                    bottomNavigationView.setSelectedItemId(R.id.nav_profile);
//                } else if (currentFragment instanceof Handshake) {
//                    bottomNavigationView.setSelectedItemId(R.id.nav_Handshake);
//                }
//            }
//        });

        if (savedInstanceState == null) {
            replaceFragment(new DashboardPage());
        }
    }

    protected void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardPage();
                } else if (itemId == R.id.nav_transactions) {
                    selectedFragment = new TransactionsPage();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new Profile();
                } else if (itemId == R.id.nav_Handshake) {
                    selectedFragment = new Handshake();
                }
                else if (itemId == R.id.nav_discover) {
                    selectedFragment = new Discover();
                }

                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}