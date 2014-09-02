package com.gnachury.ui;

import java.nio.ByteBuffer;

import android.opengl.GLES20;

public class Quad {

	final byte coord[] = {-1, 1, -1, -1, 1, 1, 1, -1};
	private ByteBuffer _bb;
	
	public Quad(){
		_bb = ByteBuffer.allocateDirect(4 * 2);
		_bb.put(coord).position(0);
	}
	
	public void render(int aPosition){
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, _bb);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}
}
