package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import idv.tfp10101.iamin.Report.ReportControl;
import idv.tfp10101.iamin.Report.Report;
import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.location.LocationControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyLoadingBar;
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
    private int groupID, sellerID, progress, goal, payment_method, group_status, condition_count;
    private int buyerChoose;//??????????????????????????? 1->??????,2->?????????
    private String contact_number, caution, groupname, seller_FCM_token;
    private Timestamp condition_Time;
    private List<Merch> localMerchs;
    private RecyclerView recyclerViewMerch;
    private Button btn_buy, btn_back, btn_next;
    private Member member;
    private TextView txv_Seller, txv_Email, txv_Seller_phone, txv_followed, txv_rating; //????????????
    private ImageView imv_Seller, imv_followed, imv_report; //?????????????????????????????????
    private TextView txv_caution;
    private int total_quantity = 0, total_price = 0;
    private double userlat,userlng;//?????????????????????
    private Group firstGroup;
    private TextView txv_group_progress,txv_group_location; //?????????????????????????????????
    private  List<Location> grouplocations; //???????????????????????????
    private Bundle bundle; //?????????????????????id??????????????????(?????????????????????)
    private LinearLayout seller_rating;
    private Member seller;
    //????????????
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
        activity.setTitle("????????????");
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
        //??????HomeData?????????????????????
        bundle = getArguments();
        if (bundle != null){
            groupID = (int) bundle.get("GroupID");
            userlat = (Double) bundle.get("Userlat");
            userlng = (Double) bundle.get("Userlng");
        }
        firstGroup = GroupControl.getGroupbyId(activity,groupID);
        if (firstGroup != null){
            sellerID = firstGroup.getMemberId();
            groupname = firstGroup.getName();
            progress = firstGroup.getProgress();
            goal = firstGroup.getGoal();
            payment_method = firstGroup.getPaymentMethod();
            group_status = firstGroup.getGroupStatus();
            condition_count = firstGroup.getConditionCount();
            contact_number = firstGroup.getContactNumber();
            caution = firstGroup.getCaution();
            condition_Time = firstGroup.getConditionTime();
        }
        if (condition_count == -1){
            txv_group_progress.setText("??????:" + progress + "???  " +"??????:" + goal + "???  ");
        }else{
            txv_group_progress.setText("??????:" + progress + "???  " + "??????:" + goal + "???  " + "????????????:" + condition_count + "???  ");
        }
        //??????????????????????????????
        grouplocations = new ArrayList<>();
        StringBuilder groupLaction = new StringBuilder();
        String loactionanddistance = "?????????????????????:\n\n";
        groupLaction.append(loactionanddistance);
        grouplocations = LocationControl.getLocationByGroupId(activity, groupID);
        if (grouplocations != null){
            for (Location location : grouplocations){
                float[] results = new float[1];
                Double groupLat = location.getLatitude();
                Double groupLng = location.getLongtitude();
                android.location.Location.distanceBetween(userlat,userlng,groupLat,groupLng,results);
                String address = latLngToName(groupLat,groupLng);
                Float km = results[0]/1000;

                Timestamp ts = location.getPickup_time();
                DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

                BigDecimal b = new BigDecimal(km);
//            //??????????????????????????????
               float groupDismin = b.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
               if (location.getPickup_time()  == null){
                   groupLaction.append(address + "?????????:" + groupDismin +"??????\n\n");
               }else {
                   groupLaction.append(address + "?????????:" + groupDismin + "??????\n" + "????????????:" + sdf.format(ts) + "\n\n");
               }
            }
            txv_group_location.setText(groupLaction);
        }

        MerchControl.getAllMerchByGroupId(activity,groupID);
        localMerchs = MerchControl.getLocalMerchs();
        if (localMerchs == null || localMerchs.isEmpty()) {
            Toast.makeText(activity, R.string.textNoGroupsFound, Toast.LENGTH_SHORT).show();
        }
        //?????????,???????????????merber??????
        Member SellerID = new Member();
        SellerID.setId(sellerID);
        //??????????????????
        seller = MemberControl.getsellerByMemberId(activity,SellerID);
        if (seller != null){
            txv_Seller.setText(seller.getNickname());
            txv_Email.setText(seller.getEmail());
            txv_Seller_phone.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //?????????
            txv_Seller_phone.setTextColor(Color.BLUE);
            txv_Seller_phone.setText(seller.getPhoneNumber());
            txv_followed.setText(String.valueOf(seller.getFollow_count()));
            txv_rating.setText(String.valueOf(seller.getRating()));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + seller.getPhoneNumber()));
        txv_Seller_phone.setOnClickListener(v ->{
            txv_Seller_phone.setTextColor(Color.rgb(255,0,255));
            startActivity(intent);
        });
        if (caution == null){
            txv_caution.setVisibility(View.GONE);
        }else {
            txv_caution.setVisibility(View.VISIBLE);
            txv_caution.setText("????????????:" + "\n" + caution);
        }
        //????????????????????????????????? ????????????????????????
        int result = MemberControl.chackfollowed(activity,member.getId(),SellerID.getId());
        if (result == 1){
            imv_followed.setImageResource(R.drawable.heart_red);
        }
        if (result == 0){
            imv_followed.setImageResource(R.drawable.heart_white);
        }
        Drawable.ConstantState white = activity.getResources().getDrawable(R.drawable.heart_white).getConstantState();
        Drawable.ConstantState red = activity.getResources().getDrawable(R.drawable.heart_red).getConstantState();
