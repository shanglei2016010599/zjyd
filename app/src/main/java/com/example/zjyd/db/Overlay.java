package com.example.zjyd.db;

import android.widget.ArrayAdapter;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

public class Overlay extends DataSupport {

    private String factoryID;

    private String latitude;

    private String longitude;

    /* 用于存储目标工厂内所含的所有机器类别 */
//    private ArrayList<String> typeList = new ArrayList<>();

    public String getFactoryID() {
        return factoryID;
    }

    public void setFactoryID(String factoryID) {
        this.factoryID = factoryID;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
