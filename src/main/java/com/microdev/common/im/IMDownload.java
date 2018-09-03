package com.microdev.common.im;

import com.microdev.common.FilePush;
import com.microdev.service.ChatMessageService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * 即时通讯聊天记录下载
 */
@Component
public class IMDownload {

    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private FilePush filePush;
    private static final String DIRECTORY = "/home/micro-worker/wgb/im/";
    //private static final String DIRECTORY = "D:/testload/good/";
    private static final Logger logger = LoggerFactory.getLogger(IMDownload.class);

    @Scheduled(cron = "0 0 2 * * ?")
    public void downloadChatMessage() {
        OffsetDateTime time = OffsetDateTime.now();
        time = time.plusHours(-time.getHour()).plusMinutes(-time.getMinute()).plusDays(-1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMM");
        String dateName = DIRECTORY + time.format(dateFormat) + File.separator + time.getDayOfMonth();
        Path pathRoot = Paths.get(dateName);
        if (!Files.exists(pathRoot)) {
            try {
                Files.createDirectories(pathRoot);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(dateName + "文件创建失败");
            }
        }
        Object result = null;
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {

            String timeStr = time.format(format);
			//String timeStr = "2018082318";
            result = chatMessageService.exportChatMessages(timeStr);
            if (result == null) {
                logger.error("Failed to get expected response by calling GET chatmessages API, maybe there is no chatmessages history at {}", timeStr);
            } else {
                logger.info(result.toString());
                try {

                    JSONObject obj = new JSONObject(result.toString());
                    JSONArray array = obj.getJSONArray("data");
                    obj = (JSONObject) array.get(0);
                    String url = obj.getString("url");

                    download(dateName, timeStr, url, null);
                    decompression(dateName, timeStr);
                    readMessage(dateName, timeStr);

                } catch (Exception e) {
                    e.printStackTrace();
                    list.add(timeStr);
                }
            }
            try {
                TimeUnit.MINUTES.sleep(1);
                time = time.plusHours(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (list.size() > 0) {
            for (String p : list) {
                String pName = dateName + File.separator + p + ".gz";
                Path path = Paths.get(pName);
                if (!Files.exists(path)) {
                    result = chatMessageService.exportChatMessages(p);
                    if (result == null) {
                        logger.error("Failed to get expected response by calling GET chatmessages API, maybe there is no chatmessages history at {}", p);
                    } else {
                        try {

                            JSONObject obj = new JSONObject(result.toString());
                            JSONArray array = obj.getJSONArray("data");
                            obj = (JSONObject) array.get(0);
                            String url = obj.getString("url");
                            logger.info("download url=" + url);
                            download(dateName, p, url, null);
                            decompression(dateName, p);
                            readMessage(dateName, p);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("下载" + p + "消息文件失败");
                        }
                    }
                }
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * 读取历史聊天信息。
     * @param dateName  文件夹名称
     * @param fileName  文件名称
     * @throws IOException
     * @throws JSONException
     */
    private void readMessage(String dateName, String fileName) throws Exception {

        FileInputStream fis = new FileInputStream(new File(dateName, fileName));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String str = null;
        JSONObject array = null;
        JSONObject payload = null;
        JSONArray bodies = null;
        JSONObject obj = null;
        while ((str = br.readLine()) != null) {
            System.out.println("读取的数据:" + str);
            obj = new JSONObject(str);
            Object timestamp = obj.getLong("timestamp");
            payload = obj.getJSONObject("payload");
            String from = payload.getString("from");
            String to = payload.getString("to");
            bodies = payload.getJSONArray("bodies");
            array = (JSONObject) bodies.get(0);
            String type = array.getString("type");
            if (!"txt".equals(type) && !"loc".equals(type)) {
                String videoName = timestamp + "_" + from + "_" + to;
                String url = array.getString("url");
                String filenameSuffix = array.getString("filename");
                filenameSuffix = filenameSuffix.substring(filenameSuffix.lastIndexOf("."));
                download(dateName, videoName, url, filenameSuffix);

            }
        }
    }

    /**
     * 解压下载的文件
     * @param dateName  文件夹名称
     * @param fileName  文件名称
     * @throws IOException
     */
    private void decompression(String dateName, String fileName) throws IOException {

        String name = fileName + ".gz";
        FileInputStream fis = new FileInputStream(new File(dateName, name));
        GZIPInputStream gs = new GZIPInputStream(fis);
        File outDir = new File(dateName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(outDir, fileName)));
        byte[] b = new byte[1024];
        int i = -1;
        while ((i = gs.read(b)) != -1) {
            bos.write(b, 0, i);
        }
        bos.close();
        gs.close();
    }

    /**
     * 下载1小时时间段的历史聊天记录
     * @param dateName  文件夹名称
     * @param fileName  文件名称
     * @param url       下载地址
     * @throws IOException
     */
    private void download(String dateName, String fileName, String url, String suffix) throws IOException {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connect = null;
        try {

            URL httpUrl = new URL(url);
            connect = (HttpURLConnection) httpUrl.openConnection();
            connect.setRequestMethod("GET");
            connect.setDoInput(true);
            connect.setDoOutput(true);
            connect.setUseCaches(false);

            connect.connect();

            input = connect.getInputStream();
            bis = new BufferedInputStream(input);
            if (!dateName.endsWith("/")) {
                dateName += "/";
            }
            String name = dateName + fileName;
            if (StringUtils.isEmpty(suffix)) {
                name += ".gz";
            } else {
                name += suffix;
            }
            output = new FileOutputStream(name);

            bos = new BufferedOutputStream(output);
            byte[] b = new byte[4096];
            int i = bis.read(b);
            while (i != -1) {
                bos.write(b, 0, i);
                i = bis.read(b);
            }
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connect != null)
                connect.disconnect();
        }

    }
}
