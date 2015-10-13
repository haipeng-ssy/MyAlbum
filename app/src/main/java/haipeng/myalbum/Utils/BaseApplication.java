package haipeng.myalbum.Utils;

import android.app.Application;
import android.util.Log;

/**
 * Created by Administrator on 2015/8/19.
 */
public class BaseApplication extends Application{

    private static BaseApplication sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;//这个相当于activity的上下文
        Log.i("tag","BaseApplication onCreate()");
    }

    public static BaseApplication getContext() {
        return sContext;
    }



}
