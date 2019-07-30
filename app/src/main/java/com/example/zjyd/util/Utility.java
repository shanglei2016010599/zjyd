package com.example.zjyd.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject =jsonArray.getJSONObject(i);
                String id= jsonObject.getString("deviceID");//获取id
                String latitude= jsonObject.getString("gPSData_latitude");//获取精度
                String longitude= jsonObject.getString("gPSData_longitude");//获取纬度
                Log.d("MapFragment","id"+i+" is "+id);
                Log.d("MapFragment","latitude "+i+" is "+latitude);
                Log.d("MapFragment","longitude "+i+" is "+longitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
