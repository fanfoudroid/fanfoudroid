package eriji.com.oauth;

import android.util.Log;

/*  log4j to android Log */
public class Logger {
	private static Logger mLogger = null;
	private static String tag = "fanfoudroid"; 

	public static Logger getLogger(Class<?> class1) {
		// TODO Auto-generated method stub
		if (mLogger == null) {
			mLogger = new Logger();
		}
		return mLogger;
	}

	public void debug(String msg) {
		// TODO Auto-generated method stub
		Log.d(tag, msg);
	}

	public void info(String msg) {
		Log.i(tag, msg);
	}

	public void error(String msg) {
		Log.e(tag, msg);
	}

}
