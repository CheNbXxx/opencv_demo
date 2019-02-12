package achieve;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/29 14:30
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        new Main().handle(args);
    }

    private void handle(String[] args) throws Exception {
        if(args.length < 1){
            log.info("|| ======== 缺少必要配置文件地址");
            return;
        }
        File files = new File(args[0]);
        if(files.isDirectory() || !files.exists() || !files.canRead() || !files.getName().endsWith(".properties")){
            log.info("|| ======== 配置文件错误");
            return;
        }
        Properties properties = new Properties();
        try(InputStream input = new FileInputStream(files)){
            properties.load(input);
        }
        // 创建需要用的参数
        Params params = new Params();
        Field[] fields = params.getClass().getDeclaredFields();
        for (Field field : fields){
            String property = properties.getProperty(field.getName());
            if(property.length() != 0 && !"".equals(property.trim())){
                try {
                    if(field.getType().getName().endsWith("Double")){
                        field.set(params,Double.valueOf(property));
                        continue;
                    }
                    if(field.getType().getName().endsWith("Integer")){
                        field.set(params,Integer.valueOf(property));
                        continue;
                    }
                    field.set(params,property);
                } catch (IllegalAccessException e) {
                    log.info("|| ======== 参数错误");
                    e.printStackTrace();
                }
            }
        }
        // 处理目标目录
        File desFile = new File(params.desFile);
        if(desFile.isFile()){
            log.info("|| ======== 目标目录错误");
            return;
        }
        // 目录不存在则全部创建
        if(!desFile.exists()){
            desFile.mkdirs();
        }

        log.info("|| ========== 参数信息如下");
        for (Field field : fields){
            Object o = field.get(params);
            log.info("|| ======== {}:[{}]",field.getName(),o.toString());
        }


        DoVideo doVideo = new DoVideo(params);
        // 处理srcFile属性
        String srcFile = params.srcFile;
        File file = new File(srcFile);
        if(file.isFile() && file.exists()) {
            doVideo.handleVideo(srcFile, params.desFile);
        }
        // 仅指定目录 获取目录下的所有MP4文件
        if(file.isDirectory() && file.exists()){
            for (File file1 : file.listFiles()){
                if(file1.getName().toLowerCase().endsWith(".mp4")){
                    doVideo.handleVideo(file1.getAbsolutePath(), params.desFile);
                }
            }
        }
        // 如果是文件，且不存在
    }

}
