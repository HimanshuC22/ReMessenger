package com.example.chatapp.models;

/**
 * Created by a_man on 08-11-2017.
 */

public class Country {
    private final String code;
    private final String name;
    private final String dialCode;

    public Country(String code, String name, String dialCode) {
        this.code = code;
        this.name = name;
        this.dialCode = dialCode;
    }

    public String getDialCode() {
        return dialCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getCode()+"(" + getDialCode() + ")";
    }
}
