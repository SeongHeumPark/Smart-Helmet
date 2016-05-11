package com.jarvis.smarthelmet.utils;

public class Constants {
	// Notification intent action string
	public static final	String 	NOTIFICATION_LISTENER_SERVICE 	= "com.example.smarthelmet.NOTIFICATION_LISTENER_SERVICE";
	public static final String 	NOTIFICATION_LISTENER 			= "com.example.smarthelmet.NOTIFICATION_LISTENER";
	
	// Notification broadcast intent key
	public static final String 	NOTIFICATION_KEY_CMD 			= "notification_command";
    public static final String 	NOTIFICATION_KEY_ID 			= "notification_id";
    public static final String 	NOTIFICATION_KEY_PACKAGE		= "notification_package";
    public static final String 	NOTIFICATION_KEY_TEXT 			= "notification_text";
    
    // Notification command type
    public static final int 	NOTIFICATION_CMD_ADD 			= 1;
    public static final int 	NOTIFICATION_CMD_REMOVE 		= 2;
    public static final int 	NOTIFICATION_CMD_LIST 			= 3;
}

// End of Constants
