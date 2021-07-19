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

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyWallet;
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
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = MemberControl.getInstance();
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
        view.findViewById(R.id.btMCOrderList).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memberCenterMemberOrderFragment));
        //前往追隨
        view.findViewById(R.id.btMCFollow).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memeberCenterFollowFragment));
        //前往錢包
        view.findViewById(R.id.btMCMyWallet).setOnClickListener(v ->{

                String jsonIn = memberRemoteAccess(activity,member,"getMyWallet");
//                Type listType = new TypeToken<List<MyWallet>>() {}.getType();
//                MyWallet myWallets = gson.fromJson(jsonIn,listType);
                  Log.d(TAG,"json: " + jsonIn);
                if(jsonIn.equals("[]")){
                    Toast.makeText(activity, "No Data Yet", Toast.LENGTH_SHORT).show();

                }else {
                    Bundle bundle = new Bundle();
                    bundle.putString("JsonWallet",jsonIn);
                    Navigation.findNavController(v).
                            navigate(R.id.action_memberCenterFragment_to_memberCenterMyWalletFragment,bundle);
                }
        });
        //前往賣家中心
//        view.findViewById(R.id.btMCSellerCenter).setOnClickListener(v ->
//                Navigation.findNavController(v).navigate(R.id.action_memberCenter_to_MC_SellerCenter));
        //回到賣家
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

        view.findViewById(R.id.btTestGround).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Navigation.findNavController(v).navigate(R.id.action_memberCenterFragment_to_memberCenterFollowTestGround);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
            member = MemberControl.getInstance();
            setTextView();
//            Log.d(TAG, "OnCreate: " + member.isUpdate());
    }

    private void setTextView() {
        email.setText(member.getEmail());
        nickname.setText(member.getNickname());
        rating.setText(member.getRating() + "");
        followCount.setText(member.getFollow_count() + "");
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