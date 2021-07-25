package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import idv.tfp10101.iamin.qrcode.Contents;
import idv.tfp10101.iamin.qrcode.QRCodeEncoder;

import static android.content.ContentValues.TAG;

public class QRCodeGenFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    private ImageView imageViewQRcode;
    // 物件
    private int memberOderId = -1;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        imageViewQRcode = view.findViewById(R.id.imageViewQRcode);
    }

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
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_q_r_code_gen, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        // 取得Bundle物件顯示，並且從server抓取圖片
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("memberOderId") < 0) {
            Toast.makeText(activity, "資料錯誤", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }
        memberOderId = bundle.getInt("memberOderId");
        // 轉字串
        String memberOderIdText = String.valueOf(memberOderId);
        Log.d(Constants.TAG, memberOderIdText);
        // QR code圖片大小是正方形，設定要多大 (寬度跟螢幕一樣寬)
        int dimension = getResources().getDisplayMetrics().widthPixels;
        // QR Code圖片編碼 (字串, Bundle, Type, 形式, 尺寸)
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(memberOderIdText, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
                dimension);
        // 產生
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imageViewQRcode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Log.e(TAG, e.toString());
        }
    }
}