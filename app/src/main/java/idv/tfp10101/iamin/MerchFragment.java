package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;

public class MerchFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private SearchView searchViewMerch;
    private SwipeRefreshLayout swipeRefreshLayoutMerch;
    private RecyclerView recyclerViewMerch;
    private Button buttonMerchInsert;
    private ImageView imageViewMerchPag;
    private ImageView imageViewGroupPag;
    private ImageView imageViewSuccessPag;
    private ImageView imageViewPaymentPag;
    // 物件
    private List<Merch> localMerchs;
    private List<Merch> searchMerchs;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        searchViewMerch = view.findViewById(R.id.searchViewSeller);
        swipeRefreshLayoutMerch = view.findViewById(R.id.swipeRefreshLayoutGroup);
        buttonMerchInsert = view.findViewById(R.id.buttonGroup);
        imageViewMerchPag = view.findViewById(R.id.imageViewMerchPag);
        imageViewGroupPag = view.findViewById(R.id.imageViewGroupPag);
        imageViewSuccessPag = view.findViewById(R.id.imageViewSuccessPag);
        imageViewPaymentPag = view.findViewById(R.id.imageViewPaymentPag);
        // 先載入RecyclerView元件，但是還沒有掛上Adapter
        recyclerViewMerch = view.findViewById(R.id.recyclerViewMerch);
        recyclerViewMerch.setLayoutManager(new LinearLayoutManager(activity));
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
        activity.setTitle("商品清單");
        // 取得Resources
        resources = getResources();
        // 初始化儲存變數
        searchMerchs = new ArrayList<>();

        return inflater.inflate(R.layout.fragment_merch, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        /** 測試資料 */
        // 跟server抓取所有Merch
        MerchControl.getAllMerchByMemberId(activity, 1);
        localMerchs = MerchControl.getLocalMerchs();
        if (localMerchs == null || localMerchs.isEmpty()) {
            Toast.makeText(activity, R.string.textNoMerchsFound, Toast.LENGTH_SHORT).show();
        }

        // 分頁跳轉
        handlePageJump();

        // 用RecyclerView顯示商品資訊
        showMerchs(localMerchs);

        // 下拉更新
        handleSwipeRefresh();

        // 搜尋功能
        handleSearch();

        // 新增商品
        handleMerchInsert();
    }

    /**
     * 賣家專區各分頁跳轉
     */
    private void handlePageJump() {
        imageViewMerchPag.setOnClickListener(view -> {
        });
        imageViewGroupPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_merchFragment_to_groupFragment);
        });
        imageViewSuccessPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_merchFragment_to_reachFragment);
        });
        imageViewPaymentPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_merchFragment_to_paymentInformationFragment);
        });
    }

    /**
     * 顯示自己的商品
     */
    private void showMerchs(List<Merch> Merchs) {
        /** RecyclerView */
        // 檢查
        MerchAdapter merchAdapter = (MerchAdapter) recyclerViewMerch.getAdapter();
        if (merchAdapter == null) {
            recyclerViewMerch.setAdapter(new MerchAdapter(activity, Merchs));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMerch.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            merchAdapter.setMerchs(Merchs);
            merchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 下拉 RecyclerView 的更新功能
     */
    private void handleSwipeRefresh() {
        swipeRefreshLayoutMerch.setOnRefreshListener(() -> {
            // 重新跟server抓資料
            MerchControl.getAllMerchByMemberId(activity, 1);
            // 播放動畫
            swipeRefreshLayoutMerch.setRefreshing(true);
            if (!searchMerchs.isEmpty()) {
                showMerchs(searchMerchs);
            }else {
                showMerchs(localMerchs);
            }
            swipeRefreshLayoutMerch.setRefreshing(false);
        });
    }

    /**
     * 搜尋功能
     */
    private void handleSearch() {
        searchViewMerch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 清空
                searchMerchs.clear();
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showMerchs(localMerchs);
                } else {
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Merch merch : localMerchs) {
                        // 強制轉大寫，contains() -> 是否包含()
                        if (merch.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchMerchs.add(merch);
                        }
                    }
                    showMerchs(searchMerchs);
                }
                return true;
            }
        });
    }

    /**
     * 點擊 新增商品 按鈕後跳轉
     */
    private void handleMerchInsert() {
        buttonMerchInsert.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_merchFragment_to_merchInsertFragment);
        });
    }

    /**
     * 自定義Adapter 繼承 RecyclerView 的 Adapter
     * 1. 建立Context & 一些需要的資訊，並constructor
     * 2. 實作 RecyclerView.ViewHolder 給 Adapter使用
     * 3. 設定父類別泛型型態
     * 4. 自動建立 Override 方法 (onCreateViewHolder, onBindViewHolder, getItemCount)
     */
    private class MerchAdapter extends RecyclerView.Adapter<MerchAdapter.MyViewHolder> {
        private List<Merch> rvMerchs;
        private LayoutInflater layoutInflater = LayoutInflater.from(activity);

        MerchAdapter(Context context, List<Merch> merches) {
            layoutInflater = LayoutInflater.from(context);
            rvMerchs = merches;
        }

        public void setMerchs(List<Merch> merches) {
            rvMerchs = merches;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewName;
            private TextView textViewPrice;
            private ImageView imageViewEdit;

            MyViewHolder(View itemView) {
                super(itemView);

                textViewName = itemView.findViewById(R.id.textViewGroupName);
                textViewPrice = itemView.findViewById(R.id.textViewGoal);
                imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 載入Layout
            View itemView = layoutInflater.inflate(R.layout.item_view_merch, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Merch rvMerch = rvMerchs.get(position); // 第幾個merch
            // 如果字數太長就縮小字體
            if (rvMerch.getName().length() > 15) {
                holder.textViewName.setTextSize(18);
            }
            holder.textViewName.setText(rvMerch.getName());
            holder.textViewPrice.setText(String.valueOf(rvMerch.getPrice())); // int -> str
            Log.d(Constants.TAG, "BindViewHolder: " + rvMerch.getName());

            // 如果有 lockCount 就關閉編輯
            if (rvMerch.getLockCount() > 0) {
                holder.imageViewEdit.setVisibility(View.GONE);
            }else {
                holder.imageViewEdit.setVisibility(View.VISIBLE);
            }
            // 編輯按鈕
            holder.imageViewEdit.setOnClickListener(view -> {
                // Bundle -> 打包資料傳遞 putXYZ(key, value)
                Bundle bundle = new Bundle();
                bundle.putSerializable("merch", rvMerch);
                // 切換頁面
                Navigation.findNavController(view)
                        .navigate(R.id.action_merchFragment_to_merchUpdateFragment, bundle);
            });

            // 長按監聽 -> 顯示彈出選單 (popupMenu.show())
            holder.itemView.setOnLongClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
                popupMenu.inflate(R.menu.popup_menu_merch);
                popupMenu.show();
                // 監聽 PopupMenu
                popupMenu.setOnMenuItemClickListener(item -> {
                    // 點擊哪一個
                    switch (item.getItemId()) {
                    case R.id.itemMrechDelete:
                        // 如果有 lockCount 不可刪除
                        if (rvMerch.getLockCount() > 0) {
                            Toast.makeText(activity, "商品目前處於團購中，不可刪除", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        // 通知server刪除
                        int result = MerchControl.deleteMerchById(activity, rvMerch.getMerchId());
                        if (result == 0) {
                            Toast.makeText(activity, R.string.textDeleteFail, Toast.LENGTH_SHORT).show();
                        } else {
                            // 與內外層一起同步移除
                            rvMerchs.remove(rvMerch); // 內層
                            MerchAdapter.this.notifyDataSetChanged(); // 刷新
                            MerchFragment.this.localMerchs.remove(rvMerch); // 外層
                            Toast.makeText(activity, R.string.textDeleteSuccess, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        break;
                    }
                    return true;
                });
                return true;
            });

        }

        @Override
        public int getItemCount() {
            return rvMerchs == null ? 0 : rvMerchs.size();
        }
    }



    /** Lifecycle-11 */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}