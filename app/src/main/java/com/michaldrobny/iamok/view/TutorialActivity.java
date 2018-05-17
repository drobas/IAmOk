package com.michaldrobny.iamok.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.TutorialPage;

import jp.wasabeef.blurry.Blurry;

public class TutorialActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    TutorialPagerAdapter mTutorialPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorial);

        mTutorialPagerAdapter =
                new TutorialPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mTutorialPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        ((TextView) findViewById(R.id.tutorial_tv1))
                .setText(TutorialPage.getDescription(TutorialPage.Expedition));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);
    }

    public void continueButtonClick(View view) {
        if  (mViewPager.getCurrentItem()+1 == TutorialFragment.PAGE_COUNT) {
            skipButtonClick(null);
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1, true);
        }
    }

    public void skipButtonClick(View view) {
        Intent intent = new Intent(TutorialActivity.this, InitiatorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageSelected(int position) {
        TutorialPage page = TutorialPage.values()[position];
        ((TextView) findViewById(R.id.tutorial_tv1))
                .setText(TutorialPage.getDescription(page));
        ((Button) findViewById(R.id.tutorial_skip_b))
                .setVisibility(position+1 == TutorialFragment.PAGE_COUNT ?
                        View.INVISIBLE : View.VISIBLE);
    }

    class TutorialPagerAdapter extends FragmentStatePagerAdapter {

        TutorialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt(TutorialFragment.ARG_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return TutorialFragment.PAGE_COUNT;
        }
    }

    public static class TutorialFragment extends Fragment {
        public static final String ARG_NUMBER = "number";
        public static final int PAGE_COUNT = 4;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.fragment_tutorial, container, false);
            Bundle args = getArguments();
            assert (args != null);

            TutorialPage page = TutorialPage.values()[args.getInt(TutorialFragment.ARG_NUMBER, 0)];
            ((ImageView) rootView.findViewById(R.id.tutorial_iv))
                    .setImageDrawable(getResources().getDrawable(TutorialPage.getImageResource(page)));

            return rootView;
        }
    }
}