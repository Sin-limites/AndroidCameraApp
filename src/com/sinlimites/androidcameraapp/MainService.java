package com.sinlimites.androidcameraapp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {
	
	private Camera camera;
    private PictureCallback rawCallback;
    private ShutterCallback shutterCallback;
    private PictureCallback jpegCallback;
	
    /**
     * Called when the service needs to stop and makes an Toast message.
     */
	@Override
	public void onDestroy(){
		Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * The first method called when the service starts. 
	 * Create the runnable to take the picture every 10 seconds.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.service_started, Toast.LENGTH_LONG).show();
		camera = CameraObject.getCamera();
		
        rawCallback = rawCallBack();
        shutterCallback = shutterCallBack();
        jpegCallback = pictureCallBack();
        performOnBackgroundThread(TakePictureRunnable());
        
		return START_STICKY;
	}
	
	/**
	 * Used to bind the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Create a ShutterCallback for the takePicture() method.
	 * @return ShutterCallback - the raw ShutterCallback.
	 */
	private ShutterCallback shutterCallBack() {
		return new ShutterCallback(){

			@Override
			public void onShutter() {
                Log.i("Log", "onShutter'd");				
			}
		};
	}
	
	/**
	 * Create an raw PictureCallback for the takePicture() method.
	 * @return PictureCallback - the raw PictureCallback.
	 */
	private PictureCallback rawCallBack() {
		return new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] arg0, Camera arg1) {
                Log.d("Log", "onPictureTaken - raw");
			}
			
		};
	}
	
	/**
	 * Creates an File containing the taken picture.
	 * @return PictureCallback - return an new PictureCallback.
	 */
	private PictureCallback pictureCallBack() {
		return new PictureCallback() {
			
			public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format(
                    		Environment.getExternalStorageDirectory().getPath()+"/%d.jpg", System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } 
                Log.d("Log", "onPictureTaken - jpeg");
            }
		};
	}
	
	/**
	 * Take a picture and write it based on the PictureCallback.
	 */
	private void captureImage() {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }
	
	/**
	 * An sleep function.
	 * @param time - Time in milliseconds the Thread has to sleep.
	 */
	private void sleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Makes an picture every 10 seconds.
	 * @return Runnable - containing the data that needs to be executed.
	 */
	private Runnable TakePictureRunnable() {
		Runnable run = new Runnable(){
			@Override
			public void run() {
				while(true){
					captureImage();
					sleep(10000);
					System.out.println("Picture made!");
				}
			}
		};
		return run;
	}
	
	/**
	 * Create an new Thread and start it.
	 * @param runnable - the Runnable that needs to be executed on this Thread.
	 */
	public void performOnBackgroundThread(final Runnable runnable) {
	    final Thread thread = new Thread() {
	    	
	        @Override
	        public void run() {
	            try {
	                runnable.run();
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
	    };
	    thread.start();
	}
}