package me.ele.homedemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import me.ele.omniknight.OKActivity;


public class MainActivity extends OKActivity {

    private ViewPager viewPager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        final MyAdapter adapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                String title = null;
                switch (position) {
                    case 0:
                        title = "4条数据";
                        break;
                    case 1:
                        title = "7条数据";
                        break;
                    case 2:
                        title = "20条数据";
                        break;
                    case 3:
                        title = "页面" + position;
                        break;
                }
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
    }

    private class MyAdapter extends FragmentStatePagerAdapter {

        private MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = FirstFragment.newInstance(4);
                    break;
                case 1:
                    fragment = FirstFragment.newInstance(7);
                    break;
                case 2:
                    fragment = FirstFragment.newInstance(20);
                    break;
                case 3:
                    fragment = SecondFragment.newInstance(position);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
