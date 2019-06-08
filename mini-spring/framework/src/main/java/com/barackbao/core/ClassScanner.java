package com.barackbao.core;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by barackbao on 2019-06-07
 * 类扫描器
 */
public class ClassScanner {

    public static List<Class<?>> scanClasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<>();
        // 先将包名转换为绝对路径
        String path = packageName.replace(".", "/");
        // 初始化类加载器实例
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 返回资源
        System.out.println("scan path: " + path);
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            // 资源的类型通过getProtocol可以获取到，项目最终都会打成jar
            // 这里对jar类型进行获取
            if (resource.getProtocol().contains("jar")) {
                JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
                // 获取jar报路径
                String jarFilePath = jarURLConnection.getJarFile().getName();

                // 获取jar下的所有类
                classList.addAll(getClassesFromJar(jarFilePath, path));

            } else {
                // todo
                System.out.println("no jar file");
            }
        }

        return classList;

    }

    /**
     * 获取jar包中的目标类
     *
     * @param jarFilePath
     * @param path        目标类的相对路径
     * @return
     */
    private static List<Class<?>> getClassesFromJar(String jarFilePath, String path) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> jarEntries = jarFile.entries();

        // 对jar包下的每个文件entry进行判断
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String entryName = jarEntry.getName(); // com/barackbao/test/Test.class
            // 传入目标类的path路径
            if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                // 获取对应类名称，只需将文件分隔符换成.，然后将.class后缀去掉即可
                String classFullName = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                // 使用类加载器将该类加载到jvm中
                classes.add(Class.forName(classFullName));

            }
        }
        return classes;
    }
}
