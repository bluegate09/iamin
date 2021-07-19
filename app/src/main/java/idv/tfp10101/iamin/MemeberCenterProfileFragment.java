package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import androidx.navigation.Navigation;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;
import idv.tfp10101.iamin.user.User;

import static android.app.Activity.RESULT_OK;

public class MemeberCenterProfileFragment extends Fragment {
    private final String TAG = "TAG_MC_Profile";
    private EditText etEmail, etPassword, etNickname, etPhoneNumber;
    private Activity activity;
    private Member member;
    private ImageView ivPic;
    private byte[] image;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private User user;
    private ProgressDialog loadingBar;
    private Uri contentUri; // 拍照需要的Uri
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = User.getInstance();
        loadingBar = new ProgressDialog(activity);
//        Log.d(TAG,"MC_Profile_OnCreate member: " + member.getNickname());

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

        setTextView();
        setImageView();

        //確認修改完成按鈕
        view.findViewById(R.id.btProfileUpdate).setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (!email.isEmpty()) {
                member.setEmail(email);
            }

            if (!password.isEmpty()){
                member.setPassword(password);
            }

            if (!TextUtils.isEmpty(nickname)) {
                member.setNickname(nickname);
            }

            if (!TextUtils.isEmpty(phoneNumber)) {
                member.setPhoneNumber(phoneNumber);
            }

            member.setEmail(email);
            member.setPassword(password);

            //member bean 更新
            MemberControl.setMember(member);
            //mysql更新修改後的資訊
            sendInfotoMysql(member);


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

    private void setTextView() {
        etEmail.setText(member.getEmail());
        etPassword.setText(member.getPassword());
        etNickname.setText(member.getNickname());
        etPhoneNumber.setText(member.getPhoneNumber());
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
            uploadImage(cropImageUri);
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

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    String token = task.getResult();
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", member.getuUId());
                    hashMap.put("email", member.getEmail());
                    hashMap.put("name", member.getNickname());
                    hashMap.put("phonenumber", member.getPhoneNumber());
                    hashMap.put("token", token);
                    if(member.getuUId() == null){
                        member.setuUId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                    db.collection("Users").document(member.getuUId()).set(hashMap)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d(TAG, "user into firebase success: " + member.getId());
                                } else {
                                    Log.e(TAG, "message: " + task.getException().getMessage());
                                }
                            });
                }
            }
        });

        if (RemoteAccess.networkConnected(activity)) {
            String url = RemoteAccess.URL_SERVER + "memberController";
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

                Navigation.findNavController(etEmail).popBackStack();
            }
        } else {
            Toast.makeText(activity, R.string.test_no_network, Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImage(Uri imageUri) {
        // 取得storage根目錄位置
        StorageReference rootRef = storage.getReference();
        final String imagePath = getString(R.string.app_name) + "/images/" + System.currentTimeMillis();
        // 建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);
        // 將儲存在uri的照片上傳
        imageRef.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = getString(R.string.textUploadSuccess);
                        Log.d(TAG, message);
                        user.setImagePath(imagePath);
                        db.collection("Users").document(member.getuUId())
                                .update("imagePath", imagePath).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(TAG, "imagePath to firebase success");
                            } else {
                                Log.d(TAG, "imagePath to firebase fail");
                            }
                        });
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        // 下載剛上傳好的照片
//                        downloadImage(imagePath);
                    } else {
                        String message = task.getException() == null ?
                                getString(R.string.textUploadFail) :
                                task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingBar(String titleText, String messageText) {
        loadingBar.setTitle(titleText);
        loadingBar.setMessage(messageText);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
    }

}