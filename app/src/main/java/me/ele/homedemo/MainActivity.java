package me.ele.homedemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;


public class MainActivity extends ActionBarActivity {

    private ViewPager viewPager;
    private Toolbar toolbar;
    private boolean isToolbarVisible = true;
    private int toolbarWidth;
    private int toolbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitle("Home Demo");

        toolbar.inflateMenu(R.menu.menu_main);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0 && !isToolbarVisible) {
                    
                    showToolbar();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class MyAdapter extends FragmentStatePagerAdapter {

        private SparseArray<Fragment> array;

        private MyAdapter(FragmentManager fm) {
            super(fm);
            array = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = array.get(position);
            if (fragment != null) {
                return fragment;
            }
            switch (position) {
                case 0:
                    fragment = new FirstFragment();
                    break;
                case 1:
                case 2:
                case 3:
                    fragment = SecondFragment.newInstance("页面" + position);
                    break;
            }
            array.put(position, fragment);
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

    public void moveToolbar(int offset) {
        if (toolbarWidth == 0) {
            toolbarWidth = toolbar.getWidth();
            toolbarHeight = toolbar.getHeight();
        }
        if (offset <= 0) {
            showToolbar();
        } else if (offset < toolbar.getHeight()) {
            toolbar.setTranslationY(-offset);
        } else {
            hideToolbar();
        }

    }

    public void showToolbar() {
        if (isToolbarVisible) {
            return;
        }
        isToolbarVisible = true;
        toolbar.animate().translationY(0).start();
    }

    public void hideToolbar() {
        if (!isToolbarVisible) {
            return;
        }
        isToolbarVisible = false;
        toolbar.animate().translationY(-toolbar.getHeight()).start();
    }

}
