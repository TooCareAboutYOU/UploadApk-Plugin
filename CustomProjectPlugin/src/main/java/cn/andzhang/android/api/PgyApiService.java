package cn.andzhang.android.api;

import java.util.List;
import java.util.Map;
import cn.andzhang.android.model.response.BaseResponseBean;
import cn.andzhang.android.model.response.pgy.PgyAppDetailInfoBean;
import cn.andzhang.android.model.response.pgy.PgyTokenBean;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * @author zhangshuai@attrsense.com
 * @date 2022/11/28 18:44
 * @description 蒲公英
 */
public interface PgyApiService {

    /**
     * 获取上传token
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("getCOSToken")
    Call<BaseResponseBean<PgyTokenBean>> getPgyToken(@FieldMap Map<String, Object> map);

    /**
     * 上传apk文件到蒲公英
     */
    @Multipart
    @POST
    Call<BaseResponseBean<Object>> uploadApkToPgy(@Url String url,
                                                  @Part List<MultipartBody.Part> parts);

    /**
     * 获取应用信息
     *
     * @param map 参数	    类型	     说明
     *            _api_key	String	(必填) API Key 点击获取_api_key
     *            appKey	String	(必填) 表示一个App组的唯一Key。例如，名称为'微信'的App上传了三个版本，那么这三个版本为一个App组，该参数表示这个组的Key。这个值显示在应用详情--应用概述--App Key。
     *            buildKey	String	(选填) Build Key是唯一标识应用的索引ID，可以通过 获取App所有版本取得
     * @return
     */
    @FormUrlEncoded
    @POST("view")
    Call<BaseResponseBean<PgyAppDetailInfoBean>> getAppDetailInfo(@FieldMap Map<String, Object> map);
}
