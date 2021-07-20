package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyWallet;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.network.RemoteAccess;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMemberOrderFragment extends Fragment {
    private final String TAG = "TAG_MemberCenterMyOrder";
    private Activity activity;
    private Member member;
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

    private void showMyOrder(List<MemberOrder> memberOrders) {
        if (memberOrders == null || memberOrders.isEmpty()) {
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
            View itemView = LayoutInflater.from(activity).inflate(R.layout.member_order_listview,parent,false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMemberOrderFragment.MyViewHolder holder, int position) {
            final MemberOrder memberOrder = memberOrderList.get(position);
            holder.groupId.setText("GroupTitle: " + memberOrder.getGroupId());
            holder.memberOrderId.setText("ID: " + memberOrder.getMemberOrderId());
            holder.totalPrice.setText("TotalPrice: " + memberOrder.getTotal());
            holder.status.setText(memberOrder.isDeliverStatus() ? "已出貨" : "未出貨");
            holder.paymentMethod.setText("PaymentMethod: " + memberOrder.getPayentMethod());

            holder.itemView.setOnClickListener(v -> {

                String orderDetailsJson = new Gson().toJson(memberOrderList.get(position).getMemberOrderDetailsList());

                Bundle bundle = new Bundle();
                bundle.putString("OrderDetails", orderDetailsJson);

                Navigation.findNavController(v).navigate(R.id.action_memberCenterMemberOrderFragment_to_memberCenterOrderDetailsFragment);

            });

        }

        @Override
        public int getItemCount() {
            return memberOrderList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView groupId,memberOrderId,totalPrice,status,paymentMethod;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            groupId = itemView.findViewById(R.id.memberOrderGroup);
            memberOrderId = itemView.findViewById(R.id.memberOrderId);
            totalPrice = itemView.findViewById(R.id.memberOrderTotalPrice);
            status = itemView.findViewById(R.id.memberOrderDeliverStatus);
            paymentMethod = itemView.findViewById(R.id.memberOrderPaymentMethod);


        }
    }

}


