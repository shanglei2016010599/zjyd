package com.example.zjyd.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.zjyd.db.Overlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

public class Utility {

    private static final String TAG = "MapFragment";

    /**
     * 解析和处理服务器返回的机器坐标
     * @param response 服务器返回的数据
     * 以布尔值作为处理结果
     */

    public static boolean handleOverlayResponse(String response) {
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allOverlays = new JSONArray(response);
                for (int i = 0; i < allOverlays.length(); i++){
                    JSONObject jsonObject = allOverlays.getJSONObject(i);
                    Overlay overlay = new Overlay();
                    overlay.setDeviceID(jsonObject.getString("deviceID"));
                    overlay.setLatitude(jsonObject.getString("gPSData_latitude"));
                    overlay.setLongitude(jsonObject.getString("gPSData_longitude"));
                    overlay.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
