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
	// ����� ����
	private final static 	String 		TAG 		= "Navigation Service";
	
	// T map ��ü �� ����
	private 				TMapView 	tmapView 	= null;
	private static final 	String 		appKey 		= "95e1343f-d830-3095-a7f5-abafb5e725d5";
	private static final 	String 		bizAppID 	= "ebb095ec84b1";

	// ����� ���� ��ü & ����
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
	// Ƽ�� �ʱ� ����
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
 	
 	// ������ �Է� �� T map ����
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
 	
 	// ���� ȭ�� ĸ�� ���
 	public void screenCapture() {
 		Log.e( TAG, "screenCapture()" );
 		
 		String filename = "screencap.png";
 		
 		// adb shell screencap -p ��ɾ�� ��ũ�� ���� ���� �� �ٽ� ���丮���� �о�� ������ �����Ѵ�.
 		try {
 			Runtime.getRuntime().exec( "su -c screencap -p /storage/emulated/0/SmartHelmet/" + filename );
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	// �̹��� ���� 
	public void transferBitmap() {
		Log.e( TAG, "transferBitmap()" );
		// ��Ʈ�� ����
 		Bitmap image 		= null;
 		Bitmap cutImage 	= null;
 		Bitmap scaledImage	= null;
 		Bitmap rotatedImage = null;
 		
 		// �̹����� ���� ����Ʈ ������
 		byte[] data			= null;
 		
 		try {
			TCPClient.write( "navi".getBytes(), 4 );
		} catch( Exception e ) {
			e.printStackTrace();
		}
 		
 		// �̹��� ���� (�ڸ���, Ȯ��, ȸ��)
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
		
		// �̹��� ����
		try {
			TCPClient.write( data, data.length );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
 	}
 	
	// �̹��� ȸ�� ���
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
				// �޸𸮰� �����Ͽ� ȸ���� ��Ű�� ���� ��� �׳� ������ ��ȯ�մϴ�.
			}
		}
		
		return bitmap;
	}
	
 	// End of User Define Method
}

// End of NavigationService