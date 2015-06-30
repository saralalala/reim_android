package netUtils.common;

import com.rushucloud.reim.R;

import classes.utils.ViewUtils;

public class NetworkConstant
{
    public static final String USER_AGENT = "ReimApp";
    public static final String X_REIM_JWT = "X-reim-jwt";

    public static final String USERNAME = "email";
    public static final String PASSWORD = "password";
    public static final String PROXY = "proxy";
    public static final String DEVICE_TYPE = "device_type";
    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String DEVICE_TOKEN = "device_token";
    public static final String SERVER_TOKEN = "server_token";

    public static final int IMAGE_TYPE_AVATAR = 0;
    public static final int IMAGE_TYPE_INVOICE = 1;
    public static final int IMAGE_TYPE_ICON = 2;

    public static final int CONTACT_TYPE_WECHAT = 0;
    public static final int CONTACT_TYPE_EMAIL = 1;
    public static final int CONTACT_TYPE_PHONE = 2;

    public static final int PUSH_TYPE_SYSTEM_MESSAGE = 1;
    public static final int PUSH_TYPE_REPORT = 2;
    public static final int PUSH_TYPE_INVITE = 3;
    public static final int PUSH_TYPE_INVITE_REPLY = 4;
    public static final int PUSH_TYPE_ADMIN_MESSAGE = 5;
    public static final int PUSH_TYPE_APPLY = 6;
    public static final int PUSH_TYPE_APPLY_REPLY = 7;

    public static final int PUSH_REPORT_TYPE_MINE_REJECTED = 0;
    public static final int PUSH_REPORT_TYPE_MINE_REJECTED_WITH_COMMENT = 1;
    public static final int PUSH_REPORT_TYPE_MINE_APPROVED = 2;
    public static final int PUSH_REPORT_TYPE_MINE_FINISHED = 3;
    public static final int PUSH_REPORT_TYPE_MINE_SUBMMITED_ONLY_COMMENT = 4;
    public static final int PUSH_REPORT_TYPE_MINE_REJECTED_ONLY_COMMENT = 5;
    public static final int PUSH_REPORT_TYPE_MINE_APPROVED_ONLY_COMMENT = 6;
    public static final int PUSH_REPORT_TYPE_MINE_FINISHED_ONLY_COMMENT = 7;
    public static final int PUSH_REPORT_TYPE_OTHERS_SUBMMITED = 8;
    public static final int PUSH_REPORT_TYPE_OTHERS_SUBMMITED_CC = 9;
    public static final int PUSH_REPORT_TYPE_OTHERS_CAN_BE_APPROVED_ONLY_COMMENT = 10;
    public static final int PUSH_REPORT_TYPE_OTHERS_SUBMITTED_ONLY_COMMENT = 11;
    public static final int PUSH_REPORT_TYPE_OTHERS_REJECTED_ONLY_COMMENT = 12;
    public static final int PUSH_REPORT_TYPE_OTHERS_APPROVED_ONLY_COMMENT = 13;

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
    public static final int ERROR_SAME_COMPANY = -37;
    public static final int ERROR_MESSAGE_DONE = -38;
    public static final int ERROR_LAST_ADMIN = -39;
    public static final int ERROR_NOT_UNIQUE = -42;
    public static final int ERROR_COMPANY_EXISTS = -49;
    public static final int ERROR_PROXY_ERROR = -58;

    public static String errorCodeToString(int code)
    {
        int result = -1;
        switch (code)
        {
            case NetworkConstant.ERROR_SYSTEM_ERROR:
                result = R.string.error_network_system_error;
                break;
            case NetworkConstant.ERROR_USER_NOT_EXISTS:
                result = R.string.error_network_user_not_exists;
                break;
            case NetworkConstant.ERROR_MAIL_SEND_ERROR:
                result = R.string.error_network_mail_send_error;
                break;
            case NetworkConstant.ERROR_PARAMETER_ERROR:
                result = R.string.error_network_parameter_error;
                break;
            case NetworkConstant.ERROR_EMPTY_HEADER:
                result = R.string.error_network_empty_header;
                break;
            case NetworkConstant.ERROR_AUTH_FAIL:
                result = R.string.error_network_auth_fail;
                break;
            case NetworkConstant.ERROR_USER_EXISTS:
                result = R.string.error_network_user_exists;
                break;
            case NetworkConstant.ERROR_AUTH_TIMEOUT:
                result = R.string.error_network_auth_timeout;
                break;
            case NetworkConstant.ERROR_BAD_PERMISSION:
                result = R.string.error_network_bad_permission;
                break;
            case NetworkConstant.ERROR_ALREADY_BOUND:
                result = R.string.error_network_already_bound;
                break;
            case NetworkConstant.ERROR_USER_AUTH_ERROR:
                result = R.string.error_network_user_auth_error;
                break;
            case NetworkConstant.ERROR_BAD_ITEMS:
                result = R.string.error_network_bad_items;
                break;
            case NetworkConstant.ERROR_EMPTY_BIND:
                result = R.string.error_network_empty_bind;
                break;
            case NetworkConstant.ERROR_CLOSE_REPORT:
                result = R.string.error_network_close_report;
                break;
            case NetworkConstant.ERROR_EMPTY_CATEGORY:
                result = R.string.error_network_empty_category;
                break;
            case NetworkConstant.ERROR_ZERO_AMOUNT:
                result = R.string.error_network_zero_amount;
                break;
            case NetworkConstant.ERROR_OLDER_COMPANY:
                result = R.string.error_network_older_company;
                break;
            case NetworkConstant.ERROR_EMPTY_REPORT:
                result = R.string.error_network_empty_report;
                break;
            case NetworkConstant.ERROR_EMPTY_ITEMS:
                result = R.string.error_network_empty_items;
                break;
            case NetworkConstant.ERROR_ITEM_ADDED:
                result = R.string.error_network_item_added;
                break;
            case NetworkConstant.ERROR_REPORT_DELETED:
                result = R.string.error_network_report_deleted;
                break;
            case NetworkConstant.ERROR_REPORT_NOT_EXISTS:
                result = R.string.error_network_report_not_exists;
                break;
            case NetworkConstant.ERROR_SIGN_IN:
                result = R.string.error_network_sign_in;
                break;
            case NetworkConstant.ERROR_NAME_EXCEED_LIMIT:
                result = R.string.error_network_name_exceed_limit;
                break;
            case NetworkConstant.ERROR_SAME_COMPANY:
                result = R.string.error_network_same_company;
                break;
            case NetworkConstant.ERROR_MESSAGE_DONE:
                result = R.string.error_network_message_done;
                break;
            case NetworkConstant.ERROR_LAST_ADMIN:
                result = R.string.error_network_last_admin;
                break;
            case NetworkConstant.ERROR_NOT_UNIQUE:
                result = R.string.error_network_not_unique;
                break;
            case NetworkConstant.ERROR_COMPANY_EXISTS:
                result = R.string.error_network_company_exists;
                break;
            case NetworkConstant.ERROR_PROXY_ERROR:
                result = R.string.error_network_proxy_error;
                break;
            default:
                break;
        }
        return result == -1 ? "" : ViewUtils.getString(result);
    }
}
