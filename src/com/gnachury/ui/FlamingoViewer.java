package com.gnachury.ui;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gnachury.blackflamingo.R;
import com.gnachury.util.OESTexture;
import com.gnachury.util.Shader;

import android.R.bool;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class FlamingoViewer extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

	private Quad _quad;
	private Camera _camera;
	
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
	
	/* test */
	private boolean f = true;
	

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
	
	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e("Shader", "Changing");
		changeShader();
		return super.onTouchEvent(event);
	}
	*/
	
	private void _setup(){
		_quad = new Quad();
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		try {
			mOffscreenShader.setProgram(R.raw.vertex_shader, R.raw.original, _context);
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
		param.setRecordingHint(true);
		
		
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
		GLES20.glClearColor(1.0f, 0.3f, 0.2f, 1.0f);
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
			/*int uScreenWidth = mOffscreenShader.getHandle("uScreenWidth");
			int uScreenHeight = mOffscreenShader.getHandle("uScreenHeight");
			int uROffset = mOffscreenShader.getHandle("uROffset");
			Double dt = (double) (new Date()).getTime();*/
			
			GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
			GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
			GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);
			/*
			GLES20.glUniform1f(uScreenWidth, (float)glViewPortW);
			GLES20.glUniform1f(uScreenHeight, (float)glViewPortH);
			GLES20.glUniform1f(uROffset, (float)Math.cos(dt));*/
			
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _oesTex.getTextureId());
			
			_quad.render(mOffscreenShader.getHandle("aPosition"));
			
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

}
