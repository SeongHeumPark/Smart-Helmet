package com.jarvis.smarthelmet.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jarvis.smarthelmet.R;
import com.jarvis.smarthelmet.R.layout;
import com.jarvis.smarthelmet.utils.AutoRepeatButton;
import com.jarvis.smarthelmet.utils.AutoRepeatRelativeLayout;
import com.jarvis.smarthelmet.utils.TCPClient;

public class RemoteModeActivity extends Activity {
	// ����� ����
	private static final 	String 						TAG			= "Remote Mode";
	
	// ��Ʈ�ѷ� ���� ��ü
	private 				ImageView 					imgView		= null;
	private 				LinearLayout 				background	= null;
	private 				AutoRepeatButton 			leftAccel 	= null;
	private 				AutoRepeatButton 			leftBreak 	= null;
	private 				AutoRepeatButton 			rightAccel 	= null;
	private 				AutoRepeatButton 			rightBreak 	= null;
	private 				AutoRepeatRelativeLayout 	left 		= null;
	private 				AutoRepeatRelativeLayout 	right 		= null;
	
	// ��� ���� ��ü
	private					TCPClient					client		= null;
	
	// �̹��� �ε� ���� ��ü & ����
	private 				String 						imagePath 	= "http://192.168.0.20:81/snapshot.cgi?user=admin&pwd=888888";
	private 				Bitmap 						image		= null;
	private 				ImageGetThread 				task1 		= new ImageGetThread();
	private 				ImageGetThread 				task2 		= new ImageGetThread();
	private 				ImageGetThread 				task3 		= new ImageGetThread();
	
	/********************************************************************************************
	 * 																							*
	 *									Handler Object Define									*
	 *																							*
	 ********************************************************************************************/
	private final Handler mHandler = new Handler() {
		public void handleMessage( Message msg ) {
			// handleMessage�� 1�� ������ UI Update
			if ( msg.what == 1 ) {
				if ( image != null ) {
					imgView.setImageBitmap( image );
				}
			}
		}
	};
	
	// End of Handler Object Define
	
	/********************************************************************************************
	 * 																							*
	 *										Override Method										*
	 *																							*
	 ********************************************************************************************/
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		Log.e( TAG, "onCreate()" );
		
		super.onCreate( savedInstanceState );
		setContentView( layout.activity_remote_mode );
		
		// ����ó���� ���� ��Ƽ�����̼��� �����Ѵ�.
		NotificationManager notificationManager
							= ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
		notificationManager.cancel( 0 );
		
		// TCP/IP ��� ����
		if ( client == null ) {
			client = new TCPClient( "call" );
			client.execute( this );
		}
		
		imgView 	= ( ImageView )    findViewById( R.id.ImageView );
		background 	= ( LinearLayout ) findViewById( R.id.baseLinearLayout );
		
		// ȭ�� ���� ����
		leftSetting();
		rightSetting();
		
