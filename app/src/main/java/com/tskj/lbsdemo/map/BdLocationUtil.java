package com.tskj.lbsdemo.map;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tskj.lbsdemo.App;

/**
 * 百度地图定位工具类
 *
 * @author LiuFeng
 * @date 2017-7-22
 */
public class BdLocationUtil {

    /**
     * 单例
     *
     * @return
     */
    public static BdLocationUtil getInstance() {
        return LocationHolder.INSTANCE;
    }

    private static class LocationHolder {
        private static final BdLocationUtil INSTANCE = new BdLocationUtil();
    }

    private BdLocationUtil() {
    }

    /**
     * 定位回调的接口
     */
    public interface MyLocationListener {
        void myLocation(BDLocation location);
    }

    /**
     * 获取当前位置
     *
     * @param listener
     */
    public void requestLocation(final MyLocationListener listener) {
        //声明LocationClient类
        final LocationClient locationClient = new LocationClient(App.getContext());
        // 设置定位条件
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集

        locationClient.setLocOption(option);
        // 注册位置监听器
        locationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                listener.myLocation(location);
                locationClient.stop();
            }
        });
        locationClient.start();
        /*
         * 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation(
         * )后，每隔设定的时间，定位SDK就会进行一次定位。如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
         * 返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
         * 定时定位时，调用一次requestLocation，会定时监听到定位结果。
         */
        locationClient.requestLocation();
    }
}
