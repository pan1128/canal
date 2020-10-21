package com.alibaba.otter.canal.adapter.launcher;

import com.alibaba.otter.canal.common.utils.CommonUtils;

import java.io.File;
import java.net.URL;

public class MessageResolvingTest {

    public static void main(String[] args) {
        /*try {
            ObjectInputStream is=new ObjectInputStream(new FileInputStream("D:\\mysqlbinlog"));
            Message message = (Message) is.readObject();
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

        File file=new File("d:\\a");
        file.delete();

        /* URL path = CommonUtils.class.getResource("");//.getPath();    空表示当前class文件的所在目录
        URL path1 = CommonUtils.class.getResource("/");//.getPath();  /表示classPath根目录
        URL path2 = CommonUtils.class.getClassLoader().getResource("");//.getPath();*/
        URL path3 = CommonUtils.class.getClassLoader().getResource("");//.getPath();
        /*System.out.println(path);
        System.out.println(path1);
        System.out.println(path2);
        System.out.println(path3);

        System.out.println(path.getPath());
        System.out.println(path1.getPath());
        System.out.println(path2.getPath());
        System.out.println(path3.getPath());*/
        /*InputStream inputStream = CommonUtils.class.getResourceAsStream("/a.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        String name = properties.getProperty("name");
        String age = properties.getProperty("age");
        System.out.println(name+":"+age);*/

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println(contextClassLoader.getResource(""));
        System.out.println(contextClassLoader.getParent().getResource(""));
        //System.out.println(contextClassLoader.getParent().getParent().getResource(""));
    }

}
