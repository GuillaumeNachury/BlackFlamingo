package com.gnachury;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gnachury.blackflamingo.R;
import com.gnachury.library.ColorPicker;
import com.gnachury.library.ColorPicker.OnColorChangedListener;
import com.gnachury.ui.FlamingoViewer;
/**
 * 
 * @author Guillaume
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener{

	private final static String tag = "MainActivity";
	private FlamingoViewer fv;
	private ImageView tolButton, satButton, lumButton, selectColor, chooseColor;
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
		chooseColor.getBackground().setAlpha(128);
		
		getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(isActivateOnTouch){
					handleTouch(event);		
				}				
				return true;
			}
		});
		
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

		//Defaut Value : Red
		colorPick.setColor(Color.argb(255, 255, 0, 0));
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
			//	chooseColor.getBackground().clearColorFilter();
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
			//	chooseColor.getBackground().clearColorFilter();
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
			//	chooseColor.getBackground().clearColorFilter();
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
			//Purple
			colorPick.setOldCenterColor(Color.argb(255,128,0,128));
			break;
			
		case R.id.colorButton:
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
		//chooseColor.getBackground().clearColorFilter();	
	}
	
	//Color picker in AlertDialog
	/*private void showColorPickerDialog()
	{
	    AlertDialog.Builder colorDialogBuilder = new AlertDialog.Builder(MainActivity.this);
	    LayoutInflater inflater = LayoutInflater.from(this);
	    View dialogview = inflater.inflate(R.layout.dialog_color, null);
	    picker = (ColorPicker) dialogview.findViewById(R.id.picker);
	    picker.setOldCenterColor(selectPixelColor);
	    picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener()
	    {
	        @Override
	        public void onColorChanged(int color) {
	        	
	        }
	    });
	    colorDialogBuilder.setTitle("Choose Text Color");
	    colorDialogBuilder.setView(dialogview);
	    colorDialogBuilder.setPositiveButton("OK",
	    new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            Log.d(tag, "Color :" + picker.getColor());
	          //  colorPickerView.setTextColor(picker.getColor());
	           // picker.getOldCenterColor();
	        }
	    });
	    colorDialogBuilder.setNegativeButton("Cancel",
	            new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	        }
	    });
	    AlertDialog colorPickerDialog = colorDialogBuilder.create();
	    colorPickerDialog.show();
	}
	*/
	
	
	

	public FlamingoViewer getFv() {
		return fv;
	}

	public void setFv(FlamingoViewer fv) {
		this.fv = fv;
	}
	
	

}
