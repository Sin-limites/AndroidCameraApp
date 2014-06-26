package com.sinlimites.objects;

import android.hardware.Camera;

public class CameraObject {
	
	private static Camera camera;
	private static boolean serviceRunning;

	/**
	 * Getter for the Camera
	 * @return Camera - the camera of the device.
	 */
	public static Camera getCamera() {
		return camera;
	}

	/**
	 * Setter for the Camera
	 * @param camera - the camera of the device.
	 */
	public static void setCamera(Camera camera) {
		CameraObject.camera = camera;
	}

	/**
	 * @return the serviceRunning
	 */
	public static boolean isServiceRunning() {
		return serviceRunning;
	}

	/**
	 * @param serviceRunning the serviceRunning to set
	 */
	public static void setServiceRunning(boolean serviceRunning) {
		CameraObject.serviceRunning = serviceRunning;
	}
}
