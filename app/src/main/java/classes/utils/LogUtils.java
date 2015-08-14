package classes.utils;

import android.util.Log;

public class LogUtils
{
    private static final boolean debugMode = true;

    public static void println(Object object)
    {
        if (debugMode)
        {
            System.out.println(object);
        }
    }

    public static void println(Object object, String tag)
    {
        if (debugMode)
        {
            Log.i(tag, object.toString());
        }
    }

    public static void printError(Object object)
    {
        if (debugMode)
        {
            Log.e(Constant.LOG_TAG, object.toString());
        }
    }

    public static void tempPrint(Object object)
    {
        System.out.println(object);
    }
}