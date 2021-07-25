package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.developer.gbuttons.GoogleSignInButton;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Objects;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.Constants.FCM_Token;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class LogInFragment extends Fragment {
    private final static String TAG = "TAG_LoginFragment";
    private Activity activity;
    private FirebaseAuth auth;
    private GoogleSignInClient client;
    private CallbackManager callbackManager;
    private Member member;
    private EditText etEmail, etPassword;
    private TextInputLayout emailTil,passwordTil;
    private ProgressDialog loadingBar;
    private final Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

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
                        Log.e(TAG, "Google Sign in Account is null");
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "Google sign in failed");
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //hide action bar
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = MemberControl.getInstance();
        callbackManager = CallbackManager.Factory.create();
        loadingBar = new ProgressDialog(activity);

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
        emailTil = view.findViewById(R.id.logInEmailTil);
        passwordTil = view.findViewById(R.id.logInPasswordTil);

        //快速填寫
        view.findViewById(R.id.idforalign).setOnClickListener(v -> {
            etEmail.setText("mysql99@test.com");
            etPassword.setText("password");
        });

        //一般信箱密碼登入
        view.findViewById(R.id.btLogIn).setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean isFormatCorrect = true;

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

            //firebase登入驗證
            if(isFormatCorrect) {
                firebaseLogIn(email, password);
            }

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
        //電話驗證
        view.findViewById(R.id.btPhoneSingIn).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_phoneAuthFragment));

        //忘記密碼 進入email fragment
        view.findViewById(R.id.btForgetPassword).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_logInFragment_to_memberForgetPassword);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_logInFragment_to_memberCenterFragment);
        }
    }

    /**
     * email格式驗證
     * @param email
     * return boolean
     */
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 進度條
     * @param titleText 標題
     * @param messageText 訊息
     */
    private void setLoadingBar(String titleText, String messageText) {
        loadingBar.setTitle(titleText);
        loadingBar.setMessage(messageText);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        setLoadingBar(getString(R.string.text_verification_account),
                getString(R.string.text_account_verification_message));

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {

                        loadingBar.dismiss();

                        Toast.makeText(activity, R.string.text_google_signin_success, Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = activity.getSharedPreferences("FCM_TOKEN", MODE_PRIVATE);
                        String myToken = sharedPreferences.getString("FCM_TOKEN", "");
                        member.setFCM_token(myToken);
                        member.setuUId(auth.getCurrentUser().getUid());

                        String jsonMember = MemberControl.memberRemoteAccess(activity,member,"findbyUuid");
                        member = new Gson().fromJson(jsonMember,Member.class);

                        if( member != null){
                            Toast.makeText(activity, R.string.text_welcome_back, Toast.LENGTH_SHORT).show();
                            MemberControl.setMember(member);
                            Navigation.findNavController(etEmail)
                                    .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                            return;
                        }
                        //以下為第一次登入
                        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity);

                        if(signInAccount != null) {

                            if(signInAccount.getEmail() != null ) {
//                                Log.d(TAG,signInAccount.getEmail());
                                member.setEmail(signInAccount.getEmail());
                            }
                            MemberControl.setMember(member);
                            Navigation.findNavController(etEmail)
                                    .navigate(R.id.action_logInFragment_to_socialLoginFragment);
                        }else{
                            Toast.makeText(activity, getString(R.string.authfailed), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        Log.d(TAG,"message: " + message);
                    }
                });
    }

    private void signInFB() {
        setLoadingBar(getString(R.string.text_creating_new_account),
                getString(R.string.text_creating_account_message));
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, getString(R.string.facebookloginsuccess) + loginResult);
                handleFaceBookCredential(loginResult.getAccessToken());
                Log.d(TAG,loginResult.getAccessToken()+"");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, getString(R.string.facebooklogincancel));
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, getString(R.string.facebookloginerror), error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
//        Log.d(TAG,"requestCode: " + requestCode);
//        Log.d(TAG,"resultCode: " + resultCode);
//        Log.d(TAG,"data: " + data);
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

                        loadingBar.dismiss();

                        member.setuUId(auth.getCurrentUser().getUid());
                        String jsonMember = memberRemoteAccess(activity,member,"findbyUuid");
                        member = gson.fromJson(jsonMember,Member.class);
                        if(member != null ){
                            Toast.makeText(activity, "Welcome back", Toast.LENGTH_SHORT).show();
                            MemberControl.setMember(member);
                            Navigation.findNavController(etEmail)
                                    .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                            return;
                        }
                        member = MemberControl.getInstance();
                        Navigation.findNavController(etEmail)
                                .navigate(R.id.action_logInFragment_to_socialLoginFragment);
                    }else {
                        Exception exception = task.getException();
                        String message = exception == null ? getString(R.string.sigininfail) : exception.getMessage();
                        Log.e(TAG, message);
                    }
                });
    }


    //Firebase 帳號密碼登入
    private void firebaseLogIn(String email, String password) {
        Log.d(TAG, "Login:" + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, getString(R.string.signinwithemailsuccess));

                        SharedPreferences sharedPreferences = activity.getSharedPreferences(FCM_Token, MODE_PRIVATE);
                        String myToken = sharedPreferences.getString(FCM_Token, "");
                        member.setFCM_token(myToken);

                        member.setEmail(email);
                        member.setuUId(auth.getCurrentUser().getUid());
                        //不存密碼 因為我們驗證只需要Uid 之後密碼可能會調整為其他用法
//                        member.setPassword(password);
                        String jsonMember = memberRemoteAccess(activity, member, "login");
                        member = new Gson().fromJson(jsonMember,Member.class);
                        if(member == null || member.getId()<0){
                            Toast.makeText(activity, getString(R.string.textnouser), Toast.LENGTH_SHORT).show();
                        }else {

                            MemberControl.setMember(member);
                            Navigation.findNavController(etEmail)
                                    .navigate(R.id.action_logInFragment_to_memberCenterFragment);
                        }
                    } else {
                        Log.w(TAG, getString(R.string.authfailed), task.getException());
                        Toast.makeText(getContext(), getString(R.string.authfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}