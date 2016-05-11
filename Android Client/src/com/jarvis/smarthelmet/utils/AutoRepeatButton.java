package com.jarvis.smarthelmet.utils;

import java.io.IOException;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


public class AutoRepeatButton extends Button {
	// 디버깅 변수
	private static final 	String 	TAG 							= "AutoRepeatButton";
	
	// 사용자 정의 변수
	private 				long 	initialRepeatDelay 				= 0;
	private 				long 	repeatIntervalInMillisectonds 	= 100;
	private 				String 	property 						= "";
	
	/********************************************************************************************
	 * 																							*
	 *									Runnable Object Define									*
	 *																							*
	 ********************************************************************************************/
	private volatile Runnable repeatClickWhileButtonHeldRunnable = new Runnable() {
		/** Override Method **/
		@Override
		public void run() {
			Log.e( TAG, property );
			
			performClick();
			
			// 서버로 데이터 전송
			try {
				TCPClient.write( property.getBytes(), property.length() );
			} catch ( IOException ioe) {
				ioe.printStackTrace();
			}
			
			postDelayed( repeatClickWhileButtonHeldRunnable, repeatIntervalInMillisectonds );
		}
	};
	
	// End of Runnable Object Define
	
	/********************************************************************************************
	 * 																							*
	 *										Constructors										*
	 *																							*
	 ********************************************************************************************/
	public AutoRepeatButton( Context context, String property ) {
		super( context );
		this.property = property;
		commonConstructorCode();
	}
	
	public AutoRepeatButton( Context context, AttributeSet attrs, String property ) {
		super( context, attrs );
		this.property = property;
		commonConstructorCode();
	}
	
	public AutoRepeatButton( Context context, AttributeSet attrs, int defStyle, String property ) {
		super( context, attrs, defStyle );
		this.property = property;
		commonConstructorCode();
	}
	
	// End of Constructors
	
	/************************************************************************************************
     *																								*
     *										User Define Method										*
     *																								*
     ************************************************************************************************/
	private void commonConstructorCode() {
		this.setOnTouchListener( new OnTouchListener() {
			@Override
			public boolean onTouch( View v, MotionEvent event ) {
				int action = event.getAction();
				if ( action == MotionEvent.ACTION_DOWN ) {
					removeCallbacks(repeatClickWhileButtonHeldRunnable);
					performClick();
					postDelayed( repeatClickWhileButtonHeldRunnable, initialRepeatDelay );
				} else if ( action == MotionEvent.ACTION_UP ) {
					removeCallbacks( repeatClickWhileButtonHeldRunnable );
				}

				return true;
			}
		});
	}
	
	// End of User Define Method
}

// End of AutoRepeatButton
