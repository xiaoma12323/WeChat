package cn.maluit.WeChat.web.servlet;

import cn.maluit.WeChat.Common.AccessTokenInfo;
import cn.maluit.WeChat.entry.AccessToken;
import cn.maluit.WeChat.util.JDBC;
import cn.maluit.WeChat.util.NetWorkHelper;
import cn.maluit.WeChat.util.PropertiesUtil;
import cn.maluit.WeChat.util.WeChatApiUtil;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Properties;

/**
 * 用于获取accessToken的Servlet
 * Created by mi on 2017/3/29.
 */
@WebServlet(
        name = "AccessTokenServlet",
        urlPatterns = {"/AccessTokenServlet"},
        loadOnStartup = 1
)
public class AccessTokenServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("启动WebServlet");
        super.init();

        //开启一个新的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //获取accessToken
                        AccessTokenInfo.accessToken = getToken();
                        //获取成功
                        if (AccessTokenInfo.accessToken != null) {
                            //获取到access_token 休眠7000秒,大约2个小时左右
                            Thread.sleep(7000 * 1000);
//                            Thread.sleep(10 * 1000);//10秒钟获取一次
                        } else {
                            //获取失败
                            Thread.sleep(1000 * 3); //获取的access_token为空 休眠3秒
                        }
                    } catch (Exception e) {
                        System.out.println("发生异常：" + e.getMessage());
                        e.printStackTrace();
                        try {
                            Thread.sleep(1000 * 10); //发生异常休眠1秒
                        } catch (Exception e1) {

                        }
                    }
                }

            }
        }).start();
    }

    /**
     * 通用接口获取Token凭证
     *
     * @return
     */
    public static AccessToken getToken() {

        //从properties文件中获取服务器凭证
        Properties prop = PropertiesUtil.getPropperties("credential.properties");
        String appId = prop.getProperty("appid");
        String appSecret = prop.getProperty("appsecret");

        AccessToken token = null;
        String tockenUrl = WeChatApiUtil.getTokenUrl(appId, appSecret);
        JSONObject jsonObject = NetWorkHelper.httpsRequest(tockenUrl, "GET", null);
        if (null != jsonObject) {
            try {
                token = new AccessToken();
                System.out.println(jsonObject.toString());
                token.setAccessToken(jsonObject.getString("access_token"));
                token.setExpiresin(jsonObject.getInt("expires_in"));

            } catch (JSONException e) {
                token = null;// 获取token失败
            }
        }
        //获取到的AccessToken存放在内存中
        AccessTokenInfo.accessToken = token;

        //获取到的AccessToken存放在数据库中
        try {
            JDBC.saveAccessToken(token.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }


//    /**
//     * 获取access_token
//     *
//     * @return AccessToken
//     */
//    private AccessToken getAccessToken(String appId, String appSecret) {
//        NetWorkHelper netHelper = new NetWorkHelper();
//        /**
//         * 接口地址为https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET，其中grant_type固定写为client_credential即可。
//         */
//        String Url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, appSecret);
//        //此请求为https的get请求，返回的数据格式为{"access_token":"ACCESS_TOKEN","expires_in":7200}
//        String result = netHelper.getHttpsResponse(Url, "");
//        System.out.println("获取到的access_token=" + result);
//        //使用FastJson将Json字符串解析成Json对象
//        JSONObject json = JSON.parseObject(result);
//        AccessToken token = new AccessToken();
//        token.setAccessToken(json.getString("access_token"));
//        token.setExpiresin(json.getInteger("expires_in"));
//        return token;
//    }
}
