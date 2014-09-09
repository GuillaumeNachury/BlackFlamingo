package com.gnachury;

import java.io.File;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class GlobalApplication extends Application{
	
	private static File rootmedia;
	private MainActivity act;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		rootmedia = new File(Environment.getExternalStorageDirectory(), "/BlackFlamingo");
		if (!rootmedia.exists()) {
			boolean foldercreate = rootmedia.mkdir() ;
			Log.e("GlobalApp", "folder = " + foldercreate);
		}
		else{
			Log.e("GlobalApp", "folder exist !");
		}
	
	}
	
	

	public MainActivity getAct() {
		return act;
	}



	public void setAct(MainActivity act) {
		this.act = act;
	}



	public static File getRootmedia() {
		return rootmedia;
	}

	public static void setRootmedia(File rootmedia) {
		GlobalApplication.rootmedia = rootmedia;
	}
}
