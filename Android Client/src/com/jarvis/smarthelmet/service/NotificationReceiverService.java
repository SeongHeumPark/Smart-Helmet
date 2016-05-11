package com.jarvis.smarthelmet.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.jarvis.smarthelmet.utils.Constants;

public class NotificationReceiverService extends NotificationListenerService {
	// 디버깅 변수
	private static final 	String 		TAG	=	"NotificationReceiverService";
    
    /********************************************************************************************
	 * 																							*
	 *										Override Method										*
	 *																							*
	 ********************************************************************************************/
    @Override
    public void onCreate() {
    	Log.e( TAG, "onCreate()" );
    	
    	super.onCreate();
    };
    
    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
    	Log.e( TAG, "onStartCommand()" );
    	
    	return super.onStartCommand( intent, flags, startId );
    }
    
    @Override
    public void onDestroy() {
    	Log.e( TAG, "onDestroy()" );
    	
    	super.onDestroy();
    }
    
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Log.e( TAG, "onNotificationPosted()" );
		
		// 다른 클래스의 브로드캐스트 리시버로 보낸다.
		Intent mIntent = new Intent( Constants.NOTIFICATION_LISTENER );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_CMD, Constants.NOTIFICATION_CMD_ADD );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_ID, sbn.getId() );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_PACKAGE, sbn.getPackageName() );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_TEXT, sbn.getNotification().tickerText );
		
        sendBroadcast( mIntent );
	}

	@Override
	public void onNotificationRemoved( StatusBarNotification sbn ) {
		Log.e( TAG, "onNotifiicationRemoved()" );
		
		// 다른 클래스의 브로드캐스트 리시버로 보낸다.
		Intent mIntent = new Intent( Constants.NOTIFICATION_LISTENER );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_CMD, Constants.NOTIFICATION_CMD_REMOVE );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_ID, sbn.getId() );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_PACKAGE, sbn.getPackageName() );
		mIntent.putExtra( Constants.NOTIFICATION_KEY_TEXT, sbn.getNotification().tickerText );
		
        sendBroadcast( mIntent );
	}
	
	// End of Override Method
}

// End of NotificationReceiverService