package com.michaldrobny.iamok.model;

import com.michaldrobny.iamok.R;

/**
 * Created by Michal Drobny on 05/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public enum ServiceType {
    Unknown,
    SpecificTime,
    PeriodicTime,
    Place,
    SOS;

    public static int getString(ServiceType type) {
        switch (type) {
            case SpecificTime:
                return R.string.services_type_specific;
            case PeriodicTime:
                return R.string.services_type_periodic;
            case SOS:
                return R.string.services_type_sos;
            case Place:
                return R.string.services_type_place;
                default:
                    return R.string.base_unknown;
        }
    }
}