package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member_order.MemberOrder;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMemberOrderFragment extends Fragment {
    private final String TAG = "TAG_MemberCenterMyOrder";
    private Activity activity;
    private Member member;
    private List<MemberOrder> memberOrderList;
    private List<MemberOrder> memberOrderFilter = new ArrayList<>();
    private RecyclerView recyclerView;
    private Spinner memberOrderSpinner;
    private Map<Integer, String> mapGroupStatus = new HashMap<>(); // 團購種類MAP
    private int groupStatus = 0; // 目前團購spinner的狀態
    private String groupSearch = ""; // 搜尋的字串

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();

        String jsonIn = memberRemoteAccess(activity,member,"getMyMemberOrder");
        Type listType = new TypeToken<List<MemberOrder>>() {}.getType();
        memberOrderList = new Gson().fromJson(jsonIn, listType);

//        Log.d(TAG, memberOrderList.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_center_member_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.svOrderSearch);
        memberOrderSpinner = view.findViewById(R.id.memberOrderSpinner);

        recyclerView = view.findViewById(R.id.rvMemberCenterOrder);
        recyclerView.setAdapter(new MyAdapter(activity,memberOrderList));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));


        //spinner
        mapGroupStatus.put(0, "團購狀態");
        mapGroupStatus.put(1, "揪團中");
        mapGroupStatus.put(2, "達標");
        mapGroupStatus.put(3, "揪團失敗");
        List<String> strings = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mapGroupStatus.entrySet()) {
            strings.add(entry.getValue());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,R.layout.spinner_seller,strings);
        adapter.setDropDownViewResource(R.layout.spinner_seller);
        memberOrderSpinner.setAdapter(adapter);
        memberOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    groupStatus = 0;
                    updateFilterMemberOrder();
                    return;
                }
                // 抓取Spinner
                String spinnerSelected = parent.getItemAtPosition(position).toString();
                for (Map.Entry<Integer, String> entry : mapGroupStatus.entrySet()) {
                    if (entry.getValue().equals(spinnerSelected)) {
//                        Log.d(TAG,entry.getValue());
                        groupStatus = entry.getKey();
                        // 篩選條件更新
                        updateFilterMemberOrder();
                        return;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    groupSearch = "";
                } else {
                    groupSearch = newText;
                }
                // 篩選條件更新
                updateFilterMemberOrder();
                return true;
            }
        });

    }

    private void updateFilterMemberOrder() {
        // 每次清空
        memberOrderFilter.clear();
        // 如果都沒有篩選條件就還原
        if (groupSearch.isEmpty() && groupStatus == 0) {
            showMyOrder(memberOrderList);
            return;
        }
        // 篩選條件一 or 一二
        if (groupSearch.isEmpty()) {
            for(MemberOrder memberOrder : memberOrderList) {
                if (memberOrder.getGroupStatus() == groupStatus) {
                    memberOrderFilter.add(memberOrder);
                }
            }
        }else {
            for(MemberOrder memberOrder : memberOrderList) {
                if (
                    // 強制轉大寫，contains() -> 是否包含()
                        memberOrder.getGroupName().toUpperCase().contains(groupSearch.toUpperCase()) &&
                                (groupStatus == memberOrder.getGroupStatus() || groupStatus == 0)
                ) {
                    memberOrderFilter.add(memberOrder);
                }
            }
        }
        showMyOrder(memberOrderFilter);
    }

    private void showMyOrder(List<MemberOrder> memberOrderList) {
        if (memberOrderList == null || memberOrderList.isEmpty()) {
        }
        MyAdapter myAdapter = (MyAdapter) recyclerView.getAdapter();
        if(myAdapter == null){
            recyclerView.setAdapter(new MyAdapter(activity,memberOrderList));
        }else{
            myAdapter.setMemberOrders(memberOrderList);
            myAdapter.notifyDataSetChanged();
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        Activity activity;
        List<MemberOrder> memberOrderList;

        public MyAdapter(Activity activity , List<MemberOrder> memberOrderList){
            this.activity = activity;
            this.memberOrderList = memberOrderList;
        }

        void setMemberOrders(List<MemberOrder> memberOrders) {
            this.memberOrderList = memberOrders;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(activity).inflate(R.layout.item_view_member_order,parent,false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMemberOrderFragment.MyViewHolder holder, int position) {
            final MemberOrder memberOrder = memberOrderList.get(position);


//            團購狀態 (1.揪團中 2.達標 3.失敗or放棄)
            if(memberOrder.getGroupStatus() == 1){
                holder.groupStatus.setImageResource(R.drawable.seller_status1);
            }else if(memberOrder.getGroupStatus() == 2){
                holder.groupStatus.setImageResource(R.drawable.seller_status3);
            }else{
                holder.groupStatus.setImageResource(R.drawable.seller_status2);
            }

            holder.groupName.setText(memberOrder.getGroupName());
            holder.memberOrderId.setText(getString(R.string.text_member_order_id) + memberOrder.getMemberOrderId());
            holder.totalPrice.setText(getString(R.string.text_total_price) + memberOrder.getTotal());
            holder.status.setText(memberOrder.isDeliverStatus() ? "已出貨" : "未出貨");

            String str = "";//收款方式 (1.面交 2.信用卡 3.兩者皆可)
            if(memberOrder.getPayentMethod() == 1){
                str = "面交";
            }else if(memberOrder.getPayentMethod() == 2){
                str = "信用卡";
            }else{
                str = "兩者皆可";
            }
            holder.paymentMethod.setText(getString(R.string.text_paymentmethod) + str);

            holder.toDetails.setOnClickListener(v -> {

                Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

                String orderDetailsJson = gson.toJson(memberOrder.getMemberOrderDetailsList());

                //取地區資料
                List<Location> locations = LocationControl.getLocationByGroupId(activity,memberOrder.getGroupId());
                String locationsJson = gson.toJson(locations);

                Bundle bundle = new Bundle();
                bundle.putString("OrderDetails", orderDetailsJson);
                bundle.putString("Locations",locationsJson);
                bundle.putString("GroupStatus",String.valueOf(memberOrder.getPayentMethod()));
                bundle.putInt("MemberOrderID", memberOrder.getMemberOrderId());

                Navigation.findNavController(v).navigate(R.id.action_memberCenterMemberOrderFragment_to_memberCenterOrderDetailsFragment,bundle);
            });
        }

        @Override
        public int getItemCount() {
            return memberOrderList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView groupName,memberOrderId,totalPrice,status,paymentMethod,toDetails;
        ImageView groupStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.memberOrderGroup);
            memberOrderId = itemView.findViewById(R.id.memberOrderId);
            totalPrice = itemView.findViewById(R.id.memberOrderTotalPrice);
            status = itemView.findViewById(R.id.memberOrderDeliverStatus);
            paymentMethod = itemView.findViewById(R.id.memberOrderPaymentMethod);
            groupStatus = itemView.findViewById(R.id.memberOrderstatus);
            toDetails = itemView.findViewById(R.id.memberOrdertoDetails);

        }
    }


}


