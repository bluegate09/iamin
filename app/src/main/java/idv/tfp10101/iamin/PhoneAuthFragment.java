package idv.tfp10101.iamin;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class PhoneAuthFragment extends Fragment {
    private final String TAG = "TAG_PhoneAuthFragment";
    private Activity activity;
    private FirebaseAuth auth;
    private TextView processText,processText2;
    private EditText phoneNumber,otpEditText;
    private ProgressBar pBCountdown;
    private Button btDown;
    private TextInputLayout phoneTil;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Member member;
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("????????????");
        auth = FirebaseAuth.getInstance();
        member = MemberControl.getInstance();
        handlePhoneAuthProvider();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phone_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phoneNumber = view.findViewById(R.id.etAuthPhoneNumber);
        processText = view.findViewById(R.id.tvPhoneProcess);
        processText2 = view.findViewById(R.id.tvPhoneCode);
        otpEditText = view.findViewById(R.id.etOTP);
        btDown = view.findViewById(R.id.btOTPConfirm);
        phoneTil = view.findViewById(R.id.phoneAuthTil);

        pBCountdown = view.findViewById(R.id.phoneAuthFinalCountDown);
        pBCountdown.setVisibility(View.GONE);

        handleVisibility(View.GONE);


        processText.setOnClickListener(v -> {
            //??????????????????
            phoneNumber.setText("0900900900");
        });

        processText2.setOnClickListener(v -> {
            otpEditText.setText("988334");
        });



        view.findViewById(R.id.btSendCode).setOnClickListener(v -> {
            String phone = phoneNumber.getText().toString().trim();
            if (phone.isEmpty()) {
                phoneTil.setErrorEnabled(true);
                phoneTil.setError(getString(R.string.cannotbeempty));
            }else if(phone.length() < 10){
                phoneTil.setErrorEnabled(true);
                phoneTil.setError(getString(R.string.textphoneformaterror));
            }else{
                phoneTil.setError(null);
                phoneTil.setErrorEnabled(false);
                requestVerificationCode(phone);
            }
        });

        view.findViewById(R.id.btReSendCode).setOnClickListener(v -> {
            String phone = phoneNumber.getText().toString().trim();
            if (phone.isEmpty()) {
                phoneTil.setErrorEnabled(true);
                phoneTil.setError(getString(R.string.cannotbeempty));
            }else if(phone.length() < 10){
                phoneTil.setErrorEnabled(true);
                phoneTil.setError(getString(R.string.textphoneformaterror));
            }else{
                phoneTil.setError(null);
                phoneTil.setErrorEnabled(false);
                resendVerificationCode(phone,mResendToken);
            }

        });

        view.findViewById(R.id.btOTPConfirm).setOnClickListener(v -> {
            String verificationCode = otpEditText.getText().toString().trim();
            if(verificationCode.isEmpty()){
                otpEditText.setError(getString(R.string.cannotbeempty));
            }
            Log.d(TAG,"mVerificationId: " +mVerificationId);
            // ??????????????????????????????????????????(verificationId)???user????????????????????????(verificationCode)??????Firebase
            verifyIDAndCode(mVerificationId, verificationCode);
        });


    }

    private void handleVisibility(int visible) {
        processText2.setVisibility(visible);
        otpEditText.setVisibility(visible);
        btDown.setVisibility(visible);
    }

    private void verifyIDAndCode(String verificationId, String verificationCode) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
        signInWithPhoneAuthCredential(credential);
    }

    private void handlePhoneAuthProvider() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted: " + credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed: " + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
//                Toast.makeText(activity, getString(R.string.textcodesent), Toast.LENGTH_SHORT).show();
                // Save verification ID and resending token so we can use them later

                pBCountdown.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pBCountdown.setProgress(0,true);
                }else{
                    pBCountdown.setProgress(0);
                }
                new Thread(() -> {
                    final int max = 600;
                    int progress = 0;
                    while (progress  <= max) {
                        pBCountdown.setProgress(progress++);
                        try {
                            long time = 100;
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();




                mVerificationId = verificationId;
                mResendToken = token;
                handleVisibility(View.VISIBLE);

            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "signInWithCredential:success");
//                FirebaseUser user = task.getResult().getUser();
//                Toast.makeText(activity, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                member.setuUId2(auth.getCurrentUser().getUid());
                member.setPhoneNumber(phoneNumber.getText().toString().trim());
                MemberControl.updatePhoneNumber(activity,member);
                MemberControl.setMember(member);

//                String jsonMember;
//                member.setuUId(auth.getCurrentUser().getUid());
//                jsonMember = memberRemoteAccess(activity,member,"findbyUuid");
//                member = gson.fromJson(jsonMember,Member.class);
//                if(member != null){
//                    Toast.makeText(activity, getString(R.string.welcomeback), Toast.LENGTH_SHORT).show();
//                    Log.d(TAG,"Welcome back");
//                    MemberControl.setMember(member);
//                    Navigation.findNavController(requireView()).navigate(R.id.action_phoneAuthFragment_to_memberCenterFragment);
//                    return;
//                }else{
//                    member = MemberControl.getInstance();
//                }


                String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                Log.d(TAG,"phoneNumber: " + phoneNumber);
                if(phoneNumber!= null) {
                    ;
                }

                Navigation.findNavController(requireView()).popBackStack();
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_phoneAuthFragment_to_socialLoginFragment);

            }else{
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(activity, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(activity, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                String str = getString(R.string.textsmserror);
                processText.setText(str);
                processText.setTextColor(Color.RED);
            }
        });
    }

    private void requestVerificationCode(String phone) {

        auth.setLanguageCode("zh-Hant");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+886"+phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phone,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions phoneAuthOptions =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+886"+phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(mCallbacks)
                        // ?????????????????????verifyCallbacks.onCodeSent()?????????token???
                        // user?????????????????????????????????token
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }
}