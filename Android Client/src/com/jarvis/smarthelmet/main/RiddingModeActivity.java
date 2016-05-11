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
	// ����� ����
	private static final 	String 						TAG 		= "Ridding Mode";
	
	// ���� ���� ��ü
	private 				NavigationService 			nvService 	= null;
	private					NotificationReceiver		nrService	= null;
	
	// �̹��� ĸ�� ���� ��ü
	private volatile		CaptureThread 				capThread 	= null;
	
	// ��� ���� ��ü
	private	volatile		TCPClient					client		= null;
	
	// UI ���� ��ü
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
        
        // UI ���� ��ü �ʱ�ȭ
        progressbar = ( ProgressBar ) findViewById( R.id.progressBar );
        tmapExecute = ( TextView ) findViewById( R.id.frontText);
        
        // ����ó���� ���� ��Ƽ�����̼��� �����Ѵ�.
        NotificationManager notificationManager
                            = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
        notificationManager.cancel(0);
        
        // TCP/IP ��� ����
        if ( client == null ) {
        	client = new TCPClient( "drive" );
        	client.execute( this );
        }
        
        // ���� ���� ����
        notification();
        navigation();
    }
    
    @Override
    protected void onRestart() {
    	Log.e( TAG, "onRestart()" );
    	
    	super.onRestart();
    	
    	// ���̾� �α� ����
    	dialog.dismiss();
    	
    	// �׺���̼� ���� �����
    	navigation();
    }
    
    @Override
    protected void onStart() {
    	Log.e( TAG, "onStart()" );
    	
    	super.onStart();
    	
    	// ProgressBar & Text View ���� ����, ����� �ÿ� ����
    	progressbar.setVisibility( ProgressBar.GONE );
    	tmapExecute.setVisibility( TextView.GONE );
    }

    @Override
    protected void onDestroy() {
    	Log.e( TAG, "onDestroy()" );
    	
    	super.onDestroy();
    	
    	// ��Ƽ�����̼� ���� ����
    	if ( nrService != null ) {
    		unregisterReceiver( nrService );
    		nrService = null;
    	}
    	
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
	// ��Ƽ�����̼� ���ù� ���񽺸� �����Ѵ�.
    private void notification() {
    	Log.e( TAG, "notification()" );
    	
    	if ( nrService == null ) {
    		nrService = new NotificationReceiver();
    		IntentFilter mIntentFilter = new IntentFilter();
    		mIntentFilter.addAction( Constants.NOTIFICATION_LISTENER );
    		registerReceiver( nrService, mIntentFilter );
    	}
    }
    
    // T map �����Ͽ� �������� �˻��ϰ� �� �ȳ��� ���� ���´�.
    private void navigation() {
    	Log.e( TAG, "navigation()" );
    	
        if ( nvService == null ) {
        	nvService = new NavigationService( this );
        }
        
        invokeSearchProtal();
    }
    
    // ������ �Է� �� T map ����
  	private void invokeSearchProtal() {
  		Log.e( TAG, "invokeSearchProtal()" );
  		
  		AlertDialog.Builder builder   = new AlertDialog.Builder( this );
  		final EditText 	    inputText = new EditText( this );
	 		
 		builder.setTitle( "T MAP ���� �˻�" );
 		builder.setView( inputText );
 		builder.setCancelable( false );
 		builder.setPositiveButton( "Ȯ��", new DialogInterface.OnClickListener() { 
 		    @Override
 		    public void onClick( DialogInterface d, int which ) {
 		    	String search = inputText.getText().toString();
 		    	
				// String ������ 0���� �� ��� T map turn on
 		    	// �ƴ� ��� �ٽ� ���̾�α� ���
				if ( search.trim().length() > 0 ) {
			        progressbar.setVisibility( ProgressBar.VISIBLE );
			        tmapExecute.setVisibility( TextView.VISIBLE );
					nvService.invokeSearchProtal( search );
				} else {
					invokeSearchProtal();
				}
			}
 		});
 		builder.setNegativeButton( "���", new DialogInterface.OnClickListener() {
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
		// ����� ����
		private final 	String 	TAG 			= "NotificationReceiver";
		
		// ��Ƽ�����̼� �����ʿ��� ���� �޾� �����ϴ� ����
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
	            		
	            		// notification id ���� �����Ͽ� ����	
	        			switch ( notiId ) {
	            			case    2	:
	            			case  123	: 		
	            			case 1234	:		
	    	            		String msg = "";
	    	            		
	            				switch ( notiId ) {
									case    2	:	msg = "kakao";	break;	// īī����
									case  123	:	msg = "mms";	break;	// �޽���
								}
		                		
	            				try {
	            					TCPClient.write( "noti".getBytes(), 4 );
		    					} catch ( IOException ioe ) {
		    						ioe.printStackTrace();
		    					}
	            				
		                		// ����� �޽��� ����
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
        				// T map ��Ƽ�����̼��� ���� �� ��� ĸ�ĸ� �����.
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
  		// ����� ����
  		private final	String	TAG			= "Capture Thread";
  		
  		// ���� ����
  		private final 	int 	RUNNING 	= 0;
  		private final 	int 	SUSPENDED 	= 1;
  		private final 	int 	STOPPED 	= 2;
  		private 		int 	state 		= SUSPENDED;
  		
  		// ������ ����
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