package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class MemberForgetPassword extends Fragment {
    private final String TAG = "TAG_MForgetPassword";
    private Activity activity;
    private TextInputLayout emailTil;
    private EditText etEmail;
    private TextView forgetTextLink;
    private String email;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_forget_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailTil =view.findViewById(R.id.forgetEmailTil);
        etEmail = view.findViewById(R.id.etForgetEmail);
        forgetTextLink = view.findViewById(R.id.forgetTextLink);

        view.findViewById(R.id.btForgetSendEmail).setOnClickListener(v -> {

            email = etEmail.getText().toString().trim();

            if (!isEmailValid(email)) {
                emailTil.setErrorEnabled(true);
                emailTil.setError(getString(R.string.textemailinvalid));
            } else {
                emailTil.setError(null);
                emailTil.setErrorEnabled(false);
                sendPasswordtoResetEmailViaFirebase(email);
            }


        });


    }

    private void sendPasswordtoResetEmailViaFirebase(String email) {

        auth.sendPasswordResetEmail(email).addOnSuccessListener(unused ->
                Toast.makeText(activity, getString(R.string.text_reset_password), Toast.LENGTH_SHORT).show()).
                addOnFailureListener(e -> {
            Toast.makeText(activity, getString(R.string.text_reset_password_error), Toast.LENGTH_SHORT).show();
            Log.d(TAG,e.getMessage());
        });

    }

    //email format驗證
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}