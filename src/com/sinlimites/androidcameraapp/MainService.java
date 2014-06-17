package com.sinlimites.androidcameraapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.Toast;

public class MainService extends Service {
	
	private Camera camera;
    private PictureCallback rawCallback;
    private ShutterCallback shutterCallback;
    private PictureCallback jpegCallback;
    private Bitmap originalImage, binarizedImage;
    private static final String BINARIZED_IMAGE_NAME = "binarized_image.png";
    private boolean finished = true;
    private ImageView imageView;
	
    /**
     * Called when the service needs to stop and makes an Toast message.
     */
	@Override
	public void onDestroy(){
		Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_LONG).show();
		CameraObject.setServiceRunning(false);
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
		CameraObject.setServiceRunning(true);
        
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
			public void onShutter() {}
		};
	}
	
	/**
	 * Create an raw PictureCallback for the takePicture() method.
	 * @return PictureCallback - the raw PictureCallback.
	 */
	private PictureCallback rawCallBack() {
		return new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] arg0, Camera arg1) {}
			
		};
	}
	
	/**
	 * Creates an File containing the taken picture.
	 * @return PictureCallback - return an new PictureCallback.
	 */
	private PictureCallback pictureCallBack() {
		return new PictureCallback() {
			
			public void onPictureTaken(byte[] data, Camera camera) {  
				imageView = (ImageView) MyApplication.getActivity().findViewById(R.id.binarized_imageview);
				data = ResizeImage(data, imageView.getWidth(), imageView.getHeight());
                originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                Binarizer binarizer = new Binarizer();
                binarizedImage = binarizer.BinarizeImage(originalImage);
                SaveImageToInternalStorage(binarizedImage);
                
                ChangeImageView(binarizedImage);
            }
		};
	}
	
	private byte[] ResizeImage(byte[] input, int width, int height) {
	    Bitmap original = BitmapFactory.decodeByteArray(input , 0, input.length);
	    Bitmap resized = Bitmap.createScaledBitmap(original, width, height, true);
	         
	    ByteArrayOutputStream blob = new ByteArrayOutputStream();
	    resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
	 
	    return blob.toByteArray();
	}
	
	/**
	 * Saves the image to the internal storage.
	 * @param image
	 */
	private void SaveImageToInternalStorage(Bitmap image) {
		try {
			FileOutputStream outputStream = null;
			String externalStorage = Environment.getExternalStorageDirectory().getPath();
			File directory = new File(externalStorage+R.string.app_name);
			if(!directory.exists())
				directory.mkdirs();
			
			File imageFile = new File(directory, BINARIZED_IMAGE_NAME);
			if(imageFile.exists())
				imageFile.delete();
			
			imageFile.createNewFile();
			
			outputStream = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}
	
	/**
	 * Take a picture and write it based on the PictureCallback.
	 */
	private void captureImage() {
		finished = false;
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
					if(finished){
						captureImage();
						sleep(10000);
						System.out.println("Picture made!");
					}
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
	
	private void ChangeImageView(Bitmap image) {
		imageView.setImageBitmap(RotateBitmap(image));
		finished = true;
	}
	
	private Bitmap RotateBitmap(Bitmap bitmap) {
		Bitmap image = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(image); 
		tempCanvas.rotate(MainActivity.ROTATE_ANGLE, bitmap.getWidth()/2, bitmap.getHeight()/2);
		tempCanvas.drawBitmap(bitmap, 0, 0, null);
		
		return image;
	}
}