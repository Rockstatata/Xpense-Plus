// app/src/main/java/com/example/myapplication/adapters/ViewPagerAdapter.java
package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.views.fragments.Stats;
import com.example.myapplication.views.fragments.TransactionsPage;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TransactionsPage();
            case 1:
                return new Stats();
            default:
                return new TransactionsPage();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}