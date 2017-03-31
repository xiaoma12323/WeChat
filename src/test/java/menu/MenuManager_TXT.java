package menu;

import cn.maluit.WeChat.Common.AccessTokenInfo;
import cn.maluit.WeChat.entry.AccessToken;
import cn.maluit.WeChat.util.NetWorkHelper;
import cn.maluit.WeChat.web.servlet.AccessTokenServlet;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 菜单管理器类
 * Created by mdy on 2017/3/30.
 */

public class MenuManager_TXT {
    private static Logger log = LoggerFactory.getLogger(MenuManager_TXT.class);

    public static void main(String[] args) {
        // 调用接口获取access_token
        AccessTokenServlet.getToken();
        AccessToken at = AccessTokenInfo.accessToken;

        if (null != at) {
            // 调用接口创建菜单
            int result = createMenu(getMenu(), at.getAccessToken());

            // 判断菜单创建结果
            if (0 == result)
                log.info("菜单创建成功！");
            else
                log.info("菜单创建失败，错误码：" + result);
        }
    }


    // 菜单创建（POST）
    private static String menu_create_url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    /**
     * 创建菜单
     *
     * @param menu        菜单实例
     * @param accessToken 有效的access_token
     * @return 0表示成功，其他值表示失败
     */
    public static int createMenu(JSONObject menu, String accessToken) {
        int result = 0;

        // 拼装创建菜单的url
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);
        // 将菜单对象转换成json字符串
        String jsonMenu = menu.toString();
        // 调用接口创建菜单
        JSONObject jsonObject = NetWorkHelper.httpsRequest(url, "POST", jsonMenu);

        if (null != jsonObject) {
            if (0 != jsonObject.getInt("errcode")) {
                result = jsonObject.getInt("errcode");
                log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }

        return result;
    }

    /**
     * 从txt文件获取menu
     *
     * @return
     */
    private static JSONObject getMenu() {

        String Str = "";

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

                /* 读入TXT文件 */
            String pathname = ".\\src\\test\\resources\\menufile.txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            line = br.readLine();
            while (line != null) {
                Str = Str + line + "\n";
                line = br.readLine(); // 一次读入一行数据
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject menu = JSONObject.fromObject(Str);

        return menu;
    }

}