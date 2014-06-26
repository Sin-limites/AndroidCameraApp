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

	/**
	 * Get the application context.
	 * @return
	 */
	public static Context getContext() {
		return context;
	}
	
	/**
	 * Set the current application Activity
	 * @param act
	 */
	public static void setActivity(Activity act) {
		MyApplication.activity = act;
	}
	
	/**
	 * Get the current application Activity
	 * @return
	 */
	public static Activity getActivity(){
		return activity;
	}

}
