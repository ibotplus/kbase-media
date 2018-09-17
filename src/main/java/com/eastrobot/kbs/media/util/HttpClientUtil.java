package com.eastrobot.kbs.media.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import java.util.Map;

/**
 * HttpClientUtil 基于HttpClient4.5
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-16 17:01
 */
@Slf4j
public class HttpClientUtil {
    /**
     * 最大连接数 如果不设置默认情况下对于同一个目标机器的最大并发连接只有2个
     */
    private final static int MAX_TOTAL_CONNECTIONS = 800;
    /**
     * 获取连接的最大等待时间
     */
    private final static int WAIT_TIMEOUT = 60000;
    /**
     * 每个路由最大连接数
     */
    private final static int MAX_ROUTE_CONNECTIONS = 400;
    /**
     * 连接超时时间
     */
    private final static int CONNECT_TIMEOUT = 10000;
    /**
     * 读取超时时间
     */
    private final static int READ_TIMEOUT = 10000;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0.1";
    private static final String CHARSET = "UTF-8";
    private static HttpClient httpClient;

    static {
        init();
    }

    private static void init() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        //socket配置
        SocketConfig socketConfig = SocketConfig.custom()
                //是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
                .setTcpNoDelay(true)
                //接收数据的等待超时时间，单位ms
                .setSoTimeout(READ_TIMEOUT)
                //关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接，此时socket.close()是阻塞的
                .setSoLinger(60)
                //在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
                .setSoReuseAddress(true)
                //开启监视TCP连接是否有效
                .setSoKeepAlive(true)
                .build();
        //消息约束
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();
        //connection相关配置
        ConnectionConfig connectConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();
        //Request配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(true)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(WAIT_TIMEOUT)
                .build();
        //连接管理器配置
        PoolingHttpClientConnectionManager connectManager = new PoolingHttpClientConnectionManager();
        connectManager.setDefaultConnectionConfig(connectConfig);
        connectManager.setDefaultSocketConfig(socketConfig);
        connectManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        connectManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        httpClient = HttpClients.custom()
                .setUserAgent(USER_AGENT)               //伪装的浏览器类型
                .setConnectionManager(connectManager)   //连接管理器
                .setDefaultRequestConfig(requestConfig) //默认请求配置
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)) //重试策略-禁用重试
                .build();
    }

    public static byte[] doGet(String url, String params) {
        HttpGet get = new HttpGet(url + (params == null ? "" : params));
        try {
            HttpResponse response = httpClient.execute(get);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                if (response.getEntity() != null) {
                    return EntityUtils.toByteArray(response.getEntity());
                }
            } else {
                get.abort();
                log.info("HttpClientUtil statusCode:" + response.getStatusLine().getStatusCode() + " url :" + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            get.abort();
        } finally {
            get.releaseConnection();
        }
        return null;
    }

    public static String doGet(String url, String params, String encode) {
        byte[] is = doGet(url, params);
        if (is != null) {
            try {
                return IOUtils.toString(is, encode);
            } catch (IOException e) {
                return "";
            }
        }
        return null;
    }

    /**
     * List <NameValuePair> nvps = new ArrayList <NameValuePair>();
     * nvps.add(new BasicNameValuePair("name", "1"));//名值对
     * nvps.add(new BasicNameValuePair("account", "xxxx"));
     */
    public static byte[] doPost(String url, List<NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        post.setHeader("Connection", "close");
        post.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        try {
            HttpResponse response = httpClient.execute(post);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                if (response.getEntity() != null) {
                    return EntityUtils.toByteArray(response.getEntity());
                }
            } else {
                log.info("HttpClientUtil statusCode:" + response.getStatusLine().getStatusCode() + " url :" + url);
                post.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
            post.abort();
        } finally {
            post.releaseConnection();
        }
        return null;
    }

    public static String doPost(String url, List<NameValuePair> params, String encode) {
        byte[] is = doPost(url, params);
        if (is != null) {
            try {
                return IOUtils.toString(is, encode);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        return null;
    }

    /**
     * 发送文件，map封装需要传入的参数和文件
     * <p>
     * put("image", file);
     * put("filename", "image.jpg");
     * <p>
     * HttpClientUtil.doPost(url, map);</pre>
     */
    public static String doPost(String url, Map<String, Object> map) throws IOException {
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof File) {
                    File file = (File) entry.getValue();
                    FileBody bin = new FileBody(file);
                    builder.addPart(entry.getKey(), bin);
                } else {
                    builder.addPart(entry.getKey(), new StringBody(entry.getValue().toString(), ContentType
                            .MULTIPART_FORM_DATA));

                }
            }
            byte[] is = doPost(url, builder.build());
            if (is != null) {
                return IOUtils.toString(is, CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            log.debug("filename parse error, " + e.getMessage());
        }
        return "";
    }

    /**
     * 发送文件，不推荐使用
     */
    private static byte[] doPost(String url, HttpEntity entity) {
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                EntityUtils.consume(entity);
                log.debug("MultipartEntity info send ok. ");
                if (response.getEntity() != null) {
                    return EntityUtils.toByteArray(response.getEntity());
                }
            } else {
                log.error(EntityUtils.toString(response.getEntity()));
                post.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
            post.abort();
        } finally {
            post.releaseConnection();
        }
        return null;
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
