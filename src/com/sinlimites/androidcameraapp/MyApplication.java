package com.sinlimites.androidcameraapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	
	private static Context context;
	private static Activity activity;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	public static Context getContext() {
		return context;
	}
	
	public static void setActivity(Activity act) {
		MyApplication.activity = act;
	}
	
	public static Activity getActivity(){
		return activity;
	}

}
