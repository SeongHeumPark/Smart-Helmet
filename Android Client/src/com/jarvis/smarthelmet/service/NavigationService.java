package com.jarvis.smarthelmet.service;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.jarvis.smarthelmet.utils.TCPClient;
import com.skp.Tmap.TMapTapi;
import com.skp.Tmap.TMapView;

public class NavigationService {
	// 디버깅 변수
	private final static 	String 		TAG 		= "Navigation Service";
	
	// T map 객체 및 변수
	private 				TMapView 	tmapView 	= null;
	private static final 	String 		appKey 		= "95e1343f-d830-3095-a7f5-abafb5e725d5";
	private static final 	String 		bizAppID 	= "ebb095ec84b1";

	// 사용자 정의 객체 & 변수
	private 				Activity 	activity 	= null;
	private					String 		filepath 	= Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartHelmet/screencap.png";
	
	/****************************************************************************************
	 * 																						*
	 *										Constructor										*
	 *																						*
	 ****************************************************************************************/
	public NavigationService( Activity activity ) {
		Log.e( TAG, "constructor()" );
		
		this.activity = activity;
		
		tmapView = new TMapView( activity );
		
		navigationSetting();
	}
	
	// End of Constructor
		
	/****************************************************************************************
     *																						*
     *									User Define Method									*
     *																						*
     ****************************************************************************************/
	// 티맵 초기 셋팅
 	private void navigationSetting() {
 		Log.e( TAG, "navigationSetting()" );
 		
 		new Thread() {
 			@Override
 			public void run() {
 				tmapView.setSKPMapApiKey  ( appKey );
 				tmapView.setSKPMapBizappId( bizAppID );
 			}
 		}.start();
 	}
 	
 	// 목적지 입력 후 T map 연동
 	public void invokeSearchProtal( final String search ) {
 		Log.e( TAG, "invokeSearchProtal()" );
 		
    	new Thread() {
			@Override
			public void run() {	
				TMapTapi tmaptapi = new TMapTapi( activity );
				tmaptapi.invokeSearchPortal( search );
			}
		}.start();
	}
 	
 	// 현재 화면 캡쳐 기능
 	public void screenCapture() {
 		Log.e( TAG, "screenCapture()" );
 		
 		String filename = "screencap.png";
 		
 		// adb shell screencap -p 명령어로 스크린 샷을 찍은 후 다시 디렉토리에서 읽어와 서버로 전송한다.
 		try {
 			Runtime.getRuntime().exec( "su -c screencap -p /storage/emulated/0/SmartHelmet/" + filename );
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	// 이미지 전송 
	public void transferBitmap() {
		Log.e( TAG, "transferBitmap()" );
		// 비트맵 변수
 		Bitmap image 		= null;
 		Bitmap cutImage 	= null;
 		Bitmap scaledImage	= null;
 		Bitmap rotatedImage = null;
 		
 		// 이미지를 담을 바이트 데이터
 		byte[] data			= null;
 		
 		try {
			TCPClient.write( "navi".getBytes(), 4 );
		} catch( Exception e ) {
			e.printStackTrace();
		}
 		
 		// 이미지 가공 (자르기, 확대, 회전)
		try {
			image = BitmapFactory.decodeFile( filepath );
			cutImage = Bitmap.createBitmap( image, 0, 330, image.getWidth() - 1, image.getHeight() - 595 );
			scaledImage = Bitmap.createScaledBitmap( cutImage, 320, 320, true );
			rotatedImage = rotateBitmap( scaledImage, 270 );
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			rotatedImage.compress( CompressFormat.JPEG, 20, baos );
			data = baos.toByteArray();
		} catch ( Exception e ) {
			e.printStackTrace();
		} catch ( OutOfMemoryError oome ) {
			oome.printStackTrace();
		}
		
		// 이미지 전송
		try {
			TCPClient.write( data, data.length );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
 	}
 	
	// 이미지 회전 기능
	private Bitmap rotateBitmap( Bitmap bitmap, int degrees ) {
		Log.e( TAG, "rotateBitmap()" );
		if ( degrees != 0 && bitmap != null ) {
			Matrix m = new Matrix();
			m.setRotate( degrees, ( float ) bitmap.getWidth() / 2, ( float ) bitmap.getHeight() / 2 );
    
			try {
				Bitmap converted = Bitmap.createBitmap( bitmap, 0, 0,  bitmap.getWidth(), bitmap.getHeight(), m, true );
				
				if ( bitmap != converted ) {
					bitmap = null;
					bitmap = converted;
				}
			}
			catch( OutOfMemoryError ex ) {
				// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
			}
		}
		
		return bitmap;
	}
	
 	// End of User Define Method
}

// End of NavigationService