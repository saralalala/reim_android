package netUtils;

public class NetworkConstant
{	
	public static String USER_AGENT = "ReimApp";
	public static String X_REIM_JWT = "X-reim-jwt";
	
	public static String USERNAME = "email";
	public static String PASSWORD = "password";
	public static String DEVICE_TYPE = "device_type";
	public static String DEVICE_TYPE_ANDROID = "android";
	public static String DEVICE_TOKEN = "device_token";
	public static String SERVER_TOKEN = "server_token";

	public static int IMAGE_TYPE_AVATAR = 0;
	public static int IMAGE_TYPE_INVOICE = 1;
	public static int IMAGE_TYPE_ICON = 2;
	
	public static final int ERROR_SYSTEM_ERROR = -1;
	public static final int ERROR_USER_NOT_EXISTS = -3;
	public static final int ERROR_MAIL_SEND_ERROR = -5;
	public static final int ERROR_PARAMETER_ERROR = -6;
	public static final int ERROR_EMPTY_HEADER = -7;
	public static final int ERROR_AUTH_FAIL = -8;
	public static final int ERROR_USER_EXISTS = -10;
	public static final int ERROR_AUTH_TIMEOUT = -11;
	public static final int ERROR_BAD_PERMISSION = -12;
	public static final int ERROR_ALREADY_BOUND = -13;
	public static final int ERROR_USER_AUTH_ERROR = -14;
	public static final int ERROR_BAD_ITEMS = -15;
	public static final int ERROR_EMPTY_BIND = -16;
	public static final int ERROR_CLOSE_REPORT = -17;
	public static final int ERROR_EMPTY_CATEGORY = -18;
	public static final int ERROR_ZERO_AMOUNT = -19;
	public static final int ERROR_OLDER_COMPANY = -20;
	public static final int ERROR_EMPTY_REPORT = -21;
	public static final int ERROR_EMPTY_ITEMS = -22;
	public static final int ERROR_ITEM_ADDED = -23;
	public static final int ERROR_REPORT_DELETED = -24;
	public static final int ERROR_REPORT_NOT_EXISTS = -25;
	public static final int ERROR_SIGN_IN = -31;
	public static final int ERROR_NAME_EXCEED_LIMIT = -33;
    public static final int ERROR_INVITE_DONE = -38;
}
