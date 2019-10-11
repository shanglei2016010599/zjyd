package com.example.zjyd.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.zjyd.R;
import com.example.zjyd.service.LongRunningService;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class ProductionDataFragment extends Fragment {

    private XYSeries series;
    private XYMultipleSeriesDataset dataSet;
    private GraphicalView chartView;

    private static final String TAG = "MainActivity";

    private int[] s = new int[24];

    private LongRunningService myService = null;

    LinearLayout chartLyt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.production_data_fragment, container,
                false);
        chartLyt = view.findViewById(R.id.chart);
        chartLyt.addView(chartView, 0);

        return  view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 图表标题 */
        series = new XYSeries("London Temperature hourly");

        /* 数据填充 */
        InitData();
        /* 创建一个系列渲染器 */
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        /* 设置线宽 */
        renderer.setLineWidth(2);
        /* 设置颜色 */
        renderer.setColor(Color.RED);
        /* Include low and max value */
        renderer.setDisplayBoundingPoints(true);
        /* 添加点标记 */
        /* 设置点格式 */
        renderer.setPointStyle(PointStyle.CIRCLE);
        /* 设置点笔画宽度 */
        renderer.setPointStrokeWidth(3);

        /* Now we add our series */
        dataSet = new XYMultipleSeriesDataset();
        dataSet.addSeries(series);

        /* 创建控制完整图标的渲染器，并为每个系列添加单个渲染器 */
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);

        /* We want to avoid black border */
        /* transparent margins */
        /* 删除黑色边框 */
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));// transparent margins
        /* Disable Pan on two axis */
        mRenderer.setPanEnabled(false, false);
        /* 设置Y值范围 */
        mRenderer.setYAxisMax(47);
        mRenderer.setYAxisMin(0);
        /* we show the grid */
        mRenderer.setShowGrid(true);

        /* 创建视图 */
        chartView = ChartFactory.
                getLineChartView(getContext(), dataSet, mRenderer);

//        chartLyt.addView(chartView, 0);

        /* 启动并绑定服务，向服务发送存储点值的数组s */
        Intent intent = new Intent(getContext(), LongRunningService.class);
        intent.putExtra("Data", s);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getContext()).bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getContext()).startService(intent);
        }
    }

    /* 绑定服务 */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /* 获得服务 */
            myService = ((LongRunningService.MyBinder) service).getService();
            /* 注册接口 */
            myService.registererOnAddCalculateListener(onAddCalculateListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private LongRunningService.OnAddCalculateListener onAddCalculateListener =
            new LongRunningService.OnAddCalculateListener() {
                @Override
                public void onAddResultCallback(int[] s) {
                    /* 服务结果回调函数，将或得到新的数组s，添加至series。并重新绘画 */
                    Log.d(TAG, "onAddResultCallback: " + Arrays.toString(s));
                    series.clear();
                    for (int i = 0; i < s.length; i++){
                        series.add(i, s[i]);
                    }
                    dataSet.clear();
                    dataSet.addSeries(series);
                    chartView.repaint();
                }
            };

    /* 初始化数据，先将点值存在数组中，再添加到series */
    private void InitData(){
        for (int i = 0; i < 24; i++){
            Random random = new Random();
            s[i] = random.nextInt(47);
            series.add(i, s[i]);
        }
    }

    /* 取消注册 */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myService != null){
            myService.unregisterOnAddCalculateListener();
        }
    }
}
