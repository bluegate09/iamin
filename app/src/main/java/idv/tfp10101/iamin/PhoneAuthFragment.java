package idv.tfp10101.iamin;

import android.app.Activity;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import idv.tfp10101.iamin.member.Member;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;
import static idv.tfp10101.iamin.member.MemberControl.storeMemberIdSharedPreference;

public class PhoneAuthFragment extends Fragment {
    private final String TAG = "TAG_PhoneAuthFragment";
    private Activity activity;
    private FirebaseAuth auth;
    private TextView processText;
    private EditText phoneNumber,otpEditText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Member member;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        member = Member.getInstance();
//        auth.getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);
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
        otpEditText = view.findViewById(R.id.etOTP);

        processText.setOnClickListener(v -> {
            //這邊自己設定
            phoneNumber.setText("+886900900900");
            otpEditText.setText("988334");
        });

        view.findViewById(R.id.btSendCode).setOnClickListener(v -> {
            String phone = phoneNumber.getText().toString().trim();
            if (phone.isEmpty()) {
                phoneNumber.setError("Cannot be empty");
                return;
            }
            requestVerificationCode(phone);
//                Log.d(TAG,phone);

        });

        view.findViewById(R.id.btReSendCode).setOnClickListener(v -> {
            String phone = phoneNumber.getText().toString().trim();
            if (phone.isEmpty()) {
                phoneNumber.setError("Cannot be empty");
                return;
            }
            resendVerificationCode(phone,mResendToken);
//                Log.d(TAG,phone);

        });

        view.findViewById(R.id.btOTPConfirm).setOnClickListener(v -> {
            String verificationCode = otpEditText.getText().toString().trim();
            if(verificationCode.isEmpty()){
                otpEditText.setError("Cannot be empty");
            }
            Log.d(TAG,"mVerificationId: " +mVerificationId);
            // 將應用程式收到的驗證識別代號(verificationId)與user輸入的簡訊驗證碼(verificationCode)送至Firebase
            verifyIDAndCode(mVerificationId, verificationCode);
        });


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

                // Save verification ID and resending token so we can use them later
                String myTestCode = "s";
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = task.getResult().getUser();
//                Toast.makeText(activity, "signInWithCredential:success", Toast.LENGTH_SHORT).show();

                member.setuUId(auth.getCurrentUser().getUid());
                String mySqlMemberId = memberRemoteAccess(activity,member,"signup");
                member.setEmail("User" + mySqlMemberId + "@phoneAuth");
                storeMemberIdSharedPreference(activity,mySqlMemberId);

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_phoneAuthFragment_to_homeFragment);

            }else{
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(activity, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(activity, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                processText.setText(task.getException().getMessage());
                processText.setTextColor(Color.RED);
            }
        });
    }



    private void requestVerificationCode(String phone) {

        auth.setLanguageCode("zh-Hant");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)       // Phone number to verify
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
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(mCallbacks)
                        // 驗證碼發送後，verifyCallbacks.onCodeSent()會傳來token，
                        // user要求重傳驗證碼必須提供token
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }
}