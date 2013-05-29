package com.codeledger.rssamplecompare;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codeledger.rssamplelib1.ScriptC_water;
import com.codeleger.rssamplelib1.OtherLibrary;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	static String TAG = MainActivity.class.getName();

	Button button1;
	TextView results1;
	ImageView imageView1;
	Button button2;
	TextView results2;
	ImageView imageView2;
	Button button3;
	Button button4;
	TextView results3;
	ImageView imageView3;
	TextView processorsView;
	TextView timingView;

	Button simpleButton;
	Button parallelButton;
	Button simplenativeButton;
	Button parallelnativeButton;
	Button renderscriptButton;
	
	Bitmap mBitmapIn;
	Bitmap mBitmapOut;
	Bitmap mBitmapOutJavaTask;

	private RenderScript mRS;
	private Allocation mInAllocation;
	private Allocation mOutAllocation;
	private ScriptC_water mScript;
	
	SimpleRun worker;
	ExecutorRun workerParallel;
	SimpleNativeRun workerNative;
	ExecutorNativeRun workerNativeParallel;
	RenderRun workerRender;
	
	Runtime runTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		runTime = Runtime.getRuntime();
		int processors = runTime.availableProcessors();
		
		processorsView = (TextView) findViewById(R.id.processorView1);
		processorsView.setText("CPU: " + processors);
		
		timingView = (TextView) findViewById(R.id.timingView1);
		timingView.setText("Timing:");
		
		mBitmapIn = loadBitmap(R.drawable.lena512color);
		mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(),mBitmapIn.getHeight(),mBitmapIn.getConfig());

		simpleButton = (Button) findViewById(R.id.simplejava);
		parallelButton = (Button) findViewById(R.id.paralleljava);
		simplenativeButton = (Button) findViewById(R.id.simplenative);
		parallelnativeButton = (Button) findViewById(R.id.parallelnative);
		renderscriptButton = (Button) findViewById(R.id.renderscriptbutton);

		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView1.setImageBitmap(mBitmapIn);

		imageView2 = (ImageView) findViewById(R.id.imageView2);
		imageView2.setImageBitmap(mBitmapOut);

		simpleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"Java start");
				doWaterViaJava(imageView2,mBitmapIn,mBitmapOut);
				Log.d(TAG,"Java end");
			}

		});

		parallelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"Executor Task start");
				doWaterViaTask(imageView2,mBitmapIn,mBitmapOut);
				Log.d(TAG,"Executor Task end");				
			}

		});

		simplenativeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"Native start");
				doWaterViaNative(imageView2,mBitmapIn,mBitmapOut);
				Log.d(TAG,"Native end");
			}

		});
		
		parallelnativeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"Native Executor Task start");
				doWaterViaNativeParallel(imageView2,mBitmapIn,mBitmapOut);
				Log.d(TAG,"Native Executor Task end");		
			}
			
		});
		
		renderscriptButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"Render Task start");
				doWaterViaRenderscript(imageView2,mBitmapIn,mBitmapOut);
				Log.d(TAG,"Render Task end");		
			}
			
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private Bitmap loadBitmap(int resource) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeResource(getResources(), resource, options);
	}

	void doWaterViaJava(ImageView outWater, Bitmap bitmapIn, Bitmap bitmapOut) {
		worker = new SimpleRun(outWater, bitmapIn, bitmapOut);
		worker.execute();
	}

	void doWaterViaTask(ImageView outWater, Bitmap bitmapIn, Bitmap bitmapOut) {
		workerParallel = new ExecutorRun(outWater, bitmapIn, bitmapOut,4);
		workerParallel.execute();
	}
	
	void doWaterViaNative(ImageView outWater, Bitmap bitmapIn, Bitmap bitmapOut) {
		workerNative = new SimpleNativeRun(outWater, bitmapIn, bitmapOut);
		workerNative.execute();
	}
	
	void doWaterViaNativeParallel(ImageView outWater, Bitmap bitmapIn, Bitmap bitmapOut) {
		workerNativeParallel = new ExecutorNativeRun(outWater, bitmapIn, bitmapOut,4);
		workerNativeParallel.execute();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void doWaterViaRenderscript(ImageView outWater, Bitmap bitmapIn, Bitmap bitmapOut) {
		mRS = RenderScript.create(this);
		
        mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
        
        mScript = new ScriptC_water(mRS,getResources(), R.raw.water);
        mScript.set_gIn(mInAllocation);
        mScript.set_gOut(mOutAllocation);
        mScript.set_gScript(mScript);
        mScript.set_height(mBitmapIn.getHeight());
        mScript.set_width(mBitmapIn.getWidth());
        
        workerRender = new RenderRun(outWater, bitmapIn, bitmapOut);
        workerRender.execute();
	}

	/** Use Java only */
	class SimpleRun extends AsyncTask<Void,Void,Bitmap> {

		int[] in;
		int[] out;

		int width;
		int height;
		Bitmap srcBitmap;
		Bitmap destBitmap;
		
		Long startTime;
		Long endTime;

		private final WeakReference<ImageView> imageViewReference;

		public SimpleRun(ImageView dest, Bitmap aSrcBitmap, Bitmap aDestBitmap) {
			imageViewReference = new WeakReference<ImageView>(dest);
			width = aSrcBitmap.getWidth();
			height = aSrcBitmap.getHeight();
			in = new int[width * height];
			out = new int[width * height];
			srcBitmap = aSrcBitmap;
			destBitmap = aDestBitmap;
			srcBitmap.getPixels(in, 0, width, 0, 0, width, height);

		}

		@Override 
		protected void onPreExecute() {
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			if(imageViewReference !=null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(destBitmap);
					imageView.invalidate();
					Log.d(TAG,"clear");
				}
			}	
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			startTime = System.nanoTime();
			OtherLibrary.waterFilter(0, 0, width, height, 5, in, out);
			endTime = System.nanoTime();
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			return destBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(imageViewReference !=null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(bitmap);
					imageView.invalidate();
					long delta = (endTime - startTime)/1000;
					timingView.setText("Timing: " + delta + "ns");
					Log.d(TAG,"done");
				}
			}
		}

	}
	
	/** Use Java with Java Executor Threads */
	class ExecutorRun extends AsyncTask<Void,Void,Bitmap> {

		int[] in;
		int[] out;

		int width;
		int widthSection;
		int sectionCounter;
		int height;
		int heightSection;
		Bitmap srcBitmap;
		Bitmap destBitmap;

		int threadSetSize;
		private final WeakReference<ImageView> imageViewReference;

		ExecutorService taskExecutorPool;

		Long startTime;
		Long endTime;
		
		public ExecutorRun(ImageView dest, Bitmap aSrcBitmap, Bitmap aDestBitmap, int aThreadSize) {
			threadSetSize = aThreadSize;
			taskExecutorPool = Executors.newFixedThreadPool(threadSetSize);
			// slice up image into 
			imageViewReference = new WeakReference<ImageView>(dest);
			width = aSrcBitmap.getWidth();
			height = aSrcBitmap.getHeight();
			in = new int[width * height];
			out = new int[width * height];
			srcBitmap = aSrcBitmap;
			destBitmap = aDestBitmap;
			srcBitmap.getPixels(in, 0, width, 0, 0, width, height);
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			dest.invalidate();

		}

		@Override
		protected void onPreExecute() {
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			if(imageViewReference !=null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(destBitmap);
					imageView.invalidate();
					Log.d(TAG,"clear");
				}
			}	
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			CompletionService<Void> cs = new ExecutorCompletionService<Void>(taskExecutorPool);
			widthSection = width / threadSetSize;
			heightSection = height / threadSetSize;
			
			startTime = System.nanoTime();
			for(sectionCounter = 0 ; sectionCounter < threadSetSize; sectionCounter++) {
				cs.submit(new Callable<Void>() {
					int startHeight = heightSection * sectionCounter;
					int endHeight = heightSection * (sectionCounter + 1);
					public Void call() throws Exception {
						Log.d(TAG,"waterFilter " + startHeight + " " + endHeight );
						OtherLibrary.waterFilter(0, startHeight, width, endHeight, 5, in, out);
						return null;
					}
				});
			}
			for(int i = 0; i < threadSetSize; i++) {
				try {
					cs.take().get();
				} catch (Exception e) {
					Log.d(TAG,"Oops error " + e.toString());
				}
			}
			endTime = System.nanoTime();
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			return destBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(imageViewReference !=null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(bitmap);
					imageView.invalidate();
					long delta = (endTime - startTime)/1000;
					timingView.setText("Timing: " + delta + "ns");
					Log.d(TAG,"done");
				}
			}
		}
	}
	
	/** Use JNI/native code only */
	class SimpleNativeRun extends AsyncTask<Void,Void,Bitmap> {

		int[] in;
		int[] out;

		int width;
		int height;
		Bitmap srcBitmap;
		Bitmap destBitmap;
		
		Long startTime;
		Long endTime;

		private final WeakReference<ImageView> imageViewReference;

		public SimpleNativeRun(ImageView dest, Bitmap aSrcBitmap, Bitmap aDestBitmap) {
			imageViewReference = new WeakReference<ImageView>(dest);
			width = aSrcBitmap.getWidth();
			height = aSrcBitmap.getHeight();
			in = new int[width * height];
			out = new int[width * height];
			srcBitmap = aSrcBitmap;
			destBitmap = aDestBitmap;
			srcBitmap.getPixels(in, 0, width, 0, 0, width, height);

		}

		@Override 
		protected void onPreExecute() {
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			if(imageViewReference !=null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(destBitmap);
					imageView.invalidate();
					Log.d(TAG,"clear");
				}
			}	
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			startTime = System.nanoTime();
			NativeWaterFilter.nativeWaterFilter(0, 0, width, height, 5, in, out);
			endTime = System.nanoTime();
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			return destBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(imageViewReference !=null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(bitmap);
					imageView.invalidate();
					long delta = (endTime - startTime)/1000;
					timingView.setText("Timing: " + delta + "ns");
					Log.d(TAG,"Simple Native done");
				}
			}
		}

	}
	
	/** Use JNI/native code and Java Executor Threads */
	class ExecutorNativeRun extends AsyncTask<Void,Void,Bitmap> {

		int[] in;
		int[] out;

		int width;
		int widthSection;
		int sectionCounter;
		int height;
		int heightSection;
		Bitmap srcBitmap;
		Bitmap destBitmap;

		int threadSetSize;
		private final WeakReference<ImageView> imageViewReference;

		ExecutorService taskExecutorPool;

		Long startTime;
		Long endTime;
		
		public ExecutorNativeRun(ImageView dest, Bitmap aSrcBitmap, Bitmap aDestBitmap, int aThreadSize) {
			threadSetSize = aThreadSize;
			taskExecutorPool = Executors.newFixedThreadPool(threadSetSize);
			// slice up image into 
			imageViewReference = new WeakReference<ImageView>(dest);
			width = aSrcBitmap.getWidth();
			height = aSrcBitmap.getHeight();
			in = new int[width * height];
			out = new int[width * height];
			srcBitmap = aSrcBitmap;
			destBitmap = aDestBitmap;
			srcBitmap.getPixels(in, 0, width, 0, 0, width, height);
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			dest.invalidate();

		}

		@Override
		protected void onPreExecute() {
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			if(imageViewReference !=null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(destBitmap);
					imageView.invalidate();
					Log.d(TAG,"clear");
				}
			}	
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			CompletionService<Void> cs = new ExecutorCompletionService<Void>(taskExecutorPool);
			widthSection = width / threadSetSize;
			heightSection = height / threadSetSize;
			
			startTime = System.nanoTime();
			for(sectionCounter = 0 ; sectionCounter < threadSetSize; sectionCounter++) {
				cs.submit(new Callable<Void>() {
					int startHeight = heightSection * sectionCounter;
					int endHeight = heightSection * (sectionCounter + 1);
					public Void call() throws Exception {
						Log.d(TAG,"nativeWaterFilter " + startHeight + " " + endHeight );
						NativeWaterFilter.nativeWaterFilter(0, startHeight, width, endHeight, 5, in, out);
						return null;
					}
				});
			}
			for(int i = 0; i < threadSetSize; i++) {
				try {
					cs.take().get();
				} catch (Exception e) {
					Log.d(TAG,"Oops error " + e.toString());
				}
			}
			endTime = System.nanoTime();
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			return destBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(imageViewReference !=null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(bitmap);
					imageView.invalidate();
					long delta = (endTime - startTime)/1000;
					timingView.setText("Timing: " + delta + "ns");
					Log.d(TAG,"Native done");
				}
			}
		}
	}
	
	/** Use Renderscript */
	class RenderRun extends AsyncTask<Void,Void,Bitmap> {

    	int[] in;
    	int[] out;

		int width;
    	int height;
    	
    	private final WeakReference<ImageView> imageViewReference;
    	Bitmap destBitmap;
    	Bitmap srcBitmap;
    	
		Long startTime;
		Long endTime;
		
    	public RenderRun(ImageView dest, Bitmap aSrcBitmap, Bitmap aDestBitmap) {
    		imageViewReference = new WeakReference<ImageView>(dest);
    		width = aSrcBitmap.getWidth();
    		height = aSrcBitmap.getHeight();
    		Log.d(TAG,"doStuff " + width + " " + height);
    		in = new int[width * height];
    		out = new int[width * height];
    		srcBitmap = aSrcBitmap;    		
    		destBitmap = aDestBitmap;
    		aSrcBitmap.getPixels(in, 0, width, 0, 0, width, height);
  		
    	}
    	
		@Override
		protected void onPreExecute() {
			destBitmap.setPixels(out, 0, width, 0, 0, width, height);
			if(imageViewReference !=null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(destBitmap);
					imageView.invalidate();
					Log.d(TAG,"clear");
				}
			}	
		}
    	@Override
    	protected Bitmap doInBackground(Void... params) {
    		// invoke Renderscript
    		startTime = System.nanoTime();
    		mScript.invoke_doWaterFilter();
    		endTime = System.nanoTime();
    		mOutAllocation.copyTo(destBitmap);
    		return destBitmap;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap bitmap) {
    		if(imageViewReference != null && bitmap != null) {
    			final ImageView imageView = imageViewReference.get();
    			if(imageView != null) {
    				imageView.setImageBitmap(bitmap);
    				imageView.invalidate();
					long delta = (endTime - startTime)/1000;
					timingView.setText("Timing: " + delta + "ns");
    			}
    		}
    		
    		Log.d(TAG,"Renderscript done");
    	}		
	}

	
}

