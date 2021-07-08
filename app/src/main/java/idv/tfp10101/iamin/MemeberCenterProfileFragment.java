package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.network.RemoteAccess;

import static android.app.Activity.RESULT_OK;

public class MemeberCenterProfileFragment extends Fragment {
    private final String TAG = "TAG_MC_Profile";
    private EditText etEmail, etPassword, etNickname, etPhoneNumber;
    private Activity activity;
    private Member member;
    private ImageView ivPic;
    private byte[] image;
    private Uri contentUri; // 拍照需要的Uri

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = Member.getInstance();
//        Log.d(TAG,"MC_Profile_OnCreate");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d(TAG,"MC_Profile_OnDestroy");
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
        etPassword = view.findViewById(R.id.etProfilePassword);
        etNickname = view.findViewById(R.id.etProfileNickname);
        etPhoneNumber = view.findViewById(R.id.etProfilePhoneNumber);
        ivPic = view.findViewById(R.id.ivProfilePic);

        //從mysql拿資料放進textVIew||imageView
        setTextView();
        setImageView();

        //確認修改完成按鈕
        view.findViewById(R.id.btProfileUpdate).setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            //確定email 跟 password格式
            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                Toast.makeText(activity, "Email/Password can't not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(nickname)) {
                member.setNickname(nickname);
            }

            if (!TextUtils.isEmpty(phoneNumber)) {
                member.setPhoneNumber(phoneNumber);
            }

            member.setEmail(email);
            member.setPassword(password);

            //mysql更新修改後的資訊
            sendInfotoMysql(member);
            Member.getInstance().setUpdate(false);
            Log.d(TAG,member.isUpdate()+"");
        });

        //照片修改
        view.findViewById(R.id.ibProfile).setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            String[] items = {"TakePicture","PickPicture"};
            alert.setSingleChoiceItems(items, -1, (dialog, which) -> {
                dialog.dismiss();
                //彈出視窗
                handleImgSelect(which);
            });
            alert.show();
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setTextView() {
        etEmail.setText(member.getEmail());
        etPassword.setText(member.getPassword());
        etNickname.setText(member.getNickname());
        etPhoneNumber.setText(member.getPhoneNumber());
    }

    private void setImageView(){
        String url = RemoteAccess.URL_SERVER + "memberServelt";
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
        if(which == 0){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Directory not created");
                    return;
                }
            }
            File file = new File(dir, "picture.jpg");
            contentUri = FileProvider.getUriForFile(
                    activity, activity.getPackageName() + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "Camera not found",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if(which == 1){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        }

    }

    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

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
//                .withAspectRatio(16, 9) // 設定裁減比例
                .withMaxResultSize(300, 300) // 設定結果尺寸不可超過指定寬高
                .getIntent(activity);
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            // 截圖的Uri
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
            String url = RemoteAccess.URL_SERVER + "memberServelt";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "update");
            jsonObject.addProperty("member", new Gson().toJson(member));
            // 有圖才上傳
            //image byte array
            if (image != null) {
                //byte to string
                jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
            }
            int count;
            //開thread 抓資料
            String result = RemoteAccess.getRometeData(url, jsonObject.toString());
            try{
                count = Integer.parseInt(result);
            }catch(NumberFormatException ex){
                ex.printStackTrace();
                count = 0;
            }
            if (count == 0) {
                Toast.makeText(activity, "Update failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Update success", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "No net work", Toast.LENGTH_SHORT).show();
        }
    }

}