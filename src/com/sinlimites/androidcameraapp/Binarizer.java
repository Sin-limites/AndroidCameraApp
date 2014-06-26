package com.sinlimites.androidcameraapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

public class Binarizer {

	/**
	 * Binarize the image
	 * @param image
	 * @return
	 */
	public Bitmap BinarizeImage(Bitmap image) {
	    int width, height, threshold;
	    height = image.getHeight();
	    width = image.getWidth();
	    threshold = 127;
	    
	    Bitmap imageBinary = ConvertToMutable(image);

	    for(int x = 0; x < width; ++x) {
	        for(int y = 0; y < height; ++y) {
	            // get one pixel color
	            int pixel = imageBinary.getPixel(x, y);
	            int red = Color.red(pixel);

	            //get binary value
	            if(red < threshold)
	            	imageBinary.setPixel(x, y, Color.BLACK);
	            else
	            	imageBinary.setPixel(x, y, Color.WHITE);
	        }
	    }
	    return imageBinary;
	}

	/**
	 * Return a mutable Bitmap so the binarizer can change it's pixels.
	 * @param image
	 * @return
	 */
	private static Bitmap ConvertToMutable(Bitmap image) {
		try {
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			int width = image.getWidth();
			int height = image.getHeight();
			Bitmap.Config type = image.getConfig();

			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, image.getRowBytes() * height);
			image.copyPixelsToBuffer(map);

			System.gc();

			image = Bitmap.createBitmap(width, height, type);
			map.position(0);
	
			image.copyPixelsFromBuffer(map);
			
			channel.close();
			randomAccessFile.close();

			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;
	}
}
