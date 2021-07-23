package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.number.Precision;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.Empty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10101.iamin.Data.HomeData;
import idv.tfp10101.iamin.Data.HomeDataControl;
import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;
import idv.tfp10101.iamin.network.RemoteAccess;

import static android.content.Context.MODE_PRIVATE;
import static android.media.CamcorderProfile.get;
import static idv.tfp10101.iamin.Constants.FCM_Token;


public class HomeFragment extends Fragment {
    private Activity activity;
    private View view;
    private BottomNavigationView bottomNavigationView;
    private ExecutorService executor;
    private RecyclerView recyclerViewGroup;
    private List<Group> localGroups;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private Member member;
    private static final int RQ_2 = 2;
    private static final String TAG = "TAG_Location";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double userlat,userlng;//使用者的緯經度
    private List<HomeData> localHomeDatas;  //把團購跟使用者最短距離裝成Homedata


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 需要開啟多個執行緒取得圖片，使用執行緒池功能
        int numProcs = Runtime.getRuntime().availableProcessors();
        Log.d("TAG", "JVM可用的處理器數量: " + numProcs);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executor = Executors.newFixedThreadPool(numProcs);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 取得Activity參考
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    /*
    取得會員資料
     */
    @Override
    public void onStart() {
        super.onStart();

        member = MemberControl.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //先前有登入就取會員資料
        if (currentUser != null && member.getuUId() == null) {

            SharedPreferences sharedPreferences = activity.getSharedPreferences(FCM_Token, MODE_PRIVATE);
            String token = sharedPreferences.getString(FCM_Token, "");
            member.setFCM_token(token);
            member.setuUId(currentUser.getUid());
            //tmp
            MemberControl.memberRemoteAccess(activity, member, "updateTokenbyUid");
            String jsonMember = MemberControl.memberRemoteAccess(activity, member, "findbyUuid");
            member = new Gson().fromJson(jsonMember, Member.class);
            MemberControl.setMember(member);
            Log.d("TAG_HOME", "Fetch Member Date Complete");
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        // 3. 詢問使用權限
        requestPermissions();

        HomeDataControl.getAllGroup(activity);
        localGroups = HomeDataControl.getLocalGroups();
        if (localGroups == null || localGroups.isEmpty()) {
            Toast.makeText(activity, "找不到團購", Toast.LENGTH_SHORT).show();
        }

        //實作取得買家緯精度方法
        getUserloaction();

//        showGroup(localGroups);
        //輸入監聽
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Group> searchGroup = new ArrayList<>();
                if (newText.equals("")) {
                    showGroup(localGroups);
                } else {
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Group group : localGroups) {
                        if (group.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchGroup.add(group);
                        }
                    }
                    showGroup(searchGroup);
                }
                return true;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //開啟動畫
            swipeRefreshLayout.setRefreshing(true);
            showGroup(localGroups);
            searchView.setQuery("", false);
            swipeRefreshLayout.setRefreshing(false);
        });

        //bottomNavigationView.getMenu().setGroupCheckable(0,false,false);
        //分類Bar監聽
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            //bottombar監聽事件
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.no:
                        searchView.setQuery("", false);
                        showGroup(localGroups);
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                List<Group> searchGroup = new ArrayList<>();
                                if (newText.equals("")) {
                                    showGroup(localGroups);
                                } else {
                                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                                    for (Group group : localGroups) {
                                        //只顯示團購狀態是1的(1-> 揪團中)
                                        if (group.getName().toUpperCase().contains(newText.toUpperCase())) {
                                            searchGroup.add(group);
                                        }
                                    }
                                    showGroup(searchGroup);
                                }
                                return true;
                            }
                        });
                        swipeRefreshLayout.setOnRefreshListener(() -> {
                            //開啟動畫
                            swipeRefreshLayout.setRefreshing(true);
                            showGroup(localGroups);
                            searchView.setQuery("", false);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                        Toast.makeText(activity, "未分類", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.food:
                        choosesort(1, localGroups);
                        Toast.makeText(activity, "美食", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.life:
                        choosesort(2, localGroups);
                        Toast.makeText(activity, "生活用品", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.theerc:
                        choosesort(3, localGroups);
                        Toast.makeText(activity, "3C", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.other:
                        choosesort(4, localGroups);
                        Toast.makeText(activity, "其他", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }
    //取得User的當前位置
    private void getUserloaction() {
        checkPositioning();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /**  取得位置 **/
        // 4. 取得定位供應器物件
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        // 5. 取得Task<Location>物件
        //取得最後位置
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                //取得緯度
                userlat = location.getLatitude();
                //取得經度
                userlng = location.getLongitude();
                showGroup(localGroups);
                coumputeDistancemin();
            }
        });
    }

    //計算使用者與團購的最短距離打包成Homedata(group,distancemin)並排序
    private void coumputeDistancemin(){

        localHomeDatas =  new ArrayList<>();
        HomeData homeData;
        for (Group group: localGroups){
            List<Float> distance = new ArrayList<>();
            List<Location> locations = group.getLocations();
            for (Location grouplocation : locations){
                float[] results = new float[1];
                //取得所有面交地點的緯經度
                Double groupLat = grouplocation.getLatitude();
                Double groupLng = grouplocation.getLongtitude();
                //取得買家與所有團購面交地點的距離
                android.location.Location.distanceBetween(userlat,userlng,groupLat,groupLng,results);
                //除以1000從公尺變成公里後加入list
                distance.add(results[0]/1000);
            }
            //由小到大排序(只取最近的距離)
            Collections.sort(distance);
            BigDecimal b = new BigDecimal(distance.get(0));
            //四捨五入到小數第一位
            float groupDismin = b.setScale(0,BigDecimal.ROUND_HALF_UP).floatValue();
            homeData = new HomeData(group,groupDismin);
            localHomeDatas.add(homeData);
        }
        //將Homedata用使用者與團購的最短距離排序
        Collections.sort(localHomeDatas, new Comparator<HomeData>() {
            @Override
            public int compare(HomeData o1, HomeData o2) {
                return (int) (o1.getDistance()-o2.getDistance());
            }
        });
    }

    //根據所選的分類去搜尋並可以下拉更新
    private void choosesort(int category_Id, List<Group> categoryGroup) {
        searchView.setQuery("", false);
        List<Group> selectGroup = new ArrayList<>();
        for (Group category : categoryGroup) {
            if (category.getCategoryId() == category_Id) {
                selectGroup.add(category);
            }
        }
        List<Group> searchGroup = new ArrayList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")||newText.isEmpty()) {
                    showGroup(searchGroup);
                } else {
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Group group : searchGroup) {
                        if (group.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchGroup.add(group);
                        }
                    }
                    showGroup(searchGroup);
                }
                return true;
            }
        });
        showGroup(selectGroup);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            searchView.setQuery("", false);
            showGroup(selectGroup);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showGroup(List<Group> localGroups) {
        /** RecyclerView */
        // 檢查
        HomeFragment.HomeAdapter groupAdapter = (HomeFragment.HomeAdapter) recyclerViewGroup.getAdapter();
        if (groupAdapter == null) {
            recyclerViewGroup.setAdapter(new HomeFragment.HomeAdapter(activity, localGroups));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewGroup.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        } else {
            // 資訊重新載入刷新
            groupAdapter.setGroups(localGroups);
            groupAdapter.notifyDataSetChanged();
        }
    }

    private void findView(View view) {
        bottomNavigationView = view.findViewById(R.id.nv_bar);
        recyclerViewGroup = view.findViewById(R.id.rv_groups);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(activity));
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        searchView = view.findViewById(R.id.searchview);
    }

    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyHomeDataViewHolder> {
        private List<Group> rsGroups;
        private LayoutInflater layoutInflater;
        private final int imageSize;

        public HomeAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            rsGroups = groups;

            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        public class MyHomeDataViewHolder extends RecyclerView.ViewHolder {
            TextView txv_group_name, txv_group_conditionTime, txv_progress,txv_distanceMin;
            ImageView imv_group;
            ProgressBar pr_bar;

            public MyHomeDataViewHolder(@NonNull View itemView) {
                super(itemView);
                txv_group_name = itemView.findViewById(R.id.txv_group_name);
                txv_group_conditionTime = itemView.findViewById(R.id.txv_group_conditionTime);
                txv_progress = itemView.findViewById(R.id.txv_progress);
                txv_distanceMin = itemView.findViewById(R.id.txv_distanceMin);
                imv_group = itemView.findViewById(R.id.imv_group);
                pr_bar = itemView.findViewById(R.id.pr_bar);
            }
        }

        public void setGroups(List<Group> Groups) {
            rsGroups = Groups;
        }

        @NonNull
        @Override
        public MyHomeDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_group_buyer, parent, false);
            return new HomeFragment.HomeAdapter.MyHomeDataViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.HomeAdapter.MyHomeDataViewHolder holder, int position) {
            final Group rsGroup = rsGroups.get(position);
            int GroupID = rsGroup.getGroupId();
            List<Location> grouplocations = new ArrayList<>();
            Bitmap Groupbitmap = HomeDataControl.getGroupimage(activity, GroupID, imageSize, executor);
            if (Groupbitmap != null) {
                holder.imv_group.setImageBitmap(Groupbitmap);
            } else {
                holder.imv_group.setImageResource(R.drawable.no_image);
            }
            holder.txv_group_name.setText(rsGroup.getName());
            Timestamp ts = rsGroup.getConditionTime();
            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            holder.txv_group_conditionTime.setText("結單日期:" + "\n" + sdf.format(ts));
            holder.pr_bar.setMax(rsGroup.getGoal());
            holder.pr_bar.setProgress(rsGroup.getProgress());
            holder.txv_progress.setText("(" + String.valueOf(rsGroup.getProgress()) + "/" + String.valueOf(rsGroup.getGoal()) + ")");
            //取的團購的所有地點
            grouplocations = LocationControl.getLocationByGroupId(activity, GroupID);
            int locations = grouplocations.size();
            List<Float> distance = new ArrayList<>();
            for (Location location : grouplocations){
                float[] results = new float[1];
                //取得所有面交地點的緯經度
                Double groupLat = location.getLatitude();
                Double groupLng = location.getLongtitude();
                //取得買家與所有團購面交地點的距離
                android.location.Location.distanceBetween(userlat,userlng,groupLat,groupLng,results);
                //除以1000從公尺變成公里後加入list
                distance.add(results[0]/1000);
            }
            //由小到大排序(只取最近的距離)
            Collections.sort(distance);
            BigDecimal b = new BigDecimal(distance.get(0));
            //四捨五入到小數第一位
            float groupDismin = b.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
            holder.txv_distanceMin.setText("距離您"+String.valueOf(groupDismin)+"公里");

            //設定點擊商品觸發
            holder.itemView.setOnClickListener(v -> {
                // Toast.makeText(activity, String.valueOf(id), Toast.LENGTH_SHORT).show();

                HashMap<String, Object> GrouphashMap = new HashMap<>();
                GrouphashMap.put("GroupID", rsGroup.getGroupId());//打包團購ID
                GrouphashMap.put("SellerID", rsGroup.getMemberId());//打包團購發起人ID
                GrouphashMap.put("Progress", rsGroup.getProgress());//打包團購進度
                GrouphashMap.put("Goal", rsGroup.getGoal());//打包團購目標
                GrouphashMap.put("Contact_Number", rsGroup.getContactNumber());//打包團購聯絡電話
                GrouphashMap.put("Payment_Method", rsGroup.getPaymentMethod());//打包付款方式
                GrouphashMap.put("Group_status", rsGroup.getGroupStatus());//打包團購狀態
                GrouphashMap.put("Caution", rsGroup.getCaution());//打包注意事項
                GrouphashMap.put("Condition_count", rsGroup.getConditionCount());//打包停單份數
                GrouphashMap.put("Condition_Time", rsGroup.getConditionTime());//打包停單時間
                Bundle bundleMap = new Bundle();
                bundleMap.putSerializable("Group", GrouphashMap);

                Navigation.findNavController(v).navigate(R.id.merchbrowseFragment, bundleMap);
            });
        }

        @Override
        public int getItemCount() {
            return rsGroups == null ? 0 : rsGroups.size();
        }
    }

    /**
     * 3. 詢問使用權限
     */
    private void requestPermissions() {
        final int result = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RQ_2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RQ_2) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserloaction();
                    Toast.makeText(activity, "開始使用定位功能", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "功能無法使用", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 定位功能檢查
     */
    private void checkPositioning() {
        // 9. 檢查定位
        // 9.1 取得SettingsClient物件
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        // 9.2 檢查裝置是否開啟定位設定
        Task<LocationSettingsResponse> task =
                settingsClient.checkLocationSettings(getLocationSettingsRequest());
        // 9.3 註冊/實作 失敗監聽器: 若裝置未開啟定位，跳轉至定位設定的對話框
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    // 跳轉至定位設定的對話框
                    resolvable.startResolutionForResult(activity, RQ_2);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 定位設定
     */
    private LocationSettingsRequest getLocationSettingsRequest() {
        // 7. 定位請求物件
        // 7.1 建立
        locationRequest = LocationRequest.create();
        // 7.2 設定更新週期
        locationRequest.setInterval(10000);
        // 7.3 設定最快更新週期
        locationRequest.setFastestInterval(1000);
        // 7.4 設定優先順序
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 8. 建立定位設定物件，並加入步驟7建立的定位請求物件
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
    }

    /**
     * 定期更新
     */
    private void intervalPositioning() {
        // 10. 定期更新定位
        // 10.1 實例化/實作 LocationCallback物件
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                /** 取得定位資訊 **/
                // 取得Location物件
                final android.location.Location location = locationResult.getLastLocation();
                // 取得緯度
                userlat = location.getLatitude();
                // 取得經度
                userlng = location.getLongitude();
                // 取得定位時間
                final long time = location.getTime();
            }
        };

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 10.2 請求定位更新
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

}