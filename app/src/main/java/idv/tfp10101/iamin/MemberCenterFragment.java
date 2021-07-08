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

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterFragment extends Fragment {
    private final static String TAG = "TAG_MemberCenter";
    private Activity activity;
    private FirebaseAuth auth;
    private TextView nickname, email, rating, followCount;
    private ImageView ivPic;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = Member.getInstance();

        Log.d(TAG, "MemberCenter_OnCreate");

        //從mysql取member data
        String jsonIn = memberRemoteAccess(activity, member, "findById");
        JsonObject setter = new Gson().fromJson(jsonIn, JsonObject.class);
        MemberControl.setMemberData(member, setter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MemberCenter_OnDestroy");
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
        //前往帳戶管理
//        view.findViewById(R.id.btMCBankAccount).setOnClickListener(v ->
//                Navigation.findNavController(v).navigate(R.id.action_memberCenter_to_MC_BankAccount));

        //登出
        view.findViewById(R.id.btMCLogout).setOnClickListener(v -> {
            //登出 firebase google facbook
            auth.signOut();

            //防止回到上一頁
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack(R.id.logInFragment, true);
            navController.popBackStack(R.id.homeFragment, true);
            navController.popBackStack(R.id.memberCenterFragment, true);
            navController.navigate(R.id.logInFragment);

        });
    }

    @Override
    public void onStart() {
        super.onStart();

        //有更新才去資料庫撈新資料
        if (!member.isUpdate()) {
            String jsonIn = memberRemoteAccess(activity, member, "findById");
            JsonObject setter = new Gson().fromJson(jsonIn, JsonObject.class);
            MemberControl.setMemberData(member, setter);
            Log.d(TAG, "OnCreate: " + member.isUpdate());
            member.setUpdate(true);
        }

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
