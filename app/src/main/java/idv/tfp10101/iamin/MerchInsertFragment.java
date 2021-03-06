package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;

import static android.app.Activity.RESULT_OK;

/**
 * 新增商品頁面
 * (需用到第三方函式庫：Ucrop)
 */
public class MerchInsertFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private RecyclerView recyclerViewMerchInsert;
    private ImageView imageViewMrechInsertImg;
    private EditText editTextMerchInsertName;
    private EditText editTextMerchInsertPrice;
    private EditText editTextMerchInsertDesc;
    private Button buttonMerchSubmit;
    // 物件
    private Member member;
    private List<byte[]> images = new ArrayList<>();
    private int imgCount = 0;
    private final int IMAGE_MAX = 5;
    private File file;
    private Uri contentUri;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewMrechInsertImg = view.findViewById(R.id.imageViewMrechInsertImg);
        editTextMerchInsertName = view.findViewById(R.id.editTextMerchInsertName);
        editTextMerchInsertPrice = view.findViewById(R.id.editTextMerchInsertPrice);
        editTextMerchInsertDesc = view.findViewById(R.id.editTextMerchInsertDesc);
        buttonMerchSubmit = view.findViewById(R.id.buttonMerchUpdate);

        /** 先載入RecyclerView元件，但是還沒有掛上Adapter */
        recyclerViewMerchInsert = view.findViewById(R.id.recyclerViewMerchInsert);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMerchInsert.setLayoutManager(linearLayoutManager);
    }

    /**
     * 新式跳轉Activity與Result作法 (可以避免多個Result，自定一堆跳轉代碼的麻煩)
     * 注意：必需在onCreate()之前
     *
     * 發射器-ActivityResultLauncher -> 註冊協議 (Contract協議, Callback)
     *
     * 註冊協議：使用 registerForActivityResult()
     * Contract協議：使用 預設 (也可以自訂但是要繼承 ActivityResultContract)
     * ActivityResultContract：可以更加方便且安全的處理 startActivityForResult
     * Callback：(參考方法) 回傳結果 -> 覆寫onActivityResult(ActivityResult result)
     *
     */
    // 拍照跳轉 - 發射器＆接收器
    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);
    // 讀入圖片跳轉 - 發射器＆接收器
    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);
    // Ucrop跳轉 - 發射器＆接收器
    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);
    // 照相權限 - 發射器＆接收器
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // 如果同意開啟權限
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(activity, "如需使用相機權限請到系統中將相機權限開啟", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * 生命週期-2
     * 初始化與畫面無直接關係之資料 (設計: )
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 生命週期-3
     * 載入並建立Layout (設計: )
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 取得Activity參考
        activity = getActivity();
        activity.setTitle("新增商品");
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_merch_insert, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        /** 抓取會員ID */
        member = MemberControl.getInstance();

        // 導航控制
        final NavController navController = Navigation.findNavController(view);

        // 用RecyclerView顯示商品圖片
        showMerchImgs();

        // 新增圖片
        habdleAddImage();
        
        // 加入商品
        handleAddSubmit(navController);
    }

    /**
     * 顯示新增的圖片
     */
    private void showMerchImgs() {
        /** RecyclerView */
        // 檢查
        MerchImgAdapter merchImgAdapter = (MerchImgAdapter) recyclerViewMerchInsert.getAdapter();
        if (merchImgAdapter == null) {
            recyclerViewMerchInsert.setAdapter(new MerchImgAdapter(activity, images));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMerchInsert.addItemDecoration(new Constants.SpacesItemDecoration("right", px));
        }else{
            // 資訊重新載入刷新
            merchImgAdapter.setMerchImg(images);
            merchImgAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 新增圖片
     * (限5張)
     */
    private void habdleAddImage() {
        imageViewMrechInsertImg.setOnClickListener(view -> {
            // 不能超過五張
            if (imgCount >= IMAGE_MAX) {
                Toast.makeText(activity, "超出5張上限", Toast.LENGTH_SHORT).show();
                return;
            }

            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("請選擇上傳圖片的方式");
            final String items[] = {"照相", "圖片"};

            // 設定＆註冊監聽 (-1代表沒有條目被選中)
            builder.setSingleChoiceItems(items, -1, (dialog, which) -> {
                // 選擇哪一個
                Log.d(Constants.TAG, "which: " +  which + ", " + items[which]);
                // 關閉
                dialog.dismiss();

                // 處理
                handleImgSelect(which);
            });
            // 顯示
            builder.show();
        });
    }

    /**
     * 新增圖片 - 載入圖片的方式
     * @param select
     */
    private void handleImgSelect(int select) {
        switch (select) {
        /** 拍照片 */
        case 0:
            checkCameraPermission();
            break;

        /** 選取及複製 手機圖片 */
        case 1:
            Intent intent;
            // Intent -> (圖片 ACTION, 圖片存放的URI(可以選擇 外部 or 內部))
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Launcher() -> 進行跳轉 等待接收回傳結果 -> pickPictureResult()
            pickPictureLauncher.launch(intent);
            break;

        default:
            break;
        }
    }

    /**
     * 檢查相機是否有權限
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 進入這兒表示沒有許可權

            //https://stackoverflow.com/questions/56412401/working-of-shouldshowrequestpermissionrationale-method
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                Log.d("CAMERA","1");
                // 提示已經禁止
                Toast.makeText(activity, "需要相機權限", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                Log.d("CAMERA","2");
            }
        } else {
            openCamera();
        }
    }

    /**
     * 開啟相機
     */
    // 開啟相機
    private void openCamera() {
        // Intent -> 相機 ACTION
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**
         * 照片的存取位置 設定
         * API 24 版本以上，Android 不再允許在 app 中透漏 file://Uri 給其他 app
         * FileProvider 將隱藏真實的共享檔案路徑，content://Uri 取代 file://Uri
         * 注意：FileProvider需要在manifest.xml做設定
         */
        // 1.照片存放目錄 = 取得外部儲存體路徑(Environment.DIRECTORY_路徑種類)
        file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 2.建立存放照片的 路徑＆檔名
        file = new File(file, "picture.jpg");
        // 3.使用FileProvider建立Uri物件 (context, 需與manifest.xml的authorities相同名, 路徑檔名)
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".provider", file);
        // 4.intent.putExtra(key, value) -> 帶值
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        try {
            // Launcher() -> 進行跳轉 等待接收回傳結果 -> takePictureResult()
            takePictureLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.textNoCameraApp, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 上傳資料的處理
     */
    private void handleAddSubmit(NavController navController) {
        buttonMerchSubmit.setOnClickListener(view -> {
            // date處理(抓取與防呆)
            Boolean passed = true;
            if (editTextMerchInsertName.getText().toString().isEmpty()) {
//                editTextMerchInsertName.setHint("必填欄位-商品名稱(最多30字)");
//                editTextMerchInsertName.setHintTextColor(resources.getColor(R.color.colorRed));
                passed = false;
            }
            if (editTextMerchInsertPrice.getText().toString().isEmpty()) {
//                editTextMerchInsertPrice.setHint("必填欄位-優惠價(阿拉伯數字)");
//                editTextMerchInsertPrice.setHintTextColor(resources.getColor(R.color.colorRed));
                passed = false;
            }
            if (!passed) {
                Toast.makeText(activity, "有必填欄位尚未填寫！", Toast.LENGTH_SHORT).show();
                return;
            }
            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("新增商品");
            builder.setMessage("確定要新增嗎？");

            builder.setPositiveButton("確認", (dialog, which) -> {
                // 關閉
                dialog.dismiss();

                // 建立一筆商品class (目前是假的memberID)
                String name = editTextMerchInsertName.getText().toString();
                int price = Integer.parseInt(editTextMerchInsertPrice.getText().toString());
                String desc = editTextMerchInsertDesc.getText().toString();
                Merch merch = new Merch(
                        0,
                        member.getId(),
                        name,
                        price,
                        desc,
                        0
                );
                /** 存入server */
                int resulr = MerchControl.insertMerch(activity, merch, images);
                if (resulr > 0) {
                    Toast.makeText(activity, "新增商品成功", Toast.LENGTH_SHORT).show();
                    /* 回前一個Fragment */
                    navController.popBackStack();
                }
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                // 關閉
                dialog.dismiss();
            });
            // 顯示
            builder.show();
        });

        /** 測試專用 */
        buttonMerchSubmit.setOnLongClickListener(view -> {
            // 設定預設資料
            editTextMerchInsertName.setText("紅龍牛肉捲");
            editTextMerchInsertPrice.setText("280");
            editTextMerchInsertDesc.setText("10 入");
            return  true;
        });
    }

    /**
     * takePictureLauncher -> Result
     * @param result
     */
    private void takePictureResult(ActivityResult result) {
        // 如果有拍照
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
    }

    /**
     * pickPictureLauncher -> Result
     * @param result
     */
    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                // result.getData() -> 取得 Intent
                // result.getData().getData() -> 在 Intent 內取得Uri的數據
                crop(result.getData().getData());
            }
        }
    }

    /**
     * 使用Ucrop進行裁剪
     * @param sourceImageUri 照片Uri
     */
    private void crop(Uri sourceImageUri) {
        // 裁剪圖片存放目錄 = 取得外部儲存體路徑(Environment.DIRECTORY_路徑種類)
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 建立裁剪圖片的 路徑＆檔名
        file = new File(file, "picture_cropped.jpg");
        // 建立Uri物件 (存放地 Uri)
        Uri destinationUri = Uri.fromFile(file);
        // 使用UCrop建立Intent
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
                .withAspectRatio(0.5f, 0.5f) // 設定裁減比例
//                .withMaxResultSize(100, 100) // 設定結果尺寸不可超過指定寬高
                .getIntent(activity);
        // Launcher() -> 進行跳轉 等待接收回傳結果 -> cropPictureResult()
        cropPictureLauncher.launch(cropIntent);
    }

    /**
     * cropPictureLauncher -> Result
     * @param result
     */
    private void cropPictureResult(ActivityResult result) {
        if (result.getData() != null) {
            // 抓取Uri
            Uri resultUri = UCrop.getOutput(result.getData());
            Bitmap bitmap = null;
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    /**
                     * Bitmap -> 藉助於BitmapFactory獲取點陣圖
                     * BitmapFactory.decodeStream： 簡單的把 Uri 轉成 bitmap
                     * Content.getContentResolves()： 整個app的資源
                     */
                    bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(resultUri));
                } else {
                    /**
                     * ImageDecoder -> 可以較方便處理 圖片放大縮小
                     *
                     * ImageDecoder.Source： 要想顯示圖片，我們就得有來源，ImageDecoder把圖片的來源統一抽象成了Source，
                     * ImageDecoder.createSource： 要建立使用的Source，可以用此靜態方法
                     * Content.getContentResolves()： 整個app的資源
                     */
                    ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), resultUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                }
                // 記憶體 的 OutputStream
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // compress壓縮 (格式, 壓縮品質, 存放位置)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                // bitmap壓縮 -> out -> 轉成byte陣列 (準備requst)
                images.add(out.toByteArray());
                // 圖片計數
                imgCount = images.size();
            } catch (IOException e) {
                Log.e(Constants.TAG, e.toString());
            }
            // 如果有圖片
            if (bitmap != null) {
                /** 刷新 ViewHolder */
                showMerchImgs();
            } else {

            }
        }
    }

    /**
     * 自定義Adapter 繼承 RecyclerView 的 Adapter
     * 1. 建立Context & 一些需要的資訊，並constructor
     * 2. 實作 RecyclerView.ViewHolder 給 Adapter使用
     * 3. 設定父類別泛型型態
     * 4. 自動建立 Override 方法 (onCreateViewHolder, onBindViewHolder, getItemCount)
     */
    private class MerchImgAdapter extends RecyclerView.Adapter<MerchImgAdapter.MyViewHolder> {
        private List<byte[]> rvimages;
        private LayoutInflater layoutInflater = LayoutInflater.from(activity);

        MerchImgAdapter(Context context, List<byte[]> images) {
            layoutInflater = LayoutInflater.from(context);
            rvimages = images;
        }

        public void setMerchImg(List<byte[]> images) {
            rvimages = images;
        }

        /** ViewHolder */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            MyViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageViewMerchImg);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 載入Layout
            View itemView = layoutInflater.inflate(R.layout.image_view_merch, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final byte[] img = rvimages.get(position); // 第幾個merch
            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            holder.imageView.setImageBitmap(bitmap);

            // 長按監聽 -> 顯示彈出選單 (popupMenu.show())
            holder.itemView.setOnLongClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
                popupMenu.inflate(R.menu.popup_menu_merch_img);
                popupMenu.show();
                // 監聽 PopupMenu
                popupMenu.setOnMenuItemClickListener(item -> {
                    // 點擊哪一個
                    switch (item.getItemId()) {
                        case R.id.itemMrechImgDelete:
                            // 與內外層一起同步移除
                            rvimages.remove(img); // 內層
                            MerchImgAdapter.this.notifyDataSetChanged(); // 刷新
                            MerchInsertFragment.this.images.remove(img); // 外層
                            Toast.makeText(activity, R.string.textDeleteSuccess, Toast.LENGTH_SHORT).show();
                            // 圖片計數
                            imgCount = images.size();
                            break;

                        default:
                            break;
                    }
                    return true;
                });
                return true;
            });

        }

        @Override
        public int getItemCount() {
            return rvimages == null ? 0 : rvimages.size();
        }
    }
}