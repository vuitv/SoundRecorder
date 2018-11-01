package com.vuitv.soundrecorder;


import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TableLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.vuitv.soundrecorder.fragment.FileViewerFragment;
import com.vuitv.soundrecorder.fragment.RecordFragment;
import com.vuitv.soundrecorder.fragment.SettingFragment;

public class MainActivity extends AppCompatActivity {
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabs = findViewById(R.id.tabs);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);

        final String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings),
                getString(R.string.tab_title_setting)};

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setTitle(titles[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        /*private String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings),
                getString(R.string.tab_title_setting)};*/

        private Drawable[] imgTitles = {getResources().getDrawable(R.drawable.ic_mic_28dp),
                getResources().getDrawable(R.drawable.ic_play_28dp),
                getResources().getDrawable(R.drawable.ic_settings_white)};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return RecordFragment.newInstance(position);
                }
                case 1: {
                    return FileViewerFragment.newInstance(position);
                }
                case 2: {
                    return SettingFragment.newInstance(position);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return imgTitles.length;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            SpannableString spannableString = new SpannableString(" ");
            Drawable drawable = imgTitles[position];
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            spannableString.setSpan(span, 0, 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }


    }


}
