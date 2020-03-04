package util;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author hhy1997
 * 2020/3/1
 */
public class PmsUploadUtil {

    public static String uploadImage(MultipartFile multipartFile){
        String url = "http://192.168.123.27";
//        获取配置文件
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
//            初始化
            ClientGlobal.init(tracker);
            //4、创建一个TrackerClient对象。
            TrackerClient trackerClient = new TrackerClient();
//5、使用TrackerClient对象获得trackerserver对象。
            TrackerServer trackerServer = trackerClient.getConnection();
//6、创建一个StorageServer的引用null就可以。
            StorageServer storageServer = null;
//7、创建一个StorageClient对象。trackerserver、StorageServer两个参数。
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
//            获取文件
            byte[] bytes = multipartFile.getBytes();
//            获取文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String ext_name = originalFilename.substring(i + 1);
            String[] uploadInfos = storageClient.upload_file(bytes, ext_name, null);
            for (String s: uploadInfos
            ) {
                url += "/" + s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return url;
        
    }

}
