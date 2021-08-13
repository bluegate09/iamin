package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.Constants.FCM_Token;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class SignUpFragment extends Fragment {
    private final static String TAG = "TAG_signup";
    private Activity activity;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextInputLayout emailTil,passwordTil,passwordTil2,phoneTil,nameTil;
    private EditText etEmail,etPassword,etPassword2,etNickname,etPhoneNumber;
    private TextView quick1;
    private ImageView quick2;
    private CheckBox checkBox;
    private Member member;
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("註冊");
        auth = FirebaseAuth.getInstance();
        member = MemberControl.getInstance();
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
//        etPhoneNumber = view.findViewById(R.id.etRegisterPhoneNumber);

        emailTil = view.findViewById(R.id.signupEmailTil);
        passwordTil = view.findViewById(R.id.signupPasswordTil);
        passwordTil2 = view.findViewById(R.id.signupPasswordTil2);
//        phoneTil = view.findViewById(R.id.signupPhoneTil);
        nameTil = view.findViewById(R.id.signupNickNameTil);

        checkBox = view.findViewById(R.id.signUPcheckBox);
        //快速輸入
        quick1 = view.findViewById(R.id.quickType1);
        quick2 = view.findViewById(R.id.idforalign);


        handleCheckBox();


        //前往註冊頁面
        view.findViewById(R.id.btSignUp).setOnClickListener(v ->{

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password2 = etPassword2.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
//            String phoneNumber = etPhoneNumber.getText().toString().trim();

            boolean isFormatCorrect = true;

            //確定email 跟 password格式
            if (email.isEmpty()) {
                emailTil.setErrorEnabled(true);
                emailTil.setError(getString(R.string.textcantbeblank));
                isFormatCorrect = false;
            } else if (!isEmailValid(email)) {
                emailTil.setErrorEnabled(true);
                emailTil.setError(getString(R.string.textemailinvalid));
                isFormatCorrect = false;
            } else {
                emailTil.setError(null);
                emailTil.setErrorEnabled(false);
            }


            if(password.isEmpty()){
                passwordTil.setErrorEnabled(true);
                passwordTil.setError(getString(R.string.textcantbeblank));
                isFormatCorrect = false;
            }else{
                passwordTil.setError(null);
                passwordTil.setErrorEnabled(false);
            }

            if(password2.isEmpty()){
                passwordTil2.setErrorEnabled(true);
                passwordTil2.setError(getString(R.string.textcantbeblank));
                isFormatCorrect = false;
            }else{
                passwordTil2.setError(null);
                passwordTil2.setErrorEnabled(false);
            }

            if(password.length()<6){
                passwordTil.setErrorEnabled(true);
                passwordTil.setError("密碼長度不足");
                isFormatCorrect = false;
            }else{
                passwordTil.setError(null);
                passwordTil.setErrorEnabled(false);
            }

            if(password2.length()<6){
                passwordTil2.setErrorEnabled(true);
                passwordTil2.setError("密碼長度不足");
                isFormatCorrect = false;
            }else{
                passwordTil2.setError(null);
                passwordTil2.setErrorEnabled(false);
            }


            if(nickname.isEmpty()){
                nameTil.setErrorEnabled(true);
                nameTil.setError(getString(R.string.textcantbeblank));
                isFormatCorrect = false;
            }else{
                nameTil.setError(null);
                nameTil.setErrorEnabled(false);
            }

            if(!(password.equals(password2))){
                passwordTil2.setErrorEnabled(true);
                passwordTil2.setError(getString(R.string.text_confirm_error));
                isFormatCorrect = false;
            }

            if(password.equals(password2)&&isFormatCorrect){
                member.setEmail(email);
                member.setPassword(password);
                member.setNickname(nickname);
                //firebase創帳號
                createAccount(member);
            }
        });
        //快速登入
        quick1.setOnClickListener(v -> {
            etEmail.setText("bobowenwen@gmail.com");
            etPassword.setText("password");
            etPassword2.setText("password");
            etNickname.setText("柏文");
        });

        quick2.setOnClickListener(v -> {
            etEmail.setText("iEiElElE@gmail.com");
            etPassword.setText("password");
            etPassword2.setText("password");
            etNickname.setText("以樂");
        });
    }

    //顯示或隱藏密碼
    private void handleCheckBox() {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                etPassword.setTransformationMethod(null);
                etPassword2.setTransformationMethod(null);
            }else{
                etPassword.setTransformationMethod(new PasswordTransformationMethod());
                etPassword2.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
    }

    //email format驗證
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createAccount(Member member) {
        auth.createUserWithEmailAndPassword(member.getEmail(), member.getPassword())
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        //mysql創帳號
                        SharedPreferences sharedPreferences = activity.getSharedPreferences(FCM_Token, MODE_PRIVATE);
                        String myToken = sharedPreferences.getString(FCM_Token, "");
                        member.setFCM_token(myToken);
                        member.setuUId(auth.getCurrentUser().getUid());
                        String memberJson = memberRemoteAccess(activity , member, "signup");
                        Member member2 = gson.fromJson(memberJson,Member.class);
                        MemberControl.setMember(member2);
                        //存memberId與Uid到firebase
                        if(member2 == null){
                            Toast.makeText(activity, "Email already in use", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        firebasedbAddOrReplace(activity,db,new Member(
//                                member2.getId()
//                                ,auth.getCurrentUser().getUid()));

                        //移動到會員中心
                        Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_memberCenterFragment);
                    } else {
//                        Log.d(TAG,"task: " + task.getResult());
                        Toast.makeText(getContext(), "此帳號已存在", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    public static void firebasedbAddOrReplace(Context context,FirebaseFirestore db,final Member member) {
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("id", member.getuUId());
//        db.collection("Users").document(member.getuUId()).set(hashMap)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "user into firebase success: " + member.getId());
//                    } else {
//                        Log.e(TAG, "message: " + task.getException().getMessage());
//                    }
//                });
//        // 如果Firestore沒有該ID的Document就建立新的，已經有就更新內容
//        db.collection("members").document(member.getId()+"").set(member)
//                .addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful()) {
//                        Log.d(TAG, "Inserted with ID: " + member.getId());
//                        Toast.makeText(context, "Inserted with ID: " + member.getId(), Toast.LENGTH_SHORT).show();
//                        // 新增完畢回上頁
//                    } else {
//                        Log.e(TAG, "message: " + task1.getException().getMessage());
//                        Toast.makeText(context, "message: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

}