		// �̹��� �ε� ������ ����
		task1.start();
		task2.start();
		task3.start();
	}
	
	@Override
	protected void onDestroy() {
		Log.e( TAG, "onDestroy()" );
		
		super.onDestroy();
		
		// �̹��� �ε� ������ ����
		task1.stop();
		task2.stop();
		task3.stop();
    	
		// TCP/IP ��� ����
		if ( client != null ) {
			client.stop();
			client = null;
		}
		
		// SmartHelmetActivity�� �����Ͽ� �ٽ� ��Ƽ�����̼��� ����.
    	Intent mIntent = new Intent( this, SmartHelmetActivity.class );
    	startActivity( mIntent );
	}
	
	// End of Override Method
	
	/************************************************************************************************
     *																								*
     *										User Define Method										*
     *																								*
     ************************************************************************************************/
	// ���� ȭ�� ����
	private void leftSetting() {
		Log.e( TAG, "leftSetting()" );
		
		// ���̾ƿ� �߰�
		if ( left == null ) {
			left = new AutoRepeatRelativeLayout( this, "left" );
			left.setLayoutParams(new LinearLayout.LayoutParams( 0, LayoutParams.MATCH_PARENT, 0.5f ) );
			background.addView( left );
		}
		
		// ���� ��ư �߰�
		if ( leftAccel == null ) {
			leftAccel = new AutoRepeatButton( this, "accel" );
			leftAccel.setText( "����" );
			leftAccel.setId( 10 );
			RelativeLayout.LayoutParams paramLA 
			                            = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT, 
			                            		                           LayoutParams.WRAP_CONTENT );
			paramLA.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			left.addView( leftAccel, paramLA );
		}
		
		// �극��ũ ��ư �߰�
		if ( leftBreak == null ) {
			leftBreak = new AutoRepeatButton( this, "break" );
			leftBreak.setText( "����" );
			RelativeLayout.LayoutParams paramLB 
			                            = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT, 
			                            		                           LayoutParams.WRAP_CONTENT );
			paramLB.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			paramLB.addRule( RelativeLayout.RIGHT_OF, 10 );
			left.addView( leftBreak, paramLB );
		}
	}
	
	// ������ ȭ�� ����
	private void rightSetting() {
		Log.e( TAG, "rightSetting()" );
		
		// ���̾ƿ� �߰�
		if ( right == null ) {
			right = new AutoRepeatRelativeLayout( this, "right" );
			right.setLayoutParams( new LinearLayout.LayoutParams( 0, LayoutParams.MATCH_PARENT, 0.5f ) );
			background.addView( right );
		}

		// ���� ��ư �߰�
		if ( rightAccel == null ) {
			rightAccel = new AutoRepeatButton( this, "accel" );
			rightAccel.setText( "����" );
			rightAccel.setId( 01 );
			RelativeLayout.LayoutParams paramRA 
			                            = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT, 
			                            		                           LayoutParams.WRAP_CONTENT );
			paramRA.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			paramRA.addRule( RelativeLayout.ALIGN_PARENT_RIGHT );
			right.addView( rightAccel, paramRA );
		}
		
		// �극��ũ ��ư �߰�
		if (rightBreak == null) {
			rightBreak = new AutoRepeatButton( this, "break" );
			rightBreak.setText( "����" );
			RelativeLayout.LayoutParams paramRB 
			                            = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT, 
			                            		                           LayoutParams.WRAP_CONTENT );
			paramRB.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			paramRB.addRule( RelativeLayout.LEFT_OF, 01 );
			right.addView( rightBreak, paramRB );
		}
	}
	
	private Bitmap loadImage( String path ) {
    	Log.e( TAG, "loadImage()" );
    	
    	InputStream is = openHttpConnection( path );
    	if( is == null ) return null;
    	
    	// ���� ȭ�� ����� ���Ѵ�.
    	int displayWidth  = getResources().getDisplayMetrics().widthPixels;
    	int displayHeight = getResources().getDisplayMetrics().heightPixels;
    	
	    Bitmap image = BitmapFactory.decodeStream( is, null, null );
	    image = Bitmap.createScaledBitmap( image, displayWidth, displayHeight, true );
    	
    	return image;
    }
    
    private InputStream openHttpConnection( String path ) {
    	Log.e( TAG, "openHttpConnection()" );
    	
    	InputStream is = null;
    	
    	try {
    		URL url = new URL( path );
    		HttpURLConnection urlConnection = ( HttpURLConnection ) url.openConnection();
    		urlConnection.setRequestMethod( "GET" );
    		urlConnection.connect();
    		
    		if ( urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK ) {
    			is = urlConnection.getInputStream();
    		}
    	} catch ( MalformedURLException mue ) {
    		mue.printStackTrace();
    	} catch ( IOException ie ) {
    		ie.printStackTrace();
    	}
    	
    	return is;
    }
    
    // End of User Define Method
	
    /************************************************************************************************
     *																								*
     *									Get Image From URL Class									*
     *																								*
     ************************************************************************************************/
	// HTTP�� �̿��� �̹��� �ε� ������ Ŭ����
   	private class ImageGetThread implements Runnable {
   		// ����� ����
   		private static final 	String 	TAG 		= "ImageGetThread";
   		
   		// ������ ��ü
   		private 				Thread 	thread		= null;
   		
   		// ������ ���� ����
   		private static final 	int 	RUNNING 	= 0;
   		private static final 	int 	SUSPENDED 	= 1;
   		private static final 	int 	STOPPED 	= 2;
   		private 				int 	state 		= RUNNING;
   	
   		/** ������ **/
   		public ImageGetThread() {
   			Log.e( TAG, "constructor()" );
   			
   			thread = new Thread( this );
   		}
   		
   		/** Override Method **/
   		@Override
   		public void run() {
   			while ( true ) {
   				// URL�� ���� �̹��� �޾ƿ�
   				image = loadImage( imagePath );
   				
   				// �ڵ鷯�� UI ������Ʈ �޽��� ����
   				mHandler.sendMessage( Message.obtain( mHandler, 1 ) );
   				
   				// ������ ���� üũ
   				if ( checkState() ) {
   					thread = null;
   					break;
   				}
   			}
   		}
   		
   		/** User Define Method **/
   		public void start() {
   			thread.start();
   		}
   		
   		public void stop() {
   			setState( STOPPED );
   		}
   		
   		private synchronized void setState( int state ) {
   			this.state = state;
   			
   			if ( this.state == RUNNING ) {
   				notify();
   			} else {
   				thread.interrupt();
   			}
   		}
   		
   		private synchronized boolean checkState() {
   			while ( state == SUSPENDED ) {
   				try {
   					wait();
   				} catch ( InterruptedException ie ) {
   					ie.printStackTrace();
   				}
   			}
   			
   			return state == STOPPED;
   		}
   	}
   	
   	// End of ImageGetThread
}

// End of RemoteModeActivity