//        ????????????????????????????????????????????? ???????????? ????????????
          imv_followed.setOnClickListener(v ->{
              //??????????????????????????????
              if (member.getId() == -1){
                  Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                  return;
              }
            Drawable.ConstantState imageView = imv_followed.getDrawable().getCurrent().getConstantState();
            int chackresult = MemberControl.chackfollowed(activity,member.getId(),SellerID.getId());
            if (imageView.equals(red)) {
                AlertDialog.Builder followed = new AlertDialog.Builder(activity);
                followed.setTitle("????????????????????????????????????")
                        .setPositiveButton("??????", (dialog, which) -> {
                            MemberControl.followed(activity, member.getId(), SellerID.getId());
                            imv_followed.setImageResource(R.drawable.heart_white);
                            Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("???????????????", (dialog, which) -> {
                            return;
                        })
                        .setCancelable(true)
                        .show();
            }else{
                MemberControl.followed(activity, member.getId(), SellerID.getId());
                Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                imv_followed.setImageResource(R.drawable.heart_red);
            }
        });

        //????????????????????????
        Bitmap bitmap = MemberControl.getsellerimageByMemberId(activity,SellerID);
        imv_Seller.setImageBitmap(bitmap);
        imv_Seller.setOnClickListener(v ->{
            Bundle bundle = new Bundle();
            bundle.putInt("followerId", sellerID);
            bundle.putString("name", seller.getNickname());
            Navigation.findNavController(v).navigate(R.id.memberCenterFollowersGroupFragment, bundle);
        });

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1,RecyclerView.HORIZONTAL);
        recyclerViewMerch.setLayoutManager(staggeredGridLayoutManager);

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerViewMerch);
        recyclerViewMerch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        showMerchs(localMerchs);

        seller_rating.setOnClickListener(v ->{
            Bundle sellerID_bundle = new Bundle();
            sellerID_bundle.putInt("member_id", sellerID);

            Navigation.findNavController(v).navigate(R.id.memberCenterRatingDialogFragment, sellerID_bundle);
        });
        //????????????icon????????????????????????
        imv_report.setOnClickListener(v ->{
            if (member.getId() == -1){
                Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_report,null);
            dialogBuilder.setView(dialogView);

            EditText report_message = dialogView.findViewById(R.id.edt_report_message);
            Button btButton = dialogView.findViewById(R.id.dialog_report_button);
            Spinner spinner = dialogView.findViewById(R.id.sp_report);
            spinner.setSelection(0,true);

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            btButton.setOnClickListener(dialog ->{
                Report report1 = new Report(member.getId(),sellerID,spinner.getSelectedItem().toString(),report_message.getText().toString());
                alertDialog.dismiss();
                int insertresult = ReportControl.insertReport(activity,report1);
                if (insertresult == 1){
                    Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //????????????????????????
        int total = staggeredGridLayoutManager.getItemCount();
        AtomicInteger count = new AtomicInteger();
        //??????back???????????????
        btn_back.setVisibility(View.INVISIBLE);
        //????????????????????????1?????????next????????????false
        if ((total - 1) == 0 ){
            btn_back.setVisibility(View.GONE);
            btn_next.setVisibility(View.GONE);
        }
        //??????next????????????????????????????????????back????????????
        btn_next.setOnClickListener(v ->{
            recyclerViewMerch.smoothScrollToPosition(count.incrementAndGet());
            if (count.get() == (total-1)){
                btn_next.setVisibility(View.INVISIBLE);
            }
            btn_back.setVisibility(View.VISIBLE);
        });
        btn_back.setOnClickListener(v ->{
            recyclerViewMerch.smoothScrollToPosition(count.decrementAndGet());
            if (count.get() == 0){
                btn_back.setVisibility(View.INVISIBLE);
            }
            btn_next.setVisibility(View.VISIBLE);
        });

        //????????????????????????
        btn_buy.setOnClickListener(v ->{
            NavController navController = Navigation.findNavController(view);
            if ( member.getId() == -1){
                AlertDialog.Builder signup = new AlertDialog.Builder(activity);
                signup.setTitle("????????????????????????")
                        .setPositiveButton("?????????", (dialog, which) -> {
                            navController.navigate(R.id.logInFragment);
                        })
                        .setNegativeButton("?????????", (dialog, which) -> {
                            navController.navigate(R.id.signUpFragment);
                        })
                        .setCancelable(false)
                        .show();
                return;
            }
            AlertDialog.Builder chackDialog = new AlertDialog.Builder(activity);
            Map<Merch,Integer> maps = ((MerchAdapter) recyclerViewMerch.getAdapter()).getMerchsMap();
            StringBuilder merchDetails = new StringBuilder();
            total_price = 0;
            total_quantity = 0;
            for (Map.Entry<Merch, Integer> entry : maps.entrySet()) {
                Merch merch = entry.getKey();
                String merchName = merch.getName();
                int amount = entry.getValue();  //??????????????????????????????
                int price = merch.getPrice();   //????????????????????????
                int format_total = price * amount; //?????????????????????????????????
                total_price += format_total; //?????????????????????????????????
                total_quantity += amount; //?????????????????????????????????
                if (amount != 0) {
                    merchDetails.append(merchName + " ??????:" + amount + "????????????" + format_total + "\n");
                }
            }
            if (total_price != 0) {
                chackDialog.setTitle("??????????????????")
                        .setMessage(merchDetails + "??????" + total_price)
                        .setPositiveButton("?????????????????????", (dialog, which) -> {
                            //?????????????????????????????????
                            createdDialog();
                        })
                        .setNegativeButton("???????????????", (dialog, which) -> {
                            return;
                        })
                        .setCancelable(false)
                        .show();
            }else {
                Toast.makeText(activity, "????????????????????????!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //??????????????????
    private void getOrder(){
        total_quantity = 0; //????????????????????????
        total_price = 0;    //??????????????????(??????)
        Map<Merch,Integer> maps = ((MerchAdapter) recyclerViewMerch.getAdapter()).getMerchsMap();
            for (Map.Entry<Merch, Integer> entry : maps.entrySet()) {
                Merch merch = entry.getKey();
                int amount = entry.getValue();  //??????????????????????????????
                int price = merch.getPrice();   //????????????????????????
                int format_total = price * amount; //?????????????????????????????????
                total_price += format_total; //?????????????????????????????????
                total_quantity += amount; //?????????????????????????????????
//                Toast.makeText(activity, merch.getName()+"??????:"+String.valueOf(amount), Toast.LENGTH_SHORT).show();
            }
        //???????????????????????????
        Group group = GroupControl.getGroupbyId(activity,groupID);

        if(group != null) {
            int progress = group.getProgress();
            int status = group.getGroupStatus();
                //??????????????????????????????
                if (new Date().before(condition_Time)){
                    //????????????????????????????????????????????? -1=????????????
                    if (condition_count != -1){
                        //??????????????????????????????????????????????????????????????????
                        if((total_quantity + progress) <= condition_count){
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
                            if ((total_quantity + progress) >= goal){
                                status = 2;
                                handleSubmit();
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
                            //Toast.makeText(activity, "??????????????????!!", Toast.LENGTH_SHORT).show();
                        }else{
                            AlertDialog.Builder updataMerchdialog = new AlertDialog.Builder(activity);
                            updataMerchdialog.setTitle("??????????????????")
                                    .setMessage("????????????????????????????????????????????????!!")
                                    .setPositiveButton("??????????????????", (dialog, which) -> {
                                        if (group.getConditionCount() == -1){
                                            txv_group_progress.setText("??????:" + group.getProgress() + "???  " +"??????:" + group.getGoal() + "???  ");
                                        }else{
                                            txv_group_progress.setText("??????:" + group.getProgress() + "???  " + "??????:" + group.getGoal() + "???  " + "????????????:" + group.getConditionCount() + "???  ");
                                        }
                                        return;
                                    })
                                    .setCancelable(false)
                                    .show();

                        }
                    }else{
                        //??????memberOrder??????
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
                        if ((total_quantity + progress) >= goal){
                            status = 2;
                            handleSubmit();
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
                    Toast.makeText(activity, "???????????????????????????????????????!!", Toast.LENGTH_SHORT).show();
                }
        }
    }
    //????????????????????????
    private void setMemberorder(MemberOrder memberOrder){

        //member_order_ID???????????????????????????
        int member_order_ID = MemberOrderControl.insertMemberOrder(activity,memberOrder);
        //???????????????
        List<MemberOrderDetails> orderDetails = new ArrayList<>();
        Map<Merch,Integer> maps = ((MerchAdapter) recyclerViewMerch.getAdapter()).getMerchsMap();

        for (Map.Entry<Merch, Integer> entry : maps.entrySet()) {
            Merch merch = entry.getKey();
            int merchID = merch.getMerchId(); //????????????ID
            int amount = entry.getValue();  //??????????????????????????????
            int price = merch.getPrice();   //????????????????????????
            int format_total = price * amount; //?????????????????????????????????
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
    //????????????
    private void updateGroup (Group group){
        NavController navController = Navigation.findNavController(view);
        int code = GroupControl.updateGroup(activity,group);
        if (code == 0 ) {
            AlertDialog.Builder gohome = new AlertDialog.Builder(activity);
            gohome.setTitle("????????????!!")
                    .setPositiveButton("?????????", (dialog, which) -> {
                        navController.navigate(R.id.homeFragment);
                    })
                    .setNegativeButton("???????????????", (dialog, which) -> {
                        navController.navigate(R.id.memberCenterMemberOrderFragment);
                    })
                    .setCancelable(false)
                    .show();
        }else{
            Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
        }
    }
    //?????????????????????????????????
    private void createdDialog(){
        AlertDialog.Builder payment_methodDialog = new AlertDialog.Builder(activity);
        switch (payment_method) {
            case 1:
                payment_methodDialog
                        .setTitle("??????????????????")
                        .setMessage("????????????????????????")
                        .setPositiveButton("?????????!!",(dialog, which) -> {
                            buyerChoose = 1;
                            getOrder();
                        })
                        .setNegativeButton("???????????????",(dialog, which) -> {return;})
                        .setCancelable(false)
                        .show();
                break;
            case 2:
                payment_methodDialog
                        .setTitle("??????????????????")
                        .setMessage("???????????????????????????")
                        .setPositiveButton("?????????!!",(dialog, which) -> {
                            buyerChoose = 2;
                            getOrder();
                        })
                        .setNegativeButton("???????????????",(dialog, which) -> {return;})
                        .setCancelable(false)
                        .show();
                break;
            case 3:
                final String[] payment_method = {"??????","???????????????"};
                buyerChoose = -1;
                payment_methodDialog
                        .setTitle("??????????????????")
                        .setSingleChoiceItems(payment_method, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                buyerChoose = which;
                            }
                        })
                        .setPositiveButton("?????????!!",(dialog, which) -> {
                            if (buyerChoose == -1){
                                Toast.makeText(activity, "?????????????????????!!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //???????????????????????????????????????
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
                            //??????????????????????????????0,??????table???+1 1->?????? 2->?????????
                        })
                        .setNegativeButton("???????????????",(dialog, which) -> {return;})
                        .setCancelable(false)
                        .show();
                break;

        }
    }

    private void findView(View view) {
        recyclerViewMerch = view.findViewById(R.id.recyclerViewMerch);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity,2);
//        recyclerViewMerch.setLayoutManager(gridLayoutManager);

        btn_buy = view.findViewById(R.id.btn_buy);
        btn_back = view.findViewById(R.id.btn_back);
        btn_next = view.findViewById(R.id.btn_next);
        txv_Seller = view.findViewById(R.id.txv_Seller);
        txv_Email = view.findViewById(R.id.txv_Email);
        txv_Seller_phone = view.findViewById(R.id.txv_Seller_phone);
        txv_followed = view.findViewById(R.id.txv_followed);
        txv_rating = view.findViewById(R.id.txv_rating);
        txv_caution = view.findViewById(R.id.txv_caution);
        imv_Seller = view.findViewById(R.id.imv_Seller);
        imv_followed = view.findViewById(R.id.imv_followed);
        txv_group_progress = view.findViewById(R.id.txv_group_progress);
        txv_group_location = view.findViewById(R.id.txv_group_location);
        imv_report = view.findViewById(R.id.imv_report);
        seller_rating = view.findViewById(R.id.seller_rating);

    }

    private void showMerchs(List<Merch> localMerchs) {
        /** RecyclerView */
        // ??????
        MerchbrowseFragment.MerchAdapter merchAdapter = (MerchbrowseFragment.MerchAdapter) recyclerViewMerch.getAdapter();
        if (merchAdapter == null) {
            recyclerViewMerch.setAdapter(new MerchbrowseFragment.MerchAdapter(activity, localMerchs));
            int px = (int) Constants.convertDpToPixel(8, activity); // ?????? 8 dp
            recyclerViewMerch.addItemDecoration(new Constants.SpacesItemDecoration("bottom", px));
        }else{
            // ????????????????????????
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
        StringBuilder merchDetils = new StringBuilder();
        Integer num = rsMerchs.get(rsMerch);
        AtomicInteger amount = new AtomicInteger(num == null ? 0 : num);
        int merch_id = rsMerch.getMerchId();
        String merch_name = rsMerch.getName();
        int merch_price = rsMerch.getPrice();
        String merch_desc = rsMerch.getMerchDesc();
        holder.txv_amount.setText(String.valueOf(num == null ? 0 : num));
        holder.txv_merch_name.setText(merch_name);
        holder.txv_merch_price.setText("??????:"+String.valueOf(merch_price));
        holder.txv_commodity_description.setText("????????????:\n"+merch_desc);

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

        // ????????????????????????
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
        MyLoadingBar.dismissLoadingBar();
        }
        //??????????????????
        @Override
        public int getItemCount() { return rsMerchs == null ? 0 : rsMerchs.size();
        }
    }

    /**
     *  ????????? ??? ??????/??????
     */
    private String latLngToName(double lat, double lng) {
        // ??????Geocoder????????????
        boolean isPresent = Geocoder.isPresent();
        if (!isPresent) {
            return "";
        }
        // ?????????Geocoder??????
        Geocoder geocoder = new Geocoder(activity);
        // ??????
        StringBuilder name = new StringBuilder();
        try {
            // ??????
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            // ?????? ??????/??????
            Address address = addressList.get(0);
            if (address != null) {
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    name.append(address.getAddressLine(i))
                            .append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return name.toString();
    }

    /**
     * ??????
     */
    private void handleSubmit() {
        NavController navController = Navigation.findNavController(view);
            // ??????????????????????????? request
            if (RemoteAccess.networkConnected(activity)) {
                // ?????? ??? Action
                String url = RemoteAccess.URL_SERVER + "FcmChatServlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "singleBuyerFcm");
                jsonObject.addProperty("sellerToken", seller.getFCM_token());
                jsonObject.addProperty("title", "??????" + groupname + "???????????????");
                jsonObject.addProperty("body", "??????????????????");
                jsonObject.addProperty("data", "Seller_Fragment");

                // requst
                String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            }else {
                Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            }
    }
}