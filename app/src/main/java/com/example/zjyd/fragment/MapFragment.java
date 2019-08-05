package com.example.zjyd.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.example.zjyd.InfoActivity;
import com.example.zjyd.R;
import com.example.zjyd.db.Overlay;
import com.example.zjyd.model.CityModel;
import com.example.zjyd.model.DistrictModel;
import com.example.zjyd.model.ProvinceModel;
import com.example.zjyd.util.HttpUtil;
import com.example.zjyd.util.LogUtil;
import com.example.zjyd.util.URLUtil;
import com.example.zjyd.util.Utility;
import com.example.zjyd.util.XmlParserHandler;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Call;
import okhttp3.Response;

public class MapFragment<sendOkHttpRequest> extends Fragment {

    private static final String TAG = "MapFragment";
    private static final int GET_LOCATION_OK = 1;

    //所有省
    protected String[] mProvinceDatas;
    //key - 省 value - 市
    protected Map<String, String[]> mCitiesDatasMap = new HashMap<>();
    //当前省的名称
    protected String mCurrentProvinceName;
    //当前市的名称
    protected String mCurrentCityName;
    //解析省市区的XML数据
    BMapManager mBMapManager = null;

    //地图相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true;
    public LocationClient mLocationClient;
    private BDLocation mlocation=null;

    //储存从服务器获取到的机器的经纬度
    private List<Overlay> overlayList = new ArrayList<>();

    //地区下拉框
    private Spinner provinceSpinner = null;  //省级（省、直辖市）
    private Spinner citySpinner = null;     //地级市
    ArrayAdapter<String> provinceAdapter = null;  //省级适配器
    ArrayAdapter<String> cityAdapter = null;    //地级适配器
    static int provincePosition = 0;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_LOCATION_OK:
                    //显示覆盖点
                    setOverlay();
                    mBaiduMap.setOnMarkerClickListener(overlayListener);//地图覆盖物事件监听器
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在fragment用getContext().getApplicationContext()获取整个应用的上下文
        mLocationClient = new LocationClient(Objects.requireNonNull(getContext()).getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(new MyLocationListener());
        //注册监听函数
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        //加入view.
        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mMapView.showScaleControl(true);//显示比例尺
        mMapView.showZoomControls(true);//显示缩放按钮
        mMapView.removeViewAt(1);//删除百度地图LoGo
        //获取下拉框
        provinceSpinner = view.findViewById(R.id.spin_province);
        citySpinner = view.findViewById(R.id.spin_city);
        //地图显示设置
        navigateTo(mlocation);
        //配置定位SDK参数
        initProvinceDatas();
        //设置下拉框
        setSpinner();
        //获取覆盖点信息
        queryOverlayFromServer();

        mBaiduMap.setOnMapClickListener(listener);//地图事件监听器


        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }//获取手机状态
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }//获取位置信息
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }//获取位置信息
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }//读写SD卡
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }//读写SD卡
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            requestPermissions(permissions, 1);
        } else {
            requestLocation();
        }

        return view;
    }

    //地图覆盖物点击事件
    BaiduMap.OnMarkerClickListener overlayListener = new BaiduMap.OnMarkerClickListener() {
        /**
         * 地图 Marker 覆盖物点击事件监听函数
         * @param marker 被点击的 marker
         */
        public boolean onMarkerClick(Marker marker){
            try{
                Intent intent = new Intent(getActivity(), InfoActivity.class);//发送id值并跳转
                intent.putExtra("ID",  marker.getExtraInfo().getString("id") );
                Objects.requireNonNull(getActivity()).startActivity(intent);
            } catch (Exception e){
                LogUtil.e(TAG, e.toString());
            }
            return true;
        }
    };

    //地图点击事件
    BaiduMap.OnMapClickListener listener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         *
         * @param point 点击的地理坐标
         */
        @Override
        public void onMapClick(LatLng point) {

            Toast.makeText(getActivity(), "你点击了地图", Toast.LENGTH_SHORT).show();

        }

        /**
         * 地图内 Poi 单击事件回调函数
         *
         * @param mapPoi 点击的 poi 信息
         */
        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            return false;
        }
    };

    //设置覆盖点
    private void setOverlay() {
        overlayList.clear();
        overlayList = DataSupport.findAll(Overlay.class);
        for (int i = 0; i < overlayList.size(); i++) {
            Double la = Double.valueOf(overlayList.get(i).getLatitude());
            Double lo = Double.valueOf(overlayList.get(i).getLongitude());
            /* 使用Bundle来存储机械编号 */
            Bundle bundle = new Bundle();
            bundle.putSerializable("id", overlayList.get(i).getDeviceID());

            //定义Maker坐标点
            LatLng point = new LatLng(la, lo);
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_marka);
            //构建MarkerOption，用于在地图上添加Marker
            MarkerOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap)
                    .extraInfo(bundle); // 覆盖物携带数据
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
        }
    }

    private void setSpinner() {
        /*
         * 设置下拉框
         */
        //绑定适配器和值
        provinceAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                R.layout.spinner_item, mProvinceDatas);
        provinceSpinner.setAdapter(provinceAdapter);
        provinceSpinner.setSelection(0,true);  //设置默认选中项，此处为默认选中第0个值
        cityAdapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item, Objects.requireNonNull(mCitiesDatasMap.get(mCurrentProvinceName)));
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setSelection(0,true);  //默认选中第0个

        //省级下拉框监听
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            // 表示选项被改变的时候触发此方法，主要实现办法：动态改变地级适配器的绑定值
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                //position为当前省级选中的值的序号
                mCurrentProvinceName = mProvinceDatas[position];
                String[] cities = mCitiesDatasMap.get(mCurrentProvinceName);
                if (cities == null) {
                    cities = new String[] { "" };
                }
                if(position!=0)
                {

                }

                //将地级适配器的值改变为city[position]中的值
                cityAdapter = new ArrayAdapter<>(
                        Objects.requireNonNull(getContext()), R.layout.spinner_item, cities);
                // 设置二级下拉列表的选项内容适配器
                citySpinner.setAdapter(cityAdapter);
                provincePosition = position;    //记录当前省级序号，留给下面修改县级适配器时用
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });

        /* 城市下拉列表监听器 */
        /* 选中后，将省、市发送给服务器 */
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /* 从服务器获得所有机器的经纬度 */
    private void queryOverlayFromServer(){
        HttpUtil.sendOkHttpRequest(URLUtil.OverlayURL, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, e.toString());
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                LogUtil.d(TAG, responseData);
                if (Utility.handleOverlayResponse(responseData)){
                    Message message = new Message();
                    message.what = GET_LOCATION_OK;
                    handler.sendMessage(message);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity(), "发生权限问题", Toast.LENGTH_SHORT).show();
//                            getActivity().finish();
//                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(getActivity(), "发生未知错误，换个新手机试试？", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                }
                break;
            default:
        }
    }

    private void requestLocation() {

        initLocation();
        //mLocationClient为第二步初始化过的LocationClient对象
        //调用LocationClient的start()方法，便可发起定位请求
    }

    //地图显示设置
    private void navigateTo(BDLocation location){
        if(location == null){
            return;
        } else if(isFirstLoc) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng ll2 = new LatLng(33.561695,104.282591);
            MapStatusUpdate update;
            update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            //zoom设置缩放等级，值越大，地点越详细
            MapStatus mMapSta0tus = new MapStatus.Builder()
                    .target(ll2)//中心坐标
                    .zoom(5)
                    .build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapSta0tus);
            //改变地图状态
            mBaiduMap.animateMapStatus(mMapStatusUpdate);
            isFirstLoc = false;
        }
        MyLocationData locationData = new MyLocationData.Builder().accuracy(20)//locData.accuracy = location.getRadius();//获取默认误差半径
                //accuracy设置精度圈大小
                //设置开发者获取到的方向信息，顺时针旋转0-360度
                .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
