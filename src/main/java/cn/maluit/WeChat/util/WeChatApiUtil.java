package cn.maluit.WeChat.util;

import cn.maluit.WeChat.Common.AccessTokenInfo;
import cn.maluit.WeChat.entry.AccessToken;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by mi on 2017/3/29.
 */
public class WeChatApiUtil {

    private static Logger log = LoggerFactory.getLogger(WeChatApiUtil.class);
    // token 接口(GET)
    private static final String ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    // 素材上传(POST)https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE
    private static final String UPLOAD_MEDIA = "https://api.weixin.qq.com/cgi-bin/media/upload";
    // 素材下载:不支持视频文件的下载(GET)
    private static final String DOWNLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";

    public static String getTokenUrl(String appId, String appSecret) {
        return String.format(ACCESS_TOKEN, appId, appSecret);
    }

    public static String getDownloadUrl(String token, String mediaId) {
        return String.format(DOWNLOAD_MEDIA, token, mediaId);
    }

    /**
     * 通用接口获取Token凭证
     *
     * @return
     */
    public static AccessToken getToken() {
        String appId="wxddd6a4773cca9ff3";
        String appSecret="ac24c6bcd94c3ca27d7b69732bb67516";
        AccessToken token = null;
        String tockenUrl = WeChatApiUtil.getTokenUrl(appId, appSecret);
        JSONObject jsonObject = httpsRequest(tockenUrl, "GET", null);
        if (null != jsonObject) {
            try {
                token.setAccessToken(jsonObject.getString("access_token"));
            } catch (JSONException e) {
                token = null;// 获取token失败
            }
        }
        AccessTokenInfo.accessToken = token;
        return token;
    }

