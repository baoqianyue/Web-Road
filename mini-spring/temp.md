## Mini-Spring的结构设计   

* 实现Core模块，包括core、beans、context包    
* 实现Web模块，集成web和webmvc      
* 添加starter，实现类spring-boot的启动方式    

### 项目启动   
在test模块下的java目录中给一个Application启动类    

然后在test的build.gradle文件中设置jar的配置清单属性        

```
jar {
    manifest {
        // 设置一个入口属性
        attributes "Main-Class": "com.barackbao.Application"
    }

    from {

        // 将其他模块的依赖jar递归的打入该目标启动模块
        configurations.compile.collect {
            it.isDirectory() ? it : zipTree(it)
        }
    }
}
```  

然后使用命令gradle build构建项目jar包    

* 如何将框架模块包含在启动模块test中       
    
    1.使用gradle的compile(implementation)命令将同一项目中的兄弟模块包含在当前模块    
    
    ```
    dependencies {
        testCompile group: 'junit', name: 'junit', version: '4.12'
        implementation(project(':framework'))
    }
    ```   
    
    2.在java代码中对启动模块和框架模块的启动类建立联系    
        将启动模块的入口类信息作为参数传入框架类中      
        
        
        
### Web服务器    

* 监听一个TCP端口    
* 转发请求，回收响应    
* 本身没有业务逻辑，只负责连接操作系统和应用程序代码    


### 框架集成Tomcat   

* 1.模仿spring-boot中的嵌入式web服务器，首先在gradle中加入嵌入式tomcat的依赖Tomcat Embed Core8.5.23    
    在framework的build.gradle中    
    ```
    dependencies {
        // https://mvnrepository.com/artifact/org.apache.tomcat.embed/tomcat-embed-core
        compile group: 'org.apache.tomcat.embed', name: 'tomcat-embed-core', version: '8.5.23'
        testCompile group: 'junit', name: 'junit', version: '4.12'
    }  
    ```   
* 2.在framework模块中定义一个tomcatServer类    
    实例化一个tomcat服务器对象，然后调用他的start方法就可以启动tomcat服务，但是为了防止服务器中途退出，要添加一个常驻等待线程    

    ```java
        public class TomcatServer {

        private Tomcat tomcat;
        private String[] args;

        public TomcatServer(String[] args) {
            this.args = args;
        }


        public void startServer() throws LifecycleException {
            tomcat = new Tomcat();
            tomcat.setPort(6699);
            // 这里的start方法会抛出异常
            tomcat.start();

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
    ```  

* 3.在framework模块的启动类中调用封装好的tomcat启动方法即可开启tomcat服务      


### 如何在tomcat容器中注册一个servlet来响应浏览器请求    

* 1.首先实现一个testServlet     
* 2.在tomcat启动方法中将testServlet的实例添加到context容器中，并且设置urlmapping，然后将注册好servlet的context容器添加到host容器中    

    ```java
    // 首先新建一个Context容器对象
        // 标准实现
        Context context = new StandardContext();
        // 设置路径为空
        context.setPath("");
        // 设置默认的生命周期监听器
        context.addLifecycleListener(new Tomcat.FixContextListener());

        TestServlet servlet = new TestServlet();
        // 将目标servlet添加到context容器中
        Tomcat.addServlet(context, "testServlet", servlet);
        // 设置urlmapping
        context.addServletMappingDecoded("/test.json", "testServlet");
        // context容器需要添加到一个host容器中才能运行
        tomcat.getHost().addChild(context);
    ```

### 为框架添加web组件    

上面通过硬编码的方式虽然也能够将一个servlet注册到容器中，并且也能设置urlmapping，但是这种方式不好进行修改。    

正常的web方式是通过在项目中配置web.xml，然后给每一个serlvet配置一个urlmapping，当项目非常大时，web.xml文件会非常臃肿，Spring的方式是先定义一个DispatcherServlet接收所有的请求，然后通过Java的类加载器扫描项目jar包下的所有类，然后通过注解过滤出所有的业务Controller类，然后通过反射机制动态实例化相应的mappinghanlder，然后进行url的响应      

* 1.首先实现web组件(注解)       

    ```java
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Controller {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RequestMapping {

        // 保存url信息
        String value();
    }


    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface RequestParam {

        String value();
    }
    ```    

* 2.对每一个具体的业务创建Controller      

    这里假设一个计算工资的Controller      

    ```java
    @Controller
    public class SalaryController {

        @RequestMapping("/get_salary.json")
        public Integer getSalary(@RequestParam("name") String name, @RequestParam("experience") String experience) {
            return 10000;
        }
    }
    ```        

* 3.创建请求映射器，保存响应方法的信息，动态创建对应的Controller实例，然后使用反射调用          

    ```java
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
    ```   

* 4.在项目启动时通过注解过滤出所有Controller类，然后实例化MappingHandler对象并保存在handler管理器中     
    ```java
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
    ```   

* 5.创建一个DispatcherServlet来接收所有的请求，直接在service方法中查找是否有处理该请求的MappingHandler      

    ```java
        public class DispatcherServlet implements Servlet {
        @Override
        public void init(ServletConfig config) throws ServletException {

        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            // 遍历保存好的mappingHandler，查找能够处理当前请求的MappingHandler请求映射器
            for (MappingHandler handler : HandlerManager.mappingHandlerList) {
                try {
                    if (handler.handle(req, res)) {
                        // TODO: 2019-06-08
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {

        }
    }
    ```   

* 6.将上面创建好的DispatcherServlet注册到tomcat容器中，并将接收请求mapping设置为“/”，表示该Servlet接收所有请求      
    然后框架的web组件就搭建完成了，框架会根据web组件注解自动寻找处理当前请求的Controller及方法，无需我们再去手动配置   

    ```java
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
    ```

 


    
    
    
   
    