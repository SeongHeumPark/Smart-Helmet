package com.jarvis.smarthelmet.main;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jarvis.smarthelmet.R;
import com.jarvis.smarthelmet.service.NavigationService;
import com.jarvis.smarthelmet.utils.Constants;
import com.jarvis.smarthelmet.utils.TCPClient;

public class RiddingModeActivity extends Activity {
	// 디버깅 변수
	private static final 	String 						TAG 		= "Ridding Mode";
	
	// 서비스 관련 객체
	private 				NavigationService 			nvService 	= null;
	private					NotificationReceiver		nrService	= null;
	
	// 이미지 캡쳐 관련 객체
	private volatile		CaptureThread 				capThread 	= null;
	
	// 통신 관련 객체
	private	volatile		TCPClient					client		= null;
	
	// UI 관련 객체
	private					DialogInterface				dialog		= null;
	private					ProgressBar 				progressbar = null;
	private					TextView					tmapExecute	= null;
	
	/********************************************************************************************
	 * 																							*
	 *										Override Method										*
	 *																							*
	 ********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.e( TAG, "onCreate()" );
    	
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_ridding_mode );
        
        // UI 관련 객체 초기화
        progressbar = ( ProgressBar ) findViewById( R.id.progressBar );
        tmapExecute = ( TextView ) findViewById( R.id.frontText);
        
        // 예외처리를 위해 노티피케이션을 제거한다.
        NotificationManager notificationManager
                            = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
        notificationManager.cancel(0);
        
        // TCP/IP 통신 시작
        if ( client == null ) {
        	client = new TCPClient( "drive" );
        	client.execute( this );
        }
        
        // 각종 서비스 시작
        notification();
        navigation();
    }
    
    @Override
    protected void onRestart() {
    	Log.e( TAG, "onRestart()" );
    	
    	super.onRestart();
    	
    	// 다이얼 로그 제거
    	dialog.dismiss();
    	
    	// 네비게이션 서비스 재시작
    	navigation();
    }
    
    @Override
    protected void onStart() {
    	Log.e( TAG, "onStart()" );
    	
    	super.onStart();
    	
    	// ProgressBar & Text View 최초 시작, 재시작 시에 숨김
    	progressbar.setVisibility( ProgressBar.GONE );
    	tmapExecute.setVisibility( TextView.GONE );
    }

    @Override
    protected void onDestroy() {
    	Log.e( TAG, "onDestroy()" );
    	
    	super.onDestroy();
    	
    	// 노티피케이션 서비스 종료
    	if ( nrService != null ) {
    		unregisterReceiver( nrService );
    		nrService = null;
    	}
    	
    	// TCP/IP 통신 종료
    	if ( client != null ) {
    		client.stop();
    		client = null;
    	}
    	
    	// SmartHelmetActivity를 실행하여 다시 노티피케이션을 띄운다.
    	Intent mIntent = new Intent( this, SmartHelmetActivity.class );
    	startActivity( mIntent );
    }
    
    // End of Override Method
    
    /************************************************************************************************
     *																								*
     *										User Define Method										*
     *																								*
     ************************************************************************************************/
	// 노티피케이션 리시버 서비스를 시작한다.
    private void notification() {
    	Log.e( TAG, "notification()" );
    	
    	if ( nrService == null ) {
    		nrService = new NotificationReceiver();
    		IntentFilter mIntentFilter = new IntentFilter();
    		mIntentFilter.addAction( Constants.NOTIFICATION_LISTENER );
    		registerReceiver( nrService, mIntentFilter );
    	}
    }
    
    // T map 연동하여 도착지를 검색하고 길 안내를 할지 묻는다.
    private void navigation() {
    	Log.e( TAG, "navigation()" );
    	
        if ( nvService == null ) {
        	nvService = new NavigationService( this );
        }
        
        invokeSearchProtal();
    }
    
    // 목적지 입력 후 T map 연동
  	private void invokeSearchProtal() {
  		Log.e( TAG, "invokeSearchProtal()" );
  		
  		AlertDialog.Builder builder   = new AlertDialog.Builder( this );
  		final EditText 	    inputText = new EditText( this );
	 		
 		builder.setTitle( "T MAP 통합 검색" );
 		builder.setView( inputText );
 		builder.setCancelable( false );
 		builder.setPositiveButton( "확인", new DialogInterface.OnClickListener() { 
 		    @Override
 		    public void onClick( DialogInterface d, int which ) {
 		    	String search = inputText.getText().toString();
 		    	
				// String 내용이 0보다 길 경우 T map turn on
 		    	// 아닐 경우 다시 다이얼로그 띄움
				if ( search.trim().length() > 0 ) {
			        progressbar.setVisibility( ProgressBar.VISIBLE );
			        tmapExecute.setVisibility( TextView.VISIBLE );
					nvService.invokeSearchProtal( search );
				} else {
					invokeSearchProtal();
				}
			}
 		});
 		builder.setNegativeButton( "취소", new DialogInterface.OnClickListener() {
 		    @Override
 		    public void onClick( DialogInterface d, int which ) {
 		        d.cancel();
 		        finish();
 		    }
 		});
 		dialog = builder.show();
 	}
    
    // End of User Define Method
    
