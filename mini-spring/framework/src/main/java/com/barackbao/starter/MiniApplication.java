package com.barackbao.starter;

import com.barackbao.beans.BeanFactory;
import com.barackbao.core.ClassScanner;
import com.barackbao.web.handler.HandlerManager;
import com.barackbao.web.server.TomcatServer;
import org.apache.catalina.LifecycleException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by barackbao on 2019-06-07
 */
public class MiniApplication {

    /**
     * 该方法为启动模块调用框架模块的入口方法
     * 为了方便使用设置为静态方法
     *
     * @param cls  启动模块入口类
     * @param args 启动模块入口类的启动参数
     */
    public static void run(Class<?> cls, String[] args) {
        System.out.println("Hello mini-spring!");

        // 启动tomcat服务器
        TomcatServer tomcatServer = new TomcatServer(args);
        try {
            tomcatServer.startServer();
            List<Class<?>> classList = ClassScanner.scanClasses(cls.getPackage().getName());
            classList.forEach(it -> System.out.println(it.getName()));

            // 初始化bean，保存到bean工厂中
            BeanFactory.initBean(classList);

            // 初始化所有的Mappinghandler
            HandlerManager.resolveMappingHandler(classList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
