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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;
import idv.tfp10101.iamin.network.RemoteAccess;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMemberOrderFragment extends Fragment {
    private final String TAG = "TAG_MemberCenterMyOrder";
    private Activity activity;
    private Member member;
    private Group group;
    private List<MemberOrder> memberOrderList;
    private RecyclerView recyclerView;
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

        recyclerView = view.findViewById(R.id.rvMemberCenterOrder);
        recyclerView.setAdapter(new MyAdapter(activity,memberOrderList));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    showMyOrder(memberOrderList);
                }else{
                    List<MemberOrder> searchOrders = new ArrayList<>();
                    for(MemberOrder result : memberOrderList){
                        if (String.valueOf(result.getGroupId()).toUpperCase().contains(query.toUpperCase())) {
                            searchOrders.add(result);
                        }

                    }showMyOrder(searchOrders);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty())
                    showMyOrder(memberOrderList);
                return false;
            }
        });

    }

    private void showMyOrder(List<MemberOrder> memberOrderList) {
        if (memberOrderList == null || memberOrderList.isEmpty()) {
            Toast.makeText(activity,"no memberOrders found", Toast.LENGTH_SHORT).show();
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

            group = GroupControl.getGroupbyId(activity,memberOrder.getGroupId());

//            團購狀態 (1.揪團中 2.達標 3.失敗or放棄)
            String str1 = "";
            if(group.getGroupStatus() == 1){
                str1 = "揪團中";
            }else if(group.getGroupStatus() == 2){
                str1 = "達標";
            }else{
                str1 = "放棄";
            }

            holder.groupStatus.setText(str1);
            holder.groupName.setText(group.getName());
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

            holder.itemView.setOnClickListener(v -> {

                Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

                String orderDetailsJson = gson.toJson(memberOrder.getMemberOrderDetailsList());
//                List<MemberOrderDetails> list = memberOrder.getMemberOrderDetailsList();
//                Log.d(TAG,"list: " + list.getClass());
//                Log.d(TAG,"orderDetailsJson: " + orderDetailsJson.getClass());

                //取地區資料
                List<Location> locations = LocationControl.getLocationByGroupId(activity,memberOrder.getGroupId());
                String locationsJson = gson.toJson(locations);



                Bundle bundle = new Bundle();
                bundle.putString("OrderDetails", orderDetailsJson);
                bundle.putString("Locations",locationsJson);
                bundle.putString("GroupStatus",String.valueOf(memberOrder.getPayentMethod()));

                Navigation.findNavController(v).navigate(R.id.action_memberCenterMemberOrderFragment_to_memberCenterOrderDetailsFragment,bundle);
            });
        }

        @Override
        public int getItemCount() {
            return memberOrderList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView groupName,memberOrderId,totalPrice,status,paymentMethod,groupStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.memberOrderGroup);
            memberOrderId = itemView.findViewById(R.id.memberOrderId);
            totalPrice = itemView.findViewById(R.id.memberOrderTotalPrice);
            status = itemView.findViewById(R.id.memberOrderDeliverStatus);
            paymentMethod = itemView.findViewById(R.id.memberOrderPaymentMethod);
            groupStatus = itemView.findViewById(R.id.memberOrderstatus);

        }
    }


}


