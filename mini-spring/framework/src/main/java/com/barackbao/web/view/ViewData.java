package com.barackbao.web.view;

import com.barackbao.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.WebConnection;

/**
 * Created by barackbao on 2019-06-11
 * <p>
 * 存储View中的数据
 */
public class ViewData {

    private HttpServletRequest request;


    public ViewData() {
        initRequest();
    }

    private void initRequest() {
        // 从requestHolder中获取当前request对象
        this.request = WebContext.requestHolder.get();
    }


    /**
     * 给request对象设置携带属性
     *
     * @param name
     * @param value
     */
    public void put(String name, Object value) {
        this.request.setAttribute(name, value);
    }


}
