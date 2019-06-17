package com.barackbao.web.handler;

import com.barackbao.beans.BeanFactory;
import com.barackbao.context.WebContext;
import com.barackbao.utils.DispatchActionConstants;
import com.barackbao.web.view.View;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by barackbao on 2019-06-08
 * 请求映射器
 */
public class MappingHandler {


    // 要处理的uri
    private String uri;
    private Method method;
    private Class<?> controller;
    private String[] args;


    public MappingHandler(String uri, Method method, Class<?> controller, String[] args) {
        this.uri = uri;
        this.method = method;
        this.controller = controller;
        this.args = args;
    }


    /**
     * 处理网络请求,获取请求参数
     * <p>
     * 动态创建处理对应请求url的Controller实例
     * 使用反射调用Controller方法
     * 将处理结果写入到ServletResponse中
     *
     * @param req
     * @param res
     * @return 返回处理结果
     */
    public boolean handle(ServletRequest req, ServletResponse res) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, IOException, ServletException {

        // 将当前Request和Response保存在ThreadLocal中，便于在Controller中使用
        WebContext.requestHolder.set((HttpServletRequest) req);
        WebContext.responseHolder.set((HttpServletResponse) res);


        // 获取请求url
        String requestUri = ((HttpServletRequest) req).getRequestURI();
//        String requestUri = parseRequestUri((HttpServletRequest) req);
        System.out.println("请求url: " + requestUri);
        System.out.println("保存的url: " + uri);

        System.out.println("servletContext: "
                + " " + ((HttpServletRequest) req).getServletPath());

        if (!uri.equals(requestUri)) {
            return false;
        }

        // 获取请求参数
        Object[] parameters = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            parameters[i] = req.getParameter(args[i]);
        }

        // 从bean工厂中获取controller对象
        Object ctl = BeanFactory.getBean(controller);
        Object response = method.invoke(ctl, parameters);

        System.out.println(response.toString());


        View view = (View) response;
        // 判断跳转方式
        if (view.getDispatchType().equals(DispatchActionConstants.FORWARD)) {
            req.getRequestDispatcher(view.getUrl()).forward(req, res);
        } else if (view.getDispatchType().equals(DispatchActionConstants.REDIRECT)) {
            ((HttpServletResponse) res).sendRedirect(((HttpServletRequest) req).getContextPath() + view.getUrl());
        } else {
            req.getRequestDispatcher(view.getUrl()).forward(req, res);
        }

        return true;
    }


    private String parseRequestUri(HttpServletRequest request) {
        String path = request.getContextPath() + "/";
        String requestUri = request.getRequestURI();
        String midUrl = requestUri.replace(path, "");
        String lastUrl = midUrl.substring(0, midUrl.lastIndexOf("."));

        return lastUrl;
    }
}
