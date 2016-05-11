package com.jarvis.smarthelmet.main;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jarvis.smarthelmet.R;

public class SmartHelmetActivity extends Activity {
	// 디버깅 변수
	private static final 	String 					TAG 				= "Smart Helmet";
	
	// 인텐트 객체
	private					Intent					ridding				= null;
	private					Intent					remote				= null;
	private					Intent					setting				= null;
	
	// 노티피케이션 관련 객체
	private 				Notification 			notification 		= null;
	private 				NotificationManager 	notificationManager	= null;
	
	/********************************************************************************************
	 * 																							*
	 *										Override Method										*
	 *																							*
	 ********************************************************************************************/
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		Log.e( TAG, "onCreate()" );
		
		super.onCreate( savedInstanceState );
		
		if ( notification == null && notificationManager == null ) {
			createNotification();
		}
		
        finish();
	}
	
    // End of Override Method
	
    /************************************************************************************************
     *																								*
     *										User Define Method										*
     *																								*
     ************************************************************************************************/
	private void createNotification() {
		Log.e( TAG, "createNotification()" );
		
		// 인텐트 등록
		ridding = new Intent( this, RiddingModeActivity.class );
		remote 	= new Intent( this,  RemoteModeActivity.class );
		setting = new Intent( "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" );
        
		// 노티피케이션 액션 시 수행할 PendingIntent 생성
        PendingIntent riddingPending = PendingIntent.getActivity( this, 0, ridding, PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent remotePending	 = PendingIntent.getActivity( this, 0, remote , PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent settingPending = PendingIntent.getActivity( this, 0, setting, 0 );
        
        // 노티피케이션 생성
        notification = new Notification.BigTextStyle( new Notification.Builder( getApplicationContext() )
        												  .setContentTitle( "Smart Helmet" )
        												  .setSmallIcon( R.drawable.ic_launcher )
        												  .addAction( 0, "DRIVE"  , riddingPending )
        												  .addAction( 0, "CALL"   , remotePending  )
        												  .addAction( 0, "SETTING", settingPending )
        											).build();
        
        // 노티피케이션 옵션
        notification.flags = Notification.PRIORITY_HIGH;
        
        // 노티피케이션 등록
		notificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( 0, notification );
	}
	
	// End of User Define Method
	
}

// End of SmarHelmetActivity