    /****************************************************************************************
	 * 																						*
	 *							Notification Service Receiver Class							*
	 *																						*
	 ****************************************************************************************/	
	private class NotificationReceiver extends BroadcastReceiver {
		// 디버깅 변수
		private final 	String 	TAG 			= "NotificationReceiver";
		
		// 노티피케이션 리스너에서 전달 받아 저장하는 변수
		private 		int 	cmd 			= 0;
		private 		int 	notiId 			= 0;
		private 		String 	packageName 	= "";
		private 		String 	textTicker 		= "";
		
		/** Override Method **/
		@Override
		public void onReceive( Context context, Intent intent ) {
			Log.e( TAG, "onReceive()" );
			
			cmd 		= intent.getIntExtra	( Constants.NOTIFICATION_KEY_CMD,  0 );
			notiId 		= intent.getIntExtra	( Constants.NOTIFICATION_KEY_ID , -1 );
			packageName = intent.getStringExtra	( Constants.NOTIFICATION_KEY_PACKAGE );
			textTicker 	= intent.getStringExtra	( Constants.NOTIFICATION_KEY_TEXT    );
            
            switch ( cmd ) {
	            case Constants.NOTIFICATION_CMD_ADD :
	            	if ( packageName != null ) {
	            		Log.d( TAG, "- Add ID : " + notiId + " / Package : " + packageName + 
	            				                             " / Text : " + textTicker );
	            		
	            		// notification id 별로 가공하여 저장	
	        			switch ( notiId ) {
	            			case    2	:
	            			case  123	: 		
	            			case 1234	:		
	    	            		String msg = "";
	    	            		
	            				switch ( notiId ) {
									case    2	:	msg = "kakao";	break;	// 카카오톡
									case  123	:	msg = "mms";	break;	// 메시지
								}
		                		
	            				try {
	            					TCPClient.write( "noti".getBytes(), 4 );
		    					} catch ( IOException ioe ) {
		    						ioe.printStackTrace();
		    					}
	            				
		                		// 헤더와 메시지 전송
		                		try {
		    						TCPClient.write( msg.getBytes(), msg.length() );
		    					} catch ( IOException ioe ) {
		    						ioe.printStackTrace();
		    					}

		                		break;
							                		
	            			case 1001091 :
	            				if ( capThread == null ) {
		            				capThread = new CaptureThread();
									capThread.start();
	            				}
								
								break;
	        			}
	        		}
	            	
	            	break;
	            	
	            case Constants.NOTIFICATION_CMD_REMOVE :
	            	if ( packageName != null ) {
	            		Log.d( TAG, "- Remove ID : " + notiId + " / Package : " + packageName + 
	            				                                " / Text : " + textTicker );
	            		
		            	switch ( notiId ) {
        				// T map 노티피케이션이 제거 될 경우 캡쳐를 멈춘다.
		            		case 1001091 :
		            			if( capThread != null ) {
			            			capThread.stop();
			            			capThread = null;
		            			}
								
								break;
		            	}
	            	}
	            	
	            	break;
            }
		}
	}
	
	// End of NotificationReceiver
    
    /****************************************************************************************************
     *																									*
     *										Capture Thread Class										*
     *																									*
     ****************************************************************************************************/
  	private class CaptureThread implements Runnable {
  		// 디버깅 변수
  		private final	String	TAG			= "Capture Thread";
  		
  		// 상태 변수
  		private final 	int 	RUNNING 	= 0;
  		private final 	int 	SUSPENDED 	= 1;
  		private final 	int 	STOPPED 	= 2;
  		private 		int 	state 		= SUSPENDED;
  		
  		// 쓰레드 변수
  		private 		Thread 	thread 		= null;
  		
  		/** Constructor **/
  		public CaptureThread() {
  			Log.e( TAG, "constructor()" );
  			
  			thread = new Thread( this );
  		}
  		
  		/** Override Method **/
  		@Override
  		public void run() {
  			Log.e( TAG, "run()" );
  			
  			while( true ) {
  				nvService.screenCapture();
  				
  				try {
  					Thread.sleep( 500 );
  				} catch( InterruptedException ie ) {
  					ie.printStackTrace();
  				}
  				
  				nvService.transferBitmap();
  				
  				if ( checkState() ) {
  					thread = null;
  					break;
  				}
  			}
  		}
  		
  		/** User Define Method **/
  		public synchronized void setState( int state ) {
  			Log.e( TAG, "setState()" );
  			
  			this.state = state;
  			
  			if ( this.state == RUNNING ) {
  				notify();
  			} else {
  				thread.interrupt();
  			}
  		}
  		
  		public synchronized boolean checkState() {
  			Log.e( TAG, "checkState()" );
  			
  			while ( state == SUSPENDED ) {
  				try {
  					wait();
  				} catch ( InterruptedException ie ) {
  					ie.printStackTrace();
  				}
  			}
  			
  			return state == STOPPED;
  		}
  		
  		public void start() {
  			Log.e( TAG, "start()" );
  			
  			thread.start();
  			
  			setState( RUNNING );
  		}
  		
  		public void stop() {
  			Log.e( TAG, "stop()" );
  			
  			setState( STOPPED );
  		}
  	}
	
    // End of CaptureThread	
}

// End of RiddingModeActivity