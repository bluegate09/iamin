package idv.tfp10101.iamin;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberCenterMemberOrderDetailsFragment extends Fragment {
    private final String TAG = "TAG_orderDetails";
    private Activity activity;
    private List<MemberOrderDetails> memberOrderDetailsList;
    private RecyclerView recyclerView;
    private List<Location> locations;
    private TextView location,deadLine;
    private ListView listView;
    private ImageButton btGooglePay;
    private String paymentMethod;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String TAG = "TAG_MemberOrderDetail";
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        String orderDetailsJson = "";
        String locationJson = "";
        paymentMethod ="";
        if(bundle!=null) {
            orderDetailsJson = bundle.getString("OrderDetails");
            locationJson = bundle.getString("Locations");
            paymentMethod = bundle.getString("GroupStatus");
        }else{
            Log.d(TAG,"bundle is null");
        }

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
        //fetch memberOrderdetailsList
        Type listType = new TypeToken<List<MemberOrderDetails>>() {}.getType();
        memberOrderDetailsList = gson.fromJson(orderDetailsJson,listType);
        //fetch locations
        Type listType_location = new TypeToken<List<Location>>() {}.getType();
        locations = gson.fromJson(locationJson,listType_location);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_member_order_details, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        location = view.findViewById(R.id.MemberOrderDetailLocation);
        deadLine = view.findViewById(R.id.MemberOrderDetailDeadLine);
        listView = view.findViewById(R.id.memberCenterOrderDetailsListView);

        btGooglePay = view.findViewById(R.id.btGooglePay);
        btGooglePay.setVisibility(View.GONE);

        SearchView searchView = view.findViewById(R.id.svOrderDetailsSearch);

        recyclerView = view.findViewById(R.id.rvMemberCenterOrderDetails);
        recyclerView.setAdapter(new MyAdapter(activity,memberOrderDetailsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        //ArrayList handle
        ArrayList<String> arrayList = new ArrayList<>();

        for(Location loc: locations){
            String str = "Pick Up Location: " + "\nLatitude: " + loc.getLatitude() + "\nLongtitude: " +loc.getLongtitude();
            arrayList.add(str);
        }
    
        ArrayAdapter arrayAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);
        
        //截止日期
        deadLine.setText("Pick Up Time: " + locations.get(0).getPickup_time());

        //googlePay imageButton
        if(!(paymentMethod.equals("1"))){
            btGooglePay.setVisibility(View.VISIBLE);
            btGooglePay.setEnabled(true);
        }

        showMyOrder(memberOrderDetailsList);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    showMyOrder(memberOrderDetailsList);
                }else{
                    List<MemberOrderDetails> searchOrders = new ArrayList<>();
                    for(MemberOrderDetails memberOrderDetails : memberOrderDetailsList){
                        if (memberOrderDetails.getMerch().getName().toUpperCase().contains(query.toUpperCase())) {
                            searchOrders.add(memberOrderDetails);
                        }
                    }
                    showMyOrder(searchOrders);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty())
                    showMyOrder(memberOrderDetailsList);
                return false;
            }
        });
    }

    private void showMyOrder(List<MemberOrderDetails> memberOrderDetailsList){
        if(memberOrderDetailsList == null || memberOrderDetailsList.isEmpty()){
            Toast.makeText(activity,"no memberOrdersDetails found", Toast.LENGTH_SHORT).show();
        }
        MyAdapter myAdapter = (MyAdapter) recyclerView.getAdapter();
        if(myAdapter == null){
            recyclerView.setAdapter(new MyAdapter(activity,memberOrderDetailsList));
        }else{
            myAdapter.setMemberOrderDetailsList(memberOrderDetailsList);
            myAdapter.notifyDataSetChanged();
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        Activity activity;
        List<MemberOrderDetails> memberOrderDetailsList;

        public MyAdapter(Activity activity, List<MemberOrderDetails> memberOrderDetailsList){
            this.activity = activity;
            this.memberOrderDetailsList = memberOrderDetailsList;
        }

        void setMemberOrderDetailsList(List<MemberOrderDetails> memberOrderDetailsList){
            this.memberOrderDetailsList = memberOrderDetailsList;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_view_member_order_details,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMemberOrderDetailsFragment.MyViewHolder holder, int position) {
            final MemberOrderDetails memberOrderDetails = memberOrderDetailsList.get(position);
            holder.index.setText(getString(R.string.text_order_details_id)+String.valueOf(memberOrderDetails.getMemberOrderDetailsId()));
            holder.merchName.setText(getString(R.string.text_merch_name) + memberOrderDetails.getMerch().getName());
            holder.totalPrice.setText(getString(R.string.text_total_format) + String.valueOf(memberOrderDetails.getFormat_total())+"元");
            holder.quantity.setText(getString(R.string.text_quantity) + String.valueOf(memberOrderDetails.getQuantity()));
            holder.merchDesc.setText(getString(R.string.text_merch_details) + memberOrderDetails.getMerch().getMerchDesc());

            int id = memberOrderDetails.getMerchId();

            byte[] image = MerchControl.getMerchImgById(activity, id);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
            if (bitmap != null) {
                holder.merchImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.merchImage.setImageBitmap(bitmap);
            }else{
                holder.merchImage.setImageResource(R.drawable.no_image);
            }
        }

        @Override
        public int getItemCount() {
            return memberOrderDetailsList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView index,merchName,totalPrice,quantity,merchDesc;
        private ImageView merchImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.memberOrderDetailsId);
            merchName = itemView.findViewById(R.id.memberOrderDetailMerchName);
            totalPrice = itemView.findViewById(R.id.memberOrderDetailTotalPrice);
            quantity = itemView.findViewById(R.id.memberOrderDetailsQuantity);
            merchImage = itemView.findViewById(R.id.memberOrderDetailImageView);
            merchDesc = itemView.findViewById(R.id.memberOrderDetailsMerchDesc);

        }

    }
}