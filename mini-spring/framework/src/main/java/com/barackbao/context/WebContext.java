package com.barackbao.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by barackbao on 2019-06-11
 * <p>
 * WebContext主要作用：
 * 存储当前线程中的ServletRequest和ServletResponse
 * 并提供getter方法
 */
public class WebContext {

    // 使用ThreadLocal在多线程中隔离变量
    public static ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();
    public static ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();

    public HttpServletRequest getRequest() {
        return requestHolder.get();
    }


    public HttpSession getSession() {
        return requestHolder.get().getSession();
    }


    public ServletContext getServletContext() {
        return requestHolder.get().getSession().getServletContext();
    }


    public HttpServletResponse getResponse() {
        return responseHolder.get();
    }


}
