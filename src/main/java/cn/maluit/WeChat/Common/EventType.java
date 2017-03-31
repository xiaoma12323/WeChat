package cn.maluit.WeChat.Common;

/**
 * 接收到的Event类型
 * Created by mi on 2017/3/31.
 */
public enum EventType {

    SUBSCRIBE,//订阅
    UNSUBCRIBE,//取消订阅
    SCAN,//扫描带参数的二维码
    LOCATION,//上报地理位置
    CLICK,//点击菜单拉取消息
    VIEW,//点击菜单跳转
}