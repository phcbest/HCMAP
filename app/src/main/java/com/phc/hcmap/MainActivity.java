package com.phc.hcmap;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements AMapLocationListener {

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
        onEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void onEvent() {
        //请求定位权限
        String[] mPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
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
        buttonPlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //定位两个位置

            }
        });
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
}