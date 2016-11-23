package com.jimu.monitor.utils.http;

import com.google.common.util.concurrent.FutureCallback;
import com.jimu.monitor.utils.http.ext.AuthSSLInitializationError;

/**
 * http回调接口
 * Created by jimin on 16/03/10.
 */
public interface HttpCallback extends FutureCallback<ResponseWrapper> {

    /**
     * 正确返回的时候将调用此方法
     *
     * @param wrapper ResponseWrapper
     */
    void onSuccess(ResponseWrapper wrapper);

    /**
     * 产生异常的时候调用此方法
     *
     * @param t Throwable
     */
    void onFailure(Throwable t);

    /**
     * https 认证失败调用
     *
     * @param t AuthSSLInitializationError
     */
    void onAuthority(AuthSSLInitializationError t);
}
