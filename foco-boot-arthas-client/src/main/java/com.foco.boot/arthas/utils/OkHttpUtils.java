package com.foco.boot.arthas.utils;

import com.alibaba.fastjson.JSON;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author ChenMing
 */
public class OkHttpUtils {
    private static volatile OkHttpClient okHttpClient = null;
    private static volatile Semaphore semaphore = null;
    private Map<String, String> headerMap;
    private Map<String, String> paramMap;
    private String url;
    private Request.Builder request;
    /*
     * Header
     */
    public static final String USER_AGENT = "User-Agent";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    /**
     * 初始化okHttpClient，并且允许https访问
     */
    private OkHttpUtils() {
        if (okHttpClient == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpClient == null) {
                    TrustManager[] trustManagers = buildTrustManagers();
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                            .hostnameVerifier((hostName, session) -> true)
                            .retryOnConnectionFailure(true)
                            .build();
                    addHeader(USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                }
            }
        }
    }

    /**
     * 用于异步请求时，控制访问线程数，返回结果
     *
     * @return
     */
    private static Semaphore getSemaphoreInstance() {
        //只能1个线程同时访问
        synchronized (OkHttpUtils.class) {
            if (semaphore == null) {
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    /**
     * 创建OkHttpUtils
     *
     * @return
     */
    public static OkHttpUtils builder() {
        return new OkHttpUtils();
    }

    /**
     * 添加url
     *
     * @param url
     * @return
     */
    public OkHttpUtils url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 添加参数
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addParam(String key, String value) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.put(key, value);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addHeader(String key, String value) {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.put(key, value);
        return this;
    }

    /**
     * 初始化get方法
     *
     * @return
     */
    private OkHttpUtils get(Type type) {
        request = new Request.Builder().get();
        if (type == Type.URL) {
            StringBuilder urlBuilder = new StringBuilder(url);
            if (paramMap != null) {
                urlBuilder.append("?");
                try {
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).
                                append("=").
                                append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name())).
                                append("&");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                urlBuilder.deleteCharAt(urlBuilder.length() - 1);
                url(urlBuilder.toString());
            }
        } else {
            throw new UnsupportedOperationException("未提供的Get请求方式");
        }
        request.url(url);
        return this;
    }

    /**
     * 初始化post方法
     *
     * @param type {@link Type#JSON}等于json的方式提交数据，类似postman里post方法的raw
     *             {@link Type#FORM}等于普通的表单提交
     * @return
     */
    private OkHttpUtils post(Type type) {
        RequestBody requestBody;
        switch (type) {
            case JSON: {
                String json = "";
                if (paramMap != null) {
                    json = JSON.toJSONString(paramMap);
                }
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                break;
            }
            case FORM: {
                FormBody.Builder formBody = new FormBody.Builder();
                if (paramMap != null) {
                    paramMap.forEach(formBody::add);
                }
                requestBody = formBody.build();
                break;
            }
            default: {
                throw new UnsupportedOperationException("未提供的Post请求类型");
            }
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    /**
     * 同步请求
     *
     * @return
     */
    private String sync() throws IOException {
        setHeader(request);
        Response response = okHttpClient.newCall(request.build()).execute();
        assert response.body() != null;
        return response.body().string();
    }

    public String syncGet() throws IOException {
        return syncGet(Type.URL);
    }


    public String asyncGet() {
        return asyncGet(Type.URL);
    }

    public String syncGet(Type type) throws IOException {
        get(type);
        return sync();
    }


    public String asyncGet(Type type) {
        get(type);
        return async();
    }

    public String syncPost(Type type) throws IOException {
        post(type);
        return sync();
    }

    public String syncPost() throws IOException {
        post(Type.JSON);
        return sync();
    }

    public String asyncPost() {
        post(Type.JSON);
        return async();
    }

    public String asyncPost(Type type) {
        post(type);
        return async();
    }

    /**
     * 异步请求，有返回值
     */
    private String async() {
        StringBuilder buffer = new StringBuilder("");
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                buffer.append("请求出错：").append(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                buffer.append(response.body().string());
                getSemaphoreInstance().release();
            }
        });
        try {
            getSemaphoreInstance().acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 异步请求，带有接口回调
     *
     * @param callBack
     */
    public void async(ICallBack callBack) {
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(call, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                callBack.onSuccessful(call, response.body().string());
            }
        });
    }

    /**
     * 为request添加请求头
     *
     * @param request
     */
    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            try {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * 自定义一个接口回调
     */
    public interface ICallBack {

        void onSuccessful(Call call, String data);

        void onFailure(Call call, String errorMsg);

    }

    public enum Type {
        JSON,
        FORM,
        URL;
    }
}


