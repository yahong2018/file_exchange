package com.zhxh.imms.omron.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.zhxh.imms.omron.backgroud.TaskConfig;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class PLcInitor {

    public TaskConfig getTaskConfig(){
        TaskConfig config = new TaskConfig();
        config.setAutoStart(false);   //默认手工启动
        config.setConnectionCheckDuration(5*60);//连接检查间隔，单位为秒，默认为5分钟
        config.setDisConnectLogDuration(60); //网络断线日志间隔，单位为秒，默认为1分钟

        return config;
    }

    public PLC[] getPlcList() {
        //可以从配置文件或者数据库中加载PLC的配置数据，测试系统从配置文件中加载
        PLC[] plcList;
        String configFileName = this.getConfigFileName();
        if (configFileName == null) {
            return null;
        }

        String jsonString = readFileContent(configFileName);
        Gson gson = new Gson();
        plcList = gson.fromJson(jsonString, PLC[].class);

        return plcList;
    }

    private String getConfigFileName() {
        String plcConfigPath = "classpath:/config/PLC.json";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] source = resolver.getResources(plcConfigPath);
            if (source.length == 0) {
                return null;
            }
            return source[0].getFile().getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }
}
