package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyLoadingBar;
import idv.tfp10101.iamin.member.MyWallet;
import idv.tfp10101.iamin.network.RemoteAccess;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterFragment extends Fragment {
    private final static String TAG = "TAG_MemberCenter";
    private Activity activity;
    private FirebaseAuth auth;
    private TextView nickname, email, rating, followCount;
    private ImageView memberClass;
    private ImageView ivPic;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("會員中心");
        auth = FirebaseAuth.getInstance();
        member = MemberControl.getInstance();



        String jsonMember = MemberControl.memberRemoteAccess(activity, member, "findbyUuid");
        if(jsonMember == null){
            Toast.makeText(activity, "網路連線異常", Toast.LENGTH_SHORT).show();
            return;
        }
        member = new Gson().fromJson(jsonMember, Member.class);
        MemberControl.setMember(member);

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
        memberClass = view.findViewById(R.id.memberClass);

        if(member == null){
            Toast.makeText(activity, "網路連線異常", Toast.LENGTH_SHORT).show();
            auth.signOut();
            //fb登出
            LoginManager.getInstance().logOut();
            Navigation.findNavController(view).navigate(R.id.action_memberCenterFragment_to_homeFragment);
            return;
        }

        if (member.getPhoneNumber() == null || member.getPhoneNumber().trim().isEmpty()) {
            memberClass.setImageResource(R.drawable.silver_member);
        }else{
            memberClass.setImageResource(R.drawable.golden_member);
        }

        setTextView();
        setImageView();

        //前往會員檔案
        view.findViewById(R.id.btMCProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memeberCenterProfileFragment));
        //前往訂單
        view.findViewById(R.id.btMCOrderList).setOnClickListener(v ->{

            MyLoadingBar.setLoadingBar(activity,"資料擷取中","請稍候");
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memberCenterMemberOrderFragment);});

        //前往追隨
        view.findViewById(R.id.btMCFollow).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memeberCenterFollowFragment));
        //前往錢包
        view.findViewById(R.id.btMCMyWallet).setOnClickListener(v ->{
            Navigation.findNavController(v).
                    navigate(R.id.action_memberCenterFragment_to_memberCenterTansferFragment);
        });

        //前往我的團購
        view.findViewById(R.id.btMCSellerCenter).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_sellerFragment));

        //前往我的評價
        view.findViewById(R.id.btMyRating).setOnClickListener(v ->{
                Bundle bundle = new Bundle();
                bundle.putInt("member_id", member.getId());

                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memberCenterRatingDialogFragment,bundle);});

        //回到首頁
        view.findViewById(R.id.btBacktoHomepage).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_homeFragment));


        //登出
        view.findViewById(R.id.btMCLogout).setOnClickListener(v -> {
            Log.d(TAG,"member: " + member.getId());
            //update登出時間
            MemberControl.memberRemoteAccess(activity,member,"logout");
            //登出
            auth.signOut();
            //fb登出
            LoginManager.getInstance().logOut();
            //member bean 清空
            MemberControl.setMember(null);
            //防止回到上一頁
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack(R.id.logInFragment, true);
            navController.popBackStack(R.id.phoneAuthFragment, true);
            navController.popBackStack(R.id.memberCenterFragment, true);
            navController.popBackStack(R.id.signUpFragment,true);
            navController.popBackStack(R.id.socialLoginFragment,true);
            navController.navigate(R.id.homeFragment);

        });

    }

    @Override
    public void onStart() {
        super.onStart();
        String jsonMember = MemberControl.memberRemoteAccess(activity, member, "findbyUuid");
        member = new Gson().fromJson(jsonMember, Member.class);
        MemberControl.setMember(member);
        member = MemberControl.getInstance();
        setTextView();
        if (member.getPhoneNumber() == null || String.valueOf(member.getPhoneNumber()).trim().isEmpty()) {
            memberClass.setImageResource(R.drawable.silver_member);
        }else{
            memberClass.setImageResource(R.drawable.golden_member);
        }
        activity.setTitle("會員中心");
    }

    private void setTextView() {
        email.setText(member.getEmail());
        nickname.setText(member.getNickname());

        if(member.getRating() < 0 ){
            rating.setText(0 + "");
        }else{
            rating.setText("評分: " +  member.getRating());
        }

        if(member.getFollow_count() < 0){
            followCount.setText(0 + "");
        }else{
            followCount.setText("追隨者數: " + member.getFollow_count());
        }
    }

    private void setImageView() {
        String url = RemoteAccess.URL_SERVER + "memberController";
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