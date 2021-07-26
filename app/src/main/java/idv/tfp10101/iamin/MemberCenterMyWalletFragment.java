package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyPercentFormatter;
import idv.tfp10101.iamin.member.MyWallet;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMyWalletFragment extends Fragment {
    private final static String TAG = "TAG_MyWallet";
    private Activity activity;
    private Member member;
    private List<MyWallet> myWallets,myWalletsYear,dataForBundle;
    private PieData pieData;
    private PieChart pieChart;
    private Spinner monthDropDown;
    //    private AutoCompleteTextView monthDropDown;
    private RecyclerView rvMyWallet;
    private String selectMonth, yearStr;
    private TextView yearTitle;
    private ImageButton leftArrow,rightArrow;
    private int currentIndex;
    private List<String> date_year;
    private List<String> date_month;
    private ArrayAdapter<String> adapterMonth;
    private ListView listView;
    private final Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
    private final int[] My_COLORS = {
            //紅
            0xFFB54434,
            //青
            0xFF006284,
            //橘
            0xFFFC9F4D,
            //綠
            0xFF2D6D4B,
            //皮膚
            0xFFB9887D
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        activity = getActivity();
        member = MemberControl.getInstance();
        //從mysql拿資料
        String jsonIn = memberRemoteAccess(activity,member,"getMyWallet");
        Type listType = new TypeToken<List<MyWallet>>() {}.getType();
        myWallets = gson.fromJson(jsonIn,listType);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_my_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"onViewCreate");

        monthDropDown = view.findViewById(R.id.spinnerWallet);
        yearTitle = view.findViewById(R.id.myWalletTextTitle);

        leftArrow = view.findViewById(R.id.myWalletLeftArrow);
        rightArrow = view.findViewById(R.id.myWalletRightArrow);

        listView = view.findViewById(R.id.myWalletListView);
        if(myWallets == null || myWallets.isEmpty()){
            Toast.makeText(activity, "您還沒有任何資料喔", Toast.LENGTH_SHORT).show();
            return;
        }
        myListAdapter adapter = new myListAdapter(activity,R.layout.my_wallet_adapter_view_layout,getMyWalletData(myWallets));

        listView.setAdapter(adapter);

        date_year = new ArrayList<>();
        date_month = new ArrayList<>();

        //整裡資料的年份 這是列出所有年份
        sortYearForDropDown(myWallets);
        //最後的index 看拿到的有多長
        int lastIndex = date_year.size();
        //取得最後一個
        yearStr = date_year.get(lastIndex-1);
        //        Log.d(TAG, "yearStr: " + yearStr);
        //設定標題的年份
        yearTitle.setText(yearStr);
        //        Log.d(TAG,"yearTitile: " + yearTitle.toString());
        //以年份去判斷資料
        myWalletsYear = new ArrayList<>();
        for(MyWallet tmp : myWallets) {
            if(yearStr.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                myWalletsYear.add(tmp);
            }
        }
        //把抓到的資料裡的月份取出並排序
        sortMonthForDropDown(myWalletsYear);
        //pieChart設定
        handlePieChartConfig(view);
        //更新pieChart 及 listView
        dataForBundle = myWalletsYear;
        updateUI(myWalletsYear);

        //月dropdown選單
        adapterMonth = new ArrayAdapter<>(activity, R.layout.mywallet_dropdown, date_month);
        adapterMonth.notifyDataSetChanged();
        monthDropDown.setAdapter(adapterMonth);
//        Log.d(TAG,"monthDro   pDown: " + monthDropDown.getAdapter().isEmpty());
        monthDropDown.getAdapter().isEmpty();
        monthDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monthDropDown.getAdapter().getItem(0);
//                Log.d(TAG,"monthDropDown: " + monthDropDown.getAdapter().getItem(0));

                if(position == 0){
                    updateUI(myWalletsYear);
                }else{
                    List<MyWallet> tmpList = new ArrayList<>();
                    //選擇的月
                    selectMonth = monthDropDown.getAdapter().getItem(position) + "";

                    //根據月去抓取資料
                    for(MyWallet tmp : myWalletsYear) {
                        if(selectMonth.equals(tmp.getUpdateTime().toString().substring(6,7))) {
                            tmpList.add(tmp);
                        }
                    }
                    dataForBundle = tmpList;
                    updateUI(tmpList);
//                    Log.d(TAG, "MyWallet_adapterMonth: " + date_month);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        currentIndex = date_year.lastIndexOf(yearTitle.getText().toString());
//        Log.d(TAG,"currentIndex: " + currentIndex + " date_year.size: " + date_year.size());

        if(currentIndex + 1 == date_year.size()){
            rightArrow.setVisibility(View.INVISIBLE);
            rightArrow.setEnabled(false);
        }

        //右邊箭頭按鈕
        rightArrow.setOnClickListener(v -> {
//                Log.d(TAG, "MyWallet_rightArrow: " + date_month);
            String currentYear = date_year.get(currentIndex + 1);
            yearTitle.setText(currentYear);
            //取出相對應年份的資料
            myWalletsYear.clear();
            for(MyWallet tmp : myWallets) {
                if(currentYear.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                    myWalletsYear.add(tmp);
                }
            }
            sortMonthForDropDown(myWalletsYear);
            updateUI(myWalletsYear);

            currentIndex = date_year.lastIndexOf(yearTitle.getText().toString());
//            Log.d(TAG,"currentIndex: " + currentIndex + " date_year.size: " + date_year.size());

            //右邊箭頭
            if(currentIndex + 1 == date_year.size()){
                rightArrow.setVisibility(View.INVISIBLE);
                rightArrow.setEnabled(false);
            }

            if(currentIndex != 0){
                leftArrow.setVisibility(View.VISIBLE);
                leftArrow.setEnabled(true);
            }
        });

        if(currentIndex == 0){
            leftArrow.setEnabled(false);
            leftArrow.setVisibility(View.INVISIBLE);
        }

        //左邊箭頭按鈕
        leftArrow.setOnClickListener(v -> {
//                Log.d(TAG, "MyWallet_leftArrow: " + date_month);
            String currentYear = date_year.get(currentIndex-1);
            yearTitle.setText(currentYear);
            //取出相對應年份的資料
            myWalletsYear.clear();
            for(MyWallet tmp : myWallets) {
                if(currentYear.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                    myWalletsYear.add(tmp);
                }
            }
            sortMonthForDropDown(myWalletsYear);
            updateUI(myWalletsYear);

            currentIndex = date_year.lastIndexOf(yearTitle.getText().toString());
//            Log.d(TAG,"currentIndex: " + currentIndex + " date_year.size: " + date_year.size());
            if(currentIndex == 0){
                leftArrow.setVisibility(View.INVISIBLE);
                leftArrow.setEnabled(false);
            }
            if(currentIndex != date_year.size()){
                rightArrow.setVisibility(View.VISIBLE);
                rightArrow.setEnabled(true);
            }
        });
    }

    //整理年份
    private void sortYearForDropDown(List<MyWallet> myWallets) {
        Set<String> hash_set_year = new HashSet<>();
        //把年取出來 存入SET 因為不重複
        for(MyWallet tmp : myWallets){
            hash_set_year.add(tmp.getUpdateTime().toString().substring(0,4));
        }
        date_year.addAll(hash_set_year);
        sort(date_year);
    }

    //整理月份
    private void sortMonthForDropDown(List<MyWallet> myWallets) {

        Set<String> hash_set_month = new HashSet<>();
        //從資料中取出月份
        for(MyWallet tmp : myWallets){
            hash_set_month.add(tmp.getUpdateTime().toString().substring(6,7));
        }
        //增加All time
        date_month.clear();
        date_month.add(getString(R.string.alltime));
        date_month.addAll(hash_set_month);

        //排序讓alltime在最上面 java8 addAll()
        sort(date_month);


    }

    //排序
    private void sort(List<String> date) {
        Collections.sort(date, (o1, o2) -> {
            if(o1.equals(o2)) //update to make is stable
                return 0;
            if(o1.equals(getString(R.string.alltime)))
                return -1;
            if(o2.equals(getString(R.string.alltime)))
                return 1;
            return o1.compareTo(o2);
        });
    }

    public class myListAdapter extends ArrayAdapter<MyWalletData>{
        private Context context;
        private int resource;
        private ArrayList<MyWalletData> myWalletData;

        public myListAdapter(Context context, int resource, ArrayList<MyWalletData> myWalletData) {
            super(context, resource, myWalletData);
            this.context = context;
            this.resource = resource;
            this.myWalletData = myWalletData;
        }

        void setMyWallets(ArrayList<MyWalletData> myWalletData) {
            this.myWalletData = myWalletData;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource,parent,false);

            TextView tvCategory = convertView.findViewById(R.id.myWalletCategory);
            TextView tvTotalPrice = convertView.findViewById(R.id.myWalletTotalPrice);
            ProgressBar tvProgressBar = convertView.findViewById(R.id.myWalletProgerssBar);
            View view = convertView.findViewById(R.id.myWalletColorView);


            tvCategory.setText(myWalletData.get(position).getCategory());
            tvTotalPrice.setText(myWalletData.get(position).getTotalPrice()+"");
            view.setBackgroundColor(My_COLORS[position]);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvProgressBar.setProgress(0,true);
                }else{
                    tvProgressBar.setProgress(0);
                }

                ;
                tvProgressBar.setProgressTintList(ColorStateList.valueOf(My_COLORS[position]));

                if(!(myWalletData.get(position).getProgressBar() == 100)) {
                    new Thread(() -> {
                        final int max = myWalletData.get(position).getProgressBar();
                        int progress;
                        while ((progress = tvProgressBar.getProgress()) < max) {
                            tvProgressBar.setProgress(progress + 6);
                            try {
                                long time = 100000 / myWalletData.get(position).getTotalPrice();
                                Thread.sleep(time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }else{
                    new Thread(() -> {
                        final int max = myWalletData.get(position).getProgressBar();
                        int progress;
                        while ((progress = tvProgressBar.getProgress()) < max) {
                            tvProgressBar.setProgress(progress + 1);
                            try {
                                long time = 6;
                                Thread.sleep(time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                //Navigation

            convertView.setOnClickListener(v -> {

                Gson gson = new Gson();

                List<MyWallet> walletData = new ArrayList<>();
                for(MyWallet data: dataForBundle){
                    if(data.getCategory().equals(tvCategory.getText().toString())){
                        walletData.add(data);
                    }
                }

                if(walletData.size() != 0) {
                    String json = gson.toJson(walletData);
                    Bundle bundle = new Bundle();
                    bundle.putString("WalletData", json);
                    bundle.putString("myWalletCategory", tvCategory.getText().toString());

                    Navigation.findNavController(v).navigate(R.id.action_memberCenterMyWalletFragment_to_memberCenterMyWalletDetailsFragment, bundle);
                }
            });

            return convertView;
        }
    }

        private void showWalletList(List<MyWallet> myWallets) {
        if (myWallets == null || myWallets.isEmpty()) {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
            myListAdapter merchAdapter = (myListAdapter) listView.getAdapter();
        if (merchAdapter == null) {
            listView.setAdapter(new myListAdapter(activity,R.layout.my_wallet_adapter_view_layout,getMyWalletData(myWallets)));
        } else {
            merchAdapter.setMyWallets(getMyWalletData(myWallets));
            merchAdapter.notifyDataSetChanged();
        }
    }


    private void updateUI(List<MyWallet> myWallets) {
        pieData = setPieData(getMyWalletEntries(myWallets));
        pieChart.setData(pieData);
        pieChart.invalidate();
        pieData.setValueFormatter(new MyPercentFormatter(pieChart));
        pieChart.animateY(1000, Easing.EaseInOutCubic);

        //更新arrayList
        showWalletList(myWallets);

    }

    private void handlePieChartConfig(View view) {
        //處理pieChart
        pieChart = view.findViewById(R.id.pieChart);
        /* 設定可否旋轉 */
        pieChart.setRotationEnabled(false);
        /* 設定圓心文字 */
        pieChart.setCenterText(getString(R.string.spending));
        /* 設定圓心文字大小 */
        pieChart.setCenterTextSize(25);

        pieChart.getDescription().setEnabled(false);
//        Description description = new Description();
//        description.setText("");
//        description.setTextSize(25);
//        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(1000, Easing.EaseInOutCubic);

        pieChart.setUsePercentValues(true);

    }

    private PieData setPieData(List<PieEntry> entries) {
        //從這裡給資料
        PieDataSet pieDataSet = new PieDataSet(entries, "");
//        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(20);
        //間距
        pieDataSet.setSliceSpace(0);
        //自己建立
        /* 官定顏色範本只有5種顏色，不過可以將多個官定範本顏色疊加 */
        //
        pieDataSet.setColors(My_COLORS);

        return new PieData(pieDataSet);
    }

    private List<PieEntry> getMyWalletEntries(List<MyWallet> myWallets) {
        List<PieEntry> myWalletsEntries = new ArrayList<>();
        int cA = 0,cB = 0,cC = 0, cD = 0;
        for(int i = 0; i < myWallets.size(); i++){

            String category = myWallets.get(i).getCategory();

            if(category.equals("美食")){
                cA += myWallets.get(i).getTotalPrice();
            }else if(category.equals("生活用品")){
                cB += myWallets.get(i).getTotalPrice();
            }else if(category.equals("3C")){
                cC += myWallets.get(i).getTotalPrice();
            }else if(category.equals("其他")) {
                cD += myWallets.get(i).getTotalPrice();
            }
        }

        if(cA != 0) {
            myWalletsEntries.add(new PieEntry(cA, "美食"));
        }else{
            myWalletsEntries.add(new PieEntry(cA, ""));
        }
        if(cB != 0) {
            myWalletsEntries.add(new PieEntry(cB, "生活用品"));
        }else{
            myWalletsEntries.add(new PieEntry(cB, ""));
        }
        if(cC != 0) {
            myWalletsEntries.add(new PieEntry(cC, "3C"));
        }else{
            myWalletsEntries.add(new PieEntry(cC, ""));
        }
        if(cD != 0) {
            myWalletsEntries.add(new PieEntry(cD, "其他"));
        }else{
            myWalletsEntries.add(new PieEntry(cD, ""));
        }
        return myWalletsEntries;

    }

    private ArrayList<MyWalletData> getMyWalletData(List<MyWallet> myWallets){

        ArrayList<MyWalletData> myWalletArrayList = new ArrayList<>();

        int cA = 0, cB = 0, cC = 0, cD = 0, totalAmount = 0;
        for(int i = 0; i < myWallets.size(); i++){

            String category = myWallets.get(i).getCategory();

            if(category.equals("美食")){
                cA += myWallets.get(i).getTotalPrice();
            }else if(category.equals("生活用品")){
                cB += myWallets.get(i).getTotalPrice();
            }else if(category.equals("3C")){
                cC += myWallets.get(i).getTotalPrice();
            }else if(category.equals("其他")) {
                cD += myWallets.get(i).getTotalPrice();
            }
        }

        totalAmount = cA + cB + cC + cD;

        int percentA = (cA*100/totalAmount);
        int percentB = (cB*100/totalAmount);
        int percentC = (cC*100/totalAmount);
        int percentD = (cD*100/totalAmount);

        myWalletArrayList.add(new MyWalletData("美食", cA, percentA));
        myWalletArrayList.add(new MyWalletData("生活用品", cB, percentB));
        myWalletArrayList.add(new MyWalletData("3C", cC, percentC));
        myWalletArrayList.add(new MyWalletData("其他", cD, percentD));

        return myWalletArrayList;
    }

    private static class MyWalletData {
        private String category;
        private int totalPrice;
        private int progressBar;

        public MyWalletData(String category, int totalPrice, int progressBar) {
            this.category = category;
            this.totalPrice = totalPrice;
            this.progressBar = progressBar;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(int totalPrice) {
            this.totalPrice = totalPrice;
        }

        public int getProgressBar() {
            return progressBar;
        }

        public void setProgressBar(int progressBar) {
            this.progressBar = progressBar;
        }
    }

}