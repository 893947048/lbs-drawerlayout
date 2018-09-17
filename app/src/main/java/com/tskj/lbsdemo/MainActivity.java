package com.tskj.lbsdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.tskj.lbsdemo.map.BdLocationUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private ListView lv_name;
    private static final int BAIDU_ACCESS_COARSE_LOCATION = 100;

    private ArrayList<String> test_data = new ArrayList<>();

    /**
     * 动态请求权限，安卓手机版本在5.0以上时需要
     */
    private void myPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查是否拥有权限，申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义)
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, BAIDU_ACCESS_COARSE_LOCATION);
            } else {
                // 已拥有权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                myLocation();
            }
        } else {
            // 配置清单中已申明权限，作相应处理，此处正对sdk版本低于23的手机
            myLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        InitView();
        myPermissionRequest();
        InitMap();
        InitData();
    }

    /**
     * 百度地图定位的请求方法   拿到 国 省 市  区
     */
    private void myLocation() {
        BdLocationUtil.getInstance().requestLocation(new BdLocationUtil.MyLocationListener() {
            @Override
            public void myLocation(BDLocation location) {
                if (location == null) {
                    return;
                }
                if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    StringBuffer sb = new StringBuffer(256);
                    sb.append("time : ");
                    sb.append(location.getTime());
                    sb.append("\nerror code : ");
                    sb.append(location.getLocType());
                    sb.append("\nlatitude : ");
                    sb.append(location.getLatitude());
                    sb.append("\nlontitude : ");
                    sb.append(location.getLongitude());
                    sb.append("\nradius : ");
                    sb.append(location.getRadius());
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                        sb.append("\nspeed : ");
                        sb.append(location.getSpeed());// 单位：公里每小时
                        sb.append("\nsatellite : ");
                        sb.append(location.getSatelliteNumber());
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                        sb.append("\ndirection : ");
                        sb.append(location.getDirection());// 单位度
                        sb.append("\naddr : ");
                        sb.append(location.getAddrStr());
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.gpssucc));

                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                        sb.append("\naddr : ");
                        sb.append(location.getAddrStr());
                        //运营商信息
                        sb.append("\noperationers : ");
                        sb.append(location.getOperators());
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.wifilocation));
                    } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.erro_offlinelocation));
                    } else if (location.getLocType() == BDLocation.TypeServerError) {
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.erro_unknow));
                    } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.erro_wifiweak));
                    } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                        sb.append("\ndescribe : ");
                        sb.append(getString(R.string.erro_restartboot));
                    }
                    sb.append("\nlocationdescribe : ");
                    sb.append(location.getLocationDescribe());// 位置语义化信息
                    List<Poi> list = location.getPoiList();// POI数据
                    if (list != null) {
                        sb.append("\npoilist size = : ");
                        sb.append(list.size());
                        for (Poi p : list) {
                            sb.append("\npoi= : ");
                            sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                        }
                    }
                    Log.e("BaiduLocation", sb.toString());
                    showCurrentPosition(location);

                }
            }
        });
    }

    private void showCurrentPosition(BDLocation location) {
        MyLocationData locationData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        MyLocationConfiguration.LocationMode locationMode = MyLocationConfiguration.LocationMode.NORMAL;
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        MyLocationConfiguration config = new MyLocationConfiguration(locationMode, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);
        mBaiduMap.setMyLocationData(locationData);
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        //设置缩放中心点；缩放比例；
        builder.target(ll).zoom(15.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }


    private void InitData() {
        test_data.add("case1");
        test_data.add("case2");
        test_data.add("case3");
        test_data.add("case4");
        test_data.add("case5");
        test_data.add("case6");
    }

    private void InitView() {
        mMapView = findViewById(R.id.bmapView);
        lv_name = findViewById(R.id.lv_name);
        lv_name.setOnItemClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //drawerlayout阴影
//        drawer.setDrawerElevation(200f);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


    }

    private void InitMap() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        if (mMapView != null) {
            // 关闭定位图层
            mBaiduMap.setMyLocationEnabled(false);
            mMapView.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (test_data.get(position).equals("case1")) {
            BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
            View bottom_view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet, null);
            bottom_view.findViewById(R.id.tel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, R.string.tel, Toast.LENGTH_SHORT).show();
                }
            });
            mBottomSheetDialog.setContentView(bottom_view);
            mBottomSheetDialog.show();
        } else if (test_data.get(position).equals("case2")) {
            dialogView();
        }
    }

    private void dialogView() {
        View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_sheet, null);

        Button cancel = view1.findViewById(R.id.cancel);
        Button ok = view1.findViewById(R.id.tel);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .create();//创建Dialog

        dialog.setTitle("Dialog自定义View");
        dialog.setCancelable(false);
        dialog.setView(view1);//设置自定义view
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });//取消按钮监听点击事件
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, R.string.tel, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });//取消按钮监听点击事件

    }


    /**
     * 权限请求的返回结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 第一次获取到权限，请求定位
                    myLocation();
                } else {
                    // 没有获取到权限，做特殊处理
                    Log.i("=========", getString(R.string.erro_permissionfailed));
                    Toast.makeText(this, getString(R.string.erro_permissionfailed), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }
}
