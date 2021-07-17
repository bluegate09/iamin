package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;

public class GroupFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private SearchView searchViewGroup;
    private SwipeRefreshLayout swipeRefreshLayoutGroup;
    private RecyclerView recyclerViewGroup;
    private Button buttonGroupInsert;
    private ImageView imageViewMerchPag;
    private ImageView imageViewGroupPag;
    private ImageView imageViewSuccessPag;
    private ImageView imageViewPaymentPag;
    // 物件
    private Member member;
    private List<Group> localGroups;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        searchViewGroup = view.findViewById(R.id.searchViewSeller);
        swipeRefreshLayoutGroup = view.findViewById(R.id.swipeRefreshLayoutGroup);
        buttonGroupInsert = view.findViewById(R.id.buttonGroup);
        imageViewMerchPag = view.findViewById(R.id.imageViewMerchPag);
        imageViewGroupPag = view.findViewById(R.id.imageViewGroupPag);
        imageViewSuccessPag = view.findViewById(R.id.imageViewSuccessPag);
        imageViewPaymentPag = view.findViewById(R.id.imageViewPaymentPag);
        // 先載入RecyclerView元件，但是還沒有掛上Adapter
        recyclerViewGroup = view.findViewById(R.id.recyclerViewMerch);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(activity));
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
        activity.setTitle("團購清單");
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_group, container, false);
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
        // member = Member.getInstance();
        // 跟server抓取所有Group
        GroupControl.getAllGroupByMemberId(activity, member.getId());
        localGroups = GroupControl.getLocalGroup();
        if (localGroups == null || localGroups.isEmpty()) {
            Toast.makeText(activity, R.string.textNoGroupsFound, Toast.LENGTH_SHORT).show();
        }

        // 分頁跳轉
        handlePageJump();

        // 用RecyclerView顯示商品資訊
        showGroups(localGroups);

        // 下拉更新
        handleSwipeRefresh();

        // 搜尋功能
        handleSearch();

        // 新增商品
        handleGroupInsert();
    }

    /**
     * 賣家專區各分頁跳轉
     */
    private void handlePageJump() {
        imageViewMerchPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_groupFragment_to_merchFragment);
        });
        imageViewGroupPag.setOnClickListener(view -> {
        });
        imageViewSuccessPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_groupFragment_to_reachFragment);
        });
        imageViewPaymentPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_groupFragment_to_paymentInformationFragment);
        });
    }

    /**
     * 顯示自己的團購單
     */
    private void showGroups(List<Group> localGroups) {
        /** RecyclerView */
        // 檢查
        GroupAdapter groupAdapter = (GroupAdapter) recyclerViewGroup.getAdapter();
        if (groupAdapter == null) {
            recyclerViewGroup.setAdapter(new GroupAdapter(activity, localGroups));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewGroup.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            groupAdapter.setGroups(localGroups);
            groupAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 下拉 RecyclerView 的更新功能
     */
    private void handleSwipeRefresh() {
        swipeRefreshLayoutGroup.setOnRefreshListener(() -> {
            GroupControl.getAllGroupByMemberId(activity, 1);
            localGroups = GroupControl.getLocalGroup();
            // 播放動畫
            swipeRefreshLayoutGroup.setRefreshing(true);
            showGroups(localGroups);
            swipeRefreshLayoutGroup.setRefreshing(false);
        });
    }

    /**
     * 搜尋功能
     */
    private void handleSearch() {
        searchViewGroup.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showGroups(localGroups);
                } else {
                    List<Group> searchGroups = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Group group : localGroups) {
                        // 強制轉大寫，contains() -> 是否包含()
                        if (group.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchGroups.add(group);
                        }
                    }
                    showGroups(searchGroups);
                }
                return true;
            }
        });
    }

    /**
     * 點擊 新增團購 按鈕後跳轉
     */
    private void handleGroupInsert() {
        buttonGroupInsert.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_groupFragment_to_groupInsertFragment);
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
        // HorizontalScrollView
        private View itemViewGroupM;
        private LayoutInflater layoutInflaterGroupM;
        private TextView textViewGroupM;
        private ImageView imageViewGroupM;

        public GroupAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            rsGroups = groups;
            layoutInflaterGroupM = LayoutInflater.from(context);
        }

        public void setGroups(List<Group> Groups) {
            rsGroups = Groups;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewGroupName;
            private TextView textViewGoal;
            private TextView textViewGroupDelete;
            private TextView textViewTAmount;
            private TextView textViewDeadlineTime;
            // HorizontalScrollView
            private LinearLayout linearLayoutGroupM;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
                textViewGoal = itemView.findViewById(R.id.textViewGoal);
                textViewGroupDelete = itemView.findViewById(R.id.textViewGroupDelete);
                textViewTAmount = itemView.findViewById(R.id.textViewTAmount);
                textViewDeadlineTime = itemView.findViewById(R.id.textViewDeadlineTime);
                // HorizontalScrollView
                linearLayoutGroupM = itemView.findViewById(R.id.linearLayoutGroupM);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_group, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Group rsGroup = rsGroups.get(position); // 第幾個group
            holder.textViewGroupName.setText(rsGroup.getName());
            holder.textViewGoal.setText(String.valueOf(rsGroup.getGoal()));  // int -> str
            holder.textViewTAmount.setText(String.valueOf(rsGroup.getTotalAmount()));   // int -> str
            // Timestamp -> String
            Timestamp ts = rsGroup.getConditionTime();
            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            holder.textViewDeadlineTime.setText(sdf.format(ts));
            Log.d(Constants.TAG, "BindViewHolder: " + rsGroup.getName());

            // HorizontalScrollView
            holder.linearLayoutGroupM.removeAllViews(); // 先清空
            for (Merch merch : rsGroup.getMerchs()) {
                View itemViewGroupM = getLayoutInflater().inflate(R.layout.item_view_group_m, null);
                textViewGroupM = itemViewGroupM.findViewById(R.id.textViewGroupM);
                textViewGroupM.setText(merch.getName());

                // 抓取對應的圖片
                byte[] image;
                image = MerchControl.getMerchImgById(activity, merch.getMerchId());
                imageViewGroupM = itemViewGroupM.findViewById(R.id.imageViewGroupM);
                if (image != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    imageViewGroupM.setImageBitmap(bitmap);
                }else {
                    imageViewGroupM.setImageResource(R.drawable.no_image);
                }
                // 間隔
                if (merch != rsGroup.getMerchs().get(0)) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(5,0,0,0);
                    itemViewGroupM.setLayoutParams(layoutParams);
                }

                holder.linearLayoutGroupM.addView(itemViewGroupM);
            }

            // 刪除
            holder.textViewGroupDelete.setOnClickListener(view -> {
                /** 建立AlertDialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("刪除團購");
                builder.setMessage("確定要刪除嗎？");

                builder.setPositiveButton("確認", (dialog, which) -> {
                    // 關閉
                    dialog.dismiss();

                    // 通知server刪除
                    int result = GroupControl.deleteGroup(activity, rsGroup.getGroupId(), rsGroup.getMerchs());
                    if (result == 0) {
                        Toast.makeText(activity, R.string.textDeleteFail, Toast.LENGTH_SHORT).show();
                    }else {
                        // 與內外層一起同步移除
                        rsGroups.remove(rsGroup); // 內層
                        GroupAdapter.this.notifyDataSetChanged(); // 刷新
                        GroupFragment.this.localGroups.remove(rsGroup); // 外層
                        Toast.makeText(activity, R.string.textDeleteSuccess, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消", (dialog, which) -> {
                    // 關閉
                    dialog.dismiss();
                });
                // 顯示
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return rsGroups == null ? 0 : rsGroups.size();
        }
    }
}