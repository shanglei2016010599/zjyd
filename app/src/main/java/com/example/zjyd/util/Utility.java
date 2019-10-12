package com.example.zjyd.util;

import android.text.TextUtils;

import com.example.zjyd.db.Overlay;
import com.example.zjyd.gson.Machine;
import com.example.zjyd.gson.MachineType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    /**
     * 解析和处理服务器返回的工厂坐标
     * @param response 服务器返回的数据
     * 以布尔值作为处理结果
     */
    public static boolean handleOverlayResponse(String response) {
        if (!TextUtils.isEmpty(response)){
            try {
                DataSupport.deleteAll("overlay");
                JSONArray allOverlays = new JSONArray(response);
                for (int i = 0; i < allOverlays.length(); i++){
                    JSONObject jsonObject = allOverlays.getJSONObject(i);
                    Overlay overlay = new Overlay();
                    overlay.setFactoryID(jsonObject.getString("factory"));
                    overlay.setLatitude(jsonObject.getString("lat"));
                    overlay.setLongitude(jsonObject.getString("lon"));
                    overlay.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理向服务器发送获取所有机器类别的请求后返回的JSON数据
     * @param response 向服务器请求所有机器类别的返回结果
     * @return  如果response不为空，返回typeList，否则返回null
     */
    public static List<MachineType> handleMachineTypeResponse(String response) {
        /* 机器类别列表 */
        List<MachineType> typeList = new ArrayList<>();
        /* 判断response是否为空 */
        if (!TextUtils.isEmpty(response)) {
            /* 如果不为空，使用JSONArray和JSONObject配合解析 */
            try {
                JSONArray allMachineTypes = new JSONArray(response);
                for (int i = 0; i < allMachineTypes.length(); i++) {
                    JSONObject machineTypeObject = allMachineTypes.getJSONObject(i);
                    MachineType machineType = new MachineType();
                    machineType.setTypeName(machineTypeObject.getString("name"));
                    machineType.setTypeCode(machineTypeObject.getInt("id"));
                    typeList.add(machineType);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return typeList;
        }
        return null;
    }

    /**
     * 处理向服务器发送获取选中的机器类别中所有的机器的请求后返回的JSON数据
     * @param response 向服务器请求所有机器类别的返回结果
     * @param typeId 机器类别编号
     * @return  如果response不为空，返回machineList，否则返回null
     */
    public static List<Machine> handleMachineResponse(String response, int typeId) {
        /* 机器列表 */
        List<Machine> machineList = new ArrayList<>();
        /* 判断response是否为空 */
        if (!TextUtils.isEmpty(response)) {
            /* 如果不为空，使用JSONArray和JSONObject配合解析 */
            try {
                JSONArray allMachines = new JSONArray(response);
                for (int i = 0; i < allMachines.length(); i++) {
                    JSONObject machineObject = allMachines.getJSONObject(i);
                    Machine machine = new Machine();
                    machine.setDeviceID(machineObject.getString("id"));
                    machine.setMachineName(machineObject.getString("name"));
                    machine.setMachineCode(machineObject.getInt("code"));
                    machine.setTypeId(typeId);
                    machineList.add(machine);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return machineList;
        }
        return null;
    }

}
