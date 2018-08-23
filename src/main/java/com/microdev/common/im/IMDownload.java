package com.microdev.common.im;

import com.microdev.service.ChatMessageService;
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
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

@Component
public class IMDownload {

    @Autowired
    private ChatMessageService chatMessageService;
    private static final String DIRECTORY = "D:/testload/";
    private static final Logger logger = LoggerFactory.getLogger(IMDownload.class);

    //@Scheduled(cron = "0 0 2 * * ?")
    public void downloadChatMessage() {
        OffsetDateTime time = OffsetDateTime.now();
        time = time.plusHours(-time.getHour()).plusMinutes(-time.getMinute()).plusDays(-1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateName = time.format(dateFormat);
        for (int i = 0; i < 24; i++) {
            time = time.plusHours(1);
            String timeStr = time.format(format);
            Object result = chatMessageService.exportChatMessages(timeStr);
            if (result == null) {
                logger.error("Failed to get expected response by calling GET chatmessages API, maybe there is no chatmessages history at {}", timeStr);
            } else {
                logger.info(result.toString());
                try {
                    String s = URLDecoder.decode((String)result, "iso-8859-1");
                    JSONObject obj = new JSONObject(s);
                    JSONArray array = obj.getJSONArray("data");
                    obj = (JSONObject) array.get(0);
                    String url = obj.getString("url");
                    download(dateName, timeStr, url);
                    decompression(dateName, timeStr);
                    readMessage(dateName, timeStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
    private void readMessage(String dateName, String fileName) throws IOException, JSONException {
        String filePath = DIRECTORY + dateName;
        FileInputStream fis = new FileInputStream(new File(filePath, fileName));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String str = null;
        JSONObject array = null;
        JSONObject payload = null;
        JSONArray bodies = null;
        JSONObject obj = null;
        while ((str = br.readLine()) != null) {
            System.out.println("读取的数据:" + str);
            obj = new JSONObject(str);
            String timestamp = obj.getString("timestamp");
            payload = obj.getJSONObject("payload");
            String from = payload.getString("from");
            String to = payload.getString("to");
            bodies = payload.getJSONArray("bodies");
            array = (JSONObject) bodies.get(0);

        }
    }

    /**
     * 解压下载的文件
     * @param dateName  文件夹名称
     * @param fileName  文件名称
     * @throws IOException
     */
    private void decompression(String dateName, String fileName) throws IOException {
        String filePath = DIRECTORY + dateName;
        String name = fileName + ".gz";
        FileInputStream fis = new FileInputStream(new File(filePath, name));
        GZIPInputStream gs = new GZIPInputStream(fis);
        File outDir = new File(filePath);
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
    private void download(String dateName, String fileName, String url) throws IOException {
        String filePath = DIRECTORY + dateName;
        File f = new File(filePath);
        if (!f.exists()) {
            f.createNewFile();
        }
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connect = null;
        URL httpUrl = new URL(url);
        connect = (HttpURLConnection) httpUrl.openConnection();
        connect.setRequestMethod("GET");
        connect.setDoInput(true);
        connect.setDoOutput(true);
        connect.setUseCaches(false);
        connect.connect();

        input = connect.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(input);
        if (!filePath.endsWith("/")) {
            filePath += "/";
        }
        String name = filePath + fileName + ".gz";
        output = new FileOutputStream(name);

        BufferedOutputStream bos = new BufferedOutputStream(output);
        byte[] b = new byte[4096];
        int i = bis.read(b);
        while (i != -1) {
            bos.write(b, 0, i);
            i = bis.read(b);
        }
        bis.close();
        bos.close();
        connect.disconnect();
    }
}
