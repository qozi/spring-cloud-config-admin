package com.didispace.scca.core.service.impl;

import com.didispace.scca.core.domain.EncryptKeyRepo;
import com.didispace.scca.core.domain.Env;
import com.didispace.scca.core.domain.EnvParamRepo;
import com.didispace.scca.core.domain.EnvRepo;
import com.didispace.scca.core.service.BaseOptService;
import com.didispace.scca.core.service.UrlMakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;

import java.util.concurrent.TimeUnit;

/**
 * Created by 程序猿DD/翟永超 on 2018/4/24.
 * <p>
 * Blog: http://blog.didispace.com/
 * Github: https://github.com/dyc87112/
 */
@Slf4j
public class BaseOptServiceImpl implements BaseOptService {

    private String encryptPath = "/encrypt";
    private String decryptPath = "/decrypt";

    private OkHttpClient okHttpClient = buildOkHttpClient();

    @Autowired
    protected EnvParamRepo envParamRepo;
    @Autowired
    protected EncryptKeyRepo encryptKeyRepo;
    @Autowired
    protected EnvRepo environmentRepo;

    @Autowired
    protected UrlMakerService urlMakerService;

    @Override
    public String encrypt(String originValue, Env env) {
        return callTextPlain(urlMakerService.configServerBaseUrl(env.getName()) + encryptPath, originValue);
    }

    @Override
    public String decrypt(String originValue, Env env) {
        return callTextPlain(urlMakerService.configServerBaseUrl(env.getName()) + decryptPath, originValue);
    }

    @Override
    public Environment getProperties(String application, String envName, String label) {
        String url = urlMakerService.propertiesLoadUrl(application, envName, label);
        return callGetProperties(url);
    }

    @SneakyThrows
    private String callTextPlain(String url, String value) {
        log.info("call text plain : " + url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "text/plain")
                .post(RequestBody.create(MediaType.parse("text/plain"), value.getBytes()))
                .build();

        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        ResponseBody responseBody = response.body();
        return responseBody.string();
    }

    @SneakyThrows
    private Environment callGetProperties(String url) {
        log.info("call get properties : " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        ResponseBody responseBody = response.body();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(responseBody.string(), Environment.class);
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        return client;
    }

}
