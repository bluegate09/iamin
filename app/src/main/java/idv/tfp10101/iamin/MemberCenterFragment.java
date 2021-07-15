package idv.tfp10101.iamin;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterFragment extends Fragment {
    private final static String TAG = "TAG_MemberCenter";
    private Activity activity;
    private FirebaseAuth auth;
    private TextView nickname, email, rating, followCount;
    private ImageView ivPic;
    private Member member;
    private Gson gson2 = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
//    private Gson gson=  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = Member.getInstance();


        //從mysql取member data
        String jsonIn = memberRemoteAccess(activity, member, "findById");
        Member memberObject = gson2.fromJson(jsonIn,Member.class);
        MemberControl.setMemberData(memberObject);

//        Log.d(TAG,"jsonIn: " + jsonIn);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nickname = view.findViewById(R.id.mcUsername);
        email = view.findViewById(R.id.mcEmail);
        ivPic = view.findViewById(R.id.ivProfilePic);
        rating = view.findViewById(R.id.tvRating);
        followCount = view.findViewById(R.id.tvMCFollowCount);

        setTextView();
        setImageView();

        //前往會員檔案
        view.findViewById(R.id.btMCProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memeberCenterProfileFragment));
        //前往訂單
//        view.findViewById(R.id.btMCOrderList).setOnClickListener(v ->
//                Navigation.findNavController(v).navigate(R.id.action_memberCenter_to_MC_OrderList));
        //前往追隨
        view.findViewById(R.id.btMCFollow).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memeberCenterFollowFragment));
        //前往錢包
        view.findViewById(R.id.btMCMyWallet).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memberCenterMyWalletFragment));
        //前往賣家中心
//        view.findViewById(R.id.btMCSellerCenter).setOnClickListener(v ->
//                Navigation.findNavController(v).navigate(R.id.action_memberCenter_to_MC_SellerCenter));
        //回到賣家
        view.findViewById(R.id.btBacktoHomepage).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_merchbrowseFragment));

        //登出
        view.findViewById(R.id.btMCLogout).setOnClickListener(v -> {
            Log.d(TAG,"member: " + member.getId());
            //update登出時間
            MemberControl.memberRemoteAccess(activity,member,"logout");
            //登出
            auth.signOut();
            //刪除 SharedPreferences 裡的member_ID
            activity.getSharedPreferences("member_ID",MODE_PRIVATE).edit().remove("member_ID").apply();
            //防止回到上一頁
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack(R.id.logInFragment, true);
            navController.popBackStack(R.id.homeFragment, true);
            navController.popBackStack(R.id.memberCenterFragment, true);
            navController.navigate(R.id.homeFragment);


        });
    }

    @Override
    public void onStart() {
        super.onStart();

            Gson gson = new Gson();
            String jsonIn = memberRemoteAccess(activity, member, "findById");
            member = gson.fromJson(jsonIn,Member.class);
            MemberControl.setMemberData(member);
//            Log.d(TAG, "OnCreate: " + member.isUpdate());
            member.setUpdate(true);


    }

    private void setTextView() {
        email.setText(member.getEmail());
        nickname.setText(member.getNickname());
        rating.setText(member.getRating() + "");
        followCount.setText(member.getFollow_count() + "");
    }

    private void setImageView() {
        String url = RemoteAccess.URL_SERVER + "memberServelt";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "getImage");
        jsonObject.addProperty("member", new Gson().toJson(member));
        Bitmap bitmap = RemoteAccess.getRemoteImage(url, jsonObject.toString());
        if (bitmap != null) {
            ivPic.setImageBitmap(bitmap);
        } else {
            ivPic.setImageResource(R.drawable.silhouettes);
        }
    }
}
