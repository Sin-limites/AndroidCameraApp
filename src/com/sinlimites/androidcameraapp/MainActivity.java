package com.sinlimites.androidcameraapp;

import java.io.Serializable;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Camera camera;
	private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private final String tag = "VideoServer";
	
    /**
     * Show the main_activity.xml layout and place an SurefaceView.
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		HandleSurfaceView();
	}
	
	/**
	 * Create an holder for the SurefaceView which can be filled with the camera preview.
	 */
	@SuppressWarnings("deprecation")
	private void HandleSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.camera_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	/**
	 * Start the camera. Called when clicked on the "Start Service" button.
	 * @param v
	 */
	public void StartService(View v){
		StartCamera();
	}
	
	/**
	 * Stop the camera. Called when clicked on the "Stop Service" button.
	 * @param v
	 */
	public void StopService(View v){	
		StopCamera();
	}

	/**
	 * Configure and start the camera. 
	 * Start the service with this camera object.
	 */
	private void StartCamera() {
		try {
			camera = getCamera();
	        Camera.Parameters parameters = camera.getParameters();
	        parameters.setPreviewSize(176, 144);
	        camera.setParameters(parameters);
	        
	        if (Build.VERSION.SDK_INT >= 8)
	            setDisplayOrientation(camera, 90);
	        else {
	            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	            	parameters.set("orientation", "portrait");
	            	parameters.set("rotation", 90);
	            }
	        }   
	        
	        try {
	            camera.setPreviewDisplay(surfaceHolder);
	            camera.startPreview();
	        } catch (Exception e) {
	            Log.e(tag, "init_camera: " + e);
	        }
	        CameraObject.setCamera(camera);
			startService(new Intent(MainActivity.this, MainService.class));	
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Fail to connect to camera service")) 
    			Toast.makeText(this, R.string.service_already_started, Toast.LENGTH_SHORT).show();
				
			e.printStackTrace();
		}
    }

	/**
	 * Stop the camera and also the service which uses this camera.
	 */
    private void StopCamera() {
    	try {
	    	camera = CameraObject.getCamera();
	        camera.stopPreview();
	        camera.release();
	        CameraObject.setCamera(camera);
			stopService(new Intent(MainActivity.this, MainService.class));
    	} catch(RuntimeException e){
    		if(e.getMessage().equals("Method called after release()"))
    			Toast.makeText(this, R.string.service_already_stopped, Toast.LENGTH_SHORT).show();
    		
    		e.printStackTrace();
    	}
    }
	
    /**
     * Gets the camera. This function is build to support different SDK versions.
     * @return Camera - the camera of the device.
     */
	private Camera getCamera() {
        try {
            Method method = Camera.class.getMethod("open", Integer.TYPE);
            return (Camera) method.invoke(null, 0);
        } catch (Exception e) {
            Log.d("CameraLoader","Error when trying to invoke Camera.open(int), reverting to open()",e);
            return Camera.open();
        }
    }

	/**
	 * Create an reflection to the setDisplayOrientation method in Camera.class.
	 * Invoke this method to set the DisplayOrientation. This is used to support different SDK versions.
	 * @param camera - the camera of the device.
	 * @param angle - the angle of the orientation.
	 */
	protected void setDisplayOrientation(Camera camera, int angle){
	    Method method;
	    try {
	    	method = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
	        if (method != null)
	        	method.invoke(camera, new Object[] { angle });
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {}
}