//        mBaiduMap.setMyLocationData(locationData);
    }

    //实现BDAbstractLocationListener接口
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            if (errorCode == BDLocation.TypeGpsLocation
                    || errorCode == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
                mlocation=location;
            }
        }
    }

    //配置定位SDK参数
    private void initLocation() {

        LocationClientOption option = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        //注册监听函数
        mLocationClient.registerLocationListener(myLocationListener);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        int span = 1000;
        option.setScanSpan(span);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者

        option.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //开始定位
        mLocationClient.start();
//        option.setIsNeedAddress(true);
//        option.setIsNeedLocationPoiList(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mLocationClient.stop();
        mMapView.onPause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
        }
    }

    //获取xml中的城市的信息
    protected void initProvinceDatas()
    {
        List<ProvinceModel> provinceList;
        AssetManager asset = Objects.requireNonNull(getActivity()).getAssets();
        try {
            InputStream input = asset.open("province_data.xml");
            // 创建一个解析xml的工厂对象
            SAXParserFactory spf = SAXParserFactory.newInstance();
            // 解析xml
            SAXParser parser = spf.newSAXParser();
            XmlParserHandler handler = new XmlParserHandler();
            parser.parse(input, handler);
            input.close();
            // 获取解析出来的数据
            provinceList = handler.getDataList();
            //*/ 初始化默认选中的省、市、区
            if (provinceList!= null && !provinceList.isEmpty()) {
                mCurrentProvinceName = provinceList.get(0).getName();
                List<CityModel> cityList = provinceList.get(0).getCityList();
                if (cityList!= null && !cityList.isEmpty()) {
                    mCurrentCityName = cityList.get(0).getName();
                    List<DistrictModel> districtList = cityList.get(0).getDistrictList();
                }
            }

            mProvinceDatas = new String[Objects.requireNonNull(provinceList).size()];
            for (int i=0; i< provinceList.size(); i++) {
                // 遍历所有省的数据
                mProvinceDatas[i] = provinceList.get(i).getName();
                List<CityModel> cityList = provinceList.get(i).getCityList();
                String[] cityNames = new String[cityList.size()];
                for (int j = 0; j < cityList.size(); j++) {
                    // 遍历省下面的所有市的数据
                    cityNames[j] = cityList.get(j).getName();
                    List<DistrictModel> districtList = cityList.get(j).getDistrictList();
                    String[] distrinctNameArray = new String[districtList.size()];
                    DistrictModel[] distrinctArray = new DistrictModel[districtList.size()];
                }
                // 省-市的数据，保存到mCitisDatasMap
                mCitiesDatasMap.put(provinceList.get(i).getName(), cityNames);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
