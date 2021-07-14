package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.member.Member;

import static idv.tfp10101.iamin.member.MemberControl.firebasedbAddOrReplace;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;
import static idv.tfp10101.iamin.member.MemberControl.storeMemberIdSharedPreference;

public class SignUpFragment extends Fragment {
    private final static String TAG = "TAG_signup";
    private Activity activity;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText etEmail,etPassword,etPassword2,etNickname,etPhoneNumber;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = Member.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etEmail = view.findViewById(R.id.etRegisterEmail);
        etPassword = view.findViewById(R.id.etRegisterPassword);
        etPassword2 = view.findViewById(R.id.etRegisterPassword2);
        etNickname = view.findViewById(R.id.etRegisterNickname);
        etPhoneNumber = view.findViewById(R.id.etRegisterPhoneNumber);

        //前往註冊頁面
        view.findViewById(R.id.btSignUp).setOnClickListener(v ->{

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password2 = etPassword2.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            //確定email 跟 password格式
            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                Toast.makeText(activity, "Email/Password can't not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!TextUtils.isEmpty(nickname)){
                member.setNickname(nickname);
            }

            if(!TextUtils.isEmpty(phoneNumber)){
                member.setPhoneNumber(phoneNumber);
            }

            if(password.equals(password2)){
                member.setEmail(email);
                member.setPassword(password);
                //firebase創帳號
                createAccount(member);
            }else{
                Toast.makeText(activity, "Password need to be the sameconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAccount(Member member) {
        auth.createUserWithEmailAndPassword(member.getEmail(), member.getPassword())
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createAccount:" + member.getEmail());
                        //mysql創帳號
                        member.setuUId(auth.getCurrentUser().getUid());
//                        Log.d(TAG,"Uid: " + auth.getCurrentUser().getUid());
                        String mySqlMemberId = memberRemoteAccess(activity , member, "signup");
                        storeMemberIdSharedPreference(activity,mySqlMemberId);
                        member.setId(Integer.parseInt(mySqlMemberId));

                        //存到MemberId與Uid到firebase
                        firebasedbAddOrReplace(activity,db,new Member(
                                 Integer.parseInt(mySqlMemberId)
                                ,auth.getCurrentUser().getUid()));

                        //移動到會員中心
                        Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_memberCenterFragment);
                    } else {
                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}