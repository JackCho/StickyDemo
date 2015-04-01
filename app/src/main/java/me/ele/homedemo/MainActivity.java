package me.ele.homedemo;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import me.ele.omniknight.OKActivity;


public class MainActivity extends OKActivity {

    private ViewPager viewPager;
    private Toolbar toolbar;
    private int toolbarHeight;
    private boolean isShowAnimating = false;
    private boolean isHideAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        final MyAdapter adapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0 && !isToolbarShown()) {
                    showToolbar();
                    eventBus.post(new FirstFragment.FixUnderToolbarEvent());
                }
            }

            @Override
            public void onPageSelected(int position) {
                String title = null;
                switch (position) {
                    case 0:
                        title = "首页";
                        break;
                    case 1:
                    case 2:
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
                    fragment = new FirstFragment();
                    break;
                case 1:
                case 2:
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
    
    private void checkToolbarSize() {
        if (toolbarHeight == 0) {
            toolbarHeight = toolbar.getHeight();
        }
    }

    public boolean moveToolbar(int offset) {
        checkToolbarSize();
        
        if (isShowAnimating || isHideAnimating) {
            return false;
        }
        
        toolbar.setTranslationY(-offset);
        return true;
    }

    public boolean showToolbar() {
        checkToolbarSize();
        
        if (isShowAnimating) {
            return false;
        }
        
        if (toolbar.getTranslationY() == -toolbarHeight) {
            isShowAnimating = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).setListener(new SimpleAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isShowAnimating = false;
                }
            }).start();
            return true;
        }

        return false;
    }
    
    public boolean isToolbarShown() {
        return toolbar.getTranslationY() == 0;
    }
    
    public boolean hideToolbar() {
        checkToolbarSize();
        
        if (isHideAnimating) {
            return false;
        }

        if (toolbar.getTranslationY() == 0) {
            isHideAnimating = true;
            toolbar.animate().translationY(-toolbarHeight).setInterpolator(new DecelerateInterpolator()).setListener(new SimpleAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isHideAnimating = false;
                }
            }).start();
            return true;
        }

        return false;
    }
    
}
