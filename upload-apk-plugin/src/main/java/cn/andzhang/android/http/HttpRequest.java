package cn.andzhang.android.http;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.commons.codec.binary.Base64;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.andzhang.android.api.FirApiService;
import cn.andzhang.android.api.PgyApiService;
import cn.andzhang.android.manager.DingDingManager;
import cn.andzhang.android.manager.FirImManager;
import cn.andzhang.android.util.Logger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import cn.andzhang.android.api.DingDingApiService;
import cn.andzhang.android.model.config.PluginConfigBean;

/**
 * @author zhangshuai@attrsense.com
 * @date 2022/11/28 18:45
 * @description
 */
public class HttpRequest {

    private static Gson mGson;
    public static PluginConfigBean mData;
    private static Long mTimestamp;

    private HttpLoggingInterceptor mInterceptor = new HttpLoggingInterceptor(s -> {
        Logger.print("输出日志：" + s);
        if (s.contains("<-- 201")) {
            Logger.print("Fir.im请求成功！！");
        }

        if (s.contains("<-- 204")) {
            Logger.print("蒲公英上传成功！！");
        }
    });
    private OkHttpClient mOkhttpClient;
    private Retrofit mRetrofit;
    private PgyApiService pgyApiService;
    private FirApiService firApiService;
    private DingDingApiService dingDingApiService;

    private HttpRequest() {
        mInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mOkhttpClient = new OkHttpClient.Builder().addInterceptor(mInterceptor).build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mData.isPgy ? mData.pgyConfig.pgyApiUrl : mData.firImConfig.firApiUrl)
                .client(mOkhttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dingDingApiService = mRetrofit.create(DingDingApiService.class);
        DingDingManager.init(dingDingApiService, mData.ddConfig.ddWebHookUrl);

        if (mData.isPgy) {
            pgyApiService = mRetrofit.create(PgyApiService.class);
        } else {
            //初始化FirIm
            firApiService = mRetrofit.create(FirApiService.class);
            FirImManager.init(mData, firApiService);
        }
    }

    public static HttpRequest getInstance() {
        return HttpRequestHolder.INSTANCE;
    }

    private static class HttpRequestHolder {
        private static final HttpRequest INSTANCE = new HttpRequest();
    }

    public static void init(Project project) {
        mGson = new Gson();
        File file = new File(project.getRootDir().getAbsolutePath() + "/upload-apk.json");
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(file));
            //拼接webhook
            createSign(mGson.fromJson(jsonReader, PluginConfigBean.class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 安全配置
     */
    private static void createSign(PluginConfigBean data) {
        try {
            Long mTimestamp = System.currentTimeMillis();
            String stringToSign = mTimestamp + "\n" + data.ddConfig.ddWebSecret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(data.ddConfig.ddWebSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            data.ddConfig.ddWebHookUrl += "&timestamp=" + mTimestamp + "&sign=" + sign;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mData = data;

        Logger.print("输出配置参数：" + mData);
    }


    /**
     * 获取token
     */
    public void getToken() {
        if (mData.isPgy) {
            if (mData.pgyConfig != null) {
//                getPgyToken();
            } else {
                System.out.println("请配置蒲公英相关数据");
            }
        } else {
            if (mData.firImConfig != null) {
                FirImManager.getInstance().getFirImToken();
            } else {
                System.out.println("请配置Fir.im相关数据");
            }
        }
    }

    /**
     * 上传apk文件
     */
    public void uploadApk() {
        if (mData.isPgy) {
            if (mData.pgyConfig != null) {
//                uploadApkToPgy();
            } else {
                System.out.println("请配置蒲公英相关数据");
            }
        } else {
            if (mData.firImConfig != null) {
                FirImManager.getInstance().uploadApkToFirIm();
            } else {
                System.out.println("请配置Fir.im相关数据");
            }
        }
    }
}