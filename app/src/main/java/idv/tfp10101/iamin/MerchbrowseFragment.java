package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order.MemberOrderControl;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetailsControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;
import idv.tfp10101.iamin.network.RemoteAccess;


public class MerchbrowseFragment extends Fragment {
    private Activity activity;
    private View view;
    private int groupID,sellerID,progress,goal,payment_method,group_status,condition_count;
    private int buyerChoose;//買家選擇的付款方式 1->面交,2->信用卡
    private String contact_number,caution;
    private Timestamp condition_Time;
    private List<Merch> localMerchs;
    private RecyclerView recyclerViewMerch;
    private Button btn_buy,btn_back,btn_next;
    private Member member;
    //商品圖片
    private List<byte[]> images = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        member = MemberControl.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_merchbrowse, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String url;
        JsonObject jsonObject;
        String jsonMember;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            member.setuUId(currentUser.getUid());
            Log.d("TAG_HOME","uUId: " + currentUser.getUid());
            url = RemoteAccess.URL_SERVER + "memberController";
            jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findbyUuid");
            jsonObject.addProperty("member", new Gson().toJson(member));
            jsonMember = RemoteAccess.getRemoteData(url, jsonObject.toString());
            member = new Gson().fromJson(jsonMember,Member.class);
            MemberControl.setMember(member);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        HashMap<String,Object> GrouphashMap;
        //取得HomeData打包過來的資料
        Bundle bundleMap = getArguments();
        if (bundleMap != null){
            GrouphashMap = (HashMap<String, Object>) bundleMap.getSerializable("Group");
            groupID = (Integer) GrouphashMap.get("GroupID");//取得團購ID
            sellerID = (Integer) GrouphashMap.get("SellerID");//取得賣家ID
            progress = (Integer) GrouphashMap.get("Progress");//取得當前進度
            goal = (Integer) GrouphashMap.get("Goal");//取得當前目標
            contact_number = (String) GrouphashMap.get("Contact_Number");//取得團購聯絡電話
            payment_method = (Integer) GrouphashMap.get("Payment_Method");//取得付款方法
            group_status = (Integer) GrouphashMap.get("Group_status");//取得團購狀態
            caution = (String) GrouphashMap.get("Caution");//取得注意事項
            condition_count = (Integer) GrouphashMap.get("Condition_count");//取得停單份數
            condition_Time = (Timestamp) GrouphashMap.get("Condition_Time");//取得停單時間
        }
        MerchControl.getAllMerchByGroupId(activity,groupID);
        localMerchs = MerchControl.getLocalMerchs();
        if (localMerchs == null || localMerchs.isEmpty()) {
            Toast.makeText(activity, R.string.textNoGroupsFound, Toast.LENGTH_SHORT).show();
        }

        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1,RecyclerView.HORIZONTAL);
        recyclerViewMerch.setLayoutManager(new StaggeredGridLayoutManager(1,RecyclerView.HORIZONTAL));

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerViewMerch);
        showMerchs(localMerchs);
        //取得商品列表總數
        int total = recyclerViewMerch.getLayoutManager().getItemCount();
        AtomicInteger count = new AtomicInteger();
        //預設back按鈕不能按
        btn_back.setEnabled(false);
        //如果商品總數只有1，也將next按鈕設為false
        if ((total - 1) == 0 ){
            btn_next.setEnabled(false);
        }
        //點選next按鈕跳轉至下一商品，並將back按鈕打開
        btn_next.setOnClickListener(v ->{
            recyclerViewMerch.smoothScrollToPosition(count.incrementAndGet());
            if (count.get() == (total-1)){
                btn_next.setEnabled(false);
            }
            btn_back.setEnabled(true);
        });
        btn_back.setOnClickListener(v ->{
            recyclerViewMerch.smoothScrollToPosition(count.decrementAndGet());
            if (count.get() == 0){
                btn_back.setEnabled(false);
            }
            btn_next.setEnabled(true);
        });
        //按下訂單前做判斷

        btn_buy.setOnClickListener(v ->{
            AlertDialog.Builder payment_methodDialog = new AlertDialog.Builder(activity);
            switch (payment_method) {
                case 1:
                    payment_methodDialog
                            .setTitle("團購付款方式")
                            .setMessage("該團購只支援面交")
                            .setPositiveButton("去買單!!",(dialog, which) -> {
                                buyerChoose = 1;
                                getOrder();
                            })
                            .setNegativeButton("我在想一下",(dialog, which) -> {return;})
                            .setCancelable(false)
                            .show();
                    break;
                case 2:
                    payment_methodDialog
                            .setTitle("團購付款方式")
                            .setMessage("該團購只支援信用卡")
                            .setPositiveButton("去買單!!",(dialog, which) -> {
                                buyerChoose = 2;
                                getOrder();
                            })
                            .setNegativeButton("我在想一下",(dialog, which) -> {return;})
                            .setCancelable(false)
                            .show();
                    break;
                case 3:
                    final String[] payment_method = {"面交","信用卡交易"};
                    buyerChoose = -1;
                    payment_methodDialog

                            .setTitle("團購付款方式")
                            .setSingleChoiceItems(payment_method, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    buyerChoose = which;
                                }
                            })
                            .setPositiveButton("去買單!!",(dialog, which) -> {
                                if (buyerChoose == -1){
                                    Toast.makeText(activity, "請選擇付款方式!!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                //判斷買家選擇面交還是信用卡
                                switch (buyerChoose){
                                    case 0:
                                        buyerChoose = 1;
                                        getOrder();
                                        break;
                                    case 1:
                                        buyerChoose = 2;
                                        getOrder();
                                        break;
                                    default:
                                    break;
                                }
                                //預設第一個選項位置是0,寫進table要+1 1->面交 2->信用卡
                            })
                            .setNegativeButton("我在想一下",(dialog, which) -> {return;})
                            .setCancelable(false)
                            .show();
                    break;

            }
        });
    }

    //取得買家下單
    private void getOrder(){
        int total_quantity = 0; //買家選擇的總數量
        int total_price = 0;    //買家商品總價(訂單)
        Map<Merch,Integer> maps = ((MerchAdapter) recyclerViewMerch.getAdapter()).getMerchsMap();

            for (Map.Entry<Merch, Integer> entry : maps.entrySet()) {
                Merch merch = entry.getKey();
                int amount = entry.getValue();  //取得買家所選商品數量
                int price = merch.getPrice();   //取得當見商品價錢
                int format_total = price * amount; //單件商品的價錢乘上數量
                total_price += format_total; //將每個商品的總價加起來
                total_quantity += amount; //將每個商品的數量加起來
//                Toast.makeText(activity, merch.getName()+"數量:"+String.valueOf(amount), Toast.LENGTH_SHORT).show();
            }
        //取得最新的團購資訊
        Group group = GroupControl.getGroupbyId(activity,groupID);
        if(group != null) {
            int progress = group.getProgress();
            int status = group.getGroupStatus();
            if ((total_quantity != 0)||(total_price != 0)){
                //判斷是否在結單時間內
                if (new Date().before(condition_Time)){
                    //判斷團購是否有設定最大購買上限 -1=沒設上限
                    if (condition_count != -1){
                        //判斷買家購買的數量加上當前進度是否過團購上限
                        if((total_quantity + progress) < condition_count){
                            MemberOrder memberOrder = new MemberOrder(
                                    0,
                                    member.getId(),
                                    groupID,
                                    buyerChoose,
                                    total_price,
                                    false,
                                    false
                            );
                            setMemberorder(memberOrder);
                            if ((total_quantity + progress) > goal){
                                status = 2;
                            }
                            Group updaategroup = new Group(
                                    group.getGroupId(),
                                    group.getMemberId(),
                                    group.getName(),
                                    total_quantity + progress,
                                    group.getGoal(),
                                    group.getCategoryId(),
                                    group.getGroupItem(),
                                    group.getContactNumber(),
                                    group.getPaymentMethod(),
                                    status,
                                    group.getCaution(),
                                    group.getPrivacyFlag(),
                                    group.getTotalAmount(),
                                    group.getAmount(),
                                    group.getConditionCount(),
                                    group.getConditionTime()
                            );
                            updateGroup(updaategroup);
                            //Toast.makeText(activity, "沒有超過上限!!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(activity, "已超過能夠買得最大上限請重新選擇!!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        //建立memberOrder資料
                        MemberOrder memberOrder = new MemberOrder(
                                0,
                                member.getId(),
                                groupID,
                                buyerChoose,
                                total_price,
                                false,
                                false
                        );
                       setMemberorder(memberOrder);
                        if ((total_quantity + progress) > goal){
                            status = 2;
                        }
                        Group updaategroup = new Group(
                                group.getGroupId(),
                                group.getMemberId(),
                                group.getName(),
                                total_quantity + progress,
                                group.getGoal(),
                                group.getCategoryId(),
                                group.getGroupItem(),
                                group.getContactNumber(),
                                group.getPaymentMethod(),
                                status,
                                group.getCaution(),
                                group.getPrivacyFlag(),
                                group.getTotalAmount(),
                                group.getAmount(),
                                group.getConditionCount(),
                                group.getConditionTime()
                        );
                        updateGroup(updaategroup);
                    }
                }else{
                    Toast.makeText(activity, "不好意思已經超過了結單時間!!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(activity, "請選擇商品及數量!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //建立買家訂單主表
    private void setMemberorder(MemberOrder memberOrder){

        //member_order_ID是回傳的自動編號值
        int member_order_ID = MemberOrderControl.insertMemberOrder(activity,memberOrder);
        //裝買家明細
        List<MemberOrderDetails> orderDetails = new ArrayList<>();
        Map<Merch,Integer> maps = ((MerchAdapter) recyclerViewMerch.getAdapter()).getMerchsMap();

        for (Map.Entry<Merch, Integer> entry : maps.entrySet()) {
            Merch merch = entry.getKey();
            int merchID = merch.getMerchId(); //取得商品ID
            int amount = entry.getValue();  //取得買家所選商品數量
            int price = merch.getPrice();   //取得當見商品價錢
            int format_total = price * amount; //單件商品的價錢乘上數量
            MemberOrderDetails memberOrderDetails = new MemberOrderDetails(
                    0,
                    member_order_ID,
                    merchID,
                    amount,
                    format_total
            );
            if (amount > 0 ) {
                orderDetails.add(memberOrderDetails);
            }
        }
        MemberOrderDetailsControl.insertMemberOrderDetails(activity,orderDetails);
    }

    private void updateGroup (Group group){
        int code = GroupControl.updateGroup(activity,group);
    }
    private void findView(View view) {
        recyclerViewMerch = view.findViewById(R.id.recyclerViewMerch);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity,2);
//        recyclerViewMerch.setLayoutManager(gridLayoutManager);

        btn_buy = view.findViewById(R.id.btn_buy);
        btn_back = view.findViewById(R.id.btn_back);
        btn_next = view.findViewById(R.id.btn_next);

    }

    private void showMerchs(List<Merch> localMerchs) {
        /** RecyclerView */
        // 檢查
        MerchbrowseFragment.MerchAdapter merchAdapter = (MerchbrowseFragment.MerchAdapter) recyclerViewMerch.getAdapter();
        if (merchAdapter == null) {
            recyclerViewMerch.setAdapter(new MerchbrowseFragment.MerchAdapter(activity, localMerchs));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMerch.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // 資訊重新載入刷新
            merchAdapter.setMerchs(localMerchs);
            merchAdapter.notifyDataSetChanged();
        }
    }

    private class MerchAdapter extends RecyclerView.Adapter<MerchAdapter.MyMerchViewHolder>{
        private Map<Merch, Integer> rsMerchs;
        private LayoutInflater layoutInflater;

        public MerchAdapter(Context context, List<Merch> merchs){
            layoutInflater = LayoutInflater.from(context);
            Map<Merch, Integer> merchsMap = new HashMap<>();
            for (Merch merch : merchs) {
                merchsMap.put(merch, 0);
            }
            rsMerchs = merchsMap;
        }

        public class MyMerchViewHolder extends RecyclerView.ViewHolder{
            TextView txv_merch_name,txv_merch_price,txv_commodity_description;
            TextView txv_amount;
            Banner banner;
            ImageView btn_sub,btn_add;
            public MyMerchViewHolder(@NonNull View itemView) {
                super(itemView);
                txv_merch_name = itemView.findViewById(R.id.txv_merch_name);
                txv_merch_price = itemView.findViewById(R.id.txv_merch_price);
                txv_commodity_description = itemView.findViewById(R.id.txv_commodity_description);
                txv_amount = itemView.findViewById(R.id.txv_amount);
                btn_add = itemView.findViewById(R.id.btn_add);
                btn_sub = itemView.findViewById(R.id.btn_sub);
                banner = itemView.findViewById(R.id.banner);
            }
        }

        public void setMerchs(List<Merch> merchs) {
            Map<Merch, Integer> merchsMap = new HashMap<>();
            for (Merch merch : merchs) {
                merchsMap.put(merch, 0);
            }
            rsMerchs = merchsMap;
        }
        public Map<Merch, Integer> getMerchsMap() { return rsMerchs; }

        @NonNull
        @Override
        public MyMerchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_merch_buyer,parent,false);
            return new MerchbrowseFragment.MerchAdapter.MyMerchViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MerchbrowseFragment.MerchAdapter.MyMerchViewHolder holder, int position) {
        final Merch rsMerch = (Merch) rsMerchs.keySet().toArray()[position];
        Integer num = rsMerchs.get(rsMerch);
        AtomicInteger amount = new AtomicInteger(num == null ? 0 : num);
        int merch_id = rsMerch.getMerchId();
        String merch_name = rsMerch.getName();
        int merch_price = rsMerch.getPrice();
        String merch_desc = rsMerch.getMerchDesc();
        holder.txv_amount.setText(String.valueOf(num == null ? 0 : num));
        holder.txv_merch_name.setText(merch_name);
        holder.txv_merch_price.setText("價格:"+String.valueOf(merch_price));
        holder.txv_commodity_description.setText("商品說明:\n"+merch_desc);

        holder.btn_sub.setOnClickListener(v ->{
            if (amount.get() > 0) {
                rsMerchs.put(rsMerch, amount.decrementAndGet());
                holder.txv_amount.setText(String.valueOf(amount.get()));
            }else{
                holder.txv_amount.setText(String.valueOf(0));
            }
        });
        holder.btn_add.setOnClickListener(v ->{
            rsMerchs.put(rsMerch, amount.incrementAndGet());
            holder.txv_amount.setText(String.valueOf(amount.get()));
        });

        // 發送商品圖片請求
        images = MerchControl.getMerchImgsById(activity, merch_id);
            List<Bitmap> bitmaps = new ArrayList<>();
            for (byte[] image: images) {
                if (image.length != 0){
                    bitmaps.add(BitmapFactory.decodeByteArray(image,0,image.length));
                }
            }
        holder.banner.addBannerLifecycleObserver((LifecycleOwner) activity)
                .setIndicator(new CircleIndicator(activity))
                .setAdapter(new BannerImageAdapter<Bitmap>(bitmaps) {
                    @Override
                    public void onBindView(BannerImageHolder holder, Bitmap data, int position, int size) {
                        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        holder.imageView.setImageBitmap(data);
                    }
                });

        }
        //設定回傳數量
        @Override
        public int getItemCount() { return rsMerchs == null ? 0 : rsMerchs.size();
        }
    }
}