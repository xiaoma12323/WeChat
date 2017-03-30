package cn.maluit.WeChat.util;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;

/**
 * 访问网络用到的工具类
 * Created by mi on 2017/3/29.
 */
public class NetWorkHelper {

    private static Logger log = LoggerFactory.getLogger(WeChatApiUtil.class);

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new JEEWeiXinX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            log.error("WeChat server connection timed out.");
        } catch (Exception e) {
            log.error("https request error:{}", e);
        }
        return jsonObject;
    }


//    /**
//     * 发起Https请求
//     * @param reqUrl 请求的URL地址
//     * @param requestMethod
//     * @return 响应后的字符串
//     */
//    public String getHttpsResponse(String reqUrl, String requestMethod) {
//        URL url;
//        InputStream is;
//        String resultData = "";
//        try {
//            url = new URL(reqUrl);
//            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//            TrustManager[] tm = {xtm};
//
//            SSLContext ctx = SSLContext.getInstance("TLS");
//            ctx.init(null, tm, null);
//
//            con.setSSLSocketFactory(ctx.getSocketFactory());
//            con.setHostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String arg0, SSLSession arg1) {
//                    return true;
//                }
//            });
//
//
//            con.setDoInput(true); //允许输入流，即允许下载
//
//            //在android中必须将此项设置为false
//            con.setDoOutput(false); //允许输出流，即允许上传
//            con.setUseCaches(false); //不使用缓冲
//            if (null != requestMethod && !requestMethod.equals("")) {
//                con.setRequestMethod(requestMethod); //使用指定的方式
//            } else {
//                con.setRequestMethod("GET"); //使用get请求
//            }
//            is = con.getInputStream();   //获取输入流，此时才真正建立链接
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader bufferReader = new BufferedReader(isr);
//            String inputLine;
//            while ((inputLine = bufferReader.readLine()) != null) {
//                resultData += inputLine + "\n";
//            }
//            System.out.println(resultData);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return resultData;
//    }
//
//    X509TrustManager xtm = new X509TrustManager() {
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return null;
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//
//        }
//
//        @Override
//        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//
//        }
//    };
}