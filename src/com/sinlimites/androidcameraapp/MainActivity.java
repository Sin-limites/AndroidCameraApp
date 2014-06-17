package com.sinlimites.androidcameraapp;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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
	private final String tag = "VideoServer";
	public static final int ROTATE_ANGLE = 90;
	private TextView loadingType;

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
	 * Start the camera. Called when clicked on the "Start Service" button.
	 * 
	 * @param v
	 */
	public void StartService(View v) {
		if(!loadingType.getText().equals(""))
			StartCamera();
		else {
			Toast.makeText(this, R.string.no_loading_type, Toast.LENGTH_SHORT).show();
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
	}

	/**
	 * Configure and start the camera. Start the service with this camera
	 * object.
	 */
	private void StartCamera() {
		try {
			camera = getCamera();

			try {
				Parameters parameters = camera.getParameters();
				parameters = ChangeRotationBasedOnSDK(parameters);
				camera.setParameters(parameters);

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
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Method called after release()"))
				Toast.makeText(this, R.string.service_already_stopped, Toast.LENGTH_SHORT).show();

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(!CameraObject.isServiceRunning())
			getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.transit:
			Toast.makeText(this, R.string.transit, Toast.LENGTH_SHORT).show();
			break;
		case R.id.unload:
			Toast.makeText(this, R.string.unload, Toast.LENGTH_SHORT).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		TextView loadingType = (TextView) findViewById(R.id.loading_type);
		loadingType.setText(item.getTitle());
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size bestSize = getBestPreviewSize(width, height, parameters);

			if (bestSize != null) {
				parameters.setPreviewSize(bestSize.width, bestSize.height);
				parameters = ChangeRotationBasedOnSDK(parameters);
				camera.setParameters(parameters);
				camera.startPreview();

				Toast.makeText(getApplicationContext(), "Best Size:\n" + String.valueOf(bestSize.width) + " : " + String.valueOf(bestSize.height), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}
}
