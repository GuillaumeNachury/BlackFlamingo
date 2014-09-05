package com.gnachury.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import com.gnachury.ShaderParam;
import com.gnachury.blackflamingo.R;
import com.gnachury.util.OESTexture;
import com.gnachury.util.Shader;

public class FlamingoViewer extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

	private Quad _quad;
	private Camera _camera;
	private boolean selectColor = false;

	private final Shader mOffscreenShader = new Shader();
	private float[] mTransformM = new float[16];
	private float[] mOrientationM = new float[16];
	private float[] mRatio = new float[2];
	
	/* ******** Textures ********* */
	private final OESTexture _oesTex = new OESTexture();
	private SurfaceTexture _currentTex;
	private boolean _isDirty=false;
	private int glViewPortW;
	private int glViewPortH;
	private int _cameraW;
	private int _cameraH;
	private Context _context;
	
	/* ******* Shader variables ******* */
	private float _tolerance = 10.0f;
	private float _saturation = .0f;
	private float _value = .0f;
	private float _selectedColor = 0.0f;
	private float _newHue = (float) ((1.0/360.0)*300.0);
	
	
	

	public FlamingoViewer(Context context) {
		super(context);
		_context = context;
		_setup();
	}
	
	public FlamingoViewer(Context context,AttributeSet attr) {
		super(context, attr);
		_context = context;
		_setup();
	}
	

	
	private void _setup(){
		_quad = new Quad();
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		try {
			mOffscreenShader.setProgram(R.raw.vertex_shader, R.raw.fragment_shader, _context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_camera = Camera.open();
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewPortW = width;
		glViewPortH = height;
		_oesTex.init();
		if(_currentTex != null) _currentTex.release();
		
		_currentTex = new SurfaceTexture(_oesTex.getTextureId());
		_currentTex.setOnFrameAvailableListener(this);
		
		_camera.stopPreview();
		try {
		
			_camera.setPreviewTexture(_currentTex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Camera.Parameters param = _camera.getParameters();
		List<Size> psize = param.getSupportedPreviewSizes();
		if(psize.size() > 0 ){
			int i;
			for (i = 0; i < psize.size(); i++){
				if(psize.get(i).width < width || psize.get(i).height < height)
					break;
			}
			if(i>0)
				i--;
			param.setPreviewSize(psize.get(i).width, psize.get(i).height);
			
			_cameraW = psize.get(i).width;
			_cameraH = psize.get(i).height;		

		}
		//param.setRecordingHint(true);
		
		
		if(_context.getResources().getConfiguration().orientation == 
				Configuration.ORIENTATION_PORTRAIT){
			Matrix.setRotateM(mOrientationM, 0, 90.0f, 0f, 0f, 1f);
			mRatio[1] = _cameraW*1.0f/height;
			mRatio[0] = _cameraH*1.0f/width;
		}
		else{
			Matrix.setRotateM(mOrientationM, 0, 0.0f, 0f, 0f, 1f);
			mRatio[1] = _cameraH*1.0f/height;
			mRatio[0] = _cameraW*1.0f/width;
		}
		
		
		_camera.setParameters(param);	
		
		
		_camera.startPreview();
		requestRender();
	}
	
	

	@Override
	public synchronized void onDrawFrame(GL10 gl) {
		if(selectColor){
			SavePNG(glViewPortW/2,glViewPortH/2,10,10,"Shader_"+Math.random()+".jpg",gl);
			
		}
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if(_isDirty){
			_isDirty = false;
			_currentTex.updateTexImage();
			_currentTex.getTransformMatrix(mTransformM);
			
			GLES20.glViewport(0,0, glViewPortW, glViewPortH);
			
			mOffscreenShader.useProgram();
			
			int uTransformM = mOffscreenShader.getHandle("uTransformM");
			int uOrientationM = mOffscreenShader.getHandle("uOrientationM");
			int uRatioV = mOffscreenShader.getHandle("ratios");
			int uHueTolerance = mOffscreenShader.getHandle("uHueTolerance");
			int uSaturation = mOffscreenShader.getHandle("uSaturation");
			int uValue = mOffscreenShader.getHandle("uValue");
			int uSelectedHue = mOffscreenShader.getHandle("uSelectedHue");
			int uNewHue = mOffscreenShader.getHandle("uNewHue");
			int uScreenWidth = mOffscreenShader.getHandle("uScreenWidth");
			int uScreenHeight = mOffscreenShader.getHandle("uScreenHeight");
			
			
			GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
			GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
			GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);
			
			GLES20.glUniform1f(uHueTolerance, _tolerance);
			GLES20.glUniform1f(uSaturation, _saturation);
			GLES20.glUniform1f(uValue, _value);
			GLES20.glUniform1f(uSelectedHue, _selectedColor);
			GLES20.glUniform1f(uNewHue, _newHue);
			GLES20.glUniform1f(uScreenWidth, (float)glViewPortW);
			GLES20.glUniform1f(uScreenHeight, (float)glViewPortH);
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _oesTex.getTextureId());
			
			_quad.render(mOffscreenShader.getHandle("aPosition"));
			
		}
		
	}
	
	/**
	 * 
	 * @param p Parametre a mettre à jour En reference à ShaderParam
	 * @param d Valeur du parametre
	 */
	public void updateShaderParameter(int p, float d){
		switch (p) {
		case ShaderParam.TOLERANCE:
			_tolerance = d;
			break;
		case ShaderParam.SATURATION:
			_saturation = d;
			break;
		case ShaderParam.VALUE:
			_value = d;
			break;
		case ShaderParam.SELECTED_HUE:
			_selectedColor = d;
			break;
		case ShaderParam.NEW_HUE:
			_newHue = d;
			break;

		default:
			break;
		}
	}
	
	public void changeShader(){
		try {
			mOffscreenShader.setProgram(R.raw.vertex_shader, R.raw.pixelate, _context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		_isDirty = true;
		requestRender();
		
	}
	
	public void getPixel(GL10 gl){
		int b[] = new int [1];
		IntBuffer ib=IntBuffer.wrap(b);
	    ib.position(0);
		gl.glReadPixels(glViewPortW/2, glViewPortH/2, 1, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
		int pix=b[0];
        int pb=(pix>>16)&0xff;
        int pr=(pix<<16)&0x00ff0000;
        int pix1=(pix&0xff00ff00) | pr | pb;
		Log.e("Flaming","color = " + pix1  );
		selectColor = false;
	}
	
	public static Bitmap SavePixels(int x, int y, int w, int h, GL10 gl)
	{  
	    int b[]=new int[w*h];
	    int bt[]=new int[w*h];
	    IntBuffer ib=IntBuffer.wrap(b);
	    ib.position(0);
	    gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

	    /*  remember, that OpenGL bitmap is incompatible with 
	        Android bitmap and so, some correction need.
	     */   
	    for(int i=0; i<h; i++)
	    {         
	        for(int j=0; j<w; j++)
	        {
	            int pix=b[i*w+j];
	            int pb=(pix>>16)&0xff;
	            int pr=(pix<<16)&0x00ff0000;
	            int pix1=(pix&0xff00ff00) | pr | pb;
	            bt[(h-i-1)*w+j]=pix1;
	        }
	    }              
	    Bitmap sb= Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
	    return sb;
	}

	public  void SavePNG(int x, int y, int w, int h, String name, GL10 gl)
	{
		selectColor = false;
	    Bitmap bmp=SavePixels(x,y,w,h,gl);
	    try
	    {
	    	File rootmedia = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BlackFlamingo");
	    	Log.e("FlamingoV",rootmedia.getPath());
	    	if (!rootmedia.exists()) {
				rootmedia.mkdirs() ;

			}
	    	FileOutputStream fos=new FileOutputStream(rootmedia.getPath()+ File.separator + name);
	        bmp.compress(CompressFormat.PNG, 100, fos);
	        try
	        {
	            fos.flush();
	        }
	        catch (IOException e)
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        try
	        {
	            fos.close();
	        }
	        catch (IOException e)
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }
	    catch (FileNotFoundException e)
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }              
	}
	
	private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
	        throws OutOfMemoryError {
	    int bitmapBuffer[] = new int[w * h];
	    int bitmapSource[] = new int[w * h];
	    IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
	    intBuffer.position(0);

	    try {
	        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
	        int offset1, offset2;
	        for (int i = 0; i < h; i++) {
	            offset1 = i * w;
	            offset2 = (h - i - 1) * w;
	            for (int j = 0; j < w; j++) {
	                int texturePixel = bitmapBuffer[offset1 + j];
	                int blue = (texturePixel >> 16) & 0xff;
	                int red = (texturePixel << 16) & 0x00ff0000;
	                int pixel = (texturePixel & 0xff00ff00) | red | blue;
	                bitmapSource[offset2 + j] = pixel;
	            }
	        }
	    } catch (GLException e) {
	        return null;
	    }

	    return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
	}
	
	public boolean isSelectColor() {
		return selectColor;
	}

	public void setSelectColor(boolean selectColor) {
		this.selectColor = selectColor;
	}
	
	

}
