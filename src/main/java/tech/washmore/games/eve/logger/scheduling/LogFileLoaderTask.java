package tech.washmore.games.eve.logger.scheduling;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.washmore.games.eve.logger.common.ParseEncoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Washmore on 2017/7/8.
 */
@Component
public class LogFileLoaderTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileLoaderTask.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Scheduled(cron = "0 30 1/4 * * ?")
    // @Scheduled(cron = "0/20 * * * * ?")
    public void loadLogFileEveryDayToMongodb() throws Exception {
        String path = System.getProperty("log.file.path");
        if (StringUtils.isEmpty(path)) {
            throw new IllegalAccessException("请指定需要收集的日志根目录路径!");
        }
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOGGER.info("EVE日志开始收集,目标路径:{},当前时间:{},收集日期:{}", path, new Date().toLocaleString(), getYstKey());
        handle(path);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    private boolean checkHasWrited(String docName) {
        return mongoTemplate.count(new Query().addCriteria(Criteria.where("logName").regex(getYstKey())), docName) > 0;
    }

    private void handle(String rootPath) throws Exception {
        File logDic = new File(rootPath);
        File[] childDics = logDic.listFiles();
        for (File dic : childDics) {
            LOGGER.info("当前收集文件夹:{}", dic.getName());
            if (checkHasWrited(dic.getName())) {
                LOGGER.info("文件夹{}的日志文件已经被收集过,忽略!", dic.getName());
                continue;
            }
            File[] logs = dic.listFiles();
            if (ArrayUtils.isEmpty(logs)) {
                LOGGER.info("文件夹{}中不存在任何日志文件,忽略!", dic.getName());
                continue;
            }
            for (File log : logs) {
                String fileName = log.getName();
                if (fileName.contains(getYstKey())) {
                    writeFileToMongo(log);
                }
            }
        }

    }

    private void writeFileToMongo(File log) throws Exception {
        ParseEncoding parse = new ParseEncoding();
        String docName = log.getParentFile().getName();
        String logName = log.getName();
        LOGGER.info("开始分析日志文件:{}", logName);

        FileInputStream fis = new FileInputStream(log);
        String encode = parse.getEncoding(fis);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(log), encode));
        String line = null;
        while ((line = reader.readLine()) != null) {
            Map<String, Object> doc = Maps.newHashMap();
            doc.put("logName", logName);
            doc.put("docName", docName);
            doc.put("content", line);
            try {
                if (line.contains("[") && line.contains("]")) {
                    String dateFormat = line.substring(2, line.indexOf("]") - 1);
                    Date logTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(dateFormat);
                    doc.put("time", logTime);
                } else {
                    doc.put("time", null);
                }
            } catch (Exception ex) {
                doc.put("time", null);
                LOGGER.error(ex.getMessage(), ex);
            }
            mongoTemplate.save(doc, docName);
        }
    }

    private String getYstKey() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis() - 24L * 3600 * 1000));
    }


    @Scheduled(cron = "? 22 8 * * ?")
    public void wakeUp() {
        try {
            Process process = Runtime.getRuntime().exec("E:\\Program Files (x86)\\Tencent\\QQMusic\\QQMusic.exe");
            LOGGER.info("程序开始启动:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
