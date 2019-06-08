package com.barackbao;


import com.barackbao.starter.MiniApplication;

/**
 * Created by barackbao on 2019-06-07
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("Hello World！");
        // 传入当前入口类信息和启动参数
        MiniApplication.run(Application.class, args);
    }
}
