package com.barackbao.web.view;

import com.barackbao.utils.DispatchActionConstants;

/**
 * Created by barackbao on 2019-06-11
 */
public class View {

    // 当前视图跳转url
    private String url;

    // 跳转类型
    private String dispatchType = DispatchActionConstants.REDIRECT;


    public View(String url) {
        this.url = url;
    }


    public View(String url, String name, Object value) {
        this.url = url;
        ViewData viewData = new ViewData();
        viewData.put(name, value);

    }


    public View(String url, String name, Object value, String dispatchType) {
        this.dispatchType = dispatchType;
        this.url = url;
        ViewData viewData = new ViewData();
        viewData.put(name, value);
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getDispatchType() {
        return dispatchType;
    }


    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
    }


    @Override
    public String toString() {
        return "View{" +
                "url='" + url + '\'' +
                ", dispatchType='" + dispatchType + '\'' +
                '}';
    }
}
