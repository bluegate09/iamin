package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import idv.tfp10101.iamin.member.MyLoadingBar;
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
    private double userlat,userlng;//?????????????????????
    private List<HomeData> localHomeDatas;  //???????????????????????????????????????Homedata
    private ProgressDialog Loading;
    private ImageView imv_locate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chat token???????????????
        getTokenSendServer();
        //
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
        // ??????????????????????????????????????????????????????????????????
        int numProcs = Runtime.getRuntime().availableProcessors();
        Log.d("TAG", "JVM????????????????????????: " + numProcs);
        // ????????????????????????????????????????????????????????????????????????????????????
        executor = Executors.newFixedThreadPool(numProcs);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ??????Activity??????
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_home, container, false);
        activity.setTitle("??????");
        return view;
    }

    /*
    ??????????????????
     */
    @Override
    public void onStart() {
        super.onStart();

        member = MemberControl.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //?????????????????????????????????
        if (currentUser != null) {

            NavController navController = Navigation.findNavController(view);
            //??????????????????????????????
            if (member.getDeleteTime() != null){
                //friebase,Google??????
                FirebaseAuth.getInstance().signOut();
                //fb??????
                LoginManager.getInstance().logOut();

                AlertDialog.Builder report = new AlertDialog.Builder(activity);
                report.setTitle("????????????????????????")
                        .setMessage("?????????????????????")
                        .setPositiveButton("????????????", (dialog, which) -> {
                            navController.navigate(R.id.logInFragment);
                        })
                        .setNegativeButton("????????????", (dialog, which) -> {
                            navController.navigate(R.id.signUpFragment);
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        searchView.setQuery("", false);
        // 3. ??????????????????
        requestPermissions();
        HomeDataControl.getAllGroup(activity);
        localGroups = HomeDataControl.getLocalGroups();
        if (localGroups == null || localGroups.isEmpty()) {
            Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        //?????????????????????
        Loading = new ProgressDialog(activity);
        Loading.setTitle("?????????");
        Loading.setMessage("Loading...");
        Loading.setCancelable(false);
        Loading.show();
        imv_locate.setOnClickListener(v ->{
            Navigation.findNavController(v).navigate(R.id.homeMapFragment);
        });
        //?????????????????????????????????
        getUserloaction();

        //???searchView??????
        searchView.setQuery("", false);
        //????????????
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<HomeData> searchHomeData = new ArrayList<>();
                if (newText.equals("")) {
                    showGroup(localHomeDatas);
                } else {
                    // ??????????????????????????????????????????(??????????????????)
                    for (HomeData group : localHomeDatas) {
                        if (group.getGroup().getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchHomeData.add(group);
                        }
                    }
                    showGroup(searchHomeData);
                }
                return true;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //????????????
            swipeRefreshLayout.setRefreshing(true);
            coumputeDistancemin();
            showGroup(localHomeDatas);
            searchView.setQuery("", false);
            swipeRefreshLayout.setRefreshing(false);
        });

        //bottomNavigationView.getMenu().setGroupCheckable(0,false,false);
        //??????Bar??????
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            //bottombar????????????
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.no:
                        searchView.setQuery("", false);
                        showGroup(localHomeDatas);
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                List<HomeData> searchHomeData = new ArrayList<>();
                                if (newText.equals("")) {
                                    showGroup(localHomeDatas);
                                } else {
                                    // ??????????????????????????????????????????(??????????????????)
                                    for (HomeData group : localHomeDatas) {
                                        //????????? ?????????????????????????????????????????? && ?????????????????????????????????????????? && ?????????????????????????????????
                                        if (group.getGroup().getName().toUpperCase().contains(newText.toUpperCase())) {
                                            searchHomeData.add(group);
                                        }
                                    }
                                    showGroup(searchHomeData);
                                }
                                return true;
                            }
                        });
                        swipeRefreshLayout.setOnRefreshListener(() -> {
                            //????????????
                            swipeRefreshLayout.setRefreshing(true);
                            coumputeDistancemin();
                            showGroup(localHomeDatas);
                            searchView.setQuery("", false);
                            swipeRefreshLayout.setRefreshing(false);
                        });
//                        Toast.makeText(activity, "?????????", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.food:
                        choosesort(1, localHomeDatas);
//                        Toast.makeText(activity, "??????", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.life:
                        choosesort(2, localHomeDatas);
//                        Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.theerc:
                        choosesort(3, localHomeDatas);
//                        Toast.makeText(activity, "3C", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.other:
                        choosesort(4, localHomeDatas);
//                        Toast.makeText(activity, "??????", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }


    //??????User???????????????
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
        Task<android.location.Location> task = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken()
        );
        task.addOnSuccessListener(location -> {
            if (location != null) {
                //????????????
                userlat = location.getLatitude();
                //????????????
                userlng = location.getLongitude();
                coumputeDistancemin();
                showGroup(localHomeDatas);
                Loading.dismiss();
                //??????member????????????????????? by:???
                MemberControl.setMemberCoordinate(new MemberControl.MemberCoordinate(userlat,userlng));
            }
        });
    }

    //????????????????????????????????????????????????Homedata(group,distancemin)?????????
    private void coumputeDistancemin(){
        Loading.dismiss();
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
                List<Float> distance = new ArrayList<>();
                List<Location> locations = group.getLocations();
                for (Location grouplocation : locations) {
                    float[] results = new float[1];
                    //????????????????????????????????????
                    Double groupLat = grouplocation.getLatitude();
                    Double groupLng = grouplocation.getLongtitude();
                    //????????????????????????????????????????????????
                    android.location.Location.distanceBetween(userlat, userlng, groupLat, groupLng, results);
                    //??????1000??????????????????????????????list
                    if (results != null) {
                        distance.add(results[0] / 1000);
                    }
                }
                //??????????????????(?????????????????????)
                Collections.sort(distance);
                BigDecimal b = new BigDecimal(distance.get(0));
                //??????????????????????????????
                float groupDismin = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                homeData = new HomeData(group, groupDismin);
                //????????????????????????????????? && ?????????????????????????????? ????????????????????????
                if (group.getProgress() != group.getConditionCount() && (new Date().before(group.getConditionTime()))) {
                    localHomeDatas.add(homeData);
                }
            }
            //???Homedata??????????????????????????????????????????
            Collections.sort(localHomeDatas, new Comparator<HomeData>() {
                @Override
                public int compare(HomeData o1, HomeData o2) {
                    if (o1.getDistance() < o2.getDistance()) {
                        return -1;
                    } else if (o1.getDistance() > o2.getDistance()) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
    }

    //???????????????????????????????????????????????????
    private void choosesort(int category_Id, List<HomeData> categoryHomeData) {
        searchView.setQuery("", false);
        List<HomeData> selectHomeData = new ArrayList<>();
        for (HomeData category : categoryHomeData) {
            //??????ID
            if (category.getGroup().getCategoryId() == category_Id) {
                selectHomeData.add(category);
            }
        }


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<HomeData> searchHomeData = new ArrayList<>();
                if (newText.equals("")||newText.isEmpty()) {
                    showGroup(selectHomeData);
                } else {
                    // ??????????????????????????????????????????(??????????????????)
                    for (HomeData group : selectHomeData) {
                        if (group.getGroup().getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchHomeData.add(group);
                        }
                    }
                    showGroup(searchHomeData);
                }
                return true;
            }
        });
        showGroup(selectHomeData);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            searchView.setQuery("", false);
            coumputeDistancemin();
            showGroup(selectHomeData);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showGroup(List<HomeData> localHomeDatas) {
        /** RecyclerView */
        // ??????
        HomeFragment.HomeAdapter groupAdapter = (HomeFragment.HomeAdapter) recyclerViewGroup.getAdapter();
        if (groupAdapter == null) {
            recyclerViewGroup.setAdapter(new HomeFragment.HomeAdapter(activity, localHomeDatas));
            int px = (int) Constants.convertDpToPixel(8, activity); // ?????? 8 dp
            recyclerViewGroup.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        } else {
            // ????????????????????????
            groupAdapter.setGroups(localHomeDatas);
            groupAdapter.notifyDataSetChanged();
        }
    }

    private void findView(View view) {
        bottomNavigationView = view.findViewById(R.id.nv_bar);
        recyclerViewGroup = view.findViewById(R.id.rv_groups);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(activity));
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        searchView = view.findViewById(R.id.searchview);
        imv_locate = view.findViewById(R.id.imv_locate);
    }

    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyHomeDataViewHolder> {
        private List<HomeData> rsHomeDatas;
        private LayoutInflater layoutInflater;
        private final int imageSize;

        public HomeAdapter(Context context, List<HomeData> homedatas) {
            layoutInflater = LayoutInflater.from(context);
            rsHomeDatas = homedatas;

            /* ??????????????????4????????????????????? */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        public class MyHomeDataViewHolder extends RecyclerView.ViewHolder {
            TextView txv_group_name, txv_group_conditionTime, txv_progress,txv_distanceMin;
            ImageView imv_group;

            public MyHomeDataViewHolder(@NonNull View itemView) {
                super(itemView);
                txv_group_name = itemView.findViewById(R.id.txv_group_name);
                txv_group_conditionTime = itemView.findViewById(R.id.txv_group_conditionTime);
                txv_progress = itemView.findViewById(R.id.txv_progress);
                txv_distanceMin = itemView.findViewById(R.id.txv_distanceMin);
                imv_group = itemView.findViewById(R.id.imv_group);
            }
        }

        public void setGroups(List<HomeData> HomeDatas) {
            rsHomeDatas = HomeDatas;
        }

        @NonNull
        @Override
        public MyHomeDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_group_buyer, parent, false);
            return new HomeFragment.HomeAdapter.MyHomeDataViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.HomeAdapter.MyHomeDataViewHolder holder, int position) {
            final HomeData rsHomeData = rsHomeDatas.get(position);

            int GroupID = rsHomeData.getGroup().getGroupId();

            Bitmap Groupbitmap = HomeDataControl.getGroupimage(activity, GroupID, imageSize, executor);
            if (Groupbitmap != null) {
                holder.imv_group.setImageBitmap(Groupbitmap);
            } else {
                holder.imv_group.setImageResource(R.drawable.no_image);
            }
            holder.txv_group_name.setText(rsHomeData.getGroup().getName());
            Timestamp ts = rsHomeData.getGroup().getConditionTime();
            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            holder.txv_group_conditionTime.setText("????????????:" + "\n" + sdf.format(ts));
            if (rsHomeData.getGroup().getConditionCount() == -1){
                holder.txv_progress.setText("??????:" + rsHomeData.getGroup().getProgress() + "???\n" +"??????:" + rsHomeData.getGroup().getGoal() + "???");
            }else{
                holder.txv_progress.setText("??????:" + rsHomeData.getGroup().getProgress() + "???\n" + "??????:" + rsHomeData.getGroup().getGoal() + "???\n" + "????????????:" + rsHomeData.getGroup().getConditionCount() + "???");
            }
            holder.txv_distanceMin.setText("?????????"+String.valueOf(rsHomeData.getDistance())+"??????");

            //????????????????????????
            holder.itemView.setOnClickListener(v -> {
                MyLoadingBar.setLoadingBar(activity,"????????????????????????","");
                searchView.setQuery("", false);
                //?????????bar???????????????
                bottomNavigationView.setSelectedItemId(R.id.no);
                Bundle bundle = new Bundle();
                bundle.putInt("GroupID", GroupID);
                bundle.putDouble("Userlat",userlat);
                bundle.putDouble("Userlng",userlng);

                Navigation.findNavController(v).navigate(R.id.merchbrowseFragment, bundle);
            });
        }

        @Override
        public int getItemCount() {
            return rsHomeDatas == null ? 0 : rsHomeDatas.size();
}
    }

    /**
     * 3. ??????????????????
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
                    Toast.makeText(activity, "????????????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

    /**
     * ????????????
     */
    private void intervalPositioning() {
        // 10. ??????????????????
        // 10.1 ?????????/?????? LocationCallback??????
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                /** ?????????????????? **/
                // ??????Location??????
                final android.location.Location location = locationResult.getLastLocation();
                // ????????????
                userlat = location.getLatitude();
                // ????????????
                userlng = location.getLongitude();
                // ??????????????????
                final long time = location.getTime();
            }
        };

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 10.2 ??????????????????
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // send Chat token
    private void getTokenSendServer() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    String token = task.getResult();
                    RemoteAccess.sendChatTokenToServer(token, activity);
                }
            }
        });
    }

}