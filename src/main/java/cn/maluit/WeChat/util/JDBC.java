package cn.maluit.WeChat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Created by mi on 2017/4/1.
 */

public class JDBC {
    /**
     * 将获取accesstoken存放数据库中
     * @param acctoken
     * @return void
     * @throws Exception
     */
    public static void saveAccessToken(String acctoken) throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn=new JDBCConnection().getConnection();
        String sql="select max(aid) from accesstoken";
        PreparedStatement ps=conn.prepareStatement(sql);
        ResultSet rs=ps.executeQuery();
        rs.next();
        int usid=rs.getInt(1)+1;
        sql="insert into accesstoken (aid,akey)values (?,?)";
        ps=conn.prepareStatement(sql);
        ps.setInt(1, usid);
        ps.setString(2, acctoken);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }
}

/**
 * 建立数据库连接
 *
 * @return
 * @throws Exception
 */
class JDBCConnection{
    String url;
    String user;
    String password;
    Connection conn;
    public Connection getConnection() throws Exception{
        Properties prop=PropertiesUtil.getPropperties("jdbc.properties");
        url=prop.getProperty("url");
        user=prop.getProperty("user");
        password=prop.getProperty("password");
        conn= DriverManager.getConnection(url, user, password);
        return conn;
    }
}