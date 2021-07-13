package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
import idv.tfp10101.iamin.member.MyWallet;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMyWalletFragment extends Fragment {
    private final static String TAG = "TAG_MyWallet";
    private Activity activity;
    private Member member;
    private List<MyWallet> myWallets;
    private List<PieEntry> defaultIncomeEntries;
    private PieData pieData;
    private AutoCompleteTextView monthDropDown;
    private RecyclerView rvMyWallet;
    private String selectMonth;
    private TextView yearTitle;
    private ImageButton leftArrow,rightArrow;
    private int i = 1;
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
        activity = getActivity();
        member = Member.getInstance();

        //從mysql拿資料
        String jsonIn = memberRemoteAccess(activity,member,"getMyWallet");
        Type listType = new TypeToken<List<MyWallet>>() {}.getType();
        myWallets = gson2.fromJson(jsonIn,listType);
//        Log.d(TAG,"MyWallets: " + myWallets.toString());
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
        monthDropDown = view.findViewById(R.id.monthAutoCompleteTextView);
        yearTitle = view.findViewById(R.id.myWalletTextTitle);

        leftArrow = view.findViewById(R.id.myWalletLeftArrow);
        rightArrow = view.findViewById(R.id.myWalletRightArrow);

        rvMyWallet = view.findViewById(R.id.rvMyWallet);
        rvMyWallet.setLayoutManager(new LinearLayoutManager(activity));

        Set<String> hash_set_year = new HashSet<>();
        Set<String> hash_set_month = new HashSet<>();
        List<String> date_year = new ArrayList<>();
        List<String> date_month = new ArrayList<>();

        defaultIncomeEntries = new ArrayList<>();
        defaultIncomeEntries = getMyWalletEntries(myWallets);



        //把xxxx年取出來 存入SET 因為不重複
        for(MyWallet tmp : myWallets){
           hash_set_year.add(tmp.getUpdateTime().toString().substring(0,4));
        }

        for(MyWallet tmp : myWallets){
            hash_set_month.add(tmp.getUpdateTime().toString().substring(6,7));
        }
        //  標題不加ALL
//        date_year.add(getString(R.string.alltime));
        date_year.addAll(hash_set_year);

        date_month.add(getString(R.string.alltime));
        date_month.addAll(hash_set_month);

        //排序讓alltime在最上面 java8 addAll()
        sort(date_year);
        sort(date_month);

        //刷新rvView
        showWalletList(myWallets);
        //最新年份
        yearTitle.setText(date_year.get(date_year.size()-1));
        handleArrowButton(view);

        PieChart pieChart = view.findViewById(R.id.pieChart);
        /* 設定可否旋轉 */
        pieChart.setRotationEnabled(false);
        /* 設定圓心文字 */
        pieChart.setCenterText(getString(R.string.income));
        /* 設定圓心文字大小 */
        pieChart.setCenterTextSize(25);

        Description description = new Description();
        description.setText("");
        description.setTextSize(25);
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
            }
            @Override
            public void onNothingSelected() {
            }
        });

        pieData = setPieData(defaultIncomeEntries);
        pieChart.setData(pieData);
        pieChart.invalidate();

        //月dropdown
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(activity,
                R.layout.mywallet_dropdown, date_month);
        monthDropDown.setAdapter(adapterMonth);

        monthDropDown.setOnItemClickListener((parent, view1, position, id) -> {
            if(position == 0){
                pieData = setPieData(defaultIncomeEntries);
                pieChart.setData(pieData);
                pieChart.invalidate();
                showWalletList(myWallets);
            }else{
                List<MyWallet> tmpList = new ArrayList<>();
                selectMonth = monthDropDown.getAdapter().getItem(position) + "";
                for(MyWallet tmp : myWallets) {
                    if(selectMonth.equals(tmp.getUpdateTime().toString().substring(6,7))) {
                        tmpList.add(tmp);
                    }
                }
                pieData = setPieData(getMyWalletEntries(tmpList));
                pieChart.setData(pieData);
                pieChart.invalidate();
                showWalletList(tmpList);
            }
        });

        int position = date_year.size();
        view.findViewById(R.id.myWalletRightArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                date_year.get(position-i);
                i++;
                if(date_year.get(0).equals(yearTitle.toString())){
                       leftArrow.setVisibility(View.INVISIBLE);
                       leftArrow.setEnabled(false);
                }else{
                    leftArrow.setVisibility(View.VISIBLE);
                    leftArrow.setEnabled(true);
                }
            }
        });
        view.findViewById(R.id.myWalletLeftArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                date_year.get(position+i);
                i--;
                if(date_year.get(0).equals(yearTitle.toString())){
                    rightArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setEnabled(false);
                }else{
                    rightArrow.setVisibility(View.VISIBLE);
                    rightArrow.setEnabled(true);
                }

            }
        });


    }

    private void handleArrowButton(View view) {

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
            Toast.makeText(activity, R.string.no_merch_found+"", Toast.LENGTH_SHORT).show();
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