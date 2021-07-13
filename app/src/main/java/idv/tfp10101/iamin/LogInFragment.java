package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Objects;

import idv.tfp10101.iamin.member.Member;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;
import static idv.tfp10101.iamin.member.MemberControl.storeMemberIdSharedPreference;

public class LogInFragment extends Fragment {
    private final static String TAG = "TAG_LoginFragment";
    private Activity activity;
    private FirebaseAuth auth;
    private GoogleSignInClient client;
    private CallbackManager callbackManager;
    private Member member;
    private EditText etEmail, etPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = Member.getInstance();
        callbackManager = CallbackManager.Factory.create();

        //用google登入firebase
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出
                .requestIdToken(getString(R.string.default_web_client_id))
                // 要求輸入email
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(activity, options);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etEmail = view.findViewById(R.id.etLoginEmail);
        etPassword = view.findViewById(R.id.etLoginPassword);

        //快速填寫
        view.findViewById(R.id.idforalign).setOnClickListener(v -> {
            etEmail.setText("mysql99@test.com");
            etPassword.setText("password");
        });

        //一般信箱密碼登入
        view.findViewById(R.id.btLogIn).setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            //firebase登入驗證
            firebaseLogIn(email, password);
            member.setEmail(email);
            member.setPassword(password);
            String mySqlMemberId = memberRemoteAccess(activity, member, "login");
            storeMemberIdSharedPreference(activity,mySqlMemberId);
            member.setId(Integer.parseInt(mySqlMemberId));
            //這裡沒有navigation 因為在firebase那邊

        });

        //google登入
        view.findViewById(R.id.btSignInGoogle).setOnClickListener(v -> {
            Intent signInIntent = client.getSignInIntent();
            // 跳出Google登入畫面
            signInGoogleLauncher.launch(signInIntent);
        });

        //fb登入
        view.findViewById(R.id.btSignInFacebook).setOnClickListener(v -> {
            signInFB();
        });

        //註冊
        view.findViewById(R.id.btToSignUp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_signUpFragment));

        view.findViewById(R.id.btPhoneSingIn).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_phoneAuthFragment));

        //忘記密碼 進入email fragment
        view.findViewById(R.id.btForgetPassword).setOnClickListener(v -> {
//            Navigation.findNavController(v).navigate(R.id.action_logInFragment_to_forgetPasswordFragment);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            SharedPreferences pref = activity.getSharedPreferences("member_ID",
                    MODE_PRIVATE);
            int mySqlMemberId = pref.getInt("member_ID", -1);
            //小於0代表出問題 所以return
            if(mySqlMemberId < 0){
                return;
            }
            member.setId(mySqlMemberId);
            Log.d(TAG,mySqlMemberId +"member_ID");
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_logInFragment_to_memberCenterFragment);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // get the unique ID for the Google account
//        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(activity, "Goolge 登入成功", Toast.LENGTH_SHORT).show();

                        //取得google登入的EMAIL firebase的auth.getCurrentUser().getEmail會return null
                        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity);
                        String personEmail = "";
                        if(signInAccount != null){
                            personEmail = signInAccount.getEmail();
                        }

                        member.setEmail(personEmail);
                        member.setPassword("google_Login");
                        member.setuUId(auth.getCurrentUser().getUid());

                        //用singup 因為要在mysql新增帳號資訊
                        String mySqlMemberId = memberRemoteAccess(activity,member,"signup");
                        //存到SharedPreference
                        storeMemberIdSharedPreference(activity,mySqlMemberId);
                        member.setId(Integer.parseInt(mySqlMemberId));

                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        Log.d(TAG,"message: " + message);
                    }
                });
    }

    ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {

                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        //註冊帳號到fireBase
                        firebaseAuthWithGoogle(account);
                    } else {
                        Log.e(TAG, "GoogleSignInAccount is null");
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "Google sign in failed");
                }
            }
    );

    private void signInFB() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFaceBookCredential(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
        Log.d(TAG,"requestCode: " + requestCode);
        Log.d(TAG,"resultCode: " + resultCode);
        Log.d(TAG,"data: " + data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFaceBookCredential(AccessToken token) {
//        Log.d(TAG, "signInFirebase: " + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (task.isSuccessful()) {

                        member.setPassword("FB_Login");
                        member.setuUId(auth.getCurrentUser().getUid());
                        Log.d(TAG,"Email: " + auth.getCurrentUser().getEmail());

                        String mySqlMemberId = memberRemoteAccess(activity,member,"signup");
                        member.setEmail("User" + mySqlMemberId + "@Facebook.com");
                        storeMemberIdSharedPreference(activity,mySqlMemberId);


                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        Log.e(TAG, message);
                    }
                });
    }


    //Firebase登入
    private void firebaseLogIn(String email, String password) {
        Log.d(TAG, "Login:" + email);
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(activity, "Email/Password can't not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Navigation.findNavController(etEmail)
                                .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}