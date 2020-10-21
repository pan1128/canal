package com.alibaba.otter.canal.admin.service.impl;

import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.CanalHeart;
import com.alibaba.otter.canal.admin.service.CanalHeartService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Service
public class CanalHeartServiceImpl implements CanalHeartService {

    @Value("${log.location:}")
    private String logLocation;
    @Override
    public BaseModel<List<CanalHeart>> getDbsyncStatus() {
        List<CanalHeart> list = CanalHeart.find.query().findList();
        return BaseModel.getInstance(list);
    }

    @Override
    public String getLog() throws Exception {
        File file=new File(logLocation);
        FileInputStream inputStream=new FileInputStream(file);
        if (inputStream == null) {
            return "";
        }
        return StringUtils.join(IOUtils.readLines(inputStream), "\n");
        //BufferedReader bs = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
       /* String logContent=new String("");
        String line="";
        while (!((line = bs.readLine()) ==null)){
            line+= '\n';
            logContent+=line;
        }*/
        //return logContent.toString();
    }
}
