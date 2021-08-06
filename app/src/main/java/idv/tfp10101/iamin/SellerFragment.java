package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupBlockade;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order.MemberOrderControl;

public class SellerFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private SearchView searchViewSeller;
    private Spinner spinnerSeller;
    private RecyclerView recyclerViewSeller;
    private Button buttonGroup;
    // 物件
    private Member member;
    private List<Group> localGroups = new ArrayList<>(); // 取得目前已達標的團購
    private List<Group> filterGroups = new ArrayList<>(); // 取得已篩選達標的團購
    private Map<Integer, String> mapGroupStatus = new HashMap<>(); // 團購種類MAP
    private int groupStatus = 0; // 目前團購spinner的狀態
    private String groupSearch = ""; // 搜尋的字串
    // 導航控制(頁面切換用)
    private NavController navController;
    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        searchViewSeller = view.findViewById(R.id.searchViewSeller);
        spinnerSeller = view.findViewById(R.id.spinnerSeller);
        buttonGroup = view.findViewById(R.id.buttonSubmit);
        // 先載入RecyclerView元件，但是還沒有掛上Adapter
        recyclerViewSeller = view.findViewById(R.id.recyclerViewSeller);
        recyclerViewSeller.setLayoutManager(new LinearLayoutManager(activity));

        navController = Navigation.findNavController(view);
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
        activity.setTitle("賣家中心");
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_seller, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);



        /** 抓取會員ID */
        member = MemberControl.getInstance();
        // 如果沒有登入
        if (member.getId() == -1) {
            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("還未登入喔請先登入會員");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 切換頁面
                    navController.navigate(R.id.action_sellerFragment_to_logInFragment);
                }
            });
            // 顯示
            builder.show();
        }else if (member.getPhoneNumber() == null || member.getPhoneNumber().isEmpty()) {
            // 一開始先判斷有沒有登入有驗證的會員
            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("您還不是認證會員喔！將移到會員中心更新會員資料");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 切換頁面
                    navController.navigate(R.id.action_sellerFragment_to_memeberCenterProfileFragment);
                }
            });
            // 顯示
            builder.show();
        }

        // 如果有團購被管理員封鎖
        List<GroupBlockade> groupBlockades = new ArrayList<>();
        groupBlockades = GroupControl.getBlockade(activity, member.getId());
        if (groupBlockades != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (GroupBlockade groupBlockade : groupBlockades) {
                if (!groupBlockade.isNotify()) {
                    stringBuffer.append(groupBlockade.getGroupName());
                    stringBuffer.append("\n");
                }
            }
            //
            if (stringBuffer.length() > 0) {
                /** 建立AlertDialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("封鎖通知!您以下的團購違反規章，已被管理員封鎖。");
                builder.setMessage(stringBuffer);
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                // 顯示
                builder.show();
            }
        }

        // 跟server抓取所有Group
        GroupControl.getAllGroupByMemberId(activity, member.getId());
        localGroups = GroupControl.getLocalGroup();
        if (localGroups == null || localGroups.isEmpty()) {
            Toast.makeText(activity, R.string.textNoGroupsFound, Toast.LENGTH_SHORT).show();
        }

        // 前往商品團購頁面
        handleButtonGroup();

        // 用RecyclerView顯示商品資訊
        showGroups(localGroups);

        // 團購狀態選擇
        handleSpinnerSeller();

        // 搜尋功能
        handleSearch();
    }

    /**
     * 顯示自己的團購單
     */
    private void showGroups(List<Group> localGroups) {
        /** RecyclerView */
        // 檢查
        GroupAdapter groupAdapter = (GroupAdapter) recyclerViewSeller.getAdapter();
        if (groupAdapter == null) {
            recyclerViewSeller.setAdapter(new GroupAdapter(activity, localGroups));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewSeller.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            groupAdapter.setGroups(localGroups);
            groupAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 團購狀態選擇
     */
    private void handleSpinnerSeller() {
        // 準備map資料
        mapGroupStatus.put(0, "團購狀態");
        mapGroupStatus.put(1, "揪團中");
        mapGroupStatus.put(2, "達標");
        mapGroupStatus.put(3, "揪團失敗");
        // 準備放入Spinner內的String
        List<String> strings = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mapGroupStatus.entrySet()) {
            strings.add(entry.getValue());
        }
        // 實例化Adapter物件 (Context, 外觀, 顯示的List<String>)
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(activity, R.layout.spinner_seller, strings);
        // 設定要下拉的樣式
        adapter.setDropDownViewResource(R.layout.spinner_seller);
        spinnerSeller.setAdapter(adapter); // Adapter 設定進 spType

        /** spinner監聽 */
        spinnerSeller.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 抓取現在的狀態
                String string = parent.getItemAtPosition(position).toString();
                for (Map.Entry<Integer, String> entry : mapGroupStatus.entrySet()) {
                    if (entry.getValue().equals(string)) {
                        groupStatus = entry.getKey();
                        // 篩選條件更新
                        updateFilterGroups();
                        return;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 搜尋功能
     */
    private void handleSearch() {
        searchViewSeller.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    groupSearch = "";
                } else {
                    groupSearch = newText;
                }
                // 篩選條件更新
                updateFilterGroups();
                return true;
            }
        });
    }

    /**
     * 篩選條件更新
     */
    private void updateFilterGroups() {
        // 每次清空
        filterGroups.clear();
        // 如果都沒有篩選條件就還原
        if (groupSearch.isEmpty() && groupStatus == 0) {
            showGroups(localGroups);
            return;
        }
        // 篩選條件一 or 一二
        if (groupSearch.isEmpty()) {
            for (Group group : localGroups) {
                if (group.getGroupStatus() == groupStatus) {
                    filterGroups.add(group);
                }
            }
        }else {
            for (Group group : localGroups) {
                if (
                    // 強制轉大寫，contains() -> 是否包含()
                    group.getName().toUpperCase().contains(groupSearch.toUpperCase()) &&
                    (groupStatus == group.getGroupStatus() || groupStatus == 0)
                ) {
                    filterGroups.add(group);
                }
            }
        }
        showGroups(filterGroups);
    }

    /**
     * 前往商品團購頁面
     */
    private void handleButtonGroup() {
        buttonGroup.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_sellerFragment_to_merchFragment);
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

        public GroupAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            rsGroups = groups;
        }

        public void setGroups(List<Group> Groups) {
            rsGroups = Groups;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewName;
            private TextView textViewNumber;
            private TextView textViewCount;
            private TextView textViewTime;
            private TextView textViewProblemNum;
            // 狀態圖
            private ImageView imageViewStatus1;
            private ImageView imageViewStatus2;
            private ImageView imageViewStatus3;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                textViewName = itemView.findViewById(R.id.textViewName);
                textViewNumber = itemView.findViewById(R.id.textViewNumber);
                textViewCount = itemView.findViewById(R.id.textViewCount);
                textViewTime = itemView.findViewById(R.id.textViewTime);
                textViewProblemNum = itemView.findViewById(R.id.textViewProblemNum);
                imageViewStatus1 = itemView.findViewById(R.id.imageViewStatus1);
                imageViewStatus2 = itemView.findViewById(R.id.imageViewStatus2);
                imageViewStatus3 = itemView.findViewById(R.id.imageViewStatus3);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_seller, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Group rsGroup = rsGroups.get(position); // 第幾個group
            //
            StringBuffer stringBuffer = new StringBuffer();
            if (rsGroup.getName().length() > 7) {
                stringBuffer.append(rsGroup.getName().substring(0, 6));
                stringBuffer.append("\n");
                stringBuffer.append(rsGroup.getName().substring(7, rsGroup.getName().length()));
                holder.textViewName.setText(stringBuffer);
            }else {
                holder.textViewName.setText(rsGroup.getName());
            }

            // 參加人數-抓取會員訂單
            holder.textViewNumber.setText("0"); // 預設
            List<MemberOrder> memberOrders =
                    MemberOrderControl.getMemberOrderByGroupId(
                            activity,
                            rsGroup.getGroupId(),
                            "PaymentInformation");
            Set<Integer> memberIds = new HashSet<>();
            if (memberOrders != null) {
                for (MemberOrder memberOrder : memberOrders) {
                    memberIds.add(memberOrder.getMemberId());
                    holder.textViewNumber.setText(String.valueOf(memberIds.size()));
                }
            }
            //
            holder.textViewCount.setText(String.valueOf(rsGroup.getProgress()));   // int -> str
            // 截止時間
            String time = timestampToString(rsGroup.getConditionTime());
            holder.textViewTime.setText(time);
            // 問與答
            holder.textViewProblemNum.setText("10");
            // 狀態圖
            switch (rsGroup.getGroupStatus()) {
                case 1:
                    holder.imageViewStatus1.setVisibility(View.VISIBLE);
                    holder.imageViewStatus2.setVisibility(View.GONE);
                    holder.imageViewStatus3.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.imageViewStatus1.setVisibility(View.GONE);
                    holder.imageViewStatus2.setVisibility(View.GONE);
                    holder.imageViewStatus3.setVisibility(View.VISIBLE);
                    break;

                case 3:
                    holder.imageViewStatus1.setVisibility(View.GONE);
                    holder.imageViewStatus2.setVisibility(View.VISIBLE);
                    holder.imageViewStatus3.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return rsGroups == null ? 0 : rsGroups.size();
        }
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
}