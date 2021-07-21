package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.member.MyWallet;

import static android.content.Context.MODE_PRIVATE;
import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMyWalletFragment extends Fragment {
    private final static String TAG = "TAG_MyWallet";
    private Activity activity;
    private Member member;
    private List<MyWallet> myWallets,myWalletsYear;
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
    private final Gson gson2 = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

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
        myWallets = gson2.fromJson(jsonIn,listType);

        for(MyWallet tmp: myWallets) {
            Log.d(TAG, "MyWallets: " + tmp);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.fragment_member_center_my_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"onViewCreate");

        monthDropDown = view.findViewById(R.id.monthAutoCompleteTextView);
        yearTitle = view.findViewById(R.id.myWalletTextTitle);

        leftArrow = view.findViewById(R.id.myWalletLeftArrow);
        rightArrow = view.findViewById(R.id.myWalletRightArrow);

        rvMyWallet = view.findViewById(R.id.rvMyWallet);
        rvMyWallet.setLayoutManager(new LinearLayoutManager(activity));

        date_year = new ArrayList<>();
        date_month = new ArrayList<>();



        Log.d(TAG,"myWallets: " + myWallets);
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
        //更新pieChart 及 recycleView
        updateUI(myWalletsYear);


        /**
         * 出問題的地方
         */
        //月dropdown選單
        adapterMonth = new ArrayAdapter<>(activity, R.layout.mywallet_dropdown, date_month);
        adapterMonth.notifyDataSetChanged();
        monthDropDown.setAdapter(adapterMonth);
        Log.d(TAG,"monthDropDown: " + monthDropDown.getAdapter().isEmpty());
        monthDropDown.getAdapter().isEmpty();
        monthDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //AutoCompleteTextView DropdownMenu
                monthDropDown.getAdapter().getItem(0);
                Log.d(TAG,"monthDropDown: " + monthDropDown.getAdapter().getItem(0));

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
                    updateUI(tmpList);
//                    Log.d(TAG, "MyWallet_adapterMonth: " + date_month);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        currentIndex = date_year.lastIndexOf(yearTitle.getText().toString());
        Log.d(TAG,"currentIndex: " + currentIndex);
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

            if(currentIndex + 1 == date_year.size()){
                rightArrow.setVisibility(View.INVISIBLE);
                rightArrow.setEnabled(false);
            }
            if(currentIndex != 0){
                leftArrow.setVisibility(View.VISIBLE);
                leftArrow.setEnabled(true);
            }
        });

//        Log.d(TAG,"currentIndex: " + currentIndex);

        if(currentIndex == 0){
            leftArrow.setEnabled(false);
            leftArrow.setVisibility(View.INVISIBLE);
        }

        //左邊箭頭按鈕
        leftArrow.setOnClickListener(v -> {
//                Log.d(TAG, "MyWallet_leftArrow: " + date_month);
            String currentYear = date_year.get(currentIndex - 1);
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

    @Override
    public void onStart() {
        super.onStart();
//        Log.d(TAG,"date_year: " + date_year);
//        Log.d(TAG,"date_month: " + date_month);
    }


    private void updateUI(List<MyWallet> myWallets) {
        pieData = setPieData(getMyWalletEntries(myWallets));
        pieChart.setData(pieData);
        pieChart.invalidate();
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        //更新recycle view
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

        Description description = new Description();
        description.setText("");
        description.setTextSize(25);
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.setUsePercentValues(true);
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

    private void showWalletList(List<MyWallet> myWallets) {
        if (myWallets == null || myWallets.isEmpty()) {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
        MyWalletAdapter merchAdapter = (MyWalletAdapter) rvMyWallet.getAdapter();
        if (merchAdapter == null) {
            rvMyWallet.setAdapter(new MyWalletAdapter(activity, myWallets));
        } else {
            merchAdapter.setMyWallets(myWallets);
            merchAdapter.notifyDataSetChanged();
        }
    }

    private class MyWalletAdapter extends RecyclerView.Adapter<MyWalletAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<MyWallet> myWallets;

        MyWalletAdapter(Context context, List<MyWallet> myWallets) {
            layoutInflater = LayoutInflater.from(context);
            this.myWallets = myWallets;
        }

        void setMyWallets(List<MyWallet> myWallets) {
            this.myWallets = myWallets;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            View background;

            MyViewHolder(View itemView) {
                super(itemView);
                background = itemView.findViewById(R.id.walletViewBackgroundColor);
                tvName = itemView.findViewById(R.id.nameWallet);
                tvPrice = itemView.findViewById(R.id.priceWallet);
            }
        }

        @Override
        public int getItemCount() {
            return myWallets == null ? 0 : myWallets.size();
        }

        @NonNull
        @Override
        public MyWalletAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_mywallet, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyWalletAdapter.MyViewHolder myViewHolder, int position) {
            final MyWallet myWallet = myWallets.get(position);
            myViewHolder.tvName.setText(myWallet.getCategory());
            myViewHolder.tvPrice.setText(myWallet.getTotoalPrice()+"");
            myViewHolder.background.setBackgroundColor(My_COLORS[position]);

            myViewHolder.tvPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Gson gson = new Gson();
                    String json = gson.toJson(myWallet.getGroupDetail());
                    Bundle bundle = new Bundle();
                    bundle.putString("myWalletCategory",myWallet.getCategory());
                    bundle.putString("myWalletdetails",json);

//                    tmpStr = yearTitle.getText().toString();
//                    tmpIndex = currentIndex;
//                    Log.d(TAG,"tmpIndex: " + tmpIndex);
                    Navigation.findNavController(v).navigate(R.id.action_memberCenterMyWalletFragment_to_memberCenterMyWalletDetailsFragment,bundle);


                }
            });
        }
    }

    private PieData setPieData(List<PieEntry> entries) {
        //從這裡給資料
        PieDataSet pieDataSet = new PieDataSet(entries, "");
//        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueTextColor(Color.BLUE);
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

        for(int i = 0; i < myWallets.size(); i++) {
            myWalletsEntries.add(new PieEntry(myWallets.get(i).getTotoalPrice(), myWallets.get(i).getCategory()));
        }
        return myWalletsEntries;
    }

}