package haipeng.myalbum.Utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class Version {

	public static String getVersionName() {
		Context context = new Application();
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("packainfo error");
			return "";
		}
	}

	public static int getVersionCode() {
		Context context = new Application();
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("packainfo error");
			return 0;
		}

	}
	
	//IMEI号
	public static String getDeviceID(){
		Context context = new Application();
		return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
}
