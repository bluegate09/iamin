package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order.MemberOrderControl;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetailsControl;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
    private Spinner spinnerReachGroup;
    private Spinner spinnerPaymentMethod;
    private Spinner spinnerPaymentStatus;
    private ImageView imageViewQRcode;
    // 物件
    private Member member;
    private Group reachGroup;
    private List<MemberOrder> allMemberOrders = new ArrayList<>(); // 此團購所有的買家訂單
    private List<MemberOrder> memberOrders = new ArrayList<>(); // 目前過濾的買家訂單
    private List<MemberOrderDetails> memberOrderDetails = new ArrayList<>();
    private List<Member> buyers = new ArrayList<>(); // 目前選擇團購的買家
    private List<Group> reachGroups = new ArrayList<>(); // 取得目前已達標的團購
    private Map<Integer, String> mapPaymentMethod = new HashMap<>();
    private Map<Integer, String> mapPaymentStatus = new HashMap<>();
    private int payentMethod = 0; // 顯示目前的收款方式
    private int payentStatus = -1; // 顯示目前的收款狀態

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewMerchPag = view.findViewById(R.id.imageViewMerchPag);
        imageViewGroupPag = view.findViewById(R.id.imageViewGroupPag);
        imageViewSuccessPag = view.findViewById(R.id.imageViewSuccessPag);
        imageViewPaymentPag = view.findViewById(R.id.imageViewPaymentPag);
        buttonUpdate = view.findViewById(R.id.buttonSubmit);
        spinnerReachGroup = view.findViewById(R.id.spinnerReachGroup);
        spinnerPaymentMethod = view.findViewById(R.id.spinnerPaymentMethod);
        spinnerPaymentStatus = view.findViewById(R.id.spinnerPaymentStatus);
        imageViewQRcode = view.findViewById(R.id.imageViewQRcode);
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

        /** 抓取會員ID */
        member = MemberControl.getInstance();
        // 抓取有達標的團購
        reachGroups = GroupControl.getReachGroup(activity, member.getId());
        if (reachGroups == null || reachGroups.isEmpty()) {
            Toast.makeText(activity, "目前沒有達標的團購", Toast.LENGTH_SHORT).show();
            return;
        }


        // 已達標團購選擇
        handleReachGroup();

        // 預設顯示第一筆成團的資訊
        handleSetNewData(reachGroups.get(0));

        // spinner監聽
        handleSpinnerListener();

        // 送交
        handleSumbit();

        // QRcode掃描
        handleQRcode();
    }

    /**
     * 抓新資料後，選項重置+刷新RecyclerView
     */
    private void handleSetNewData(Group group) {
        /** 抓取會員訂單 */
        memberOrders = MemberOrderControl.getMemberOrderByGroupId
                (activity, group.getGroupId(), "PaymentInformation");
        // 紀錄-此團購所有的買家訂單
        allMemberOrders = memberOrders;
        /** 抓取會員訂單明細 */
        memberOrderDetails = MemberOrderDetailsControl.getMemberOrderDetailsByMemberOrders
                (activity, memberOrders, "PaymentInformation");

        handleRefresh();
    }
    /**
     * 選項重置+刷新RecyclerView
     */
    private void handleRefresh() {
        // 付款方式選擇
        handlePaymentMethod();
        // 已付款狀態選擇
        handlePaymentStatus();
        //
        showMemberOrderDetails(memberOrders);
    }

    /**
     * 顯示會員訂單明細
     */
    private void showMemberOrderDetails(List<MemberOrder> memberOrders) {
        /** RecyclerView */
        // 檢查
        MODAdapter modAdapter = (MODAdapter) recyclerViewMember.getAdapter();
        if (modAdapter == null) {
            recyclerViewMember.setAdapter(new MODAdapter(activity, memberOrders));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMember.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            modAdapter.setMemberOrders(memberOrders);
            modAdapter.notifyDataSetChanged();
        }
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
     * 已達標團購選擇
     */
    private void handleReachGroup() {
        // 準備放入Spinner內的String
        List<String> strings = new ArrayList<>();
        for (Group group : reachGroups) {
            strings.add(group.getName());
        }
        // 實例化Adapter物件 (Context, 外觀, 顯示的List<String>)
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(activity, R.layout.spinner_group_insert_category, strings);
        // 設定要下拉的樣式
        adapter.setDropDownViewResource(R.layout.spinner_group_insert_category);
        spinnerReachGroup.setAdapter(adapter); // Adapter 設定進 spType
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
        mapPaymentStatus.put(-1, "付款狀態");
        mapPaymentStatus.put(0, "未付款");
        mapPaymentStatus.put(1, "已付款");
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
        // 已達標團購選擇
        spinnerReachGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 抓取已選擇的團購
                reachGroup = reachGroups.get(position);
                /** 使用選擇的團購抓取目前的會員購買清單 */
                // 抓取會員訂單
                memberOrders = MemberOrderControl.getMemberOrderByGroupId
                        (activity, reachGroup.getGroupId(), "PaymentInformation");
                // 紀錄-此團購所有的買家訂單
                allMemberOrders = memberOrders;
                // 抓取會員訂單明細
                memberOrderDetails = MemberOrderDetailsControl.getMemberOrderDetailsByMemberOrders
                        (activity, memberOrders, "PaymentInformation");

                showMemberOrderDetails(memberOrders);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 付款方式選擇
        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = "";
                // 重新載入-此團購所有的買家訂單
                memberOrders = allMemberOrders;
                // 抓取現在的收款方式
                string = parent.getItemAtPosition(position).toString();
                for (Map.Entry<Integer, String> entry : mapPaymentMethod.entrySet()) {
                    if (entry.getValue().equals(string)) {
                        payentMethod = entry.getKey();
                        break;
                    }
                }
                // 過濾 memberOrders
                List<MemberOrder> filterMemberOrders = new ArrayList<>();
                for (MemberOrder memberOrder : memberOrders) {
                    if (payentMethod == 0) {
                        switch (payentStatus) {
                            case 0:
                                if (!memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            case 1:
                                if (memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                        }
                    } else {
                        switch (payentStatus) {
                            case 0:
                                if (memberOrder.getPayentMethod() == payentMethod &&
                                        !memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            case 1:
                                if (memberOrder.getPayentMethod() == payentMethod &&
                                        memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            default:
                                if (memberOrder.getPayentMethod() == payentMethod) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                        }
                    }
                }
                // 覆蓋 memberOrders
                if (!filterMemberOrders.isEmpty()) {
                    memberOrders = filterMemberOrders;
                }
                showMemberOrderDetails(memberOrders);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 已付款狀態選擇
        spinnerPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = "";
                // 重新載入-此團購所有的買家訂單
                memberOrders = allMemberOrders;
                // 抓取現在的收款狀態
                string = parent.getItemAtPosition(position).toString();
                for (Map.Entry<Integer, String> entry : mapPaymentStatus.entrySet()) {
                    if (entry.getValue().equals(string)) {
                        payentStatus = entry.getKey();
                        break;
                    }
                }
                // 過濾 memberOrders
                List<MemberOrder> filterMemberOrders = new ArrayList<>();
                for (MemberOrder memberOrder : memberOrders) {
                    if (payentMethod == 0) {
                        switch (payentStatus) {
                            case 0 :
                                if (!memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            case 1 :
                                if (memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                        }
                    }else {
                        switch (payentStatus) {
                            case 0 :
                                if (memberOrder.getPayentMethod() == payentMethod &&
                                        !memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            case 1 :
                                if (memberOrder.getPayentMethod() == payentMethod &&
                                        memberOrder.isReceivePaymentStatus()) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                            default:
                                if (memberOrder.getPayentMethod() == payentMethod) {
                                    filterMemberOrders.add(memberOrder);
                                }
                                break;
                        }
                    }
                }
                // 覆蓋 memberOrders
                if (!filterMemberOrders.isEmpty()) {
                    memberOrders = filterMemberOrders;
                }
                showMemberOrderDetails(memberOrders);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 送交
     */
    private void handleSumbit() {
        buttonUpdate.setOnClickListener(view -> {
            /** AlertDialog */
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setTitle("準備更新訂單");
            dialog.setMessage("更新訂單後就不可以修改了喔！");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MemberOrderControl.updateMemberOrders(activity, memberOrders, "PaymentInformation");
                    // 在本地更新ReceivePaymentStatus
                    for (MemberOrder memberOrder : memberOrders) {
                        if (memberOrder.isDeliverStatus()) {
                            memberOrder.setReceivePaymentStatus(true);
                        }
                    }
                    // 選項重置+刷新
                    handleRefresh();
                    Toast.makeText(activity, "更新成功", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.setNeutralButton("取消",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });
            dialog.show();
        });
    }

    /**
     * QRcode掃描
     */
    private void handleQRcode() {
        imageViewQRcode.setOnClickListener(view -> {
            /* 若在Activity內需要呼叫IntentIntegrator(Activity)建構式建立IntentIntegrator物件；
             * 而在Fragment內需要呼叫IntentIntegrator.forSupportFragment(Fragment)建立物件，
             * 掃瞄完畢時，Fragment.onActivityResult()才會被呼叫 */
            // IntentIntegrator integrator = new IntentIntegrator(this);
            // 設定現在是哪一頁 (之後要跳轉回來)
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(PaymentInformationFragment.this);
            // 設定要啟動 BarcodeImageEnabled
            integrator.setBarcodeImageEnabled(true);
            // 如果有掃描到是否要發出聲響
            integrator.setBeepEnabled(true);
            // 0:主鏡頭 1:後鏡頭
            integrator.setCameraId(0);
            // 畫面是否鎖定 (目前失效 強制鎖定)
            integrator.setOrientationLocked(true);
            // 進入掃描畫面加入提示文字
            integrator.setPrompt("Scan a QR Code");
            //

            // Initiates a scan (啟動掃描)
            integrator.initiateScan();
        });
    }

    /**
     * QRcode掃描 - 回傳
     */
    @Override
    /* 目前IntentIntegrator.initiateScan()內容仍是呼叫startActivityForResult()，
       所以仍須覆寫onActivityResult()，即使onActivityResult()已經被列為deprecated
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null && intentResult.getContents() != null) {
            Log.d(Constants.TAG, "QR Code Result: " + intentResult.getContents());
            // Str -> int
            int memberOderId = Integer.parseInt(intentResult.getContents());
            // 如果此會員訂單非當下的團購，不給更新
            boolean isMemberOrder = false;
            for (MemberOrder memberOrder : allMemberOrders) {
                if (memberOrder.getMemberOrderId() == memberOderId) {
                    isMemberOrder = true;
                }
            }
            if (!isMemberOrder) {
                Toast.makeText(activity, "QRcode並非本次團購", Toast.LENGTH_SHORT).show();
                return;
            }
            // 更新發貨狀態
            int result = MemberOrderControl.updateDeliverStatus(activity, memberOderId, "PaymentInformation");
            // 重新刷新
            if (result > 0) {
                for (MemberOrder memberOrder : memberOrders) {
                    if (memberOrder.getMemberOrderId() == memberOderId) {
                        memberOrder.setDeliverStatus(true);
                        // 在本地更新ReceivePaymentStatu
                        memberOrder.setReceivePaymentStatus(true);
                        // 選項重置+刷新
                        handleRefresh();
                        Toast.makeText(activity, "掃描成功！", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }else {
                Toast.makeText(activity, "DB更新失敗", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(Constants.TAG, "ResultNotFound");
        }
    }

    /**
     * 自定義Adapter 繼承 RecyclerView 的 Adapter
     * 1. 建立Context & 一些需要的資訊，並constructor
     * 2. 實作 RecyclerView.ViewHolder 給 Adapter使用
     * 3. 設定父類別泛型型態
     * 4. 自動建立 Override 方法 (onCreateViewHolder, onBindViewHolder, getItemCount)
     */
    private class MODAdapter extends RecyclerView.Adapter<MODAdapter.MyViewHolder> {
        private List<MemberOrder> rsMemberOrders;
        private LayoutInflater layoutInflater;
        boolean[] PStatus; // 收款狀態
        boolean[] DStatus; // 發貨狀態
        boolean[] DStatusOriginal; // DB的發貨狀態

        public MODAdapter(Context context, List<MemberOrder> memberOrders) {
            layoutInflater = LayoutInflater.from(context);
            rsMemberOrders = memberOrders;
            DStatus = new boolean[memberOrders.size()];
            PStatus = new boolean[memberOrders.size()];
            DStatusOriginal = new boolean[memberOrders.size()];
            // 保存DB的發貨狀態
            for (int i = 0; i < memberOrders.size(); i++) {
                DStatusOriginal[i] = memberOrders.get(i).isDeliverStatus();
            }
        }

        public void setMemberOrders(List<MemberOrder> memberOrders) {
            rsMemberOrders = memberOrders;
            DStatus = new boolean[memberOrders.size()];
            PStatus = new boolean[memberOrders.size()];
            DStatusOriginal = new boolean[memberOrders.size()];
            // 保存DB的發貨狀態
            for (int i = 0; i < memberOrders.size(); i++) {
                DStatusOriginal[i] = memberOrders.get(i).isDeliverStatus();
            }
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewBuyerName;
            private TextView textViewContactNumber;
            private ImageView imageViewConfirm;
            private TextView textViewPaymentStatus;
            private LinearLayout lineraLayout;
            // 內容顯示控制
            private ConstraintLayout layoutMain;
            private ConstraintLayout layoutAttach;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                textViewBuyerName = itemView.findViewById(R.id.textViewBuyerName);
                textViewContactNumber = itemView.findViewById(R.id.textViewContactNumber);
                imageViewConfirm = itemView.findViewById(R.id.imageViewConfirm);
                textViewPaymentStatus = itemView.findViewById(R.id.textViewPaymentStatus);
                lineraLayout = itemView.findViewById(R.id.lineraLayout);
                // 內容顯示控制
                layoutMain = itemView.findViewById(R.id.layoutMain);
                layoutAttach = itemView.findViewById(R.id.layoutAttach);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_payment_information, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final MemberOrder rsMemberOrder = rsMemberOrders.get(position); // 第幾個group
            // 姓名
            holder.textViewBuyerName.setText(rsMemberOrder.getNickname());
            // 電話
            holder.textViewContactNumber.setText(rsMemberOrder.getPhone());
            // 付款狀態
            if (rsMemberOrder.isReceivePaymentStatus()) {
                PStatus[position] = true;
            }
            // 看PStatus更改值
            if (PStatus[position]) {
                holder.textViewPaymentStatus.setText("已付款");
                holder.textViewPaymentStatus.setTextColor(resources.getColor(R.color.colorGreen));
            }else {
                holder.textViewPaymentStatus.setText("尚未付款");
                holder.textViewPaymentStatus.setTextColor(resources.getColor(R.color.colorRed));
            }
            // LineraLayout
            holder.lineraLayout.removeAllViews(); // 先清空
            for (MemberOrderDetails memberOrderDetail : memberOrderDetails) {
                if (memberOrderDetail.getMemberOrderId() == rsMemberOrder.getMemberOrderId()) {
                    String string =
                            memberOrderDetail.getName() + "\t\t\t數量：" + memberOrderDetail.getQuantity();
                    // TextView
                    TextView textView = new TextView(activity);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.bottomMargin = 8;
                    textView.setLayoutParams(layoutParams);
                    textView.setTextSize(14);
                    textView.setText(string);
                    // linearLayout 加入一筆
                    holder.lineraLayout.addView(textView);
                }
            }
            // 發貨狀態
            if (rsMemberOrder.isDeliverStatus()) {
                holder.imageViewConfirm.setImageResource(R.drawable.payment_information_confirm);
            }else {
                holder.imageViewConfirm.setImageResource(R.drawable.payment_information_unconfirm);
            }
            // 設定監聽 點擊勾勾紀錄狀態
            holder.imageViewConfirm.setOnClickListener(view -> {
                // 更改圖示
                int pos = holder.getBindingAdapterPosition();
                // 如果還沒勾選
                if (!DStatusOriginal[pos]) {
                    DStatus[pos] = !DStatus[pos]; // 更新發貨狀態
                    // 紀錄
                    rsMemberOrder.setDeliverStatus(DStatus[pos]);
                    memberOrders.get(position).setDeliverStatus(DStatus[pos]);
                    //  Adapter 呼叫 notifyDataSetChanged()，會全部重刷新 (重建一次RecyclerView)
                    notifyDataSetChanged();
                }else {
                    Toast.makeText(activity, "此訂單已經完成囉，不可再更改！", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return rsMemberOrders == null ? 0 : rsMemberOrders.size();
        }
    }
}