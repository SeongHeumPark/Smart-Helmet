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
	// ����� ����
	private static final 	String 					TAG 				= "Smart Helmet";
	
	// ����Ʈ ��ü
	private					Intent					ridding				= null;
	private					Intent					remote				= null;
	private					Intent					setting				= null;
	
	// ��Ƽ�����̼� ���� ��ü
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
		
		// ����Ʈ ���
		ridding = new Intent( this, RiddingModeActivity.class );
		remote 	= new Intent( this,  RemoteModeActivity.class );
		setting = new Intent( "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" );
        
		// ��Ƽ�����̼� �׼� �� ������ PendingIntent ����
        PendingIntent riddingPending = PendingIntent.getActivity( this, 0, ridding, PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent remotePending	 = PendingIntent.getActivity( this, 0, remote , PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent settingPending = PendingIntent.getActivity( this, 0, setting, 0 );
        
        // ��Ƽ�����̼� ����
        notification = new Notification.BigTextStyle( new Notification.Builder( getApplicationContext() )
        												  .setContentTitle( "Smart Helmet" )
        												  .setSmallIcon( R.drawable.ic_launcher )
        												  .addAction( 0, "DRIVE"  , riddingPending )
        												  .addAction( 0, "CALL"   , remotePending  )
        												  .addAction( 0, "SETTING", settingPending )
        											).build();
        
        // ��Ƽ�����̼� �ɼ�
        notification.flags = Notification.PRIORITY_HIGH;
        
        // ��Ƽ�����̼� ���
		notificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( 0, notification );
	}
	
	// End of User Define Method
	
}

// End of SmarHelmetActivity
