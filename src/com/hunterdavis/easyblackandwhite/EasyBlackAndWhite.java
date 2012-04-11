package com.hunterdavis.easyblackandwhite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.hunterdavis.easyblackandwhite.ColorPickerDialog.OnColorChangedListener;

public class EasyBlackAndWhite extends Activity {
	
	int SELECT_PICTURE = 22;
	int color1 = 0;
	int color2 = 0;
	ImageView image;

	Uri selectedImageUri = null;
	Bitmap photoBitmap = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
        
		// Create an anonymous implementation of OnClickListener
		OnClickListener loadButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Source Photo"),
						SELECT_PICTURE);
			}
		};
		
		
		// Create an anonymous implementation of OnClickListener
		OnClickListener saveButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				Boolean didWeSave = saveImage(v.getContext());
		}
		};
		

		
		Button loadButton = (Button) findViewById(R.id.loadButton);
		loadButton.setOnClickListener(loadButtonListner);
		
		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListner);
		
		
		
		// image listeners
		// photo on click listener
		ImageView imageOne = (ImageView) findViewById(R.id.ColorImageOne);
		ImageView imageTwo = (ImageView) findViewById(R.id.ColorImageTwo);
		genColor(imageOne,Color.WHITE);
		genColor(imageTwo,Color.DKGRAY);
		
		imageOne.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ImageView imageOne = (ImageView) findViewById(R.id.ColorImageOne);
				colorPicker(imageOne, v.getContext());
			}
		});
		
		imageTwo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ImageView imageOne = (ImageView) findViewById(R.id.ColorImageTwo);
				colorPicker(imageOne, v.getContext());
			}
		});
		
		
		// seek bar on change listener
		// implement a seekbarchangelistener for this class
		SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				refresh();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		};
		
		SeekBar onlySeekBar = (SeekBar) findViewById(R.id.threshold);
		onlySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
       
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				selectedImageUri = data.getData();
				refresh();
			}
		}
	}
	
	public void refresh() {
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		Boolean scaleDisplay = scaleURIAndDisplay(getBaseContext(),
				selectedImageUri, imgView);
	}
	
	public void colorPicker(ImageView imagee, Context context) {
		// initialColor is the initially-selected color to be shown in the
		// rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware
		// of the initial 0xff which is the alpha.
		image = imagee;
		OnColorChangedListener ourchangelistener = new OnColorChangedListener() {
			@Override
			public void colorChanged(int color) {
				// TODO Auto-generated method stub
				genColor(image, color);
				refresh();
			}
		};
		new ColorPickerDialog(context, ourchangelistener, 333444).show();

	}
	public Boolean genColor(ImageView imgview, int Color) {

		ImageView imageOne = (ImageView) findViewById(R.id.ColorImageOne);
		if(imgview == imageOne) {
			color1 = Color;
		}
		else {
			color2 = Color;
		}
		// create a width*height long int array and populate it with random 1 or
		// 0
		// final Random myRandom = new Random();
		int rgbSize = 100 * 100;
		int[] rgbValues = new int[rgbSize];
		for (int i = 0; i < rgbSize; i++) {
			rgbValues[i] = Color;
		}

		// create a width*height bitmap
		BitmapFactory.Options staticOptions = new BitmapFactory.Options();
		staticOptions.inSampleSize = 2;
		Bitmap staticBitmap = Bitmap.createBitmap(rgbValues, 100, 100,
				Bitmap.Config.RGB_565);

		// set the imageview to the static
		imgview.setImageBitmap(staticBitmap);

		return true;

	}
    
	public Boolean scaleURIAndDisplay(Context context, Uri uri,
			ImageView imgview) {
		double divisorDouble = 800;
		InputStream photoStream;
		try {
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;

		photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
		if (photoBitmap == null) {
			return false;
		}
		int h = photoBitmap.getHeight();
		int w = photoBitmap.getWidth();
				
		
		// This is gonna take up some time....
		photoBitmap = toGrayscale(photoBitmap);
		int maxpixel = 0;
		int minpixel = Integer.MAX_VALUE;
		
		// get max and min
		for(int i = 0;i<h;i++)
		{
			for(int j = 0;j<w;j++)
			{
				int pixel = photoBitmap.getPixel(j,i);
				if(pixel > maxpixel) {
					maxpixel = pixel;
				}
				if(pixel < minpixel) {
					minpixel = pixel;
				}
				
			}
		}
		
		
		SeekBar thresholdbar = (SeekBar) findViewById(R.id.threshold);
		double thresholdDoub = thresholdbar.getProgress() * .01;
		int range = maxpixel - minpixel;
		
		float scaledrange = (float) (range * thresholdDoub);
		int threshold = (int) (minpixel + scaledrange);

		
		
		for(int i = 0;i<h;i++)
		{
			for(int j = 0;j<w;j++)
			{
				int pixel = photoBitmap.getPixel(j,i);
				if(pixel > threshold) {
					photoBitmap.setPixel(j,i,color1);
				}
				else {
					photoBitmap.setPixel(j, i, color2);
				}
			}
		}
		
		
		if ((w > h) && (w > divisorDouble)) {
			double ratio = divisorDouble / w;
			w = (int) divisorDouble;
			h = (int) (ratio * h);
		} else if ((h > w) && (h > divisorDouble)) {
			double ratio = divisorDouble / h;
			h = (int) divisorDouble;
			w = (int) (ratio * w);
		}

		Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
		imgview.setImageBitmap(scaled);
		return true;
	}    
	
	public Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    return bmpGrayscale;
	}
	
	public Boolean saveImage(Context context) {
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		if(selectedImageUri == null)
		{
			return false;
		}
		// actually save the file

		OutputStream outStream = null;
		String newFileName = null;
		

		String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME /* col1 */};
		Cursor c = context.getContentResolver().query(selectedImageUri, projection, null,
				null, null);
		if (c != null && c.moveToFirst()) {
			String oldFileName = c.getString(0);
			int dotpos = oldFileName.lastIndexOf(".");
			if (dotpos > -1) {
				newFileName = oldFileName.substring(0, dotpos) + "-negative.png";
			}
		}
		

		if (newFileName != null) {
			{
				File file = new File(extStorageDirectory, newFileName);
				try {
					outStream = new FileOutputStream(file);
					photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
					try {
						outStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}

					Toast.makeText(context, "Saved " + newFileName,
							Toast.LENGTH_LONG).show();
					new SingleMediaScanner(context, file);

				} catch (FileNotFoundException e) {
					// do something if errors out?
					return false;
				}
			}

			return true;

		}
		return false;
	}
    
}