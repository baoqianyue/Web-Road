package com.barackbao.web.handler;


import com.barackbao.web.mvc.Controller;
import com.barackbao.web.mvc.RequestMapping;
import com.barackbao.web.mvc.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by barackbao on 2019-06-08
 * mappingHandler的管理器
 */
public class HandlerManager {

    // 给一个静态容器保存所有的MappingHandler
    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();


    /**
     * 从传入的类中将带有Controller注解的类过滤出来
     * 并将对应的处理方法实例化成MappingHandler对象
     *
     * @param classList
     */
    public static void resolveMappingHandler(List<Class<?>> classList) {
        for (Class<?> cls : classList) {
            // 判断当前类是否标注了Controller注解
            if (cls.isAnnotationPresent(Controller.class)) {
                // 解析该Controller类
                parseHandlerFromController(cls);
            }
        }
    }

    /**
     * 解析当前类，过滤类中的注解方法
     * 过滤方法中的注解参数，以及参数的值
     * 然后实例化一个请求映射器MappingHandler
     *
     * @param cls
     */
    private static void parseHandlerFromController(Class<?> cls) {
        // 通过反射获取该类中的所有方法
        Method[] methods = cls.getDeclaredMethods();
        // 遍历所有方法，过滤出被RequestMapping注解的方法
        for (Method method : methods) {
            if (!method.isAnnotationPresent(RequestMapping.class)) {
                continue;
            }

            // 从注解中获取对应的uri和请求参数
            String uri = method.getDeclaredAnnotation(RequestMapping.class).value();
            // 临时保存该方法的请求参数名
            List<String> paramNameList = new ArrayList<>();

            for (Parameter parameter : method.getParameters()) {
                // 过滤到被RequestParam注解的参数
                if (parameter.isAnnotationPresent(RequestParam.class)) {
                    // 将该参数的值保存起来
                    paramNameList.add(parameter.getDeclaredAnnotation(RequestParam.class).value());
                }
            }

            // 将参数容器转为数组类型
            String[] params = paramNameList.toArray(new String[paramNameList.size()]);
            // 当前uri, 方法名，参数值，类都已明确，实例化一个MappingHandler
            // 并将该实例保存在静态数组中
            MappingHandler mappingHandler = new MappingHandler(uri, method, cls, params);
            HandlerManager.mappingHandlerList.add(mappingHandler);
        }
    }
}


