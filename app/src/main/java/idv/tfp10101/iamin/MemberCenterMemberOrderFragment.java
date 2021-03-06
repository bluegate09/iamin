package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.tv.TvContentRating;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tfp10101.iamin.Rating.Rating;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyLoadingBar;
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
    private Map<Integer, String> mapGroupStatus = new HashMap<>(); // ????????????MAP
    private int groupStatus = 0; // ????????????spinner?????????
    private String groupSearch = ""; // ???????????????


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("????????????");

        member = MemberControl.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_center_member_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String jsonIn = memberRemoteAccess(activity,member,"getMyMemberOrder");
        Type listType = new TypeToken<List<MemberOrder>>() {}.getType();
        memberOrderList = new Gson().fromJson(jsonIn, listType);

        SearchView searchView = view.findViewById(R.id.svOrderSearch);
        memberOrderSpinner = view.findViewById(R.id.memberOrderSpinner);
        try {
            MyLoadingBar.dismissLoadingBar();
        }catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView = view.findViewById(R.id.rvMemberCenterOrder);
        recyclerView.setAdapter(new MyAdapter(activity,memberOrderList));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        //spinner
        mapGroupStatus.put(0, "????????????");
        mapGroupStatus.put(1, "?????????");
        mapGroupStatus.put(2, "??????");
        mapGroupStatus.put(3, "????????????");
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
                // ??????Spinner
                String spinnerSelected = parent.getItemAtPosition(position).toString();
                for (Map.Entry<Integer, String> entry : mapGroupStatus.entrySet()) {
                    if (entry.getValue().equals(spinnerSelected)) {
//                        Log.d(TAG,entry.getValue());
                        groupStatus = entry.getKey();
                        // ??????????????????
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
                // ??????????????????
                updateFilterMemberOrder();
                return true;
            }
        });
    }



    private void updateFilterMemberOrder() {
        // ????????????
        memberOrderFilter.clear();
        // ????????????????????????????????????
        if (groupSearch.isEmpty() && groupStatus == 0) {
            showMyOrder(memberOrderList);
            return;
        }
        // ??????????????? or ??????
        if (groupSearch.isEmpty()) {
            for(MemberOrder memberOrder : memberOrderList) {
                if (memberOrder.getGroupStatus() == groupStatus) {
                    memberOrderFilter.add(memberOrder);
                }
            }
        }else {
            for(MemberOrder memberOrder : memberOrderList) {
                if (
                    // ??????????????????contains() -> ????????????()
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
        MyAdapter myAdapter = (MyAdapter) recyclerView.getAdapter();
        if (memberOrderList == null || memberOrderList.isEmpty()) {
            Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
            myAdapter.setMemberOrders(memberOrderList);
            myAdapter.notifyDataSetChanged();
            return ;
        }
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
//            ???????????? (1.????????? 2.?????? 3.??????or??????)
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
            holder.status.setText(memberOrder.isDeliverStatus() ? "?????????" : "?????????");

            String str = "";//???????????? (1.?????? 2.????????? 3.????????????)
            if(memberOrder.getPayentMethod() == 1){
                str = "??????";
            }else if(memberOrder.getPayentMethod() == 2){
                str = "?????????";
            }else{
                str = "????????????";
            }
            holder.paymentMethod.setText(getString(R.string.text_paymentmethod) + str);

            holder.toDetails.setOnClickListener(v -> {

                Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

                String orderDetailsJson = gson.toJson(memberOrder.getMemberOrderDetailsList());

                //???????????????
                List<Location> locations = LocationControl.getLocationByGroupId(activity,memberOrder.getGroupId());
                String locationsJson = gson.toJson(locations);

                Bundle bundle = new Bundle();
                bundle.putString("OrderDetails", orderDetailsJson);
                bundle.putString("Locations",locationsJson);

                String memberOrderJson = gson.toJson(memberOrder);
                bundle.putString("MemberOrder",memberOrderJson);

//                bundle.putString("GroupStatus",String.valueOf(memberOrder.getPayentMethod()));
//                bundle.putInt("TotalPrice",memberOrder.getTotal());
//                bundle.putInt("MemberOrderID", memberOrder.getMemberOrderId());
                bundle.putBoolean("ReceivePaymentStatus",memberOrder.isReceivePaymentStatus());

                Navigation.findNavController(v).navigate(R.id.action_memberCenterMemberOrderFragment_to_memberCenterOrderDetailsFragment,bundle);
            });

            //?????????????????????

                Rating checkRating = MemberControl.checkIsRate(activity,memberOrder.getMemberOrderId());
                if(checkRating == null){
                    if(!(memberOrder.isDeliverStatus() && memberOrder.isReceivePaymentStatus())){
                        holder.tvRatingButton.setVisibility(View.GONE);
                    }else{
                        holder.tvRatingButton.setVisibility(View.VISIBLE);
                        holder.tvRatingButton.setEnabled(true);
                        holder.tvRatingButton.setText("??????????????????");
                    }
                }else{
                        holder.tvRatingButton.setEnabled(false);
                        holder.tvRatingButton.setText("?????????");
                }




            holder.tvRatingButton.setOnClickListener(v -> {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_rating,null);
                    dialogBuilder.setView(dialogView);

                EditText message = dialogView.findViewById(R.id.edt_rating_message);
                Button btButton = dialogView.findViewById(R.id.dialog_rating_button);
                RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    }
                });

                btButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Rating rating = new Rating(memberOrder.getMemberOrderId(),
                                                   member.getId(),
                                                   memberOrder.getMemberOrderDetailsList().get(0).getMerch().getMemberId(),//?????????seller_memberId ??????index = 0??????
                                                   (int) ratingBar.getRating(),
                                                   message.getText().toString(),
                                                   new Timestamp(System.currentTimeMillis()),
                                                   memberOrder.getGroupName());

//                        Log.d(TAG,"GROUP_NAME: " + memberOrder.getGroupName());
                        MemberControl.submitRating(activity,rating);

                        alertDialog.cancel();

                        holder.tvRatingButton.setEnabled(false);
                        holder.tvRatingButton.setText("?????????");

                        Toast.makeText(activity, getString(R.string.text_rating_submit), Toast.LENGTH_SHORT).show();

                    }
                });

            });


        }

        @Override
        public int getItemCount() {
            return memberOrderList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView groupName,memberOrderId,totalPrice,status,paymentMethod,toDetails,tvRatingButton;
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
            tvRatingButton = itemView.findViewById(R.id.memberOrderToRating);

        }
    }


}


