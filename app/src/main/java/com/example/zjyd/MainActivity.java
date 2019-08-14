package com.example.zjyd;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.zjyd.fragment.ChooseMachineFragment;
import com.example.zjyd.fragment.MapFragment;
import com.example.zjyd.fragment.ProductionDataFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    /* 底部导航按钮点击事件 */
    /* 动态切换碎片 */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    replaceFragment(new MapFragment());
                    return true;
                case R.id.navigation_dashboard:
                    replaceFragment(new ProductionDataFragment());
                    return true;
                case R.id.navigation_notifications:
                    replaceFragment(new ChooseMachineFragment());
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            SDKInitializer.initialize(getApplicationContext());
        } catch (Exception e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        /* DrawerLayout初始化 */
        mDrawerLayout = findViewById(R.id.drawer_layout);
        /* NavigationView初始化 */
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.inflateMenu(R.menu.nav_menu);
        /* 底部导航初始化 */
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        /* 碎片初始化 */
        replaceFragment(new MapFragment());

        /* 默认选中电话 */
        navigationView.setCheckedItem(R.id.nav_call);
        /* 左侧菜单栏点击事件 */
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch ( menuItem.getItemId() ) {
                    case R.id.nav_call:
                        Toast.makeText(MainActivity.this, "You clicked call",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_friends:
                        Toast.makeText(MainActivity.this, "You clicked friends",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_location:
                        Toast.makeText(MainActivity.this, "You clicked location",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_mail:
                        Toast.makeText(MainActivity.this, "You clicked mail",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_task:
                        Toast.makeText(MainActivity.this, "You clicked task",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });
    }

    /* 动态添加碎片 */
    /*
        1.创建待添加的碎片实例
        2.获取FragmentManager，在获得中可以直接通过调用getSupportFragmentManager()方法得到
        3.开启一个事务，通过调用beginTransaction()方法开启
        4.向容器内添加或替换碎片，一般使用replace()方法实现，需要传入容器的id和待添加的碎片实例
        5.提交事务，调用commit()方法来完成。
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

}
