package com.im.inspection.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 远程调用工具类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/08/11 16:51:03
 */
public class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 注释: 以get方式调用第三方接口
     *
     * @param url 地址
     * @return java.lang.String
     */
    public static String get(String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);

        // token自定义header头，用于token验证使用
        httpGet.addHeader("token", "");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 返回json格式
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            logger.error("http请求异常", e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("关闭HTTP客户端异常", e);
            }
        }
        return null;
    }

    /**
     * 注释: 以post方式调用第三方接口
     *
     * @param url  地址
     * @param json 入参
     * @return java.lang.String
     */
    public static String post(String url, String json) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        // token自定义header头，用于token验证使用
        httpPost.addHeader("token", "");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");

        try {
            StringEntity se = new StringEntity(json);
            se.setContentEncoding("UTF-8");
            se.setContentType("application/json");
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            logger.error("http请求异常", e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("关闭HTTP客户端异常", e);
            }
        }
        return null;
    }
}