package com.barackbao.web.server;

import com.barackbao.web.servlet.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

/**
 * Created by barackbao on 2019-06-07
 */
public class TomcatServer {

    private Tomcat tomcat;
    private String[] args;

    public TomcatServer(String[] args) {
        this.args = args;
    }


    public void startServer() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setPort(6699);
        tomcat.start();

        // 首先新建一个Context容器对象
        // 标准实现
        Context context = new StandardContext();
        // 设置路径为空
        context.setPath("");
        // 设置默认的生命周期监听器
        context.addLifecycleListener(new Tomcat.FixContextListener());

        DispatcherServlet servlet = new DispatcherServlet();
        // 将目标servlet添加到context容器中
        Tomcat.addServlet(context, "dispatcherServlet", servlet).setAsyncSupported(true);
        // 设置urlmapping
        context.addServletMappingDecoded("/", "dispatcherServlet");
        // context容器需要添加到一个host容器中才能运行
        tomcat.getHost().addChild(context);


        // 为了防止服务器中途退出，添加一个常驻线程
        Thread awaitThread = new Thread("tomcat_await_thread") {
            @Override
            public void run() {
                // 调用tomcat的等待方法
                TomcatServer.this.tomcat.getServer().await();
            }
        };

        // 设置为非守护线程
        awaitThread.setDaemon(false);
        // 将线程启动，让tomcat一直在等待
        awaitThread.start();
    }
}
