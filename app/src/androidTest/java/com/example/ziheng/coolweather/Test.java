package com.example.ziheng.coolweather;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.example.ziheng.coolweather.util.HttpCallbackListener;
import com.example.ziheng.coolweather.util.HttpUtil;

/**
 * Created by ziheng on 2015/9/28 0028.
 */
public class Test extends InstrumentationTestCase implements HttpCallbackListener {
    public void test() {
        HttpUtil.sendHttpRequest("http://www.weather.com.cn/data/list3/city.xml", null);
        Log.d("TAG", "hahahahahh");
    }

    @Override
    public void onFinish(String response) {
        System.out.println(response);
    }

    @Override
    public void onError(Exception e) {

    }
}
