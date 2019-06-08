package com.barackbao.web.handler;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
     *
     * 动态创建处理对应请求url的Controller实例
     * 使用反射调用Controller方法
     * 将处理结果写入到ServletResponse中
     *
     * @param req
     * @param res
     * @return 返回处理结果
     */
    public boolean handle(ServletRequest req, ServletResponse res) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, IOException {
        // 获取请求url
        String requestUri = ((HttpServletRequest) req).getRequestURI();

        if (!uri.equals(requestUri)) {
            return false;
        }

        // 获取请求参数
        Object[] parameters = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            parameters[i] = req.getParameter(args[i]);
        }

        // 动态实例化controller
        Object ctl = controller.newInstance();
        Object response = method.invoke(ctl, parameters);

        // 将结果放入Servlet的response
        res.getWriter().println(response);

        return true;
    }
}
