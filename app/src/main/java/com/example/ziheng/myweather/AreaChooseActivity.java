package com.example.ziheng.myweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ziheng.myweather.db.CoolWeatherDB;
import com.example.ziheng.myweather.model.City;
import com.example.ziheng.myweather.model.County;
import com.example.ziheng.myweather.model.Province;
import com.example.ziheng.myweather.util.HttpCallbackListener;
import com.example.ziheng.myweather.util.HttpUtil;
import com.example.ziheng.myweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class AreaChooseActivity extends AppCompatActivity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private int currentLevel;

    private ProgressDialog progressDialog;
    private TextView tittle;
    private ListView lv;

    private CoolWeatherDB coolWeatherDB;
    private List<String> datalist = new ArrayList<String>();
    private ArrayAdapter adapter;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areachoose);
        tittle = (TextView) findViewById(R.id.title);
        lv = (ListView) findViewById(R.id.lv);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(i);
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(i);
                        queryCounty();
                        break;
                    case LEVEL_COUNTY:
                        break;
                }
            }
        });

        coolWeatherDB = CoolWeatherDB.getInstance(this);
        queryProvince();
    }

    private void queryProvince() {
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            datalist.clear();
            for (Province province : provinceList) {
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            tittle.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    private void queryCity() {
        System.out.println("queryCity-------------------");
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            datalist.clear();
            for (City city : cityList) {
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            tittle.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    private void queryCounty() {
        System.out.println("queryCounty-------------------");
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            datalist.clear();
            for (County county : countyList) {
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            tittle.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    private void queryFromServer(final String code, final String style) {
        String address;
        if (code == null) {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";

        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                System.out.print(response);
                switch (style) {
                    case "province":
                        Utility.handleProvincesResponse(coolWeatherDB, response);
                        break;
                    case "city":
                        Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                        break;
                    case "county":
                        Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
                        break;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                        switch (style) {
                            case "province":
                                queryProvince();
                                break;
                            case "city":
                                queryCity();
                                break;
                            case "county":
                                queryCounty();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(AreaChooseActivity.this,
                                "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载，请稍后...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCity();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvince();
        } else {
            finish();
        }
    }
}
