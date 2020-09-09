package com.tjaklin.groupwakeclock.Home;


import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tjaklin.groupwakeclock.Alarm.AlarmSQLiteFragment;
import com.tjaklin.groupwakeclock.Event.EventsFragment;
import com.tjaklin.groupwakeclock.Friend.FriendsFragment;

class ViewPagerFragmentsAdapter extends FragmentPagerAdapter {

    int numberOfTabs;

    public ViewPagerFragmentsAdapter(FragmentManager fragmentManager, int tabsNo) {
        super(fragmentManager);
        numberOfTabs = tabsNo;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Log.d("[VPFAdapter]", "new GroupFrag returned!");
                return new EventsFragment();

            case 1:
                Log.d("[VPFAdapter]", "new FriendsFrag returned!");
                return new FriendsFragment();

            case 2:
                Log.d("[VPFAdapter]", "new AlarmSQLiteFrag returned!");
                return new AlarmSQLiteFragment();
                
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
