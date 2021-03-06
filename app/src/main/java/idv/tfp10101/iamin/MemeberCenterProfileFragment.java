package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

import static android.app.Activity.RESULT_OK;

public class MemeberCenterProfileFragment extends Fragment {
    private final String TAG = "TAG_MC_Profile";
    private EditText etEmail, etPassword, etNickname, etPhoneNumber;
    private Activity activity;
    private Member member;
    private ImageView ivPic;
    private TextInputLayout phoneTil;
    private TextView myPhoneNumber;
    private byte[] image;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Button btPhoneAuth, btResetPhoneNumber;
    private Uri contentUri; // ???????????????Uri
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

    //callback method?????? Permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // ????????????????????????
                if (isGranted) {
                    openCamera();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Toast.makeText(activity, "????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("????????????");
        member = MemberControl.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memeber_center_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etEmail = view.findViewById(R.id.etProfileEmail);
        etNickname = view.findViewById(R.id.etProfileNickname);
        ivPic = view.findViewById(R.id.ivProfilePic);
        myPhoneNumber = view.findViewById(R.id.myProfilePhoneNumber);

        btResetPhoneNumber = view.findViewById(R.id.resetPhoneNumber);
        btPhoneAuth = view.findViewById(R.id.phoneButton);

        setTextView();
        setImageView();

        //????????????????????????
        view.findViewById(R.id.btProfileUpdate).setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();

            if (!email.isEmpty()) {
                member.setEmail(email);
            }

            if (!TextUtils.isEmpty(nickname)) {
                member.setNickname(nickname);
            }

            //member bean ??????
            MemberControl.setMember(member);
            //mysql????????????????????????
            sendInfotoMysql(member);
        });

        btPhoneAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_memeberCenterProfileFragment_to_phoneAuthFragment);
            }
        });


        //????????????????????????
        btResetPhoneNumber.setOnClickListener(v -> {
            if (RemoteAccess.networkConnected(activity)) {
                // ?????? ??? Action
                String url = RemoteAccess.URL_SERVER + "memberController";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "ResetPhoneNumberRequest");
                jsonObject.addProperty("member", new Gson().toJson(member));
                String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

                member.setPhoneNumber("");

                if(Integer.parseInt(result) == -1){
                    Toast.makeText(activity, "??????????????????????????? ?????????????????????????????????", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            }
        });

        //????????????
        view.findViewById(R.id.ibProfile).setOnClickListener(v -> {

            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            String[] items = {getString(R.string.text_takePicture),getString(R.string.text_pickpicture)};
            alert.setSingleChoiceItems(items, -1, (dialog, which) -> {
                dialog.dismiss();
                //????????????
                handleImgSelect(which);
            });
            alert.show();
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        setTextView();
        setImageView();
    }

    private void setTextView() {
        etEmail.setText(member.getEmail());
        etNickname.setText(member.getNickname());
        myPhoneNumber.setText("");
        myPhoneNumber.setText(member.getPhoneNumber());

        if(member.getPhoneNumber() == null || member.getPhoneNumber().isEmpty()){
            btPhoneAuth.setVisibility(View.VISIBLE);
            btPhoneAuth.setEnabled(true);
            btResetPhoneNumber.setVisibility(View.GONE);
            btResetPhoneNumber.setEnabled(false);
        }else{
            btPhoneAuth.setVisibility(View.GONE);
            btPhoneAuth.setEnabled(false);
            btResetPhoneNumber.setVisibility(View.VISIBLE);
            btResetPhoneNumber.setEnabled(true);
        }
    }

    private void setImageView(){
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

    private void handleImgSelect(int which) {
        // ??????
        if(which == 0){
            checkCameraPermission();
        }
        // ??????
        if(which == 1){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        }
    }

    // ???????????????????????????
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // ?????????????????????????????????

            //https://stackoverflow.com/questions/56412401/working-of-shouldshowrequestpermissionrationale-method
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                Log.d("CAMERA","1");
                // ??????????????????
                Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                Log.d("CAMERA","2");
            }
        } else {
            openCamera();
        }
    }

    // ????????????
    private void openCamera() {
        // Intent -> ?????? ACTION
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**
         * ????????????????????? ??????
         * API 24 ???????????????Android ??????????????? app ????????? file://Uri ????????? app
         * FileProvider ???????????????????????????????????????content://Uri ?????? file://Uri
         * ?????????FileProvider?????????manifest.xml?????????
         */
        // 1.?????????????????? = ???????????????????????????(Environment.DIRECTORY_????????????)
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 2.????????????????????? ???????????????
        dir = new File(dir, "picture.jpg");
        // 3.??????FileProvider??????Uri?????? (context, ??????manifest.xml???authorities?????????, ????????????)
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".provider", dir);
        // 4.intent.putExtra(key, value) -> ??????
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        try {
            // Launcher() -> ???????????? ???????????????????????? -> takePictureResult()
            takePictureLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.textNoCameraApp, Toast.LENGTH_SHORT).show();
        }
    }


    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
    }

    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                crop(result.getData().getData());
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri destinationUri = Uri.fromFile(file);
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
//                .withAspectRatio(16, 9) // ??????????????????
                .withMaxResultSize(300, 300) // ??????????????????????????????????????????
                .getIntent(activity);
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            // ?????????Uri
            Uri cropImageUri = UCrop.getOutput(result.getData());
            if (cropImageUri != null) {
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(
                            activity.getContentResolver().openInputStream(cropImageUri));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    image = out.toByteArray();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                if (bitmap != null) {
                    ivPic.setImageBitmap(bitmap);
                } else {
                    ivPic.setImageResource(R.drawable.silhouettes);
                }
            }
        }
    }


    private void sendInfotoMysql(Member member) {

        if (RemoteAccess.networkConnected(activity)) {
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "update");
            jsonObject.addProperty("member", new Gson().toJson(member));
            // ???????????????
            //image byte array
            if (image != null) {
                //byte to string
                jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
            }
            int count;
            //???thread ?????????
            String result = RemoteAccess.getRemoteData(url, jsonObject.toString());
            try{
                count = Integer.parseInt(result);
            }catch(NumberFormatException ex){
                ex.printStackTrace();
                count = 0;
            }
            if (count == 0) {
                Toast.makeText(activity, getString(R.string.text_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, getString(R.string.text_update_success), Toast.LENGTH_SHORT).show();

                Navigation.findNavController(etEmail).navigate(R.id.action_memeberCenterProfileFragment_to_memberCenterFragment);
            }
        } else {
            Toast.makeText(activity, R.string.test_no_network, Toast.LENGTH_SHORT).show();
        }

    }

}