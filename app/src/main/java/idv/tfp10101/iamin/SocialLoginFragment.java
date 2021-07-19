package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

public class SocialLoginFragment extends Fragment {
    private String TAG = "TAG_SocialLoginFragment";
    private Activity activity;
    private EditText etNickName;
    private TextInputLayout til;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_social_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNickName= view.findViewById(R.id.etSocialSignUpNickName);
        til = view.findViewById(R.id.tilSocialSignUp);


        view.findViewById(R.id.btSocialLogin).setOnClickListener(v -> {
            String nickname = etNickName.getText().toString().trim();
            if(nickname.isEmpty()){
                til.setErrorEnabled(true);
                til.setError(getString(R.string.textinputtexthelper));
            }else{
                til.setError(null);
                til.setErrorEnabled(false);
                sendDateToMysql(nickname);
            }
        });
    }

    private void sendDateToMysql(String nickname) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        member.setuUId(auth.getCurrentUser().getUid());
        member.setNickname(nickname);

        String memberUrl = RemoteAccess.URL_SERVER + "memberController";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "signup");
        jsonObject.addProperty("member", new Gson().toJson(member));
        String json = RemoteAccess.getRemoteData(memberUrl, jsonObject.toString());
        member = new Gson().fromJson(json,Member.class);
        Log.d(TAG,"member: " + member);
        if(member == null){
            Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show();
            return;
        }else{
            MemberControl.setMember(member);
            Navigation.findNavController(etNickName).navigate(R.id.action_socialLoginFragment_to_memberCenterFragment);
        }
    }
}