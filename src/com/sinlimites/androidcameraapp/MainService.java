package com.sinlimites.androidcameraapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.sinlimites.objects.Container;
import com.sinlimites.objects.ContainerLocation;
import com.sinlimites.objects.Handling;
import com.sinlimites.objects.LocationDTO;

public class MainService extends Service {

	private TessBaseAPI baseApi = new TessBaseAPI();
	private Camera camera;
	private PictureCallback rawCallback;
	private ShutterCallback shutterCallback;
	private PictureCallback jpegCallback;
	private Bitmap originalImage, binarizedImage;
	private boolean finished = true;
	private ImageView imageView;
	private static String lastContainerCode = "";
	private double longitude, latitude;
	private static ArrayList<Handling> handlingArrayList = new ArrayList<Handling>();

	/**
	 * Called when the service needs to stop and makes an Toast message.
	 */
	@Override
	public void onDestroy() {
		GPSLocTrack gps = new GPSLocTrack(MyApplication.getActivity());
		if (gps.canGetLocation()){
			gps.getLocation();
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
		} else {
			latitude = 0;
			longitude = 0;
			gps.showSettingsAlert();
		}	
		UpdateDatabase(lastContainerCode, true);
		Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_LONG).show();
		CameraObject.setServiceRunning(false);
	}

	/**
	 * The first method called when the service starts. Create the runnable to
	 * take the picture every 10 seconds.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.service_started, Toast.LENGTH_LONG).show();
		if(handlingArrayList.size()==0)
			new JSONGetAsync().execute();
		
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
	 * 
	 * @return ShutterCallback - the raw ShutterCallback.
	 */
	private ShutterCallback shutterCallBack() {
		return new ShutterCallback() {

			@Override
			public void onShutter() {
			}
		};
	}

	/**
	 * Create an raw PictureCallback for the takePicture() method.
	 * 
	 * @return PictureCallback - the raw PictureCallback.
	 */
	private PictureCallback rawCallBack() {
		return new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] arg0, Camera arg1) {
			}

		};
	}

	/**
	 * Creates an File containing the taken picture.
	 * 
	 * @return PictureCallback - return an new PictureCallback.
	 */
	private PictureCallback pictureCallBack() {
		return new PictureCallback() {

			public void onPictureTaken(byte[] data, Camera camera) {
				imageView = (ImageView) MyApplication.getActivity().findViewById(R.id.binarized_imageview);
				data = ResizeImage(data, imageView.getWidth(), imageView.getHeight());
				originalImage = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Config.ARGB_8888);
				Options options = new Options();
				options.inPreferredConfig = Config.ARGB_8888;
				originalImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				Binarizer binarizer = new Binarizer();
				binarizedImage = binarizer.BinarizeImage(originalImage);
				ChangeImageView(binarizedImage);

				String code = processTesseract(binarizedImage);
				System.out.println("Scanned code:"+code);
				code = CheckForContainerCode(code);
				System.out.println("Scanned code after Regex:"+code);

				CheckDifferentCode(code);
			}
		};
	}


	private void CheckDifferentCode(String code) {
		if(!code.equals(lastContainerCode) && !code.equals("")){
			if(!lastContainerCode.equals("")){
				System.out.println("Container code is different! Old code: "+lastContainerCode+" | New code: "+code);
				UpdateDatabase(code, true);
			}
			System.out.println("Update database with code: "+code);
			UpdateDatabase(code, false);
			lastContainerCode = code;
		}
	}
	
	private void UpdateDatabase(String code, boolean loadingTypeDone) {
		GPSLocTrack gps = new GPSLocTrack(MyApplication.getActivity());
		if (gps.canGetLocation()){
			gps.getLocation();
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			System.out.println("GPS retreived! Longitude: "+longitude+" | Latitude: "+latitude);
		} else {
			latitude = 0;
			longitude = 0;
			gps.showSettingsAlert();
		}		
		String json = BuildObject(latitude, longitude, loadingTypeDone);
		System.out.println("Json builded: "+json);
		if(!json.equals(""))
			new JSONUpdateAsync(code, json).execute();
	}
	
	private String BuildObject(double latitude, double longitude, boolean loadingTypeDone) {
		try {
			if(handlingArrayList.size()>0) {
				TextView type = (TextView) MyApplication.getActivity().findViewById(R.id.loading_type);
	            ContainerLocation location = new ContainerLocation();
	            Container container = new Container();
	            Handling handling = new Handling();
	            for(int i=0;i<handlingArrayList.size();i++){
	            	Handling handlingList = handlingArrayList.get(i);
	            	if(handlingList.getHandlingName().equals(type.getText().toString()) && !loadingTypeDone)
	            		handling = handlingList;
	            	else if (loadingTypeDone){
	            		if (type.getText().toString().equals(MyApplication.getActivity().getResources().getString(R.string.load)) 
	            				&& handlingList.getHandlingName().equals(MyApplication.getActivity().getResources().getString(R.string.transit))){
	            			handling = handlingList;
	            		} else if (type.getText().toString().equals(MyApplication.getActivity().getResources().getString(R.string.unload))
	            				&& handlingList.getHandlingName().equals(MyApplication.getActivity().getResources().getString(R.string.discharge))){
	            			handling = handlingList;
	            		}
	            	}
	            }
	            container.setHandlingID(handling);
	            location.setEquipmentNumber(container);
	            location.setLongitude(longitude);
	            location.setLatitude(latitude);
	
	            LocationDTO dto = new LocationDTO();
	            dto.setLocationID(location);
	
	            return post(dto);
			} else {
				sleep(1000);
				BuildObject(latitude, longitude, loadingTypeDone);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
		return "";
    }

    public static String post(LocationDTO obj) {
    	Gson gson = new GsonBuilder().create();
    	String json = gson.toJson(obj);
		return json;
	}

	private String CheckForContainerCode(String code) {
		code = code.replaceAll(" ", "");
		Pattern regex = Pattern.compile("(^[a-zA-Z]{4})([0-9]{7})");
		Matcher matcher = regex.matcher(code);
		if(matcher.find())
			return matcher.group();
		else 
			return "";
	}

	private byte[] ResizeImage(byte[] input, int width, int height) {
		Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
		Bitmap resized = Bitmap.createScaledBitmap(original, width, height, true);

		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);

		return blob.toByteArray();
	}

	/**
	 * Take a picture and write it based on the PictureCallback.
	 */
	private void captureImage() {
		if(camera!=null)
			camera.autoFocus(autoFocus());
	}

	/**
	 * An sleep function.
	 * 
	 * @param time - Time in milliseconds the Thread has to sleep.
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Makes an picture every 10 seconds.
	 * 
	 * @return Runnable - containing the data that needs to be executed.
	 */
	private Runnable TakePictureRunnable() {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (finished) {
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
	 * 
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

	private AutoFocusCallback autoFocus() {
		return new AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				finished = false;
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			}
		};
	}

	private void ChangeImageView(Bitmap image) {
		imageView.setImageBitmap(RotateBitmap(image));
		finished = true;
	}

	private Bitmap RotateBitmap(Bitmap bitmap) {
		Bitmap image = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(image);
		tempCanvas.rotate(MainActivity.ROTATE_ANGLE, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		tempCanvas.drawBitmap(bitmap, 0, 0, null);

		return image;
	}

	private String processTesseract(Bitmap image) {
		String tesseractFolder = Environment.getExternalStorageDirectory() + "/tesseract/tesseract-ocr/";
		String path = null;
		if (Environment.getExternalStorageDirectory() != null) {
			File folder = new File(tesseractFolder + "/tessdata");
			if(!folder.exists()) {
				try {
					String assetsFolder[] = getAssets().list("tesseract-ocr");
					for (int i = 0; i < assetsFolder.length; i++)
						if (assetsFolder[i].equals("tessdata"))
							copyFolderToExternalStorage("tesseract-ocr/"+assetsFolder[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			path = tesseractFolder;
		}
		// Path to Tesseract dir
		else
			path = getFilesDir() + "/tesseract/";

		baseApi.init(path, "eng");

		Pix img = ReadFile.readBitmap(image);
		baseApi.setImage(img);
		return baseApi.getUTF8Text();
	}

	private void copyFolderToExternalStorage(String name) {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list(name);
			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				// First: checking if there is already a target folder
				File folder = new File(Environment.getExternalStorageDirectory() + "/tesseract/" + name);
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdirs();
				}
				if (success) {
					try {
						in = assetManager.open(name + "/" + filename);
						out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/tesseract/" + name + "/" + filename);
						Log.i("WEBVIEW", Environment.getExternalStorageDirectory() + "/tesseract/" + name + "/" + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch (IOException e) {
						Log.e("ERROR", "Failed to copy asset file: " + filename, e);
					}
				} 
			}
		} catch (IOException e) {
			Log.e("ERROR", "Failed to get asset file list.", e);
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public static ArrayList<Handling> getHandlingArrayList() {
		return handlingArrayList;
	}

	public static void setHandlingArrayList(ArrayList<Handling> handlingArrayList) {
		MainService.handlingArrayList = handlingArrayList;
	}
}