    /**
     * 微信服务器素材上传
     *
     * @param file  表单名称media
     * @param token access_token
     * @param type  type只支持四种类型素材(video/image/voice/thumb)
     */
    public static JSONObject uploadMedia(File file, String token, String type) {
        if (file == null || token == null || type == null) {
            return null;
        }

        if (!file.exists()) {
            System.out.println("上传文件不存在,请检查!");
            return null;
        }

        String url = UPLOAD_MEDIA;
        JSONObject jsonObject = null;
        PostMethod post = new PostMethod(url);
        post.setRequestHeader("Connection", "Keep-Alive");
        post.setRequestHeader("Cache-Control", "no-cache");
        FilePart media;
        HttpClient httpClient = new HttpClient();
        //信任任何类型的证书
        Protocol myhttps = new Protocol("https", new SSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", myhttps);

        try {
            media = new FilePart("media", file);
            Part[] parts = new Part[]{new StringPart("access_token", token),
                    new StringPart("type", type), media};
            MultipartRequestEntity entity = new MultipartRequestEntity(parts,
                    post.getParams());
            post.setRequestEntity(entity);
            int status = httpClient.executeMethod(post);
            if (status == HttpStatus.SC_OK) {
                String text = post.getResponseBodyAsString();
                jsonObject = JSONObject.fromObject(text);
            } else {
                System.out.println("upload Media failure status is:" + status);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 多媒体下载接口
     *
     * @param fileName 素材存储文件路径
     * @param token    认证token
     * @param mediaId  素材ID（对应上传后获取到的ID）
     * @return 素材文件
     * @comment 不支持视频文件的下载
     */
    public static File downloadMedia(String fileName, String token,
                                     String mediaId) {
        String url = getDownloadUrl(token, mediaId);
        return httpRequestToFile(fileName, url, "GET", null);
    }


    /**
     * 以http方式发送请求,并将请求响应内容输出到文件
     *
     * @param path   请求路径
     * @param method 请求方法
     * @param body   请求数据
     * @return 返回响应的存储到文件
     */
    public static File httpRequestToFile(String fileName, String path, String method, String body) {
        if (fileName == null || path == null || method == null) {
            return null;
        }

        File file = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream fileOut = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(method);
            if (null != body) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.getBytes("UTF-8"));
                outputStream.close();
            }

            inputStream = conn.getInputStream();
            if (inputStream != null) {
                file = new File(fileName);
            } else {
                return file;
            }

            //写入到文件
            fileOut = new FileOutputStream(file);
            if (fileOut != null) {
                int c = inputStream.read();
                while (c != -1) {
                    fileOut.write(c);
                    c = inputStream.read();
                }
            }
        } catch (Exception e) {
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            /*
             * 必须关闭文件流
             * 否则JDK运行时，文件被占用其他进程无法访问
             */
            try {
                inputStream.close();
                fileOut.close();
            } catch (IOException execption) {
            }
        }
        return file;
    }

    /**
     * 上传素材
     *
     * @param filePath 媒体文件路径(绝对路径)
     * @param type     媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @return
     */
    public static JSONObject uploadMedia(String filePath, String type) {
        File f = new File(filePath); // 获取本地文件
        String token = WeChatApiUtil.getToken().getAccessToken();
        JSONObject jsonObject = uploadMedia(f, token, type);
        return jsonObject;
    }

//    /**
//     * 发送请求以https方式发送请求并将请求响应内容以String方式返回
//     *
//     * @param path   请求路径
//     * @param method 请求方法
//     * @param body   请求数据体
//     * @return 请求响应内容转换成字符串信息
//     */
//    public static String httpsRequestToString(String path, String method, String body) {
//        if (path == null || method == null) {
//            return null;
//        }
//
//        String response = null;
//        InputStream inputStream = null;
//        InputStreamReader inputStreamReader = null;
//        BufferedReader bufferedReader = null;
//        HttpsURLConnection conn = null;
//        try {
//            TrustManager[] tm = {new JEEWeiXinX509TrustManager()};
//            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
//            sslContext.init(null, tm, new java.security.SecureRandom());
//            SSLSocketFactory ssf = sslContext.getSocketFactory();
//            System.out.println(path);
//            URL url = new URL(path);
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setSSLSocketFactory(ssf);
//
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setUseCaches(false);
//            conn.setRequestMethod(method);
//            if (null != body) {
//                OutputStream outputStream = conn.getOutputStream();
//                outputStream.write(body.getBytes("UTF-8"));
//                outputStream.close();
//            }
//
//            inputStream = conn.getInputStream();
//            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//            bufferedReader = new BufferedReader(inputStreamReader);
//            String str = null;
//            StringBuffer buffer = new StringBuffer();
//            while ((str = bufferedReader.readLine()) != null) {
//                buffer.append(str);
//            }
//
//            response = buffer.toString();
//        } catch (Exception e) {
//
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//            try {
//                bufferedReader.close();
//                inputStreamReader.close();
//                inputStream.close();
//            } catch (IOException execption) {
//
//            }
//        }
//        return response;
//    }

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new JEEWeiXinX509TrustManager() };
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
/*

    //测试素材上传和下载
    public static void main(String[] args) throws Exception {
        //媒体文件路径
        String filePath = "D:/JavaSoftwareDevelopeFolder/IntelliJ IDEA_Workspace/WxStudy/web/media/image/我.jpg";
        //String filePath = "D:/JavaSoftwareDevelopeFolder/IntelliJ IDEA_Workspace/WxStudy/web/media/voice/voice.mp3";
        //String filePath = "D:\\JavaSoftwareDevelopeFolder\\IntelliJ IDEA_Workspace\\WxStudy\\web\\media\\video\\小苹果.mp4";
        //媒体文件类型
        String type = "image";
        //String type = "voice";
        //String type = "video";
        JSONObject uploadResult = uploadMedia(filePath, type);
        //{"media_id":"dSQCiEHYB-pgi7ib5KpeoFlqpg09J31H28rex6xKgwWrln3HY0BTsoxnRV-xC_SQ","created_at":1455520569,"type":"image"}
        System.out.println(uploadResult.toString());

        //下载刚刚上传的图片以id命名
        String media_id = uploadResult.getString("media_id");
        File file = downloadMedia("D:/" + media_id + ".png", media_id);
        System.out.println(file.getName());

    }
    */
}

class JEEWeiXinX509TrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}