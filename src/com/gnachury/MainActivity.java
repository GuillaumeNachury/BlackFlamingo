package com.gnachury;

import java.io.File;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gnachury.blackflamingo.R;
import com.gnachury.library.ColorPicker;
import com.gnachury.library.ColorPicker.OnColorChangedListener;
import com.gnachury.ui.FlamingoViewer;
/**
 * 
 * @author Guillaume
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener, MyListener{

	private final static String tag = "MainActivity";
	private FlamingoViewer fv;
	private ImageView tolButton, satButton, lumButton, selectColor, chooseColor, crosshair, camera;
	private float screenHeight;
	private float tolValue = 0;
	private float satValue = 0;
	private float lumValue = 0;
	float[] colors;
	private boolean isActivateSatButton = false;
	private boolean isActivateLumButton = false;
	private boolean isActivateTolButton = false;
	private boolean isActivateOnTouch = false;
	private boolean isOpenChooseColor = false;
	private TextView textDebug;
	private int selectButtonId;
	private int selectPixelColor;
	private float selectColorPickerAngle;
	private ColorPicker colorPick;
	private GlobalApplication application;
	private boolean succesPicture;
	private NotificationManager myNotificationManager;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		setFv((FlamingoViewer)findViewById(R.id.renderer_view));
		
		tolButton = (ImageView)findViewById(R.id.tolerance);
		satButton = (ImageView)findViewById(R.id.saturation);
		lumButton = (ImageView)findViewById(R.id.luminance);
		textDebug = (TextView)findViewById(R.id.texttodebug);
		selectColor = (ImageView)findViewById(R.id.selectColor);
		chooseColor = (ImageView)findViewById(R.id.colorButton);
		colorPick = (ColorPicker)findViewById(R.id.color_picker);
		colorPick.setVisibility(View.INVISIBLE);
		crosshair = (ImageView)findViewById(R.id.crosshair);
		camera = (ImageView)findViewById(R.id.camera);

		//Add listener 
		tolButton.setOnClickListener(this);
		satButton.setOnClickListener(this);
		lumButton.setOnClickListener(this);
		selectColor.setOnClickListener(this);
		chooseColor.setOnClickListener(this);
		camera.setOnClickListener(this);
		
		//initial mode select
		selectColor.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
		fv.setModeSelectReelColor(1);
		fv.setSelectColor(true);
		
		//setOpacity 128 = 50%
		tolButton.getBackground().setAlpha(128);
		satButton.getBackground().setAlpha(128);
		lumButton.getBackground().setAlpha(128);
		chooseColor.getBackground().setAlpha(128);
		selectColor.getBackground().setAlpha(128);
		
		getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(isActivateOnTouch){
					Log.e(tag, "isActivateOnTouch");
					handleTouch(event);		
				}	
			/*	if(!isActivateOnTouch && fv.isTakePicture()){
					Log.e(tag, "isTakePicture");

					handleTouch(event);		
				}
			*/
				return true;
			}
		});
		
		if(succesPicture){
			Log.e(tag, "succesPicture");
		}
		
		colorPick.setOnColorChangedListener(new OnColorChangedListener() {
	        @Override
	        public void onColorChanged(int color) {
	        	float[] colors = new float[3];
	    		Color.colorToHSV(color, colors);
	    		//set the angle value 
	        	selectColorPickerAngle = (float) colors[0];
	        	textDebug.setText("color = " +  colors[0]);
	        		
	        	fv.updateShaderParameter(ShaderParam.NEW_HUE, (float)((1.0/360.0)*selectColorPickerAngle));
	        }
	    });
		colorPick.setShowOldCenterColor(true);
		
		colorPick.setOldCenterColor(fv.getPixelColor());
		//Defaut Value : Red
		colorPick.setColor(Color.argb(255, 255, 0, 0));
	}
	
	//Method touchEvent
	public void handleTouch(MotionEvent m)
	{	
			Log.e(tag, "handletouche");
	    	int pointerCount = m.getPointerCount();
	    	//Screen dimensions
	    	Display display = getWindowManager().getDefaultDisplay(); 
	    	int height = display.getHeight();  // deprecated
	    	screenHeight = height;
	    	
	    	for (int i = 0; i < pointerCount; i++)
	    	{
	    		int x = (int) m.getX(i);
	    		int y = (int) m.getY(i);    		
	    		int id = m.getPointerId(i);
	    		int action = m.getActionMasked();
	    		int actionIndex = m.getActionIndex();
	    		String actionString;
	    		
	    		switch (action)
	    		{
	    			//some exemple of action event
	    			case MotionEvent.ACTION_DOWN:
	    				actionString = "DOWN";
	    				fv.setEventMotion(actionString);	
	    				fv.setSelectColor(true);
	    				break;
	    			case MotionEvent.ACTION_UP:
	    				actionString = "UP";
	    				//fv.setEventMotion(actionString);
	    				fv.setSelectColor(false);
	    				break;	
	    			case MotionEvent.ACTION_POINTER_DOWN:
	    				actionString = "PNTR DOWN";
	    				//fv.setTakePicture(true);
	    				break;
	    			case MotionEvent.ACTION_POINTER_UP:
	        			actionString = "PNTR UP";
	        			break;
	    			case MotionEvent.ACTION_MOVE:
	    				switch (selectButtonId) {
	    					//set value into variable
							case R.id.tolerance:
								//tolerance : 0 <= value <= 60
								tolValue =  (60 - (y/screenHeight * 60f));
			    				textDebug.setText("tolerance " + tolValue + " y=" + y);
			    				fv.updateShaderParameter(ShaderParam.TOLERANCE, tolValue);
								break;
							case R.id.saturation:
								//saturation : 0 <= value <= 1
								satValue =  (1 - (y/screenHeight));
			    				textDebug.setText("saturation " + satValue + " y=" + y);
			    				fv.updateShaderParameter(ShaderParam.SATURATION, satValue);
								break;							
							case R.id.luminance:
								//luminance : 0 <= value <= 1
								lumValue =  (1 - (y/screenHeight));
			    				textDebug.setText("luminance " + lumValue + " y=" + y);
			    				fv.updateShaderParameter(ShaderParam.VALUE, lumValue);
								break;
	
							default:
								break;
						}
	    				
	    				actionString = "MOVE";
	    				break;
	    			default:
	    				actionString = "";
	    		}
	    		//debug 
	    	//	String touchStatus = "Action: " + actionString + " Index: " + actionIndex + " ID: " + id + " X: " + x + " Y: " + y;
	    	//	Log.e("MainActivity", touchStatus);
	    	}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fv.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		fv.onPause();
	}

	@Override
	public void onClick(View v) { 
		crosshair.setVisibility(View.INVISIBLE);
		//Select a button to apply its color,  and disable the others
		isActivateOnTouch = true;
		selectButtonId = v.getId();
		switch (v.getId()) {
		case R.id.tolerance:
			fv.setModeSelectReelColor(0);
			selectColor.getBackground().clearColorFilter();
			
			if(!isActivateTolButton){
				isActivateOnTouch = true;
				isActivateTolButton = true;
				isActivateLumButton = false;
				isActivateSatButton = false;
				tolButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
				lumButton.getBackground().clearColorFilter();
				satButton.getBackground().clearColorFilter();
			//	chooseColor.getBackground().clearColorFilter();
			}
			else{
				isActivateOnTouch = false;
				isActivateTolButton = false;
				tolButton.getBackground().clearColorFilter();
			}
			//fv.setModeSelectReelColor(0);
			break;
			
		case R.id.luminance:
			fv.setModeSelectReelColor(0);
			selectColor.getBackground().clearColorFilter();
			if(!isActivateLumButton){
				isActivateOnTouch = true;
				isActivateLumButton = true;
				isActivateTolButton = false;
				isActivateSatButton = false;
				tolButton.getBackground().clearColorFilter();
				lumButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
				satButton.getBackground().clearColorFilter();
			//	chooseColor.getBackground().clearColorFilter();
			}
			else{
				isActivateOnTouch = false;
				isActivateLumButton = false;
				lumButton.getBackground().clearColorFilter();
			}
		//	fv.setModeSelectReelColor(0);
			break;
			
		case R.id.saturation:
			fv.setModeSelectReelColor(0);
			selectColor.getBackground().clearColorFilter();
			if(!isActivateSatButton){
				isActivateOnTouch = true;
				isActivateSatButton = true;
				isActivateTolButton = false;
				isActivateLumButton = false;
				tolButton.getBackground().clearColorFilter();
				lumButton.getBackground().clearColorFilter();
			//	chooseColor.getBackground().clearColorFilter();
				satButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
			}
			else{
				isActivateOnTouch = false;
				isActivateSatButton = false;
				satButton.getBackground().clearColorFilter();
			}
		//	
			break;
			
		case R.id.selectColor:
			chooseColor.getBackground().clearColorFilter();
			colorPick.setVisibility(View.INVISIBLE);
			isOpenChooseColor = false;
			if(fv.getModeSelectReelColor() == 0){
				crosshair.setVisibility(View.VISIBLE);
				fv.setModeSelectReelColor(1);
				selectColor.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
			}
			else{
				fv.setModeSelectReelColor(0);
				selectColor.getBackground().clearColorFilter();
			}
			
			break;
			
		case R.id.colorButton:
			
			colorPick.setOldCenterColor(fv.getPixelColor());
			fv.setModeSelectReelColor(0);
			selectColor.getBackground().clearColorFilter();
			
			if(!isOpenChooseColor){
				isOpenChooseColor = true;
				isActivateOnTouch = true;
				isActivateSatButton = true;
				isActivateTolButton = false;
				isActivateLumButton = false;
				tolButton.getBackground().clearColorFilter();
				lumButton.getBackground().clearColorFilter();
				satButton.getBackground().clearColorFilter();
				chooseColor.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
				colorPick.setVisibility(View.VISIBLE);
			}
			else{
				isActivateOnTouch = false;
				isOpenChooseColor = false;
				chooseColor.getBackground().clearColorFilter();
				colorPick.setVisibility(View.INVISIBLE);
			}
			break;
			
		case R.id.camera:
			fv.setTakePicture(true);


		default:
			break;
		}
		
	}
	
	
	public void resetAllColorButton(){
		isActivateSatButton = false;
		isActivateTolButton = false;
		isActivateLumButton = false;
		isOpenChooseColor = false;
		tolButton.getBackground().clearColorFilter();
		lumButton.getBackground().clearColorFilter();
		satButton.getBackground().clearColorFilter();		
	}
	

	public FlamingoViewer getFv() {
		return fv;
	}

	public void setFv(FlamingoViewer fv) {
		this.fv = fv;
	}

	public int getSelectPixelColor() {
		return selectPixelColor;
	}

	public void setSelectPixelColor(int selectPixelColor) {
		this.selectPixelColor = selectPixelColor;
	}

	public boolean isSuccesPicture() {
		return succesPicture;
	}

	public void setSuccesPicture(boolean succesPicture) {
		this.succesPicture = succesPicture;
	}

	@Override
	public void callback(boolean result, File pathPicture) {
		final File pathImage = pathPicture;
		//File mediaStorageDir = new File(Environment.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES), "BlackFlamingo");
		
		((Activity) MainActivity.this).runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	createNotification(pathImage);
				Toast.makeText(MainActivity.this, "Capture Image", Toast.LENGTH_SHORT).show();
		    }
		});
	}
	
	public void createNotification(File path) {
		Log.e(tag, "path = " + path);
		// Invoking the default notification service
	      NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);	
	    //Define sound URI
	     //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	      Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blackflamingo_soundnotification );
	 
	      mBuilder.setContentTitle("Image sauvegardée");
	      mBuilder.setContentText("Cliquez pour visionner l'image");
	      mBuilder.setTicker("BlackFlamingo !");
	      mBuilder.setAutoCancel(true);
	      Log.e(tag, sound.toString());
	      mBuilder.setSound(sound);
	      mBuilder.setSmallIcon(R.drawable.ic_launcher);

	      // Increase notification number every time a new notification arrives 
	      //mBuilder.setNumber(++numMessagesOne);
	      
	      // Creates an explicit intent for an Activity in your app 
	      //Intent resultIntent = new Intent(this, NotificationReceiverActivity.class);
	      //resultIntent.putExtra("notificationId", 111);

	      Intent intent = new Intent(Intent.ACTION_VIEW);
	      intent.setDataAndType(Uri.fromFile(path), "image/*");
	      
	      PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
	      /*This ensures that navigating backward from the Activity leads out of the app to Home page
	      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

	      // Adds the back stack for the Intent
	      stackBuilder.addParentStack(NotificationReceiverActivity.class);

	      // Adds the Intent that starts the Activity to the top of the stack
	      stackBuilder.addNextIntent(resultIntent);
	      PendingIntent resultPendingIntent =
	         stackBuilder.getPendingIntent(
	            0,
	            PendingIntent.FLAG_ONE_SHOT //can only be used once
	         );
	      // start the activity when the user clicks the notification text
	      mBuilder.setContentIntent(resultPendingIntent);
	*/
	    		  
	      mBuilder.setContentIntent(contentIntent);
	      myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	      // pass the Notification object to the system 
	      myNotificationManager.notify(111, mBuilder.build());
	}
}
