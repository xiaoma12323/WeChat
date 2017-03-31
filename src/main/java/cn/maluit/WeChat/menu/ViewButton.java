package cn.maluit.WeChat.menu;

/**
 * view类型的菜单
 * Created by mi on 2017/3/31.
 */


public class ViewButton extends Button {
    private String type;
    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}