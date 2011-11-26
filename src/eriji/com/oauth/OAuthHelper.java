package eriji.com.oauth;

import java.util.Scanner;

import android.util.Log;

import com.ch_linghu.fanfoudroid.fanfou.Weibo;

public class OAuthHelper {
	
	public OAuthHelper() {
	}
	
	public static void main(String arg[]) throws Exception {
		Weibo api = new Weibo();
		OAuthClient oauth = api.getHttpClient().getOAuthClient();
		oauth.setStore(new OAuthFileStore("/sdcard/fanfoudroid/"));

		try {
			// 只运行一次
			if (!oauth.hasAccessToken()) {
				// 获取request token, 并储存起来
				String authUrl = oauth.retrieveRequestToken();
				// 询问用户pinCode
				Log.i("LDS", "尚未授权本程序访问权, 请前去服务器授权, 并记下PinCode : "
						+ authUrl);
				System.out.println("请输入PinCode:");
				Scanner in = new Scanner(System.in);
				// 利用储存起来的request token和PinCode去与服务器交换access Token, 并储存起来
				//oauth.retrieveAccessToken(in.nextLine());
			}

			// 测试API服务器是否工作正常
			//Log.i("LDS", api.test() ? "Ok" : "Oh, no." );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
