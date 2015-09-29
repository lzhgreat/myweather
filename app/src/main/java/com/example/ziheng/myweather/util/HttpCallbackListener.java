package com.example.ziheng.myweather.util;

/**
 * Created by ziheng on 2015/9/27 0027.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
