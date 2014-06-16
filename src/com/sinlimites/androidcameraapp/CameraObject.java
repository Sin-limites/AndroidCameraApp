package com.sinlimites.androidcameraapp;

import android.hardware.Camera;

public class CameraObject {
	
	private static Camera camera;

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
}
