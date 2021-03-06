package idv.tfp10101.iamin;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.member.CustomMapView;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order.MemberOrderControl;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;
import idv.tfp10101.iamin.merch.MerchControl;

public class MemberCenterMemberOrderDetailsFragment extends Fragment {
    private static final int REQ_POSITIONING = 1;
    private final String TAG = "TAG_orderDetails";
    private Activity activity;
    private List<MemberOrderDetails> memberOrderDetailsList;
    private RecyclerView recyclerView;
    private List<Location> locations;
    private List<Marker> markers;
    private TextView deadLine1,deadLine2,deadLine3,location1,location2,location3,tloc2,tloc3,tvTotalPrice,pt1,pt2,pt3;
    private Button btGooglePay;
    private GoogleMap googleMap;
    private MemberOrder memberOrder;
    private int paymentMethod;
    private SearchView searchView;
    private boolean receivePaymentStatus;
    private int totalPrice = 0 ;
    private ImageView imageViewQRcode;
    private final Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
    // ??????
    private int memberOderId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String TAG = "TAG_MemberOrderDetail";
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("????????????");

        Bundle bundle = getArguments();
        String orderDetailsJson = "";
        String locationJson = "";
        paymentMethod = 0;
        if(bundle!=null) {
            orderDetailsJson = bundle.getString("OrderDetails");
            locationJson = bundle.getString("Locations");

            String jsonMemberOrder = bundle.getString("MemberOrder");
            memberOrder = gson.fromJson(jsonMemberOrder,MemberOrder.class);

            paymentMethod = memberOrder.getPayentMethod();
            memberOderId = memberOrder.getMemberOrderId();
            totalPrice = memberOrder.getTotal();

//            paymentMethod = bundle.getString("GroupStatus");
//            memberOderId = bundle.getInt("MemberOrderID");
//            totalPrice = bundle.getInt("TotalPrice");
            receivePaymentStatus = bundle.getBoolean("ReceivePaymentStatus");
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

        deadLine1 = view.findViewById(R.id.memberOrderDetailDeadLine1);
        deadLine2 = view.findViewById(R.id.memberOrderDetailDeadLine2);
        deadLine3 = view.findViewById(R.id.memberOrderDetailDeadLine3);

        //pickuptime textview
        pt1 = view.findViewById(R.id.pt1);
        pt2 = view.findViewById(R.id.pt2);
        pt3 = view.findViewById(R.id.pt3);

        location1 = view.findViewById(R.id.memberOrderLocation1);
        location2 = view.findViewById(R.id.memberOrderLocation2);
        location3 = view.findViewById(R.id.memberOrderLocation3);

        tloc2 = view.findViewById(R.id.tPickupDetails2);
        tloc3 = view.findViewById(R.id.tPickupDetails3);

        tvTotalPrice = view.findViewById(R.id.memberOrderDetailTotalPirce);
        tvTotalPrice.setText(String.valueOf(totalPrice) + "???");

        btGooglePay = view.findViewById(R.id.btGooglePay);
//        btGooglePay.setVisibility(View.GONE);

        markers = new ArrayList<>();

        //mapView
        CustomMapView mapView = view.findViewById(R.id.memberOrdermapView);
        mapView.onCreate(savedInstanceState);
        mapView.onStart();

        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            //?????????mapView????????????
            handlePickUpLocation();
            // ??????????????????(?????????) googleMap.setMyLocationEnabled(true);
//            CameraPosition cameraPosition = new CameraPosition.Builder() .target(new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongtitude()))
//                    .zoom(18)
//                    .tilt(45) // ??????????????????
//                    .bearing(90) // ??????????????????
//                    .build(); // ??????????????????
//            CameraUpdate cameraUpdate =
//                    CameraUpdateFactory.newCameraPosition(cameraPosition);
//            googleMap.animateCamera(cameraUpdate);

        });

        searchView = view.findViewById(R.id.svOrderDetailsSearch);

        recyclerView = view.findViewById(R.id.rvMemberCenterOrderDetails);
        recyclerView.setAdapter(new MyAdapter(activity,memberOrderDetailsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        handleSerchView();
        String[] time = {"","",""};
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).getPickup_time() != null)
            time[i] = locations.get(i).getPickup_time().toString().substring(0,16);
        }
        Log.d(TAG,time[0]);
        //????????????
        if(!(time[0].isEmpty())){
            deadLine1.setText(time[0]);
        }else{
            pt1.setText("");
            deadLine1.setText("");
        }
        if(!(time[1].isEmpty())){
            deadLine2.setText(time[1]);
        }else{
            pt2.setText("");
            deadLine2.setText("");
        }
        if(!(time[2]).isEmpty()){
            deadLine3.setText(time[2]);
        }else{
            pt3.setText("");
            deadLine3.setText("");
        }

        //googlePay Button
        if(paymentMethod ==1){
            btGooglePay.setVisibility(View.GONE);
            btGooglePay.setEnabled(false);
        }else{
            if(paymentMethod == 2 &&!receivePaymentStatus){
                btGooglePay.setVisibility(View.VISIBLE);
                btGooglePay.setEnabled(true);
                toTapPay();
            }else{
                btGooglePay.setEnabled(false);
                btGooglePay.setText("?????????");
            }
        }




        showMyOrder(memberOrderDetailsList);

        // QRcode
        imageViewQRcode = view.findViewById(R.id.imageViewQRcode);
        handleQRcode();
    }

    private void toTapPay() {
        btGooglePay.findViewById(R.id.btGooglePay).setOnClickListener(v -> {

            List<MemberOrder> memberOrders = new ArrayList<>();
            memberOrders.add(memberOrder);
            MemberOrderControl.updateMemberOrders(activity,memberOrders,"PaymentInformation");

            Intent intent = new Intent(getActivity(), TappayActivity.class);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("memberOderId", memberOderId);
            startActivity(intent);
        });
    }

    private void handlePickUpLocation() {
        ArrayList<String> locList = new ArrayList<>();
        HashSet<String> locSet = new HashSet<>();

        double latitude,longtitude;
        for(int i = 0; i < locations.size(); i++){
            latitude = locations.get(i).getLatitude();
            longtitude = locations.get(i).getLongtitude();

            LatLng latLng = new LatLng(latitude, longtitude);
            MarkerOptions markerOptions = new MarkerOptions() .position(latLng)
                    .title("????????????" + i)
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapview_pin))
                    .draggable(false);

            Marker marker = googleMap.addMarker(markerOptions);
            markers.add(marker);

            String loc = transferLocation(latitude,longtitude);

            if(!(loc.isEmpty())){
                locSet.add(loc);
            }
        }

        handleMarkersInView();

        locList.addAll(locSet);
        String[] loc = {"","",""};
        for(int i = 0; i < locList.size(); i++){
            loc[i] = locList.get(i);
        }

        location1.setText(loc[0]);

        if(loc[1].isEmpty()){
            tloc2.setVisibility(View.GONE);
            location2.setText("");
        }else{
            location2.setText(loc[1]);
        }
        if(loc[2].isEmpty()) {
            tloc3.setVisibility(View.GONE);
            location3.setText("");
        }else{
            location3.setText(loc[2]);
        }

    }

    /**
     * ???????????????????????????????????????
     */
    private void handleMarkersInView() {
        if (markers.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 150; // ???????????????????????????????????????
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        googleMap.animateCamera(cu);
    }

    /**
     * ???????????????
     */
    public String transferLocation(double latitude, double longitude){
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(activity, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        String str = address.substring(postalCode.length());

        String loc = str;

//        Log.d(TAG, "address: " + address+ "\ncity: " + city + "\nstate: " + state + "\ncountry: " + country + "\npistalCode: " + postalCode + "\nknownName: " + knownName);
        return loc;
    }

    private void handleSerchView() {
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
            holder.index.setText(getString(R.string.text_order_details_id) +" "+ memberOrderDetails.getMemberOrderDetailsId());
            holder.merchName.setText(getString(R.string.text_merch_name) + " " + memberOrderDetails.getMerch().getName());
            holder.totalPrice.setText(getString(R.string.text_total_format) + " " +memberOrderDetails.getFormat_total()+"???");
            holder.quantity.setText(getString(R.string.text_quantity) + " " + memberOrderDetails.getQuantity());
            holder.merchDesc.setText(getString(R.string.text_merch_details));
            if(memberOrderDetails.getMerch().getMerchDesc().length() > 10){
                holder.description.setTextSize(12);
                holder.description.setText(memberOrderDetails.getMerch().getMerchDesc());
            }


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
        final private TextView index,merchName,totalPrice,quantity,merchDesc,description;
        final  ImageView merchImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.memberOrderDetailsId);
            merchName = itemView.findViewById(R.id.memberOrderDetailMerchName);
            totalPrice = itemView.findViewById(R.id.memberOrderDetailTotalPrice);
            quantity = itemView.findViewById(R.id.memberOrderDetailsQuantity);
            merchImage = itemView.findViewById(R.id.memberOrderDetailImageView);
            merchDesc = itemView.findViewById(R.id.memberOrderDetailsMerchDesc);
            description = itemView.findViewById(R.id.detailsMerchDesc);

        }

    }

    /**
     * ??? MemberOrderID ?????? QRcode
     */
    private void handleQRcode() {
        imageViewQRcode.setOnClickListener(view -> {
            if (memberOderId < 0) {
                Toast.makeText(activity, "????????????ID??????", Toast.LENGTH_SHORT).show();
                return;
            }
            // Bundle -> ?????????????????? putXYZ(key, value)
            Bundle bundle = new Bundle();
            bundle.putInt("memberOderId", memberOderId);
            //
            Navigation.findNavController(view)
                    .navigate(R.id.action_memberCenterOrderDetailsFragment_to_QRCodeGenFragment, bundle);
        });
    }
}