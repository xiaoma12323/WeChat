package cn.maluit.WeChat.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by mi on 2017/4/1.
 */
public class PropertiesUtil {
    private static PropertiesUtil propUtilinstance; //本类自己实例
    private static Properties properties = new Properties();
    private static InputStream is;

    /**
     * 获取本类对象(单例模式)
     *
     * @return 本类对象
     */
    public static PropertiesUtil getInstance() {
        if (propUtilinstance == null) {
            propUtilinstance = new PropertiesUtil();
        }
        return propUtilinstance;
    }

    /**
     * 返回properties
     *
     * @param path properties文件路径
     * @return properties
     */
    public static Properties getPropperties(String path) {
        try {
            is = PropertiesUtil.class.getClassLoader().getResourceAsStream(path);
            properties.load(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("异常:路径或文件名有误!");
        }
        return properties;
    }
}
