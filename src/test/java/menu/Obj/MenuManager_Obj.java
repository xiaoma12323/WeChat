package menu.Obj;

import cn.maluit.WeChat.Common.AccessTokenInfo;
import cn.maluit.WeChat.entry.AccessToken;
import cn.maluit.WeChat.util.NetWorkHelper;
import cn.maluit.WeChat.web.servlet.AccessTokenServlet;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 菜单管理器类
 * Created by mdy on 2017/3/30.
 */

public class MenuManager_Obj {
    private static Logger log = LoggerFactory.getLogger(MenuManager_Obj.class);

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
    public static int createMenu(Menu menu, String accessToken) {
        int result = 0;

        // 拼装创建菜单的url
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);
        // 将菜单对象转换成json字符串
        String jsonMenu = JSONObject.fromObject(menu).toString();
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
     * 组装菜单数据
     *
     * @return
     */
    private static Menu getMenu() {
        ViewButton btn11=new ViewButton();
        btn11.setName("联系车主");
        btn11.setType("view");
        btn11.setUrl("http://www.baidu.com");

        ViewButton btn21=new ViewButton();
        btn21.setName("联系我们");
        btn21.setType("view");
        btn21.setUrl("http://www.maluit.cn/WeChat");

        CommonButton btn31 = new CommonButton();
        btn31.setName("aaa");
        btn31.setType("click");
        btn31.setKey("31");

        CommonButton btn32 = new CommonButton();
        btn32.setName("bbb");
        btn32.setType("click");
        btn32.setKey("32");

        CommonButton btn33 = new CommonButton();
        btn33.setName("ccc");
        btn33.setType("click");
        btn33.setKey("33");


        /**
         * 微信： mainBtn3底部的一级菜单。
         */


        ComplexButton mainBtn3 = new ComplexButton();
        mainBtn3.setName("我的信息");
        mainBtn3.setSub_button(new CommonButton[]{btn31, btn32, btn33});


        /**
         * 封装整个菜单
         */
        Menu menu = new Menu();
        menu.setButton(new Button[]{btn11, btn21, mainBtn3});

        return menu;
    }
}
