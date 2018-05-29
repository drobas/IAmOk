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

import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.TutorialPage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TutorialActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.tutorial_root_view) ViewGroup viewGroup;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.tutorial_tv1) TextView textView;
    @BindView(R.id.tutorial_skip_button) Button skipButton;
    @BindView(R.id.tutorial_next_tip_button) Button nextTipButton;
    @BindView(R.id.tabDots) TabLayout tabLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        viewPager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(this);
        textView.setText(TutorialPage.getDescription(TutorialPage.values()[0]));
        tabLayout.setupWithViewPager(viewPager, true);
    }

   @OnClick(R.id.tutorial_next_tip_button) void nextTipOnClick() {
        int currentPosition = viewPager.getCurrentItem();
        if (currentPosition == TutorialFragment.PAGE_COUNT-1) {
            // last position
            skipButtonClick();
        } else {
            viewPager.setCurrentItem(currentPosition+1, true);
        }
    }

    @OnClick(R.id.tutorial_skip_button) public void skipButtonClick() {
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
        textView.setText(TutorialPage.getDescription(page));
        if (position+1 == TutorialFragment.PAGE_COUNT) {
            // last screen
            skipButton.setVisibility(View.INVISIBLE);
            nextTipButton.setText(R.string.base_continue);
        } else {
            skipButton.setVisibility(View.VISIBLE);
            nextTipButton.setText(R.string.tutorial_next_tip);
        }
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

        public static final String ARG_NUMBER = "arg_number";
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