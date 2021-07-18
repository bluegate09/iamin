package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.network.RemoteAccess;

public class ReachFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private RecyclerView recyclerViewReach;
    private ImageView imageViewMerchPag;
    private ImageView imageViewGroupPag;
    private ImageView imageViewSuccessPag;
    private ImageView imageViewPaymentPag;
    // 物件
    private Member member;
    List<Group> reachGroups = new ArrayList<>(); // 取得目前已達標的團購
    List<Location> locations = new ArrayList<>();
    Map<Integer, List<Location>> locationMap = new HashMap<>(); // 所選擇團購的地址存放
    private String displayTime = null; // (顯示用)
    private String formatTime = null; // 截止時間 (儲存-轉格式用)
    private Timestamp timestamp = null; // 截止時間 (儲存用)
    // 導航控制(頁面切換用)
    private NavController navController;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewMerchPag = view.findViewById(R.id.imageViewMerchPag);
        imageViewGroupPag = view.findViewById(R.id.imageViewGroupPag);
        imageViewSuccessPag = view.findViewById(R.id.imageViewSuccessPag);
        imageViewPaymentPag = view.findViewById(R.id.imageViewPaymentPag);
        // 先載入RecyclerView元件，但是還沒有掛上Adapter
        recyclerViewReach = view.findViewById(R.id.recyclerViewReach);
        recyclerViewReach.setLayoutManager(new LinearLayoutManager(activity));

        navController = Navigation.findNavController(view);
    }

    /**linearLayoutLocation
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
        activity.setTitle("達標成團");
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_reach, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        // 分頁跳轉
        handlePageJump();

        /** 抓取會員ID */
        member = MemberControl.getInstance();
        // 抓取有達標的團購
        reachGroups = GroupControl.getReachGroup(activity, member.getId());
        if (reachGroups == null || reachGroups.isEmpty()) {
            Toast.makeText(activity, "目前沒有達標的團購", Toast.LENGTH_SHORT).show();
            return;
        }

        // 用GroupId抓取發貨地址
        handleGroupLocation();

        // 用RecyclerView顯示商品資訊
        showGroups(reachGroups);
    }

    /**
     * 用GroupId抓取發貨地址
     */
    private void handleGroupLocation() {
        for (Group group : reachGroups) {
            locations = LocationControl.getLocationByGroupId(activity, group.getGroupId());
            locationMap.put(group.getGroupId(), locations);
        }
    }

    /**
     * 顯示自己的團購單
     */
    private void showGroups(List<Group> localGroups) {
        /** RecyclerView */
        // 檢查
        GroupAdapter groupAdapter = (GroupAdapter) recyclerViewReach.getAdapter();
        if (groupAdapter == null) {
            recyclerViewReach.setAdapter(new GroupAdapter(activity, localGroups));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewReach.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            groupAdapter.setGroups(localGroups);
            groupAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 賣家專區各分頁跳轉
     */
    private void handlePageJump() {
        imageViewMerchPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_reachFragment_to_merchFragment);
        });
        imageViewGroupPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_reachFragment_to_groupFragment);
        });
        imageViewSuccessPag.setOnClickListener(view -> {
        });
        imageViewPaymentPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_reachFragment_to_paymentInformationFragment);
        });
    }

    /**
     * 自定義Adapter 繼承 RecyclerView 的 Adapter
     * 1. 建立Context & 一些需要的資訊，並constructor
     * 2. 實作 RecyclerView.ViewHolder 給 Adapter使用
     * 3. 設定父類別泛型型態
     * 4. 自動建立 Override 方法 (onCreateViewHolder, onBindViewHolder, getItemCount)
     */
    private class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {
        private List<Group> rsGroups;
        private LayoutInflater layoutInflater;
        boolean[] expanded; // 詳細內容展開

        public GroupAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            rsGroups = groups;
            expanded = new boolean[groups.size()];
        }

        public void setGroups(List<Group> Groups) {
            rsGroups = Groups;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageViewNotification; // 發送通知按鈕
            private TextView textViewGroupName; // 團購名
            private TextView textViewPayment; // 付款方式
            private TextView textViewMemberId; // 主購ID
            private TextView textViewPhone; // 聯絡電話
            private TextView textViewAmount; // 目前收款金額
            private LinearLayout linearLayoutMerch; // 商品明細
            private LinearLayout linearLayoutLocation; // 地址清單
            // 內容顯示控制
            private ConstraintLayout layoutTitle;
            private ConstraintLayout layoutDetail;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewNotification = itemView.findViewById(R.id.imageViewNotification);
                textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
                textViewPayment = itemView.findViewById(R.id.textViewPayment);
                textViewMemberId = itemView.findViewById(R.id.textViewMemberId);
                textViewPhone = itemView.findViewById(R.id.textViewPhone);
                textViewAmount = itemView.findViewById(R.id.textViewAmount);
                linearLayoutMerch = itemView.findViewById(R.id.linearLayoutMerch);
                linearLayoutLocation = itemView.findViewById(R.id.linearLayoutLocation);
                // 內容顯示控制
                layoutTitle = itemView.findViewById(R.id.layoutTitle);
                layoutDetail = itemView.findViewById(R.id.layoutDetail);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_reach, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Group rsGroup = rsGroups.get(position); // 第幾個group
            // 發送通知按鈕

            // 團購名
            holder.textViewGroupName.setText(rsGroup.getName());
            // 付款方式
            String stringPM =  paymentMethodToString(rsGroup.getPaymentMethod());
            holder.textViewPayment.setText(stringPM);
            // 主購ID
            holder.textViewMemberId.setText(String.valueOf(rsGroup.getMemberId()));
            // 聯絡電話
            holder.textViewPhone.setText(rsGroup.getContactNumber());
            // 目前收款金額
            holder.textViewAmount.setText(String.valueOf(rsGroup.getAmount()));
            // 商品明細
            holder.linearLayoutMerch.removeAllViews(); // 先清空
            for (Merch merch : rsGroup.getMerchs()) {
                TextView textView = new TextView(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.bottomMargin = 8;
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(14);
                textView.setText(merch.getName());
                // linearLayout 加入一筆
                holder.linearLayoutMerch.addView(textView);
            }
            // 地址清單
            holder.linearLayoutLocation.removeAllViews(); // 先清空
            String addressName = ""; // 地址名字
            List<Location> locations; // 地址物件清單
            locations = locationMap.get(rsGroup.getGroupId());
            for (Location location : locations) {
                TextView textView = new TextView(activity);
                // 地址名字
                addressName = latLngToName(location.getLatitude(), location.getLongtitude());
                /** 建立textView  */

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.bottomMargin = 8;
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(18);
                textView.setBackground(resources.getDrawable(R.drawable.bg_radius_textview));
                // 如果有取貨時間 就顯示
                if (location.getPickup_time() != null) {
                    String dbPickupTime = timestampToString(location.getPickup_time());
                    textView.setText(addressName + "取貨時間 - " + dbPickupTime);
                }else {
                    textView.setText(addressName);
                }
                /** 點擊開啟取貨時間設定 */
                final String address = addressName;
                textView.setOnClickListener(view -> {
                    settingCalendar(location, (TextView) view, address);
                });
                // linearLayout 加入一筆
                holder.linearLayoutLocation.addView(textView);
            }
            //
            holder.imageViewNotification.setOnClickListener(view -> {

                return;
            });
            // 預設 不展開內容
            holder.layoutDetail.setVisibility(expanded[position] ? View.VISIBLE : View.GONE);
            // 設定監聽 內容 被點擊時 展開
            holder.layoutTitle.setOnClickListener(view -> {
                int pos = holder.getBindingAdapterPosition();
                expanded[pos] = !expanded[pos];
                //  Adapter 呼叫 notifyDataSetChanged()，會全部重刷新 (重建一次RecyclerView)
                notifyDataSetChanged();
            });

            // 成團推播按鈕
            holder.imageViewNotification.setOnClickListener(view -> {
                // 依照 GroupId 團體推播
                sendFcmByGroupId(rsGroup.getGroupId(), rsGroup.getName());
            });
        }

        @Override
        public int getItemCount() {
            return rsGroups == null ? 0 : rsGroups.size();
        }
    }

    /**
     * 成團推播 (目前測試資料)
     */
    private void sendFcmByGroupId(int groupId, String groupName) {
        // Bundle -> 打包資料傳遞 putXYZ(key, value)
        Bundle bundle = new Bundle();
        bundle.putString("groupName", groupName);
        bundle.putInt("groupId", groupId);
        // 切換頁面
        navController.navigate(R.id.action_reachFragment_to_reachNotificationFragment, bundle);
    }

    /**
     * Timestamp 轉 String
     */
    private String timestampToString(Timestamp pickup_time) {
        String string = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            string = dateFormat.format(pickup_time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     * 付款方式轉字串
     */
    private String paymentMethodToString(int method) {
        switch (method) {
            case 1:
                return "面交";
            case 2:
                return "信用卡";
            case 3:
                return "面交, 信用卡";
            default:
                return "付款方式錯誤";
        }
    }

    /**
     *  緯經度 轉 地名/地址
     */
    private String latLngToName(double lat, double lng) {
        // 判斷Geocoder是否可用
        boolean isPresent = Geocoder.isPresent();
        if (!isPresent) {
            return "";
        }
        // 實例化Geocoder物件
        Geocoder geocoder = new Geocoder(activity);
        // 地名
        StringBuilder name = new StringBuilder();
        try {
            // 轉換
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            // 取得 地名/地址
            Address address = addressList.get(0);
            if (address != null) {
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    name.append(address.getAddressLine(i))
                        .append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return name.toString();
    }

    /**
     * 設定取貨時間 (日期)
     */
    public void settingCalendar(Location location, TextView textView, String addressName){
        // 取得Calendar物件
        Calendar calendar = Calendar.getInstance();
        // 實例化DatePickerDialog物件
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                activity,
                (DatePicker datePicker, int year, int month, int dayOfMonth) -> {
                    // 顯示用
                    displayTime = year + "/" + (month + 1) + "/" + dayOfMonth;
                    // 儲存-轉格式用
                    formatTime = year + "-" + (month + 1) + "-" + dayOfMonth;
                    // 設定 時分
                    settingTime(location, textView, addressName);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // 顯示對話框
        datePickerDialog.show();
    }

    /**
     * 設定取貨時間 (時分)
     */
    public void settingTime(Location location, TextView textView, String addressName) {
        // 取得Calendar物件
        Calendar calendar = Calendar.getInstance();
        // 實例化TimePickerDialog物件
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                activity,
                (TimePicker view, int hourOfDay, int minute) -> {
                    String h, m;
                    if (hourOfDay < 10) {
                        h = "0" + hourOfDay;
                    }else {
                        h = "" + hourOfDay;
                    }
                    if (minute < 10) {
                        m = "0" + minute;
                    }else {
                        m = "" + minute;
                    }
                    // 顯示用
                    displayTime += " " + h + ":" + m;
                    // 儲存-轉格式用
                    formatTime += " " + h + ":" + m + ":00";

                    // 存入TextView顯示
                    textView.setText(addressName + "取貨時間 - " + displayTime);
                    // 存入location物件
                    timestamp = new Timestamp(System.currentTimeMillis());
                    try {
                        timestamp = Timestamp.valueOf(formatTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    location.setPickup_time(timestamp);
                    // 存入Server
                    LocationControl.update(activity, location);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        // 顯示對話框
        timePickerDialog.show();
    }
}