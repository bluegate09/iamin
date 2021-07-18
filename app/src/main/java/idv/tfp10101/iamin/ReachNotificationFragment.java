package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.network.RemoteAccess;

public class ReachNotificationFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private TextView textViewGroupName;
    private RadioGroup radioGroupPayment;
    private EditText editTextTitle;
    private EditText editTextBody;
    private Button buttonSubmit;
    // 物件
    private int GroupPaymentSelect = 0;
    // 導航控制(頁面切換用)
    private NavController navController;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        textViewGroupName = view.findViewById(R.id.textViewGroupName);
        radioGroupPayment = view.findViewById(R.id.radioGroupPayment);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextBody = view.findViewById(R.id.editTextBody);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);

        navController = Navigation.findNavController(view);
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

        return inflater.inflate(R.layout.fragment_reach_notification, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        // 取得Bundle物件
        Bundle bundle = getArguments();
        String groupName = bundle.getString("groupName");
        // 顯示團購名稱
        textViewGroupName.setText(groupName);

        // 選擇要推送訊息的類別
        handleRadioGroupPayment();

        // 發送
        handleSubmit();
    }

    /**
     * 選擇要推送訊息的類別
     */
    private void handleRadioGroupPayment() {
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonAll:
                    GroupPaymentSelect = 0;
                    break;

                case R.id.radioButtonPaid:
                    GroupPaymentSelect = 1;
                    break;

                case R.id.radioButtonUnpaid:
                    GroupPaymentSelect = 2;
                    break;

                default:
                    break;
            }
        });
    }

    /**
     * 發送
     */
    private void handleSubmit() {
        // 取得Bundle物件
        Bundle bundle = getArguments();
        int groupId = bundle.getInt("groupId");

        String title = "";
        String body = "";
        title = editTextTitle.getText().toString().trim();
        body = editTextBody.getText().toString().trim();

        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(activity)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Fcm";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "sendFcmByGroupId");
            jsonObject.addProperty("groupId", groupId);
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("body", body);
            jsonObject.addProperty("data", "data------");

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            if (jsonString != null) {
                /** 建立AlertDialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("發送成功");
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* 回前一個Fragment */
                        navController.popBackStack();
                    }
                });

                // 顯示
                builder.show();
            }else {
                Toast.makeText(activity, "推播失敗", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }
}