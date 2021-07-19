package idv.tfp10101.iamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupCategory;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.group.GroupInsertAddViewData;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.merch.MerchControl;

public class GroupInsertFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    // 元件
    private ScrollView scrollViewMain;
    private EditText editTextName; // 團購名稱
    private EditText editTextGoal; // 達標數量
    private LinearLayout linearLayoutMerchs;
    private ImageView imageViewAddMerch;
    private TextView textViewTotalAmount;
    private Spinner spinnerCategory;
    private EditText editTextItem; // 自訂項目
    private TextView textViewConditionTime; // 截止時間 (顯示用)
    private ImageView imageViewConditionTime;
    private RadioGroup RadioGroupConditionCount;
    private EditText editTextConditionCount; // 停單條件(份數)
    private EditText editTextContactNumber; // 聯絡電話
    private CheckBox checkBoxCreditcard, checkBoxFaceToFace;
    private ImageView imageViewAddLocation;
    private LinearLayout linearLayoutLocation;
    private EditText editTextCaution; // 注意事項
    private RadioGroup RadioGroupPrivacy;
    private Button buttonGroupInsert;
    // 物件
    private Member member;
    List<GroupCategory> groupCategories; // 種類清單
    private int totalAmount = 0; // 總金額
    private Boolean isConditionCount = false; // 停單條件
    private Boolean isCC = false, isFF = false;
    private Boolean isPrivacy = false; // 隱私設定
    private String ConditionTime = null; // (顯示用)
    private String stringConditionTime = null; // 截止時間 (儲存-轉格式用)
    private Timestamp timestampCondition = null; // 截止時間 (儲存用)

    // 導航控制(頁面切換用)
    private NavController navController;

    /**
     * 取得xml元件
     * @param view Activity下的view
     */
    private void findViews(View view) {
        scrollViewMain = view.findViewById(R.id.scrollViewMain);
        editTextName = view.findViewById(R.id.editTextName);
        editTextGoal = view.findViewById(R.id.editTextGoal);
        linearLayoutMerchs = view.findViewById(R.id.linearLayoutMerchs);
        imageViewAddMerch = view.findViewById(R.id.imageViewAddMerch);
        textViewTotalAmount = view.findViewById(R.id.textViewTotalAmount);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        editTextItem = view.findViewById(R.id.editTextItem);
        textViewConditionTime = view.findViewById(R.id.textViewConditionTime);
        imageViewConditionTime = view.findViewById(R.id.imageViewConditionTime);
        RadioGroupConditionCount = view.findViewById(R.id.RadioGroupConditionCount);
        editTextConditionCount = view.findViewById(R.id.editTextConditionCount);
        editTextContactNumber = view.findViewById(R.id.editTextContactNumber);
        checkBoxCreditcard = view.findViewById(R.id.checkBoxCreditcard);
        checkBoxFaceToFace = view.findViewById(R.id.checkBoxFaceToFace);
        imageViewAddLocation = view.findViewById(R.id.imageViewAddLocation);
        linearLayoutLocation = view.findViewById(R.id.linearLayoutLocation);
        editTextCaution = view.findViewById(R.id.editTextCaution);
        RadioGroupPrivacy = view.findViewById(R.id.RadioGroupPrivacy);
        buttonGroupInsert = view.findViewById(R.id.buttonSubmit);

        navController = Navigation.findNavController(view);
    }

    /**
     * 生命週期-2
     * 初始化與畫面無直接關係之資料 (設計: )
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("tag", "onCreate");
    }

    /**
     * 生命週期-3
     * 載入並建立Layout (設計: )
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("tag", "onCreateView");
        // 取得Activity參考
        activity = getActivity();
        // 取得Resources
        resources = getResources();

        return inflater.inflate(R.layout.fragment_group_insert, container, false);
    }

    /**
     * 生命週期-4
     * Layout已建立後 (設計: 互動的處理)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("tag", "onViewCreated");

        findViews(view);
        if (ConditionTime != null) {
            textViewConditionTime.setText(ConditionTime);
        }

        /** 抓取會員ID */
        member = MemberControl.getInstance();

        // 加入商品
        handleAddMerch();

        // 類別的選擇
        handleCategory();

        // 選擇截止時間
        handleConditionTime();

        // 是否要設定達標份數
        handleConditionCount();

        // 設定收款方式
        handlePayment();

        // 加入發貨地址
        handleAddLocation();

        // 隱私設定
        handlePrivacy();

        // 新增團購
        handleAddSubmit(view);

        // 監聽頁面跳回來的結果
        handleFragmentResult();
    }

    /**
     * 加入商品
     */
    private void handleAddMerch() {
        imageViewAddMerch.setOnClickListener(view -> {
            GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
            if (giavd.MerchsId().size() > 0) {
                // Bundle -> 打包資料傳遞 putXYZ(key, value)
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("merchsId", giavd.MerchsId());
                navController.navigate(R.id.action_groupInsertFragment_to_groupSelectMFragment, bundle);
            }else {
                // 切換頁面
                navController.navigate(R.id.action_groupInsertFragment_to_groupSelectMFragment);
            }
        });
    }

    /**
     * 類別的選擇
     */
    private void handleCategory() {
        // 從server抓取資料
        groupCategories = GroupControl.getAllCategory(activity);
        if (groupCategories == null || groupCategories.isEmpty()) {
            Toast.makeText(activity, R.string.textNoGroupCategorieFound, Toast.LENGTH_SHORT).show();
        }else {
            // 準備放入Spinner內的String
            List<String> strings = new ArrayList<>();
            for (GroupCategory groupCategory : groupCategories) {
                strings.add(groupCategory.getCategory());
            }
            // 實例化Adapter物件 (Context, 外觀, 顯示的List<String>)
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(activity, R.layout.spinner_group_insert_category, strings);
            // 設定要下拉的樣式
            adapter.setDropDownViewResource(R.layout.spinner_group_insert_category);
            spinnerCategory.setAdapter(adapter); // Adapter 設定進 spType
        }
    }

    /**
     * 選擇截止日期
     */
    private void handleConditionTime() {
        imageViewConditionTime.setOnClickListener(view -> {
            // 取得Calendar物件
            Calendar calendar = Calendar.getInstance();
            // 實例化DatePickerDialog物件
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    activity,
                    (DatePicker datePicker, int year, int month, int dayOfMonth) -> {
                        // 顯示用
                        ConditionTime = year + "/" + (month + 1) + "/" + dayOfMonth;
                        // 儲存用
                        stringConditionTime = year + "-" + (month + 1) + "-" + dayOfMonth;
                        settingTime();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            // 顯示對話框
            datePickerDialog.show();
        });
    }

    /**
     * 設定截止時間
     */
    private void settingTime() {
        // 取得Calendar物件
        Calendar calendar = Calendar.getInstance();
        // 實例化TimePickerDialog物件
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                activity,
                (TimePicker view, int hourOfDay, int minute) -> {
                    //final String text = "" + hourOfDay + ":" + minute;
                    //textViewConditionTime.append(text);
                    String h, m;
                    if (hourOfDay < 10) {
                        h = "0" + hourOfDay;
                    }else {
                        h = "" + hourOfDay;
                    }
                    if (minute < 10) {
                        m = "0" + minute;
                    }else {
                        m = "" + minute;
                    }
                    // 顯示用
                    ConditionTime += " " + h + ":" + m;
                    // 儲存用
                    stringConditionTime += " " + h + ":" + m + ":00";

                    textViewConditionTime.setText(ConditionTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        // 顯示對話框
        timePickerDialog.show();
    }

    /**
     * 是否要設定達標份數
     */
    private void handleConditionCount() {
        RadioGroupConditionCount.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonCountOff:
                    isConditionCount = false;
                break;

                case R.id.radioButtonCountOn:
                    isConditionCount = true;
                break;

                default:
                break;
            }
        });
    }

    /**
     * 設定收款方式
     */
    private void handlePayment() {
        final CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                switch (buttonView.getId()) {
                    case R.id.checkBoxCreditcard:
                        isCC = true;
                        break;

                    case R.id.checkBoxFaceToFace:
                        isFF = true;
                        break;

                    default:
                        break;
                }
            }else {
                switch (buttonView.getId()) {
                    case R.id.checkBoxCreditcard:
                        isCC = false;
                        break;

                    case R.id.checkBoxFaceToFace:
                        isFF = false;
                        break;

                    default:
                        break;
                }
            }
        };
        checkBoxCreditcard.setOnCheckedChangeListener(listener);
        checkBoxFaceToFace.setOnCheckedChangeListener(listener);
    }

    /**
     * 加入發貨地址
     */
    private void handleAddLocation() {
        imageViewAddLocation.setOnClickListener(view -> {
            GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
            if (giavd.LatLngs().size() > 0) {
                // Bundle -> 打包資料傳遞 putXYZ(key, value)
                Bundle bundle = new Bundle();
                // 緯度[]
                double[] latArr = new double[giavd.Lats().size()];
                for (int i = 0; i < latArr.length; i++) {
                    latArr[i] = giavd.Lats().get(i);
                }
                bundle.putDoubleArray("lats", latArr);
                // 經度[]
                double[] lngArr = new double[giavd.Lngs().size()];
                for (int i = 0; i < latArr.length; i++) {
                    lngArr[i] = giavd.Lngs().get(i);
                }
                bundle.putDoubleArray("lngs", lngArr);
                // 地址[]
                bundle.putStringArrayList("locations", giavd.Locations());
                // 切換頁面
                navController.navigate(R.id.action_groupInsertFragment_to_groupInsertLocationFragment, bundle);
            }else {
                // 切換頁面
                navController.navigate(R.id.action_groupInsertFragment_to_groupInsertLocationFragment);
            }
        });
    }

    /**
     * 隱私設定
     */
    private void handlePrivacy() {
        RadioGroupPrivacy.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonPrivacyOff:
                    isPrivacy = false;
                    break;

                case R.id.radioButtonPrivacyOn:
                    isPrivacy = true;
                    break;

                default:
                    break;
            }
        });
    }

    /**
     * 新增團購
     */
    private void handleAddSubmit(View v) {
        buttonGroupInsert.setOnClickListener(view -> {
            List<Merch> merches = new ArrayList<>();
            /** 拿到跳轉分頁所選擇的資料 */
            GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
            /** 標題 */
            if (editTextName.getText().toString().trim().isEmpty()) {
                Toast.makeText(activity, "標題未設定", Toast.LENGTH_SHORT).show();
                editTextName.setHintTextColor(resources.getColor(R.color.colorRed));
                scrollViewMain.scrollTo(0, v.findViewById(R.id.editTextName).getTop());
                return;
            }
            /** 目標份數 */
            if (editTextGoal.getText().toString().trim().isEmpty()) {
                Toast.makeText(activity, "標目標份數未設定", Toast.LENGTH_SHORT).show();
                editTextGoal.setHint("需設定");
                editTextGoal.setHintTextColor(resources.getColor(R.color.colorRed));
                scrollViewMain.scrollTo(0, v.findViewById(R.id.editTextGoal).getTop());
                return;
            }
            /** 商品 */
            if (giavd.MerchsId().isEmpty()) {
                Toast.makeText(activity, "未選擇商品", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.layoutMerch).getTop());
                return;
            }
            for (int merchId : giavd.MerchsId()) {
                Merch merch = new Merch(
                        merchId,
                        member.getId(),
                        "",
                        0,
                        "",
                        0
                );
                merch.setMerchId(merchId);
                merches.add(merch);
            }
            /** 類別 */
            int categoryId = -1;
            String categoryString = null;
            for (GroupCategory groupCategory : groupCategories) {
                if (groupCategory.getCategory().equals(spinnerCategory.getSelectedItem().toString())) {
                    // ID
                    categoryId = groupCategory.getGroupCategoryId();
                    // 名稱
                    categoryString = groupCategory.getCategory();
                }
            }
            /** 團購項目 */

            /** 停單條件(時間) */
            if (stringConditionTime == null) {
                Toast.makeText(activity, "停單時間未設定", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.layoutTime).getTop());
                return;
            }
            timestampCondition = new Timestamp(System.currentTimeMillis());
            try {
                timestampCondition = Timestamp.valueOf(stringConditionTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /** 停單條件(份數) */
            int conditionCount = -1;
            if (isConditionCount) {
                if (editTextConditionCount.getText().toString().trim().isEmpty()) {
                    Toast.makeText(activity, "停單份數未設定", Toast.LENGTH_SHORT).show();
                    scrollViewMain.scrollTo(0, v.findViewById(R.id.layoutTime).getTop());
                    return;
                }
                String sConditionCount = editTextConditionCount.getText().toString().trim();
                conditionCount = Integer.parseInt(sConditionCount);
            }
            /** 聯絡電話 */
            if (editTextContactNumber.getText().toString().trim().isEmpty()) {
                Toast.makeText(activity, "聯絡電話未設定", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.editTextContactNumber).getTop());
                return;
            }
            String pattern = "09[0-9]{8}";
            if (!editTextContactNumber.getText().toString().trim().matches(pattern)) {
                Toast.makeText(activity, "電話格式錯誤", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.editTextContactNumber).getTop());
                return;
            }
            /** 收款方式 (1.面交 2.信用卡 3.兩者皆可) */
            int paymentMethod = -1;
            if (isCC && isFF) {
                // 兩者皆可
                paymentMethod = 3;
            }else if (!isCC && isFF) {
                // 信用卡
                paymentMethod = 2;
            }else if (isCC && !isFF) {
                // 面交
                paymentMethod = 1;
            }else {
                Toast.makeText(activity, "付款方式未設定", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.layoutPayment).getTop());
                return;
            }
            /** 地址 */
            if (giavd.LatLngs().isEmpty()) {
                Toast.makeText(activity, "地址未設定", Toast.LENGTH_SHORT).show();
                scrollViewMain.scrollTo(0, v.findViewById(R.id.layoutAddress).getTop());
                return;
            }

            /** 團購 Table */
            Group group = new Group(
                    -1,
                    member.getId(), // 會員ID
                    editTextName.getText().toString().trim(), // 標題 (清除左右空白)
                    0, // 目標進度
                    Integer.parseInt(editTextGoal.getText().toString().trim()), // 目標份數
                    categoryId, // 類別 ID
                    editTextItem.getText().toString().trim(), // 團購項目
                    editTextContactNumber.getText().toString().trim(), // 聯絡電話
                    paymentMethod, // 收款方式
                    1, // 團購狀態
                    editTextCaution.getText().toString().trim(), // 注意事項
                    isPrivacy, // 隱私設定
                    // totalAmount (商品總金額 * 目標份數)
                    (totalAmount * Integer.parseInt(editTextGoal.getText().toString().trim())),
                    0, // 目前收款金額
                    conditionCount, // 停單條件(份數)
                    timestampCondition, // 停單條件(時間)
                    categoryString, // 類別名稱
                    merches // 商品列表
            );
            // 發貨地址清單

            /** 建立AlertDialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("新增團購");
            builder.setMessage("確定要新增嗎？");

            builder.setPositiveButton("確認", (dialog, which) -> {
                // 關閉
                dialog.dismiss();

                /** 存入server */
                int resulr;
                // group Table
                resulr = GroupControl.insertGroup(activity, group, giavd.LatLngs());

                if (resulr > 0) {
                    // 清出儲存資料
                    GroupInsertAddViewData.remove();
                    // 回前一個Fragment
                    navController.popBackStack();
                }else {
                    Toast.makeText(activity, "表單新增失敗", Toast.LENGTH_SHORT).show();
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
     * 監聽頁面跳回來的結果
     */
    private void handleFragmentResult() {
        // 監聽頁面回傳
        getParentFragmentManager()
                .setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
                    GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
                    /** 商品清單處理 */
                    giavd.setMerchName(bundle.getStringArrayList("merchsName"));
                    giavd.setMerchPrice(bundle.getIntegerArrayList("merchsPrice"));
                    giavd.setMerchsId(bundle.getIntegerArrayList("merchsId"));
                    showLinearLayoutMerchs(giavd.MerchsId());

                    /** 地址的處理 */
                    if (giavd.Locations() != null) {
                        for (int i = 0; i < giavd.Locations().size(); i++) {
                            // 存入db用的資料
                            Double[] doubles = {giavd.Lats().get(i), giavd.Lngs().get(i)};
                            // 地址[]
                            String location = giavd.Locations().get(i);
                            // 顯示
                            showLinearLayoutLocation(location, doubles);
                        }
                    }
                });
        // 監聽頁面回傳
        getParentFragmentManager()
                .setFragmentResultListener("requestLocationKey", this, (requestKey, bundle) -> {
                    // 畫面移動到指定的位置
                    scrollViewMain.scrollTo(0, imageViewAddLocation.getTop());
                    
                    GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
                    /** 商品清單處理 */
                    showLinearLayoutMerchs(giavd.MerchsId());

                    /** 地址的處理 */
                    // 清空
                    giavd.Lats().clear();
                    giavd.Lngs().clear();
                    giavd.LatLngs().clear();
                    // 緯度[]
                    double[] latArr = bundle.getDoubleArray("lats");
                    // 經度[]
                    double[] lngArr = bundle.getDoubleArray("lngs");
                    // 地址[]
                    giavd.setLocations(bundle.getStringArrayList("locations"));
                    if (giavd.Locations() != null) {
                        for (int i = 0; i < giavd.Locations().size(); i++) {
                            // 緯度[]
                            giavd.Lats().add(latArr[i]);
                            // 經度[]
                            giavd.Lngs().add(lngArr[i]);
                            // 地址[]
                            String location = giavd.Locations().get(i);
                            // 存入db用的資料
                            Double[] doubles = {latArr[i], lngArr[i]};
                            giavd.LatLngs().add(doubles);
                            // 顯示
                            showLinearLayoutLocation(location, doubles);
                        }
                    }
                });
    }

    /**
     * 顯示地址清單
     */
    private void showLinearLayoutLocation(String location, Double[] doubles) {
        GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
        TextView textView = new TextView(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.rightMargin = 8;
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(18);
        textView.setText(location);
        linearLayoutLocation.addView(textView);

        // 長按監聽是否要刪除
        textView.setOnLongClickListener(tView -> {
            PopupMenu popupMenu = new PopupMenu(activity, tView, Gravity.END);
            popupMenu.inflate(R.menu.popup_menu_group_location);
            popupMenu.show();
            // 監聽 PopupMenu
            popupMenu.setOnMenuItemClickListener(item -> {
                // 點擊哪一個
                switch (item.getItemId()) {
                    case R.id.itemDelete:
                        // 儲存資料刪除
                        giavd.LatLngs().remove(doubles);
                        giavd.Locations().remove(location);
                        // LinearLayout刪除
                        linearLayoutLocation.removeView(textView);
                        break;

                    default:
                        break;
                }
                return true;
            });
            return true;
        });
    }

    /**
     * 顯示選擇的商品清單
     */
    private void showLinearLayoutMerchs(List<Integer> newMerchsId) {
        GroupInsertAddViewData giavd = GroupInsertAddViewData.getGroupInsertAddViewData();
        int count = 0;
        // 每次回傳回來都先清空資料
        totalAmount = 0;
        // 抓取賣家的所有商品，準備抓取有選擇的商品
        List<Merch> merches = MerchControl.getLocalMerchs();
        if (merches == null || merches.isEmpty()) {
            Toast.makeText(activity, R.string.textNoMerchsFound, Toast.LENGTH_SHORT).show();
        }

        for (Integer merchId : newMerchsId) {
            // 顯示選擇的商品列表
            TextView textView = new TextView(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            //layoutParams.gravity = Gravity.CENTER;
            layoutParams.rightMargin = 8;
            textView.setLayoutParams(layoutParams);
            textView.setTextSize(24);
            String string = giavd.MerchName().get(count) + "\t\t\t\t\t\t$ " + giavd.MerchPrice().get(count);
            textView.setText(string);
            linearLayoutMerchs.addView(textView);
            count ++;
            // 計算所有商品總金額
            for (Merch merch : merches) {
                if (merch.getMerchId() == merchId) {
                    totalAmount += merch.getPrice();
                    break;
                }
            }
        }
        // 顯示商品總金額
        textViewTotalAmount.setText("總金額： " + totalAmount + " NT");
    }
}