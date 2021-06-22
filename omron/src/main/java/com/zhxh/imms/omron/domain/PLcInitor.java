package com.zhxh.imms.omron.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class PLcInitor {
    private PLC[] plcList;

    public PLC[] getPlcList() {
        return plcList;
    }

    @PostConstruct
    private void initPLcList() {
        String configFileName = this.getConfigFileName();
        if (configFileName == null) {
            return;
        }

        String jsonString = readFileContent(configFileName);
        Gson gson = new Gson();
        this.plcList = gson.fromJson(jsonString, PLC[].class);
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
