package com.phc.hcmap;

import android.Manifest;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.phc.hcmap.pojo.LatLngPojo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements AMapLocationListener, RouteSearch.OnRouteSearchListener {

    private static final String TAG = "MAIN";
    private Toolbar toolbar;
    private MapView mapView;
    private MaterialButton buttonFind;
    private MaterialButton buttonPlanning;
    private AMapLocationClient mAMapLocationClient;
    private AMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        toolbar.setTitle("HCMAP");
        toolbar.setTitleTextColor(0xffffffff);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
        try {
            onEvent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void onEvent() throws IOException {
        //请求定位权限
        String[] mPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
        EasyPermissions.requestPermissions(this, "请给予定位权限,否则app无法正常运行", 1, mPermissions);

        //
        mAMapLocationClient = new AMapLocationClient(this);
        AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
        mAMapLocationClient.setLocationListener(this);
        aMapLocationClientOption.setLocationMode(
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setInterval(2000);
        mAMapLocationClient.setLocationOption(aMapLocationClientOption);

        buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAMapLocationClient.startLocation();
            }
        });
        //解析json文件
        AssetManager am = getAssets();
        InputStream is = am.open("latlng.json");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        output.close();
        is.close();
        List<LatLngPojo> latLngPojos = new Gson().fromJson(output.toString(),
                new TypeToken<List<LatLngPojo>>() {
                }.getType());
        buttonPlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //让用户选择位置
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请选择开始地与目的地");
                View view = MainActivity.this.getLayoutInflater()
                        .inflate(R.layout.dialog_select, null);

                Spinner spinnerStart = (Spinner) view.findViewById(R.id.spinner_start);
                Spinner spinnerEnd = (Spinner) view.findViewById(R.id.spinner_end);
                MaterialButton btn = (MaterialButton) view.findViewById(R.id.btn);

                ArrayList<String> address = new ArrayList<>();
                for (LatLngPojo latLngPojo : latLngPojos) {
                    address.add(latLngPojo.getCountry() +
                            latLngPojo.getProvince() +
                            latLngPojo.getCity() +
                            latLngPojo.getArea());
                }
                spinnerStart.setAdapter(new ArrayAdapter<String>(
                        v.getContext(), android.R.layout.simple_list_item_1,
                        android.R.id.text1, address));

                spinnerEnd.setAdapter(new ArrayAdapter<String>(
                        v.getContext(), android.R.layout.simple_list_item_1,
                        android.R.id.text1, address));

                builder.setView(view);

                AlertDialog show = builder.show();
                btn.setOnClickListener(v1 -> {
                    //规划路线
                    double sLat = Double.parseDouble(latLngPojos.get(
                            spinnerStart.getSelectedItemPosition()).getLat());
                    double sLng = Double.parseDouble(latLngPojos.get(
                            spinnerStart.getSelectedItemPosition()).getLng());

                    double eLat = Double.parseDouble(latLngPojos.get(
                            spinnerEnd.getSelectedItemPosition()).getLat());
                    double eLng = Double.parseDouble(latLngPojos.get(
                            spinnerEnd.getSelectedItemPosition()).getLng());
                    LatLonPoint latLonPoint = new LatLonPoint(sLat, sLng);
                    drawLines(v, sLat, sLng, latLonPoint, eLat, eLng);
                    show.cancel();
                });
            }
        });
    }

    private void drawLines(View v, double sLat, double sLng, LatLonPoint latLonPoint, double eLat, double eLng) {
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                latLonPoint,
                new LatLonPoint(eLat, eLng)
        );
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(sLat, sLng),
                        18, 30, 30)));
        RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(
                fromAndTo,
                RouteSearch.DrivingDefault,
                null,
                null,
                ""
        );
        RouteSearch routeSearch = new RouteSearch(v.getContext());
        routeSearch.setRouteSearchListener(MainActivity.this);
        routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
    }


    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapView = (MapView) findViewById(R.id.map_view);
        buttonFind = (MaterialButton) findViewById(R.id.button_find);
        buttonPlanning = (MaterialButton) findViewById(R.id.button_planning);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                double latitude = amapLocation.getLatitude();
                double longitude = amapLocation.getLongitude();
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.CHINA);
                Date date = new Date(amapLocation.getTime());
                //定位时间
                df.format(date);
                Toast.makeText(this, "当前定位点:维度" + latitude + "经度" + longitude,
                        Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(new LatLng(latitude, longitude),
                                18, 30, 30)));
                mAMapLocationClient.stopLocation();
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        mMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            Toast.makeText(this, "路线规划成功", Toast.LENGTH_SHORT).show();
            List<DrivePath> paths = result.getPaths();
            PolylineOptions options = (new PolylineOptions()).width(10)
                    .color(0xff66cccc);
            for (DriveStep step : paths.get(0).getSteps()) {
                for (LatLonPoint latLonPoint : step.getPolyline()) {
                    options.add(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()));
                }
            }
            mMap.addPolyline(options);
        } else {
            Toast.makeText(this, "路线规划失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }
}