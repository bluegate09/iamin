package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;
import idv.tfp10101.iamin.merch.MerchSelect;

public class GroupSelectMFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private SearchView searchViewMerch;
    private RecyclerView recyclerViewMerch;
    private Button buttonMerchsAdd;
    // 物件
    private List<MerchSelect> merchSelects;
    private List<Merch> localMerchs;
    private ArrayList<Integer> selectMerchsId; // 所選擇的商品ID
    private ArrayList<String> selectMerchsName;
    private ArrayList<Integer> selectMerchsPrice;
    // 導航控制(頁面切換用)
    private NavController navController;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        searchViewMerch = view.findViewById(R.id.searchViewMerch);
        buttonMerchsAdd = view.findViewById(R.id.buttonMerchsAdd);
        navController = Navigation.findNavController(view);
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
        activity.setTitle("加入商品");
        // 取得Resources
        resources = getResources();
        // new List<Merch>
        selectMerchsId = new ArrayList<>();
        selectMerchsName = new ArrayList<>();
        selectMerchsPrice = new ArrayList<>();
        // 返回鍵監聽
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressed);

        return inflater.inflate(R.layout.fragment_group_select_m, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        // 抓取資料
        handleMerchData();

        // 把之前已選擇的商品置頂
        handleSelectMerchTop();

        // 用RecyclerView顯示商品資訊
        showMerchs(merchSelects);

        // 搜尋功能
        handleSearch();

        // 選擇完成
        handleSelect();
    }

    /**
     * 抓取資料
     */
    private void handleMerchData() {
        /** 測試資料MemberID = 1 */
        // 跟server抓取所有Merch
        MerchControl.getAllMerchByMemberId(activity, 1);
        localMerchs = MerchControl.getLocalMerchs();
        if (localMerchs == null || localMerchs.isEmpty()) {
            Toast.makeText(activity, R.string.textNoMerchsFound, Toast.LENGTH_SHORT).show();
        }
        // 取得Bundle物件，判斷先前以選擇的商品
        merchSelects = new ArrayList<>();
        Boolean bool;
        Bundle bundle = getArguments();
        if (bundle != null) {
            ArrayList<Integer> toChoose;
            toChoose = bundle.getIntegerArrayList("merchsId");
            for (Merch merch : localMerchs) {
                bool = false;
                for (int c : toChoose) {
                    if (merch.getMerchId() == c) {
                        MerchSelect merchSelect = new MerchSelect(merch, true);
                        merchSelects.add(merchSelect);
                        // 回傳資料
                        selectMerchsId.add(merchSelect.getMerch().getMerchId());
                        selectMerchsName.add(merchSelect.getMerch().getName());
                        selectMerchsPrice.add(merchSelect.getMerch().getPrice());
                        // bundle有無資料判斷
                        bool = true;
                        break;
                    }
                }
                if (!bool) {
                    MerchSelect merchSelect = new MerchSelect(merch, false);
                    merchSelects.add(merchSelect);
                }
            }
        }else {
            for (Merch merch : localMerchs) {
                MerchSelect merchSelect = new MerchSelect(merch, false);
                merchSelects.add(merchSelect);
            }
        }
    }

    /**
     * 把之前已選擇的商品置頂
     */
    private void handleSelectMerchTop() {
        ArrayList<MerchSelect> topList = new ArrayList<>();
        ArrayList<MerchSelect> bottomList = new ArrayList<>();
        for (MerchSelect merchSelect : merchSelects) {
            if (merchSelect.getSelect()) {
                topList.add(merchSelect);
            }else {
                bottomList.add(merchSelect);
            }
        }
        topList.addAll(bottomList);
        merchSelects = topList;
    }

    /**
     * 顯示自己的商品
     */
    private void showMerchs(List<MerchSelect> merchSelects) {
        /** RecyclerView */
        // 檢查
        MerchAdapter merchAdapter = (MerchAdapter) recyclerViewMerch.getAdapter();
        if (merchAdapter == null) {
            recyclerViewMerch.setAdapter(new MerchAdapter(activity, merchSelects));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMerch.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            merchAdapter.setMerchs(merchSelects);
            merchAdapter.notifyDataSetChanged();
        }
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
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showMerchs(merchSelects);
                } else {
                    List<MerchSelect> searchMerchs = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (MerchSelect merchSelect : merchSelects) {
                        // 強制轉大寫，contains() -> 是否包含()
                        if (merchSelect.getMerch().getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchMerchs.add(merchSelect);
                        }
                    }
                    showMerchs(searchMerchs);
                }
                return true;
            }
        });
    }

    /**
     * 選擇完成
     */
    private void handleSelect() {
        buttonMerchsAdd.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("merchsId", selectMerchsId);
            bundle.putStringArrayList("merchsName", selectMerchsName);
            bundle.putIntegerArrayList("merchsPrice", selectMerchsPrice);
            getParentFragmentManager().setFragmentResult("requestKey", bundle);

            navController.popBackStack();
        });
    }

    /**
     * 點擊返回鍵
     */
    private final OnBackPressedCallback backPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("merchsId", selectMerchsId);
            bundle.putStringArrayList("merchsName", selectMerchsName);
            bundle.putIntegerArrayList("merchsPrice", selectMerchsPrice);
            getParentFragmentManager().setFragmentResult("requestKey", bundle);

            navController.popBackStack();
        }
    };

    /**
     * 自定義Adapter 繼承 RecyclerView 的 Adapter
     * 1. 建立Context & 一些需要的資訊，並constructor
     * 2. 實作 RecyclerView.ViewHolder 給 Adapter使用
     * 3. 設定父類別泛型型態
     * 4. 自動建立 Override 方法 (onCreateViewHolder, onBindViewHolder, getItemCount)
     */
    private class MerchAdapter extends RecyclerView.Adapter<MerchAdapter.MyViewHolder> {
        private List<MerchSelect> rvMerchSelects;
        private LayoutInflater layoutInflater;

        MerchAdapter(Context context, List<MerchSelect> merchSelects) {
            layoutInflater = LayoutInflater.from(context);
            rvMerchSelects = merchSelects;
        }

        public void setMerchs(List<MerchSelect> merchSelects) {
            rvMerchSelects = merchSelects;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private ConstraintLayout layoutMain;
            private TextView textViewName;
            private TextView textViewPrice;

            MyViewHolder(View itemView) {
                super(itemView);

                layoutMain = itemView.findViewById(R.id.layoutMain);
                textViewName = itemView.findViewById(R.id.textViewGroupName);
                textViewPrice = itemView.findViewById(R.id.textViewGoal);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 載入Layout
            View itemView = layoutInflater.inflate(R.layout.item_view_group_select_merch, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final MerchSelect rvMerchSelect = rvMerchSelects.get(position); // 第幾個merch
            holder.textViewName.setText(rvMerchSelect.getMerch().getName());
            holder.textViewPrice.setText(String.valueOf(rvMerchSelect.getMerch().getPrice())); // int -> str
            if (rvMerchSelect.getSelect()) {
                holder.layoutMain.setBackgroundColor(resources.getColor(R.color.colorSecondary));
            }else {
                holder.layoutMain.setBackgroundColor(resources.getColor(R.color.colorSecondaryLight));
            }
            Log.d(Constants.TAG, "BindViewHolder: " + rvMerchSelect.getMerch().getName());


            // 選擇
            holder.layoutMain.setOnClickListener(view -> {
                if (!rvMerchSelect.getSelect()) {
                    selectMerchsId.add(rvMerchSelect.getMerch().getMerchId());
                    selectMerchsName.add(rvMerchSelect.getMerch().getName());
                    selectMerchsPrice.add(rvMerchSelect.getMerch().getPrice());
                    holder.layoutMain.setBackgroundColor(resources.getColor(R.color.colorSecondary));
                    rvMerchSelect.setSelect(true);
                }else {
                    selectMerchsId.remove(Integer.valueOf(rvMerchSelect.getMerch().getMerchId()));
                    selectMerchsName.remove(rvMerchSelect.getMerch().getName());
                    selectMerchsPrice.remove(Integer.valueOf(rvMerchSelect.getMerch().getPrice()));
                    holder.layoutMain.setBackgroundColor(resources.getColor(R.color.colorSecondaryLight));
                    rvMerchSelect.setSelect(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return rvMerchSelects == null ? 0 : rvMerchSelects.size();
        }
    }
}