package netUtils;

public class NetworkConstant
{
    public static final String USER_AGENT = "ReimApp";
    public static final String X_REIM_JWT = "X-reim-jwt";

    public static final String USERNAME = "email";
    public static final String PASSWORD = "password";
    public static final String DEVICE_TYPE = "device_type";
    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String DEVICE_TOKEN = "device_token";
    public static final String SERVER_TOKEN = "server_token";

    public static final int IMAGE_TYPE_AVATAR = 0;
    public static final int IMAGE_TYPE_INVOICE = 1;
    public static final int IMAGE_TYPE_ICON = 2;

    public static final int PUSH_TYPE_SYSTEM_MESSAGE = 1;
    public static final int PUSH_TYPE_REPORT = 2;
    public static final int PUSH_TYPE_INVITE = 3;
    public static final int PUSH_TYPE_INVITE_REPLY = 4;
    public static final int PUSH_TYPE_ADMIN_MESSAGE = 5;
    public static final int PUSH_TYPE_APPLY = 6;
    public static final int PUSH_TYPE_APPLY_REPLY = 7;

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

    public static String errorCodeToString(int code)
    {
        String result = null;
        switch (code)
        {
            case NetworkConstant.ERROR_SYSTEM_ERROR:
                result = "系统错误，请稍后尝试";
                break;
            case NetworkConstant.ERROR_USER_NOT_EXISTS:
                result = "用户不存在或密码错误";
                break;
            case NetworkConstant.ERROR_MAIL_SEND_ERROR:
                result = "邮件发送错误";
                break;
            case NetworkConstant.ERROR_PARAMETER_ERROR:
                result = "参数错误";
                break;
            case NetworkConstant.ERROR_EMPTY_HEADER:
                result = "非法请求";
                break;
            case NetworkConstant.ERROR_AUTH_FAIL:
                result = "认证失败";
                break;
            case NetworkConstant.ERROR_USER_EXISTS:
                result = "用户已经存在";
                break;
            case NetworkConstant.ERROR_AUTH_TIMEOUT:
                result = "认证超时";
                break;
            case NetworkConstant.ERROR_BAD_PERMISSION:
                result = "权限不足本次操作";
                break;
            case NetworkConstant.ERROR_ALREADY_BOUND:
                result = "用户已绑定";
                break;
            case NetworkConstant.ERROR_USER_AUTH_ERROR:
                result = "用户认证失败";
                break;
            case NetworkConstant.ERROR_BAD_ITEMS:
                result = "条目信息不齐全，请重新填写";
                break;
            case NetworkConstant.ERROR_EMPTY_BIND:
                result = "尚未绑定账号";
                break;
            case NetworkConstant.ERROR_CLOSE_REPORT:
                result = "您提交的报告已经处于关闭状态";
                break;
            case NetworkConstant.ERROR_EMPTY_CATEGORY:
                result = "没有选定分类";
                break;
            case NetworkConstant.ERROR_ZERO_AMOUNT:
                result = "没有报销额度";
                break;
            case NetworkConstant.ERROR_OLDER_COMPANY:
                result = "报销中有条目不属于当前公司";
                break;
            case NetworkConstant.ERROR_EMPTY_REPORT:
                result = "报销不存在";
                break;
            case NetworkConstant.ERROR_EMPTY_ITEMS:
                result = "没有提交项目";
                break;
            case NetworkConstant.ERROR_ITEM_ADDED:
                result = "条目已加入报销";
                break;
            case NetworkConstant.ERROR_REPORT_DELETED:
                result = "报告已被删除";
                break;
            case NetworkConstant.ERROR_REPORT_NOT_EXISTS:
                result = "报告不存在";
                break;
            case NetworkConstant.ERROR_SIGN_IN:
                result = "用户名或密码错误";
                break;
            case NetworkConstant.ERROR_NAME_EXCEED_LIMIT:
                result = "报告名不能超过50个字";
                break;
            case NetworkConstant.ERROR_SAME_COMPANY:
                result = "已经在同一家公司";
                break;
            case NetworkConstant.ERROR_MESSAGE_DONE:
                result = "此消息已被处理";
                break;
            case NetworkConstant.ERROR_LAST_ADMIN:
                result = "你是公司里最后一个管理员，请先指定公司里另一成员成为管理员";
                break;
            case NetworkConstant.ERROR_NOT_UNIQUE:
                result = "";
                break;
            case NetworkConstant.ERROR_COMPANY_EXISTS:
                result = "公司已存在";
                break;
            default:
                break;
        }
        return result;
    }
}
