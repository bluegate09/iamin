package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;

public class PaymentInformationFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private ImageView imageViewMerchPag;
    private ImageView imageViewGroupPag;
    private ImageView imageViewSuccessPag;
    private ImageView imageViewPaymentPag;
    private RecyclerView recyclerViewMember;
    private Button buttonUpdate;
    private Spinner spinnerPaymentMethod;
    private Spinner spinnerPaymentStatus;
    // 物件
    private List<Group> reachGroups = new ArrayList<>(); // 取得目前已達標的團購
    private List<Group> filterGroups = new ArrayList<>(); // 取得已篩選達標的團購
    private Map<Integer, String> mapPaymentMethod = new HashMap<>();
    private Map<Integer, String> mapPaymentStatus = new HashMap<>();

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewMerchPag = view.findViewById(R.id.imageViewMerchPag);
        imageViewGroupPag = view.findViewById(R.id.imageViewGroupPag);
        imageViewSuccessPag = view.findViewById(R.id.imageViewSuccessPag);
        imageViewPaymentPag = view.findViewById(R.id.imageViewPaymentPag);
        buttonUpdate = view.findViewById(R.id.buttonGroup);
        spinnerPaymentMethod = view.findViewById(R.id.spinnerPaymentMethod);
        spinnerPaymentStatus = view.findViewById(R.id.spinnerPaymentStatus);
        // 先載入RecyclerView元件，但是還沒有掛上Adapter
        recyclerViewMember = view.findViewById(R.id.recyclerViewMember);
        recyclerViewMember.setLayoutManager(new LinearLayoutManager(activity));
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
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_payment_information, container, false);
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

        // 付款方式選擇
        handlePaymentMethod();

        // 已付款狀態選擇
        handlePaymentStatus();

        /** 設定預設memberId */
        // 抓取有達標的團購
        reachGroups = GroupControl.getReachGroup(activity, 1);
        if (reachGroups == null) {
            Toast.makeText(activity, "目前沒有達標的團購", Toast.LENGTH_SHORT).show();
            return;
        }

        // 篩選已達標的團購
        handleFilterGroups();
    }



    /**
     * 賣家專區各分頁跳轉
     */
    private void handlePageJump() {
        imageViewMerchPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_paymentInformationFragment_to_merchFragment);
        });
        imageViewGroupPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_paymentInformationFragment_to_groupFragment);
        });
        imageViewSuccessPag.setOnClickListener(view -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_paymentInformationFragment_to_reachFragment);
        });
        imageViewPaymentPag.setOnClickListener(view -> {

        });
    }

    /**
     * 付款方式選擇
     */
    public void handlePaymentMethod() {
        // 準備map資料
        mapPaymentMethod.put(0, "付款方式");
        mapPaymentMethod.put(1, "面交");
        mapPaymentMethod.put(2, "信用卡");
        // 準備放入Spinner內的String
        List<String> strings = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mapPaymentMethod.entrySet()) {
            strings.add(entry.getValue());
        }
        // 實例化Adapter物件 (Context, 外觀, 顯示的List<String>)
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(activity, R.layout.spinner_group_insert_category, strings);
        // 設定要下拉的樣式
        adapter.setDropDownViewResource(R.layout.spinner_group_insert_category);
        spinnerPaymentMethod.setAdapter(adapter); // Adapter 設定進 spType
    }

    /**
     * 已付款狀態選擇
     */
    public void handlePaymentStatus() {
        // 準備map資料
        mapPaymentStatus.put(0, "付款狀態");
        mapPaymentStatus.put(1, "已付款");
        mapPaymentStatus.put(2, "未付款");
        // 準備放入Spinner內的String
        List<String> strings = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mapPaymentStatus.entrySet()) {
            strings.add(entry.getValue());
        }
        // 實例化Adapter物件 (Context, 外觀, 顯示的List<String>)
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(activity, R.layout.spinner_group_insert_category, strings);
        // 設定要下拉的樣式
        adapter.setDropDownViewResource(R.layout.spinner_group_insert_category);
        spinnerPaymentStatus.setAdapter(adapter); // Adapter 設定進 spType
    }

    /**
     * spinner監聽
     */
    public void handleSpinnerListener() {
        // 付款方式選擇
        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 已付款狀態選擇
        spinnerPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    /**
     * 篩選已達標的團購
     */
    public void handleFilterGroups() {
        // 選出目前
    }
}