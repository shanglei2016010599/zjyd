package com.example.zjyd.fragment;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjyd.R;
import com.example.zjyd.gson.Machine;
import com.example.zjyd.gson.MachineType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChooseMachineFragment extends Fragment {

    private static final String TAG = "ChooseMachineFragment";

    public static final int LEVEL_TYPE = 0;

    public static final int LEVEL_MACHINE = 1;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /* 机器类别列表 */
    private List<MachineType> typeList = new ArrayList<>();

    /* 机器列表 */
    private List<Machine> machineList = new ArrayList<>();

    /* 选中的机器类别 */
    private MachineType selectedType;

    /* 选中的机器 */
    private Machine selectedMachine;

    /* 当前选中的级别 */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_machine, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                    android.R.layout.simple_list_item_1, dataList);
        }
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_TYPE) {
                    selectedType = typeList.get(position);
                    Toast.makeText(getContext(), selectedType.getTypeName(),
                            Toast.LENGTH_SHORT).show();
                } else if (currentLevel == LEVEL_MACHINE) {
                    selectedMachine = machineList.get(position);
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_MACHINE) {

                }
            }
        });
        testInit();
    }

    /**
     * 查询所有的机器列表
     */
    private void queryMachineType() {
        titleText.setText("浙江易锻");
        backButton.setVisibility(View.GONE);

    }

    /**
     * 查询选中类别内所有的机器
     */
    private void queryMachine() {

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void testInit(){
        titleText.setText("浙江易锻");
        backButton.setVisibility(View.GONE);
        for (int i = 0; i < 20; i++) {
            Random random = new Random();
            MachineType machineType = new MachineType();
            machineType.setTypeName("type is " + random.nextInt(47));
            machineType.setTypeCode(i);
            typeList.add(machineType);
            dataList.add(machineType.getTypeName());
        }
        adapter.notifyDataSetChanged();
        currentLevel = LEVEL_TYPE;
    }

}
