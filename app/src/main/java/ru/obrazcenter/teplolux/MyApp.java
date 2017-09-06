package ru.obrazcenter.teplolux;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "sokolovphilip@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_message)

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}