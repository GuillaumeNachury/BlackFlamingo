package com.gnachury;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gnachury.blackflamingo.R;
import com.gnachury.ui.FlamingoViewer;
/**
 * 
 * @author Guillaume
 *
 */
public class MainActivity extends Activity implements OnClickListener{

	private final static String tag = "MainActivity";
	private FlamingoViewer fv;
	private Button tolButton, satButton, lumButton, selectColor, chooseColor;
	private float screenHeight;
	private float tolValue = 0;
	private float satValue = 0;
	private float lumValue = 0;
	private boolean isActivateSatButton = false;
	private boolean isActivateLumButton = false;
	private boolean isActivateTolButton = false;
	private boolean isActivateOnTouch = false;
	private TextView textDebug;
	private int selectButtonId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		setFv((FlamingoViewer)findViewById(R.id.renderer_view));
		tolButton = (Button)findViewById(R.id.tolerance);
		satButton = (Button)findViewById(R.id.saturation);
		lumButton = (Button)findViewById(R.id.luminance);
		textDebug = (TextView)findViewById(R.id.texttodebug);
		selectColor = (Button)findViewById(R.id.selectColor);
		chooseColor = (Button)findViewById(R.id.colorButton);

		//Add listener 
		tolButton.setOnClickListener(this);
		satButton.setOnClickListener(this);
		lumButton.setOnClickListener(this);
		selectColor.setOnClickListener(this);
		chooseColor.setOnClickListener(this);
		
		//setOpacity 128 = 50%
		tolButton.getBackground().setAlpha(128);
		satButton.getBackground().setAlpha(128);
		lumButton.getBackground().setAlpha(128);
		
		getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(isActivateOnTouch){
					handleTouch(event);		
				}				
				return true;
			}
		});

	}
	
	//Method touchEvent
	public void handleTouch(MotionEvent m)
	{
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
	    				break;
	    			case MotionEvent.ACTION_UP:
	    				actionString = "UP";
	    				break;	
	    			case MotionEvent.ACTION_POINTER_DOWN:
	    				actionString = "PNTR DOWN";
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
								break;
							case R.id.saturation:
								//saturation : 0 <= value <= 1
								satValue =  (1 - (y/screenHeight));
			    				textDebug.setText("saturation " + satValue + " y=" + y);
								break;							
							case R.id.luminance:
								//luminance : 0 <= value <= 1
								lumValue =  (1 - (y/screenHeight));
			    				textDebug.setText("luminance " + lumValue + " y=" + y);
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
	    		String touchStatus = "Action: " + actionString + " Index: " + actionIndex + " ID: " + id + " X: " + x + " Y: " + y;
	    		Log.v("MainActivity", touchStatus);
	    	}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) { 
		//Select a button to apply its color,  and disable the others
		isActivateOnTouch = true;
		selectButtonId = v.getId();
		switch (v.getId()) {
		case R.id.tolerance:
			if(!isActivateTolButton){
				isActivateOnTouch = true;
				isActivateTolButton = true;
				isActivateLumButton = false;
				isActivateSatButton = false;
				tolButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
				lumButton.getBackground().clearColorFilter();
				satButton.getBackground().clearColorFilter();
			}
			else{
				isActivateOnTouch = false;
				isActivateTolButton = false;
				
				tolButton.getBackground().clearColorFilter();
			}
			
			break;
			
		case R.id.luminance:
			if(!isActivateLumButton){
				isActivateOnTouch = true;
				isActivateLumButton = true;
				isActivateTolButton = false;
				isActivateSatButton = false;
				tolButton.getBackground().clearColorFilter();
				lumButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
				satButton.getBackground().clearColorFilter();
			}
			else{
				isActivateOnTouch = false;
				isActivateLumButton = false;
				lumButton.getBackground().clearColorFilter();
			}
			break;
			
		case R.id.saturation:
			if(!isActivateSatButton){
				isActivateOnTouch = true;
				isActivateSatButton = true;
				isActivateTolButton = false;
				isActivateLumButton = false;
				tolButton.getBackground().clearColorFilter();
				lumButton.getBackground().clearColorFilter();
				satButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.SRC_ATOP));
			}
			else{
				isActivateOnTouch = false;
				isActivateSatButton = false;
				satButton.getBackground().clearColorFilter();
			}
			break;
			
		case R.id.selectColor:
			resetAllColorButton();
			//ToDo catch the pixel 
			break;
			
		case R.id.colorButton:
			resetAllColorButton();
			
			//ToDo open circle color 
			break;

		default:
			break;
		}
		
	}
	
	public void resetAllColorButton(){
		isActivateSatButton = false;
		isActivateTolButton = false;
		isActivateLumButton = false;
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
}
