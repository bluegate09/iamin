package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.member.MyIncome;
import idv.tfp10101.iamin.member.MyWallet;

public class MemberCenterIncomeDeatilsFragment extends Fragment {
    private final String TAG = "TAG_IncomeDetail";
    private Activity activity;
    private TextView title;
    private RecyclerView recyclerView;
    private List<MyIncome> incomeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("收入細項");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_income_deatils, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.tvIncomeDetailsTitle);

        recyclerView = view.findViewById(R.id.rvIncome);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
        Bundle bundle = this.getArguments();
        String json = bundle.getString("IncomeData").toString();
        Log.d(TAG,"json: " +  json);

        Type listType = new TypeToken<List<MyIncome>>() {}.getType();
        incomeList = gson.fromJson(json, listType);

        title.setText(incomeList.get(0).getCategory());
        showIncomeList(incomeList);

    }

    private void showIncomeList(List<MyIncome> incomeList) {
        if(incomeList == null || incomeList.isEmpty()){
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
        MyIncomeAdapter myIncomeAdapter = (MyIncomeAdapter) recyclerView.getAdapter();
        if(myIncomeAdapter == null){
            recyclerView.setAdapter(new MyIncomeAdapter(activity,incomeList));
        }else{
            myIncomeAdapter.setMyIncomes(incomeList);
            myIncomeAdapter.notifyDataSetChanged();
        }
    }

    private class MyIncomeAdapter extends RecyclerView.Adapter<MyIncomeAdapter.MyViewHolder>{
        private final LayoutInflater layoutInflater;
        private List<MyIncome> incomeList;

        MyIncomeAdapter(Context context, List<MyIncome> incomeList){
            layoutInflater = LayoutInflater.from(context);
            this.incomeList = incomeList;
        }


        void setMyIncomes(List<MyIncome> incomeList){
            this.incomeList = incomeList;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView memberOrderId,groupTitle,price,date;

            public MyViewHolder(View itemView) {
                super(itemView);
                memberOrderId = itemView.findViewById(R.id.incomeMemberOrderId);
                groupTitle = itemView.findViewById(R.id.incomeGroupTitle);
                price = itemView.findViewById(R.id.incomePrice);
                date = itemView.findViewById(R.id.incomeDate);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_myincome,parent,false);
            return new MyViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return incomeList == null ? 0 : incomeList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final MyIncome myIncome = incomeList.get(position);
            holder.groupTitle.setText(myIncome.getGroupTitle());
            holder.memberOrderId.setText(myIncome.getMemberOrderId()+"");
            holder.price.setText(myIncome.getTotalPrice()+"");
            int num = myIncome.getUpdateTime().toString().length();
            holder.date.setText(myIncome.getUpdateTime().toString().substring(0,num-5));

        }

    }
}