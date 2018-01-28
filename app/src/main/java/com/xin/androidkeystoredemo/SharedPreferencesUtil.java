package com.xin.androidkeystoredemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xin on 28/01/2018.
 * SharedPreference ConvertUtil stashed IV and encrypted data.
 */

public class SharedPreferencesUtil {

    private static SharedPreferencesUtil instance;

    private static final String SP_NAME = "db";

    private SharedPreferences sp;

    public static SharedPreferencesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtil(context);
            return instance;
        }
        return instance;
    }

    private SharedPreferencesUtil(Context context) {
        this.sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void saveData(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public String getData(String key) {
        return sp.getString(key, "");
    }
}
