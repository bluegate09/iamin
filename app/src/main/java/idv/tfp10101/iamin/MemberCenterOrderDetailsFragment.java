package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;

public class MemberCenterOrderDetailsFragment extends Fragment {
    private Activity activity;
    private MemberOrderDetails memberOrderDetails;
    private List<MemberOrderDetails> memberOrderDetailslist;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        String orderDetailsJson = bundle.getString("OrderDetails");

        Type listType = new TypeToken<List<MemberOrder>>() {}.getType();
        memberOrderDetailslist = new Gson().fromJson(orderDetailsJson,listType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_order_details, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.svOrderSearch);

        recyclerView = view.findViewById(R.id.rvMemberCenterOrder);
//        recyclerView.setAdapter(new );
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    showMyOrder(memberOrderDetailslist);
                }else{
                    List<MemberOrderDetails> searchOrders = new ArrayList<>();
                    for(MemberOrderDetails result : memberOrderDetailslist){
//                        if () {
//                            searchOrders.add(result);
//                        }

                    }
//                    showMyOrder(searchOrders);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty())
                    showMyOrder(memberOrderDetailslist);
                return false;
            }
        });


    }

    private void showMyOrder(List<MemberOrderDetails> memberOrderDetailslist){
        if(memberOrderDetailslist == null || memberOrderDetailslist.isEmpty()){
            Toast.makeText(activity,"no memberOrdersDetails found", Toast.LENGTH_SHORT).show();
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        Activity activity;
        List<MemberOrderDetails> memberOrderDetailsList;

        public MyAdapter(Activity activity, List<MemberOrderDetails> memberOrderDetailsList){
            this.activity = activity;
            this.memberOrderDetailsList = memberOrderDetailsList;
        }

        void setMemberOrderDetailsList(){
            this.memberOrderDetailsList = memberOrderDetailslist;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.member_order_listview,parent,false);
            return new MyViewHolder(null);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterOrderDetailsFragment.MyViewHolder holder, int position) {
            final MemberOrderDetails memberOrderDetails = memberOrderDetailsList.get(position);

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder{

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

    }
}