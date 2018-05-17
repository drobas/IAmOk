package com.michaldrobny.iamok.model;

import com.michaldrobny.iamok.R;

/**
 * Created by Michal Drobny on 08/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public enum TutorialPage {
    Expedition,
    Meeting,
    Senior,
    Solution;

    public static int getDescription(TutorialPage page) {
        switch (page) {
            case Expedition:
                return R.string.tutorial_expedition;
            case Meeting:
                return R.string.tutorial_place;
            case Senior:
                return R.string.tutorial_senior;
            case Solution:
                return R.string.tutorial_solution;

        }

        return 0;
    }

    public static int getImageResource(TutorialPage page) {
        switch (page) {
            case Expedition:
                return R.drawable.expedition;
            case Meeting:
                return R.drawable.meeting;
            case Senior:
                return R.drawable.senior;
            case Solution:
                return R.drawable.solution;
        }

        return 0;
    }
}