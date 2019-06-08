package com.barackbao.beans;

import com.barackbao.web.mvc.Controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by barackbao on 2019-06-09
 * Bean工厂，在应用开启后将所有的bean初始化并保存在工厂内
 */
public class BeanFactory {


    private static Map<Class<?>, Object> classToBean = new ConcurrentHashMap<>();

    public static Object getBean(Class<?> cls) {
        return classToBean.get(cls);
    }


    /**
     * 初始化所有的bean
     * 并且要进行依赖注入
     *
     * @param classes
     */
    public static void initBean(List<Class<?>> classes) throws Exception {

        // 重新创建一个Classes容器
        // 每创建一个bean，就从该容器中删除一个class
        List<Class<?>> toCreate = new ArrayList<>(classes);

        // 循环判断未创建的class数量，如果class数量不变
        // 说明有循环依赖，抛出异常
        while (toCreate.size() != 0) {

            int remainSize = toCreate.size();

            for (int i = 0; i < toCreate.size(); i++) {

                // 进行bean的创建
                if (finishCreate(toCreate.get(i))) {
                    toCreate.remove(i);
                }
            }

            if (toCreate.size() == remainSize) {
                throw new Exception("cycle dependency");
            }

        }
    }

    /**
     * 进行bean的初始化，并返回创建结果
     *
     * @param cls
     * @return
     */
    private static boolean finishCreate(Class<?> cls) throws IllegalAccessException, InstantiationException {

        // 首先判断当前类是否为bean类，如果不是直接返回true，从classes中移除该class
        if (!cls.isAnnotationPresent(Bean.class) &&
                !cls.isAnnotationPresent(Controller.class)) {
            return true;
        }

        // 实例化bean，并添加到bean工厂中
        Object bean = cls.newInstance();
        // 遍历该bean类的属性，看是否有依赖需要注入
        for (Field field : cls.getDeclaredFields()) {
            // 使用AutoWired注解进行过滤
            if (field.isAnnotationPresent(AutoWired.class)) {
                // 获取当前依赖属性的class
                Class<?> fieldType = field.getType();
                // 在bean工厂中查找对应的bean对象
                Object reliantBean = classToBean.get(fieldType);

                if (reliantBean == null) {
                    return false;
                }

                // 如果存在依赖的bean
                field.setAccessible(true);
                field.set(bean, reliantBean);

            }
        }

        // 将创建好的bean加入bean工厂
        classToBean.put(cls, bean);
        return true;

    }

}
