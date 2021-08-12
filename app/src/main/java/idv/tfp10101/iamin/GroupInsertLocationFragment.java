package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupInsertLocationFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 此頁面所需要的權限
    private List<String> permissions = new ArrayList<>();
    // 定位供應器
    private FusedLocationProviderClient fusedLocationProviderClient;
    // 定位請求
    private LocationRequest locationRequest;
    // 緯度 經度 地址
    private double lat = -180;
    private double lng = -180;
    private String Address = "";
    private double[] lats, lngs;
    private double[] newLats, newLngs;
    private ArrayList<String> listAddress;
    // 地理轉換
    private Geocoder geocoder;
    // Google Map 的操作與設定
    private GoogleMap map;
    // 地圖標記
    List<Marker> markers;

    // 元件
    private SearchView searchViewLocation;
    private MapView mapViewGroup;
    private Button buttonGroupLocation;
    // 導航控制(頁面切換用)
    private NavController navController;
    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        // 取得定位供應器
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        navController = Navigation.findNavController(view);
        searchViewLocation = view.findViewById(R.id.searchViewLocation);
        mapViewGroup = view.findViewById(R.id.mapViewGroup);
        buttonGroupLocation = view.findViewById(R.id.buttonGroupLocation);
    }

    /**
     * 生命週期-2
     * 初始化與畫面無直接關係之資料 (設計: )
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 生命週期-3
     * 載入並建立Layout (設計: )
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 取得Activity參考
        activity = getActivity();
        activity.setTitle("新增取貨地址");
        // 取得Resources
        resources = getResources();
        // 返回鍵監聽
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressed);

        return inflater.inflate(R.layout.fragment_group_insert_location, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        // 每次都重置
        lats = null;
        lngs = null;
        newLats = null;
        newLngs = null;
        markers = new ArrayList<>();

        // Bundle 資料抓取
        Bundle bundle = getArguments();
        if (bundle != null) {
            // 緯度[]
            lats = bundle.getDoubleArray("lats");
            // 經度[]
            lngs = bundle.getDoubleArray("lngs");
            // 地址[]
            listAddress = bundle.getStringArrayList("locations");
        }else {
            listAddress = new ArrayList<>();
        }

        // 定位功能 (暫時沒時機可測試)
        handleFusedLocation();

        // Geocoder
        handleGeocoder();

        // SearchView 輸入地址後的相關處理
        handelSearchView();

        // Google Map 顯示
        handleGoogleMap(savedInstanceState);

        // 新增地址
        handleSubmit();
    }

    /**
     * 生命週期-6
     * 畫面即將顯示前 (設計: 詢問使用權限 - 危險權限要事先詢問)
     */
    @Override
    public void onStart() {
        super.onStart();
        // 此頁面所需要的權限
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        // 詢問使用權限
        Constants.requestPermissions(activity, permissions);
    }

    /**
     * 權限詢問後，返回的狀態判斷
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQ_POSITIONING) {
        }
    }

    /**
     * 定位功能 (進入點)
     */
    private void handleFusedLocation() {
        // 定位功能的檢查與設定
        checkPositioning();

        // 判斷是否有權限
        boolean isPermission;
        isPermission =
                (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)
                        &&
                        (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED);
        if (!isPermission) {
            return;
        }
        // Task<Location> (最後記錄位置 - 目前使用 概略定位 權限)
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        // 定位成功監聽器
        // 註冊/實作 定位成功監聽器
        task.addOnSuccessListener(location -> {
            if (location != null) {
                // 取得緯度
                lat = location.getLatitude();
                // 取得經度
                lng = location.getLongitude();

                //Toast.makeText(activity, "Lat: " + lat + " Lng: " + lng, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 定位功能檢查
     */
    private void checkPositioning() {
        // 檢查定位
        // 取得SettingsClient物件
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        // 檢查裝置是否開啟定位設定
        Task<LocationSettingsResponse> task =
                settingsClient.checkLocationSettings(getLocationSettingsRequest());
        // 註冊/實作 失敗監聽器: 若裝置未開啟定位，跳轉至定位設定的對話框
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    // 跳轉至定位設定的對話框
                    resolvable.startResolutionForResult(activity, Constants.REQ_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(Constants.TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 定位設定
     */
    private LocationSettingsRequest getLocationSettingsRequest() {
        // 定位請求物件
        // 建立
        locationRequest = LocationRequest.create();
        // 設定更新週期
        locationRequest.setInterval(10000);
        // 設定最快更新週期
        locationRequest.setFastestInterval(5000);
        // 設定優先順序
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 建立 定位設定 物件，並加入 定位請求 物件
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
    }

    /**
     * 輸入地址後的相關處理
     */
    private void handelSearchView() {
        searchViewLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                final Address address = nameToLatLng(query);
                if (address != null) {
                    // 儲存地址名稱
                    Address = query;
                    // 取得 緯經度
                    if (lat == address.getLatitude() && lng == address.getLongitude()) {
                        return false;
                    }
                    lat = address.getLatitude();
                    lng = address.getLongitude();
                    Toast.makeText(activity, "Lat: " + lat + " Lng: " + lng, Toast.LENGTH_SHORT).show();
                    // 地圖相機移動
                    cameraSetting();
                    // 加入標記
                    addMarker(lat, lng, Address);
                    return false;
                }else {
                    Address = "";
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    /**
     * Geocoder 相關處理
     */
    private void handleGeocoder() {
        // 判斷Geocoder是否可用
        boolean isPresent = Geocoder.isPresent();
        if (!isPresent) {
            return;
        }

        // 實例化Geocoder物件
        geocoder = new Geocoder(activity);
    }

    /**
     * 地名/地址 轉 緯經度
     * @param name 地名/地址
     */
    private Address nameToLatLng(final String name) {
        try {
            // 轉換
            List<Address> addressList = geocoder.getFromLocationName(name, 1);
            if (addressList != null && addressList.size() > 0) {
                return addressList.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Google Map
     */
    private void handleGoogleMap(Bundle bundle) {
        // 呼叫MapView.onCreate()與onStart()才可正常顯示地圖
        mapViewGroup.onCreate(bundle);
        mapViewGroup.onStart();

        mapViewGroup.getMapAsync(googleMap -> {
            // 抓取開啟的GoogleMap
            map = googleMap;
            // 標示先前所選擇的地址
            handleMarkers(lats, lngs);
            // 讓所有標記都能顯示在畫面上
            handleMarkersInView();
        });
    }

    /**
     * 設定地圖相機
     */
    private void cameraSetting() {
        // 相機移動到輸入的緯經度
        if (lat < -180 || lng < -180) {
            Toast.makeText(activity, R.string.textLocationNotFound, Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng latLng = new LatLng(lat, lng);
        // 設定相機的位置與更新
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(50)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        // 移動
        map.animateCamera(cameraUpdate);
    }

    /**
     * 加入標記
     */
    private void addMarker(double lat, double lng, String address) {
        LatLng position = new LatLng(lat, lng);
        Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .title("12345")
                .snippet(address));

        markers.add(marker);

        // 讓所有標記都能顯示在畫面上
        handleMarkersInView();
    }

    /**
     * 標示先前所選擇的地址
     * @param lats
     * @param lngs
     */
    private void handleMarkers(double[] lats, double[] lngs) {
        if (lats == null) {
            return;
        }
        for (int i = 0; i < lats.length; i++) {
            int number = i + 1;
            LatLng position = new LatLng(lats[i], lngs[i]);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title("地址：" + number)
                    .snippet(""));

            markers.add(marker);
        }
    }

    /**
     * 讓所有標記都能顯示在畫面上
     */
    private void handleMarkersInView() {
        if (markers.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100; // 以像素為單位從地圖邊緣偏移
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        map.animateCamera(cu);
    }

    /**
     * 新增地址
     */
    private void handleSubmit() {
        buttonGroupLocation.setOnClickListener(view -> {
            Bundle bundle = getArguments();
            // 如果地址錯誤 或是 沒有輸入
            if (Address.isEmpty()) {
                Toast.makeText(activity, "找不到地址無法新增", Toast.LENGTH_SHORT).show();
                return;
            }
            // 如果有之前的資料
            if (bundle != null) {
                // 準備回傳的資料 緯
                newLats = new double[lats.length + 1];
                for (int i = 0; i < lats.length; i++) {
                    newLats[i] = lats[i];
                }
                newLats[lats.length] = lat;
                // 準備回傳的資料 經
                newLngs = new double[lngs.length + 1];
                for (int i = 0; i < lngs.length; i++) {
                    newLngs[i] = lngs[i];
                }
                newLngs[lngs.length] = lng;
            }else {
                newLats = new double[1];
                newLats[0] = lat;
                newLngs = new double[1];
                newLngs[0] = lng;
            }
            // 準備回傳的資料 地址
            listAddress.add(Address);

            Bundle reBundle = new Bundle();
            reBundle.putDoubleArray("lats", newLats);
            reBundle.putDoubleArray("lngs", newLngs);
            reBundle.putStringArrayList("locations", listAddress);
            getParentFragmentManager().setFragmentResult("requestLocationKey", reBundle);

            navController.popBackStack();
        });
        // 長按直接輸入預設地址
        buttonGroupLocation.setOnLongClickListener(view -> {
            searchViewLocation.setQuery("104台北市中山區南京東路三段219號", false);
            return false;
        });
    }

    /**
     * 點擊返回鍵
     */
    private final OnBackPressedCallback backPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Bundle bundle = getArguments();
            // 如果有之前的資料
            if (bundle != null) {
                newLats = lats;
                newLngs = lngs;
            }else {
                newLats = null;
                newLngs = null;
                listAddress = null;
            }
            Bundle reBundle = new Bundle();
            reBundle.putDoubleArray("lats", newLats);
            reBundle.putDoubleArray("lngs", newLngs);
            reBundle.putStringArrayList("locations", listAddress);
            getParentFragmentManager().setFragmentResult("requestLocationKey", reBundle);

            navController.popBackStack();
        }
    };
}