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
import idv.tfp10101.iamin.member.MyIncome;
import idv.tfp10101.iamin.member.MyPercentFormatter;
import idv.tfp10101.iamin.member.MyWallet;
import idv.tfp10101.iamin.member_order.MemberOrder;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMyIncomeFragment extends Fragment {
    private final static String TAG = "TAG_MyIncome";
    private Activity activity;
    private Member member;
    private List<MyIncome> myIncomes,myIncomesYear,dataForBundle;
    private List<MemberOrder> memberOrders;
    private PieData pieData;
    private PieChart pieChart;
    private Spinner monthDropDown;
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
            //???
            0xFFB54434,
            //???
            0xFF006284,
            //???
            0xFFFC9F4D,
            //???
            0xFF2D6D4B,
            //??????
            0xFFB9887D
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        activity = getActivity();
        activity.setTitle("???????????????????????????");
        member = MemberControl.getInstance();
        //???mysql?????????
        String jsonIn = memberRemoteAccess(activity,member,"getMyIncome");
        Type listType = new TypeToken<List<MyIncome>>() {}.getType();
        myIncomes = gson.fromJson(jsonIn,listType);

//        memberOrders = MemberControl.getMyMemberOrder(activity,member);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_my_income, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"onViewCreate");

        monthDropDown = view.findViewById(R.id.spinnerMyIncome);
        yearTitle = view.findViewById(R.id.myIncomeTextTitle);

        leftArrow = view.findViewById(R.id.myIncomeLeftArrow);
        rightArrow = view.findViewById(R.id.myIncomeRightArrow);

        listView = view.findViewById(R.id.myIncomeListView);
        if(myIncomes == null || myIncomes.isEmpty()){
            Toast.makeText(activity, "???????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        MyListAdapter adapter = new MyListAdapter(activity,R.layout.my_wallet_adapter_view_layout,getMyIncomeData(myIncomes));

        listView.setAdapter(adapter);

        date_year = new ArrayList<>();
        date_month = new ArrayList<>();

        //????????????????????? ????????????????????????
        sortYearForDropDown(myIncomes);
        //?????????index ?????????????????????
        int lastIndex = date_year.size();
        //??????????????????
        yearStr = date_year.get(lastIndex-1);
        //        Log.d(TAG, "yearStr: " + yearStr);
        //?????????????????????
        yearTitle.setText(yearStr);
        //        Log.d(TAG,"yearTitile: " + yearTitle.toString());
        //????????????????????????
        myIncomesYear = new ArrayList<>();
        for(MyIncome tmp : myIncomes) {
            if(yearStr.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                myIncomesYear.add(tmp);
            }
        }
        //?????????????????????????????????????????????
        sortMonthForDropDown(myIncomesYear);
        //pieChart??????
        handlePieChartConfig(view);
        //??????pieChart ??? listView
        dataForBundle = myIncomesYear;
        updateUI(myIncomesYear);

        //???dropdown??????
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
                    updateUI(myIncomesYear);
                }else{
                    List<MyIncome> tmpList = new ArrayList<>();
                    //????????????
                    selectMonth = monthDropDown.getAdapter().getItem(position) + "";

                    //????????????????????????
                    for(MyIncome tmp : myIncomesYear) {
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

        //??????????????????
        rightArrow.setOnClickListener(v -> {
//                Log.d(TAG, "MyWallet_rightArrow: " + date_month);
            String currentYear = date_year.get(currentIndex + 1);
            yearTitle.setText(currentYear);
            //??????????????????????????????
            myIncomesYear.clear();
            for(MyIncome tmp : myIncomes) {
                if(currentYear.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                    myIncomesYear.add(tmp);
                }
            }
            sortMonthForDropDown(myIncomesYear);
            updateUI(myIncomesYear);

            currentIndex = date_year.lastIndexOf(yearTitle.getText().toString());
//            Log.d(TAG,"currentIndex: " + currentIndex + " date_year.size: " + date_year.size());

            //????????????
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

        //??????????????????
        leftArrow.setOnClickListener(v -> {
//                Log.d(TAG, "MyWallet_leftArrow: " + date_month);
            String currentYear = date_year.get(currentIndex-1);
            yearTitle.setText(currentYear);
            //??????????????????????????????
            myIncomesYear.clear();
            for(MyIncome tmp : myIncomes) {
                if(currentYear.equals(tmp.getUpdateTime().toString().substring(0,4))) {
                    myIncomesYear.add(tmp);
                }
            }
            sortMonthForDropDown(myIncomesYear);
            updateUI(myIncomesYear);

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

    //????????????
    private void sortYearForDropDown(List<MyIncome> myIncomes) {
        Set<String> hash_set_year = new HashSet<>();
        //??????????????? ??????SET ???????????????
        for(MyIncome tmp : myIncomes){
            hash_set_year.add(tmp.getUpdateTime().toString().substring(0,4));
        }
        date_year.addAll(hash_set_year);
        sort(date_year);
    }

    //????????????
    private void sortMonthForDropDown(List<MyIncome> myIncomes) {

        Set<String> hash_set_month = new HashSet<>();
        //????????????????????????
        for(MyIncome tmp : myIncomes){
            hash_set_month.add(tmp.getUpdateTime().toString().substring(6,7));
        }
        //??????All time
        date_month.clear();
        date_month.add(getString(R.string.alltime));
        date_month.addAll(hash_set_month);

        //?????????alltime???????????? java8 addAll()
        sort(date_month);


    }

    //??????
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

    public class MyListAdapter extends ArrayAdapter<MyIncomeData>{
        private Context context;
        private int resource;
        private ArrayList<MyIncomeData> myIncomeData;

        public MyListAdapter(Context context, int resource, ArrayList<MyIncomeData> myIncomeData) {
            super(context, resource, myIncomeData);
            this.context = context;
            this.resource = resource;
            this.myIncomeData = myIncomeData;
        }

        void setMyIncomes(ArrayList<MyIncomeData> myIncomeData) {
            this.myIncomeData = myIncomeData;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource,parent,false);

            TextView tvCategory = convertView.findViewById(R.id.myWalletCategory);
            TextView tvTotalPrice = convertView.findViewById(R.id.myWalletTotalPrice);
            ProgressBar tvProgressBar = convertView.findViewById(R.id.myWalletProgerssBar);
            View view = convertView.findViewById(R.id.myWalletColorView);


            tvCategory.setText(myIncomeData.get(position).getCategory());
            tvTotalPrice.setText(myIncomeData.get(position).getTotalPrice()+"");
            view.setBackgroundColor(My_COLORS[position]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvProgressBar.setProgress(0,true);
            }else{
                tvProgressBar.setProgress(0);
            }

            ;
            tvProgressBar.setProgressTintList(ColorStateList.valueOf(My_COLORS[position]));

            if(!(myIncomeData.get(position).getProgressBar() == 100)) {
                new Thread(() -> {
                    final int max = myIncomeData.get(position).getProgressBar();
                    int progress;
                    while ((progress = tvProgressBar.getProgress()) < max) {
                        tvProgressBar.setProgress(progress + 6);
                        try {
                            long time = 100000 / myIncomeData.get(position).getTotalPrice();
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }else{
                new Thread(() -> {
                    final int max = myIncomeData.get(position).getProgressBar();
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

                Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

                List<MyIncome> incomeData = new ArrayList<>();
                for(MyIncome data: dataForBundle){
                    if(data.getCategory().equals(tvCategory.getText().toString())){
                        incomeData.add(data);
                    }
                }

                if(incomeData.size() != 0) {
                    String json = gson.toJson(incomeData);
                    Bundle bundle = new Bundle();
                    bundle.putString("IncomeData", json);

                    Navigation.findNavController(v).navigate(R.id.action_memberCenterMyIncome_to_memberCenterIncomeDeatilsFragment, bundle);
                }
            });

            return convertView;
        }
    }

    private void showIncomeList(List<MyIncome> myIncomes) {
        if (myIncomes == null || myIncomes.isEmpty()) {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
        MyListAdapter merchAdapter = (MyListAdapter) listView.getAdapter();
        if (merchAdapter == null) {
            listView.setAdapter(new MyListAdapter(activity,R.layout.my_wallet_adapter_view_layout,getMyIncomeData(myIncomes)));
        } else {
            merchAdapter.setMyIncomes(getMyIncomeData(myIncomes));
            merchAdapter.notifyDataSetChanged();
        }
    }


    private void updateUI(List<MyIncome> myIncomes) {
        pieData = setPieData(getMyIncomeEntries(myIncomes));
        pieChart.setData(pieData);
        pieChart.invalidate();
        pieData.setValueFormatter(new MyPercentFormatter(pieChart));
        pieChart.animateY(1000, Easing.EaseInOutCubic);

        //??????arrayList
        showIncomeList(myIncomes);

    }

    private void handlePieChartConfig(View view) {
        //??????pieChart
        pieChart = view.findViewById(R.id.IncomePieChart);
        /* ?????????????????? */
        pieChart.setRotationEnabled(false);
        /* ?????????????????? */
        pieChart.setCenterText(getString(R.string.income));
        /* ???????????????????????? */
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
        //??????????????????
        PieDataSet pieDataSet = new PieDataSet(entries, "");
//        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(20);
        //??????
        pieDataSet.setSliceSpace(0);
        //????????????
        /* ????????????????????????5????????????????????????????????????????????????????????? */
        //
        pieDataSet.setColors(My_COLORS);

        return new PieData(pieDataSet);
    }

    private List<PieEntry> getMyIncomeEntries(List<MyIncome> myIncomes) {
        List<PieEntry> myWalletsEntries = new ArrayList<>();
        int cA = 0,cB = 0,cC = 0, cD = 0;
        for(int i = 0; i < myIncomes.size(); i++){

            String category = myIncomes.get(i).getCategory();

            if(category.equals("??????")){
                cA += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("????????????")){
                cB += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("3C")){
                cC += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("??????")) {
                cD += myIncomes.get(i).getTotalPrice();
            }
        }

        if(cA != 0) {
            myWalletsEntries.add(new PieEntry(cA, "??????"));
        }else{
            myWalletsEntries.add(new PieEntry(cA, ""));
        }
        if(cB != 0) {
            myWalletsEntries.add(new PieEntry(cB, "????????????"));
        }else{
            myWalletsEntries.add(new PieEntry(cB, ""));
        }
        if(cC != 0) {
            myWalletsEntries.add(new PieEntry(cC, "3C"));
        }else{
            myWalletsEntries.add(new PieEntry(cC, ""));
        }
        if(cD != 0) {
            myWalletsEntries.add(new PieEntry(cD, "??????"));
        }else{
            myWalletsEntries.add(new PieEntry(cD, ""));
        }
        return myWalletsEntries;

    }

    private ArrayList<MyIncomeData> getMyIncomeData(List<MyIncome> myIncomes){

        ArrayList<MyIncomeData> myWalletArrayList = new ArrayList<>();

        int cA = 0, cB = 0, cC = 0, cD = 0, totalAmount = 0;
        for(int i = 0; i < myIncomes.size(); i++){

            String category = myIncomes.get(i).getCategory();

            if(category.equals("??????")){
                cA += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("????????????")){
                cB += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("3C")){
                cC += myIncomes.get(i).getTotalPrice();
            }else if(category.equals("??????")) {
                cD += myIncomes.get(i).getTotalPrice();
            }
        }

        totalAmount = cA + cB + cC + cD;

        int percentA = (cA*100/totalAmount);
        int percentB = (cB*100/totalAmount);
        int percentC = (cC*100/totalAmount);
        int percentD = (cD*100/totalAmount);

        myWalletArrayList.add(new MyIncomeData("??????", cA, percentA));
        myWalletArrayList.add(new MyIncomeData("????????????", cB, percentB));
        myWalletArrayList.add(new MyIncomeData("3C", cC, percentC));
        myWalletArrayList.add(new MyIncomeData("??????", cD, percentD));

        return myWalletArrayList;
    }

    private static class MyIncomeData {
        private String category;
        private int totalPrice;
        private int progressBar;

        public MyIncomeData(String category, int totalPrice, int progressBar) {
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