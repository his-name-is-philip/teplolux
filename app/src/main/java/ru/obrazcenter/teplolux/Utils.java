package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.res.Configuration.KEYBOARD_NOKEYS;
import static android.util.DisplayMetrics.DENSITY_DEFAULT;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static ru.obrazcenter.teplolux.Main.mainActivity;
import static ru.obrazcenter.teplolux.StartActivity.activity1;
import static ru.obrazcenter.teplolux.StartActivity.theProject;

enum Utils {
    ;

    @Contract(pure = true)
    static int toPx(int dp) {
        return Math.round(dp * (getActivity().getResources()
                .getDisplayMetrics().ydpi / DENSITY_DEFAULT));
    }

    static float toDp(int px) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        return (float) px * DisplayMetrics.DENSITY_DEFAULT / metrics.ydpi;
    }

    static boolean isHardwareKeyBoardAvailable() {
        return mainActivity.getResources().getConfiguration().keyboard != KEYBOARD_NOKEYS;
    }

    @Contract(pure = true)
    static SharedPreferences getPreferences(String name) {
        return getActivity().getSharedPreferences(name, MODE_PRIVATE);
    }

    @SuppressLint("ApplySharedPref")
    static void renameMyPrefsFile(String nameOld, String nameNew) {
        SharedPreferences settingsOld = getPreferences(nameOld);
        SharedPreferences settingsNew = getPreferences(nameNew);
        SharedPreferences.Editor editorNew = settingsNew.edit();
        Map<String, ?> all = settingsOld.getAll();
        for (Entry<String, ?> x : all.entrySet()) {
            Object o = x.getValue();
            if (o instanceof String)
                editorNew.putString(x.getKey(), (String) o);
            else if (o instanceof Set)
                //noinspection unchecked
                editorNew.putStringSet(x.getKey(), (Set<String>) o);
            else throw new RuntimeException(
                        "В SharedPreferences проекта " + theProject +
                                " обнаружена переменная типа " + o.getClass());
        }
        editorNew.commit();
        SharedPreferences.Editor editorOld = settingsOld.edit();
        editorOld.clear();
        editorOld.apply();
    }

    static void closeKeyBrd(Activity a) {
        try { //noinspection ConstantConditions
            ((InputMethodManager) a.getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            String message = e.getMessage();
            Log.v("KeyBrdUtils", message == null ? "" : message);
        }
    }

    static void closeKeyBrd2(Activity a) {
        a.getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // only for Resources & Preferences
    @Contract(pure = true)
    private static Activity getActivity() {
        return activity1 != null ? activity1 : mainActivity;
    }
}