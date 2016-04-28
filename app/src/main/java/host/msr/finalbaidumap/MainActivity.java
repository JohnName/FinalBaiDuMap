package host.msr.finalbaidumap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import host.msr.finalbaidumap.overlayutil.OverlayManager;

public class MainActivity extends Activity implements BDLocationListener, OnGetPoiSearchResultListener {

    private Button request;
    private MapView mapView;
    private BaiduMap baiduMap;
    private TextView totalBank;
    private LocationClient locationClient = null;
    private double latitude;
    private double longitude;
    private String city;
    private PoiSearch poiSearch;
    private List<PoiInfo> lists;
    private ListView listView;
    private View view= null; //判断气泡是否已经创建
    private MyAdapter myAdapter ;
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 111:
                    LatLng latLng = new LatLng(latitude, longitude);
                    initMap(latLng);
                    nearbySearch(0);
                    locationClient.stop();//定位结束后关闭
                    break;
                case 000:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.nearmap);
        listView = (ListView) findViewById(R.id.displaylist);
        totalBank = (TextView) findViewById(R.id.nearatm);
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(this);
        mapView.showZoomControls(false);
        mapView.showScaleControl(false);
        mapView.removeViewAt(1);
        baiduMap = mapView.getMap();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(this);
        initLocation();
        //手动定位按钮
        request = (Button) findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view != null){
                    mapView.removeView(view);
                }
                locationClient.start();
            }
        });
    }

    /**
     * 启动的时候初始化地图
     */
    private void initMap(LatLng latLng) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(latLng)
                .zoom(18)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        baiduMap.setMapStatus(mMapStatusUpdate);
    }

    /**
     * 设置定位规则
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);
        locationClient.start();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location == null) {
            return;
        } else if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation || location.getLocType() == BDLocation.TypeOffLineLocation) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            city = location.getCity();
            mHandler.sendEmptyMessage(111);
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            //"服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因"
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            //"网络不同导致定位失败，请检查网络是否通畅"
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            //"无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机"
        }
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        //没有检索到结果
        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MainActivity.this, "这附近没有银行", Toast.LENGTH_LONG).show();
        }
        //结果正常返回
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {

            lists = poiResult.getAllPoi();
            myAdapter = new MyAdapter(getApplicationContext(),lists);
            listView.setAdapter(myAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View contentView, int position, long id) {
                    if (view == null){
                        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.map_item,null);
                        TextView tv = (TextView) view.findViewById(R.id.my_postion);
                        tv.setText(lists.get(position).name);
                        tv.setTextColor(Color.RED);
                        MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
                        MapViewLayoutParams params = builder.position(lists.get(position).location).width(WindowManager.LayoutParams.WRAP_CONTENT).height(WindowManager.LayoutParams.WRAP_CONTENT).layoutMode(MapViewLayoutParams.ELayoutMode.mapMode).build();
                        mapView.addView(view, params);
                    }else {
                        mapView.removeView(view);
                        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.map_item,null);
                        TextView tv = (TextView) view.findViewById(R.id.my_postion);
                        tv.setText(lists.get(position).name);
                        tv.setTextColor(Color.RED);
                        MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
                        MapViewLayoutParams params = builder.position(lists.get(position).location).width(WindowManager.LayoutParams.WRAP_CONTENT).height(WindowManager.LayoutParams.WRAP_CONTENT).layoutMode(MapViewLayoutParams.ELayoutMode.mapMode).build();
                        mapView.addView(view, params);
                    }
                    initMap(lists.get(position).location);
                    Intent intent = new Intent(MainActivity.this,Detail.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putDouble("latitude",lists.get(position).location.latitude);
//                    bundle.putDouble("longitude",lists.get(position).location.longitude);
//                    bundle.putString("name",lists.get(position).name);
//                    bundle.putString("address",lists.get(position).address);
//                    bundle.putString("phoneNum",lists.get(position).phoneNum);
                    startActivity(intent);
                    Log.i("bbbbbbbbb",lists.get(position).name + " ");
                    Log.i("bbbbbbbbb",lists.get(position).phoneNum + " ");
                    Log.i("bbbbbbbbb",lists.get(position).address + " ");
                    Log.i("bbbbbbbbb",lists.get(position).city + " ");
                    Log.i("bbbbbbbbb",lists.get(position).uid + " ");
                    Log.i("bbbbbbbbb",lists.get(position).hasCaterDetails + " ");
                    Log.i("bbbbbbbbb",lists.get(position).isPano + " ");
                    Log.i("bbbbbbbbb",lists.get(position).type + " ");
                    Log.i("bbbbbbbbb",lists.get(position).location.latitude + " ");
                }
            });
            totalBank.setText("周边总共有" + poiResult.getAllPoi().size() + "个网点信息");
            baiduMap.clear();
            MyPoiOverlay myPoiOverlay = new MyPoiOverlay(baiduMap);
            myPoiOverlay.setData(poiResult, latitude, longitude);
            baiduMap.setOnMarkerClickListener(myPoiOverlay);
            myPoiOverlay.addToMap();// 将所有的overlay添加到地图上
            myPoiOverlay.zoomToSpan();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "银行",
                    Toast.LENGTH_SHORT).show();
        } else {// 正常返回结果的时候，此处可以获得很多相关信息
            if (view == null){
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.map_item,null);
                TextView tv = (TextView) view.findViewById(R.id.my_postion);
                tv.setText(poiDetailResult.getName());
                tv.setTextColor(Color.RED);
                MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
                MapViewLayoutParams params = builder.position(poiDetailResult.getLocation()).width(WindowManager.LayoutParams.WRAP_CONTENT).height(WindowManager.LayoutParams.WRAP_CONTENT).layoutMode(MapViewLayoutParams.ELayoutMode.mapMode).build();
                mapView.addView(view, params);
                //设置点击mark，listview滚动到相应的网点
                myAdapter.notifyDataSetChanged();
            }else {
                mapView.removeView(view);
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.map_item,null);
                TextView tv = (TextView) view.findViewById(R.id.my_postion);
                tv.setText(poiDetailResult.getName());
                tv.setTextColor(Color.RED);
                MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
                MapViewLayoutParams params = builder.position(poiDetailResult.getLocation()).width(WindowManager.LayoutParams.WRAP_CONTENT).height(WindowManager.LayoutParams.WRAP_CONTENT).layoutMode(MapViewLayoutParams.ELayoutMode.mapMode).build();
                mapView.addView(view, params);
            }
            initMap(poiDetailResult.getLocation());
            Toast.makeText(
                    MainActivity.this,
                    poiDetailResult.getName() + ": "
                            + poiDetailResult.getAddress(),
                    Toast.LENGTH_LONG).show();
        }
    }
//    //这个只能显示10个信息
//    class MyPoiOverlay extends PoiOverlay {
//        public MyPoiOverlay(BaiduMap baiduMap) {
//            super(baiduMap);
//        }
//
//        @Override
//        public boolean onPoiClick(int arg0) {
//            super.onPoiClick(arg0);
//            PoiInfo poiInfo = getPoiResult().getAllPoi().get(arg0);
//            poiSearch.searchPoiDetail(new PoiDetailSearchOption()
//                    .poiUid(poiInfo.uid));
//            return true;
//        }
//    }

    /**
     * 范围检索
     */
    private void boundSearch() {
        PoiBoundSearchOption boundSearchOption = new PoiBoundSearchOption();
        LatLng southwest = new LatLng(latitude - 0.01, longitude - 0.012);// 西南
        LatLng northeast = new LatLng(latitude + 0.01, longitude + 0.012);// 东北
        LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
                .include(northeast).build();// 得到一个地理范围对象
        boundSearchOption.bound(bounds);// 设置poi检索范围
        boundSearchOption.keyword("银行");// 检索关键字
        boundSearchOption.pageCapacity(20);
        poiSearch.searchInBound(boundSearchOption);// 发起poi范围检索请求
    }

    /**
     * 城市内搜索
     */
    private void citySearch(int page) {
        // 设置检索参数
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
        citySearchOption.city("成都");// 城市
        citySearchOption.keyword("银行");// 关键字
        citySearchOption.pageCapacity(30);// 默认每页10条
        citySearchOption.pageNum(page);// 分页编号
        // 发起检索请求
        poiSearch.searchInCity(citySearchOption);
    }

    /**
     * 附近检索
     */
    private void nearbySearch(int page) {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(new LatLng(latitude, longitude));
        nearbySearchOption.keyword("交通银行");
        nearbySearchOption.radius(10000);// 检索半径，单位是米
        nearbySearchOption.pageCapacity(50);
        nearbySearchOption.pageNum(page);
        poiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
    }

    /**
     * 覆盖物
     */
    private class MyPoiOverlay extends OverlayManager {
        private PoiResult poiResult = null;
        private double latitude;
        private double longitude;

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        public void setData(PoiResult poiResult, double latitude, double longitude) {
            this.poiResult = poiResult;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (marker.getExtraInfo() != null) {
                int index = marker.getExtraInfo().getInt("index");
                PoiInfo poi = poiResult.getAllPoi().get(index);
                setMarkOcliked(index + 1);
                // 详情搜索
                poiSearch.searchPoiDetail((new PoiDetailSearchOption())
                        .poiUid(poi.uid));
                return true;
            }
            return false;
        }

        @Override
        public List<OverlayOptions> getOverlayOptions() {
            if ((this.poiResult == null)
                    || (this.poiResult.getAllPoi() == null))
                return null;
            ArrayList<OverlayOptions> arrayList = new ArrayList<OverlayOptions>();
            Log.i("aaaaaaaaaa", this.poiResult.getAllPoi().size() + " ");
            for (int i = 0; i < this.poiResult.getAllPoi().size(); i++) {
                if (this.poiResult.getAllPoi().get(i).location == null)
                    continue;
                // 给marker加上标签
                Bundle bundle = new Bundle();
                bundle.putInt("index", i);
                arrayList.add(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(setNumToIcon(i + 1))).extraInfo(bundle)
                        .position(this.poiResult.getAllPoi().get(i).location));
            }

            arrayList.add(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_mark)).position(new LatLng(latitude, longitude)));
            return arrayList;
        }

        /**
         * 当选中的时候变化
         */
        private Bitmap setMarkOcliked(int num) {
            BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(
                    R.mipmap.icon_focus_mark);
            Bitmap bitmap = bd.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            int widthX;
            int heightY = 0;
            if (num < 10) {
                paint.setTextSize(30);
                widthX = 8;
                heightY = 6;
            } else {
                paint.setTextSize(20);
                widthX = 11;
            }

            canvas.drawText(String.valueOf(num),
                    ((bitmap.getWidth() / 2) - widthX),
                    ((bitmap.getHeight() / 2) + heightY), paint);
            return bitmap;
        }

        /**
         * 往图片添加数字
         */
        private Bitmap setNumToIcon(int num) {
            BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(
                    R.mipmap.icon_mark);
            Bitmap bitmap = bd.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            int widthX;
            int heightY = 0;
            if (num < 10) {
                paint.setTextSize(30);
                widthX = 8;
                heightY = 6;
            } else {
                paint.setTextSize(20);
                widthX = 11;
            }

            canvas.drawText(String.valueOf(num),
                    ((bitmap.getWidth() / 2) - widthX),
                    ((bitmap.getHeight() / 2) + heightY), paint);
            return bitmap;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            return false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        poiSearch.destroy();// 释放poi检索对象
        mapView.onDestroy();
    }
}
