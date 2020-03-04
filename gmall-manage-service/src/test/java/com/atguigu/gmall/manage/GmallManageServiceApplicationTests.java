package com.atguigu.gmall.manage;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallManageServiceApplicationTests {

    @Test
    void contextLoads(){

    }

    @Test
    public void test() throws IOException {
        try {
            String path = "/Users/handaxingyuner/tracker.conf";
            ClientGlobal.init(path);
        } catch (Exception e) {
            System.out.println("hh");
            e.printStackTrace();
        }
//4、创建一个TrackerClient对象。
        TrackerClient trackerClient = new TrackerClient();
//5、使用TrackerClient对象获得trackerserver对象。
        TrackerServer trackerServer = trackerClient.getConnection();
//6、创建一个StorageServer的引用null就可以。
        StorageServer storageServer = null;
//7、创建一个StorageClient对象。trackerserver、StorageServer两个参数。
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
//8、使用StorageClient对象上传文件。
        String[] strings = null;
        try {
            strings = storageClient.upload_file("/Users/handaxingyuner/Pictures/wechat.jpg","jpg",null);
        } catch (Exception e) {
            System.out.println("jj");
            e.printStackTrace();
        }

        for (String string : strings) {
            System.out.println(string);

        }
    }

}
