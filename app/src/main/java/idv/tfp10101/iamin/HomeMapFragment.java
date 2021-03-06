package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import idv.tfp10101.iamin.Data.HomeData;
import idv.tfp10101.iamin.Data.HomeDataControl;
import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.member.CustomMapView;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyLoadingBar;
import idv.tfp10101.iamin.merch.Merch;


public class HomeMapFragment extends Fragment {

    private Activity activity;
    private View view;
    private List<Marker> markers;
    private List<Group> localGroups;
    private List<HomeData> localHomeDatas;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int RQ_2 = 2;
    private static final String TAG = "TAG_Location";
    private double userlat,userlng;//?????????????????????
    private GoogleMap googleMap;
    private MapView mapView;
    private Button btn_serch;
    private EditText edt_scope;
    private float scope = 0f; //??????????????????
    private ProgressDialog loadingBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ??????Activity??????
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_home_map, container, false);
        activity.setTitle("????????????");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        //mapView
        mapView = view.findViewById(R.id.homemapView);
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        scope = 0f;
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            googleMap.clear();
            btn_serch.setOnClickListener(v ->{
                if (String.valueOf(edt_scope.getText()).isEmpty()){
                    Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingBar = new ProgressDialog(activity);
                loadingBar.setTitle("?????????");
                loadingBar.setMessage("?????????");
                loadingBar.show();

                scope = Float.parseFloat(String.valueOf(edt_scope.getText())) * 1000;
                googleMap.clear();

                //????????????(??????)
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(new LatLng(userlat,userlng));
                circleOptions.radius(scope);
                //????????????
                circleOptions.fillColor(Color.argb(50, 90, 90, 98));
                //????????????
                circleOptions.strokeColor(R.color.purple_200);
                googleMap.addCircle(circleOptions);
                getUserloaction();
            });
            //???????????????????????????????????????
            googleMap.setOnInfoWindowClickListener(marker -> {
                MyLoadingBar.setLoadingBar(activity,"????????????????????????","");
                edt_scope.setText("");
                Bundle bundle = new Bundle();
                bundle.putInt("GroupID", (int) marker.getTag());
                bundle.putDouble("Userlat",userlat);
                bundle.putDouble("Userlng",userlng);

                Navigation.findNavController(view).navigate(R.id.merchbrowseFragment, bundle);
            });
        });
        getUserloaction();
    }

    /**
     * ??????User???????????????
     */
    private void getUserloaction() {
        checkPositioning();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /**  ???????????? **/
        // 4. ???????????????????????????
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        // 5. ??????Task<Location>??????
        //??????????????????
        Task<Location> task = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken()
        );
        task.addOnSuccessListener(location -> {
            if (location != null) {
                //????????????
                userlat = location.getLatitude();
                //????????????
                userlng = location.getLongitude();
                mapView.getMapAsync(googleMap -> {
                    this.googleMap = googleMap;
                    UiSettings uiSettings = googleMap.getUiSettings();
                    uiSettings.setMyLocationButtonEnabled(true);
                    googleMap.setMyLocationEnabled(true);   //??????????????????(?????????) 
                    CameraPosition cameraPosition = new CameraPosition.Builder() .target(new LatLng(userlat,userlng))
                            .zoom(17)
                            .tilt(45) // ??????????????????
//                            .bearing(90) // ??????????????????
                            .build(); // ??????????????????
                    CameraUpdate cameraUpdate =
                            CameraUpdateFactory.newCameraPosition(cameraPosition);
                    googleMap.animateCamera(cameraUpdate);
                    coumputeDistancemin();
                });
            }
        });
    }

    /**
     * ????????????????????????????????????????????????Homedata(group,distancemin)?????????
     */
    private void coumputeDistancemin(){
        localHomeDatas =  new ArrayList<>();
        localGroups = new ArrayList<>();
        HomeDataControl.getAllGroup(activity);
        localGroups = HomeDataControl.getLocalGroups();
        if (localGroups == null || localGroups.isEmpty()) {
            Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        HomeData homeData;
        if (localGroups != null) {
            for (Group group : localGroups) {
                Map<Float, LatLng> groupMap = new TreeMap<>(
                        new Comparator<Float>() {
                            @Override
                            public int compare(Float o1, Float o2) {
                                if (o1 < o2) {
                                    return -1;
                                } else if (o1 > o2) {
                                    return 1;
                                }
                                return 0;
                            }
                        }
                );
                List<idv.tfp10101.iamin.location.Location> locations = group.getLocations();
                for (idv.tfp10101.iamin.location.Location grouplocation : locations) {
                    float[] results = new float[1];
                    //????????????????????????????????????
                    Double groupLat = grouplocation.getLatitude();
                    Double groupLng = grouplocation.getLongtitude();
                    //????????????????????????????????????????????????
                    android.location.Location.distanceBetween(userlat, userlng, groupLat, groupLng, results);
                    LatLng latLng = new LatLng(groupLat, groupLng);
                    //??????1000??????????????????????????????list
                    if (results != null) {
                        groupMap.put(results[0] / 1000, latLng);
                    }
                }
                float mindis = 0f;
                LatLng latLng = null;
                for (Map.Entry<Float, LatLng> entry : groupMap.entrySet()) {
                    mindis = entry.getKey();
                    latLng = entry.getValue();
                    if (mindis != 0 || latLng != null) {
                        break;
                    }
                }
                BigDecimal b = new BigDecimal(mindis);
                //??????????????????????????????
                float groupDismin = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                homeData = new HomeData(group, groupDismin, latLng);
                //????????????????????????????????? && ?????????????????????????????? ????????????????????????
                if (group.getProgress() != group.getConditionCount() && (new Date().before(group.getConditionTime()))) {
                    if (scope == 0){
                        localHomeDatas.add(homeData);
                    }else if(homeData.getDistance() <= scope/1000){
                        localHomeDatas.add(homeData);
                    }
                }
                mapView.getMapAsync(googleMap -> {

                    this.googleMap = googleMap;
                    double latitude, longtitude;
                    for (HomeData mapHomeData : localHomeDatas) {
                        latitude = mapHomeData.getLatLng().latitude;
                        longtitude = mapHomeData.getLatLng().longitude;
                        String groupName = mapHomeData.getGroup().getName();//??????????????????
                        int groupId = mapHomeData.getGroup().getGroupId(); //????????????id(????????????????????????)
                        float dis = mapHomeData.getDistance();

                        LatLng maplatLng = new LatLng(latitude, longtitude);
                        MarkerOptions mapmarker = new MarkerOptions().position(maplatLng)
                                .title(groupName)
                                .snippet("????????????????????????????????????" + dis + "??????")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapview_pin))
                                .draggable(false);
                        Marker marker = googleMap.addMarker(mapmarker);
                        marker.setTag(groupId);
                    }
                    if (loadingBar != null){
                        loadingBar.dismiss();
                    }
                });
            }
        }
    }

    private void findView(View view) {
        edt_scope = view.findViewById(R.id.edt_scope);
        btn_serch = view.findViewById(R.id.btn_serch);
    }

    /**
     * ??????????????????
     */
    private void checkPositioning() {
        // 9. ????????????
        // 9.1 ??????SettingsClient??????
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        // 9.2 ????????????????????????????????????
        Task<LocationSettingsResponse> task =
                settingsClient.checkLocationSettings(getLocationSettingsRequest());
        // 9.3 ??????/?????? ???????????????: ????????????????????????????????????????????????????????????
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    // ?????????????????????????????????
                    resolvable.startResolutionForResult(activity, RQ_2);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * ????????????
     */
    private LocationSettingsRequest getLocationSettingsRequest() {
        // 7. ??????????????????
        // 7.1 ??????
        locationRequest = LocationRequest.create();
        // 7.2 ??????????????????
        locationRequest.setInterval(10000);
        // 7.3 ????????????????????????
        locationRequest.setFastestInterval(3000);
        // 7.4 ??????????????????
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        locationRequest.setNumUpdates(2);

        // 8. ??????????????????????????????????????????7???????????????????????????
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
    }


}