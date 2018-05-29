package com.michaldrobny.iamok;

import android.support.annotation.NonNull;

import com.michaldrobny.iamok.model.Day;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Michal Drobny on 25/05/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class Utils {

    public static int[] convertIntegers(@NonNull List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) ret[i] = iterator.next();
        return ret;
    }

    public static String concatenateDays(@NonNull int[] days) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<days.length; i++) {
            sb.append(Day.values()[days[i]].name());
            if (i != days.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
