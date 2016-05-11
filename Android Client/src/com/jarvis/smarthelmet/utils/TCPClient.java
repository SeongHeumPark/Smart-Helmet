package com.jarvis.smarthelmet.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class TCPClient extends AsyncTask<Object, Void, Object> {
	// 디버깅 변수
	private static final 	String 		TAG 		= "TCPClient";
	
	// 사용자 정의 객체 & 변수
	private 				InetAddress serverAddr 	= null;
	private	static			Socket 		socket 		= null;
	private final			String 		serverIP 	= "210.118.75.161";
	private final 			int 		port 		= 3000;
	private					String		mode		= null;
	
	/****************************************************************************************
	 * 																						*
	 *										Constructor										*
	 *																						*
	 ****************************************************************************************/
	public TCPClient( String mode ) {
		// 안드로이드가 어떤 모드인지 알려주기 위해 필요함
		this.mode = mode;
	}
	
	// End of Constructor
	
	/********************************************************************************************
	 * 																							*
	 *										Override Method										*
	 *																							*
	 ********************************************************************************************/
	@Override
	protected Object doInBackground( Object... params ) {
		try {
			Log.d( TAG, "Connecting..." );
			
			serverAddr = InetAddress.getByName( serverIP );
			socket = new Socket( serverAddr, port );
		} catch ( UnknownHostException uhe ) {
			uhe.printStackTrace();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
		
		publishProgress( ( Void ) null );
		
		return null;
	}
	
	@Override
	protected void onPostExecute( Object result ) {
		super.onPostExecute( result );

		try {
			write( "android".getBytes(), 7 );
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
		
		try {
			write( mode.getBytes(), mode.length() );
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}
	
	// End of Override Method
	
	/************************************************************************************************
     *																								*
     *										User Define Method										*
     *																								*
     ************************************************************************************************/
	public void stop() {
		this.cancel( true );
		
		if ( isCancelled() == true ) {
			Log.e( TAG, "Task is cancelled" );
			
			try {
				if ( socket != null ) {
					socket.close();
					socket = null;
					
					Log.d( TAG, "socket close success" );
				}
			} catch ( IOException ioe ) {
				ioe.printStackTrace();
			}
		} else {
			Log.e( TAG, "안꺼짐 ㅡㅡ " );
		}
	}
	
	// 받는 부분
	public void read() throws IOException {
		DataInputStream dis = new DataInputStream( socket.getInputStream() );
		
		byte[] data = null;
		
		dis.read( data );
	}
	
	// 보내는 부분
	public static synchronized void write( byte[] data, int dsize ) throws IOException {
		DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );
		
		if( socket == null ) return;
		
		synchronized (dos) {
			dos.write( data, 0, dsize );
			dos.flush();
		}
	}
	
	// End of User Define Method
}

// End of TCPClient