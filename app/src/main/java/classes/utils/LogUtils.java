package classes.utils;

import android.util.Log;

public class LogUtils
{
    private static final boolean debugMode = false;

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

    public static void tempPrint(Object object)
    {
        System.out.println(object);
    }
}