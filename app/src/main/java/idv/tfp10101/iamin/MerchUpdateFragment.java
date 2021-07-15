package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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

import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;

import static android.app.Activity.RESULT_OK;

public class MerchUpdateFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private RecyclerView recyclerViewMerchUpdate;
    private ImageView imageViewMrechUpdateImg;
    private EditText editTextMerchUpdateName;
    private EditText editTextMerchUpdatePrice;
    private EditText editTextMerchUpdateDesc;
    private Button buttonMerchUpdate;
    // 物件
    private List<byte[]> images = new ArrayList<>();
    private int imgCount = 0;
    private final int IMAGE_MAX = 5;
    private File file;
    private Uri contentUri;
    private Merch merch;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewMrechUpdateImg = view.findViewById(R.id.imageViewMrechUpdateImg);
        editTextMerchUpdateName = view.findViewById(R.id.editTextMerchUpdateName);
        editTextMerchUpdatePrice = view.findViewById(R.id.editTextMerchUpdatePrice);
        editTextMerchUpdateDesc = view.findViewById(R.id.editTextMerchUpdateDesc);
        buttonMerchUpdate = view.findViewById(R.id.buttonMerchUpdate);

        /** 先載入RecyclerView元件，但是還沒有掛上Adapter */
        recyclerViewMerchUpdate = view.findViewById(R.id.recyclerViewMerchUpdate);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMerchUpdate.setLayoutManager(linearLayoutManager);
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
        activity.setTitle("更新商品");
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_merch_update, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        // 導航控制
        final NavController navController = Navigation.findNavController(view);
        // 取得Bundle物件顯示，並且從server抓取圖片
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("merch") == null) {
            Toast.makeText(activity, R.string.textNoMerchFound, Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        // 載入編輯商品的資訊 (NavController->Bundle->"merch")
        loadMarchData(bundle);

        // 用RecyclerView顯示商品圖片
        showMerchImgs();

        // 新增圖片
        habdleAddImage();

        // 加入商品
        handleAddSubmit(navController);
    }

    /**
     * 載入編輯商品的資訊
     * @param bundle
     */
    private void loadMarchData(Bundle bundle) {
        // 文字
        merch = (Merch) bundle.getSerializable("merch");
        editTextMerchUpdateName.setText(merch.getName());
        editTextMerchUpdatePrice.setText(String.valueOf(merch.getPrice()));
        editTextMerchUpdateDesc.setText(merch.getMerchDesc());
        // 圖片
        images = MerchControl.getMerchImgsById(activity, merch.getMerchId());
        showMerchImgs();
    }

    /**
     * 顯示新增的圖片
     */
    private void showMerchImgs() {
        /** RecyclerView */
        // 檢查
        MerchImgAdapter merchImgAdapter = (MerchImgAdapter) recyclerViewMerchUpdate.getAdapter();
        if (merchImgAdapter == null) {
            recyclerViewMerchUpdate.setAdapter(new MerchImgAdapter(activity, images));
            int px = (int) Constants.convertDpToPixel(8, activity); // 間距 8 dp
            recyclerViewMerchUpdate.addItemDecoration(new Constants.SpacesItemDecoration("right", px));
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
        imageViewMrechUpdateImg.setOnClickListener(view -> {
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
                int select = which;
                Log.d(Constants.TAG, "which: " +  which + ", " + items[which]);
                // 關閉
                dialog.dismiss();

                // 處理
                handleImgSelect(select);
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
        Intent intent;
        switch (select) {
            /** 拍照片 */
            case 0:
                // Intent -> 相機 ACTION
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                break;
            /** 選取及複製 手機圖片 */
            case 1:
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
     * 上傳資料的處理
     */
    private void handleAddSubmit(NavController navController) {
        buttonMerchUpdate.setOnClickListener(view -> {
            // date處理(抓取與防呆)
            Boolean passed = true;
            if (editTextMerchUpdateName.getText().toString().isEmpty()) {
                editTextMerchUpdateName.setHint("必填欄位-商品名稱(最多30字)");
                editTextMerchUpdateName.setHintTextColor(resources.getColor(R.color.colorRed));
                passed = false;
            }
            if (editTextMerchUpdatePrice.getText().toString().isEmpty()) {
                editTextMerchUpdatePrice.setHint("必填欄位-優惠價(阿拉伯數字)");
                editTextMerchUpdatePrice.setHintTextColor(resources.getColor(R.color.colorRed));
                passed = false;
            }
            if (!passed) {
                return;
            }
            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("更新商品");
            builder.setMessage("確定要更新嗎？");

            builder.setPositiveButton("確認", (dialog, which) -> {
                // 關閉
                dialog.dismiss();

                // 文字
                merch.setName(editTextMerchUpdateName.getText().toString());
                merch.setPrice(Integer.parseInt(editTextMerchUpdatePrice.getText().toString()));
                merch.setMerchDesc(editTextMerchUpdateDesc.getText().toString());

                /** 更新server */
                int result = MerchControl.updateMerch(activity, merch, images);
                if (result > 0) {
                    Toast.makeText(activity, "更新商品成功", Toast.LENGTH_SHORT).show();
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
                .withAspectRatio(1, 1) // 設定裁減比例
//                .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
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
            final byte[] img = rvimages.get(position); // 第幾個img
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
                            notifyDataSetChanged(); // 刷新
                            images.remove(img); // 外層
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