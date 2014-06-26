package com.sinlimites.androidcameraapp;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import com.sinlimites.objects.CameraObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Serializable {

	private static final long serialVersionUID = 1L;

	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	public static final int ROTATE_ANGLE = 90;
	private TextView loadingType;
	private boolean cameraStarted = false, fromCameraService = false;
	private String loadingTypeText;

	/**
	 * Show the main_activity.xml layout and place an SurefaceView.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.setActivity(this);
		setContentView(R.layout.main_activity);
		loadingType = (TextView) findViewById(R.id.loading_type);

		HandleSurfaceView();
	}

	/**
	 * Create an holder for the SurefaceView which can be filled with the camera
	 * preview.
	 */
	@SuppressWarnings("deprecation")
	private void HandleSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.camera_view);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/**
	 * Set the surfaceView if the camerea is started else make it black.
	 */
	@Override
	public void onResume() {
	    if(cameraStarted)
			surfaceView.setBackgroundColor(Color.TRANSPARENT);
	    else 
			surfaceView.setBackgroundColor(Color.BLACK);
	    
	    loadingType.setText(loadingTypeText);
	    super.onResume();
	}
	
	/**
	 * Set the variable loadingTypeText with the selected loading type
	 */
	@Override
	public void onPause() {
		loadingTypeText = loadingType.getText().toString();
		super.onPause();
	}
	
	/**
	 * No action when the user clicks on the back button
	 */
	@Override
	public void onBackPressed() {}
	
	/**
	 * Start the camera. Called when clicked on the "Start Service" button.
	 * 
	 * @param v
	 */
	public void StartService(View v) {
		if (!loadingType.getText().equals("")) {
			StartCamera();
			cameraStarted = true;
			surfaceView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			Toast.makeText(this, R.string.no_loading_type, Toast.LENGTH_SHORT).show();
			fromCameraService = true;
			openOptionsMenu();
		}
	}

	/**
	 * Stop the camera. Called when clicked on the "Stop Service" button.
	 * 
	 * @param v
	 */
	public void StopService(View v) {
		StopCamera();
		cameraStarted = false;
		surfaceView.setBackgroundColor(Color.BLACK);
	}

	/**
	 * Configure and start the camera. Start the service with this camera
	 * object.
	 */
	private void StartCamera() {
		try {
			camera = CameraObject.getCamera();

			try {
				Parameters parameters = camera.getParameters();
				parameters = ChangeRotationBasedOnSDK(parameters);
				camera.setParameters(parameters);
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
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
		} catch (RuntimeException e) {
			if (e.getCause() != null){
				if (e.getMessage().equals("Method called after release()")){
					Toast.makeText(this, R.string.service_already_stopped, Toast.LENGTH_SHORT).show();
				}
			}
			else {
				Toast.makeText(this, R.string.camera_never_started, Toast.LENGTH_SHORT).show();
			}

			e.printStackTrace();
		} 
	}

	/**
	 * Gets the camera. This function is build to support different SDK
	 * versions.
	 * 
	 * @return Camera - the camera of the device.
	 */
	private Camera getCamera() {
		try {
			Method method = Camera.class.getMethod("open", Integer.TYPE);
			return (Camera) method.invoke(null, 0);
		} catch (Exception e) {
			Log.d("CameraLoader", "Error when trying to invoke Camera.open(int), reverting to open()", e);
			return Camera.open();
		}
	}

	/**
	 * Set the display orientation of the camera based on the orientation of the phone
	 * @param parameters
	 * @return
	 */
	private Parameters ChangeRotationBasedOnSDK(Parameters parameters) {
		if (Build.VERSION.SDK_INT >= 8)
			setDisplayOrientation(camera, ROTATE_ANGLE);
		else {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				parameters.set("orientation", "portrait");
				parameters.set("rotation", ROTATE_ANGLE);
			}
		}
		return parameters;
	}

	/**
	 * Create an reflection to the setDisplayOrientation method in Camera.class.
	 * Invoke this method to set the DisplayOrientation. This is used to support
	 * different SDK versions.
	 * 
	 * @param camera
	 *            - the camera of the device.
	 * @param angle
	 *            - the angle of the orientation.
	 */
	protected void setDisplayOrientation(Camera camera, int angle) {
		Method method;
		try {
			method = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			if (method != null)
				method.invoke(camera, new Object[] { angle });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the best preview size for the camera preview
	 * @param width
	 * @param height
	 * @param parameters
	 * @return
	 */
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
		Camera.Size bestSize = null;
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

		bestSize = sizeList.get(0);

		for (int i = 1; i < sizeList.size(); i++) {
			if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
				bestSize = sizeList.get(i);
			}
		}

		return bestSize;
	}

	/**
	 * Create the option menu when the user doesn't have any loading type selected.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!CameraObject.isServiceRunning())
			getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Handler to handle the option buttons. Set the text so the user knows which loading is selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.load:
			Toast.makeText(this, R.string.load, Toast.LENGTH_SHORT).show();
			break;
		case R.id.unload:
			Toast.makeText(this, R.string.unload, Toast.LENGTH_SHORT).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		TextView loadingType = (TextView) findViewById(R.id.loading_type);
		loadingType.setText(item.getTitle());
		if (fromCameraService)
			StartService(null);
		
		return true;
	}

	/**
	 * Surface changer for the SurfaceView so the camera gets updated.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			if (camera != null) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size bestSize = getBestPreviewSize(width, height, parameters);

				if (bestSize != null) {
					parameters.setPreviewSize(bestSize.width, bestSize.height);
					parameters = ChangeRotationBasedOnSDK(parameters);
					camera.setParameters(parameters);
					camera.setPreviewDisplay(surfaceHolder);
					camera.startPreview();
				}
			} else {
				camera = getCamera();
				Parameters parameters = camera.getParameters();
				parameters = ChangeRotationBasedOnSDK(parameters);
				camera.setParameters(parameters);
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
			}
			CameraObject.setCamera(camera);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {}
}
