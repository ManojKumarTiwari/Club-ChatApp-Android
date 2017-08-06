package com.maddyplays.club;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Switch;

/**
 * Created by Maddy on 6/23/2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter{
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3; //3 because we have 3 tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "REQUEST";

            case 1:
                return "CHATS";

            case 2:
                return "MEMBERS";
                
            default:
                return null;
        }
    }
}
