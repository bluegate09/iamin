package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import idv.tfp10101.iamin.member.MyWallet;

public class MemberCenterMyWalletDetailsFragment extends Fragment {
    private final String TAG = "TAG_MCMDFragment";
    private Activity activity;
    private RecyclerView rvMyWalletDetails;
    private List<MyWallet> myMemberOrderDetailsList;
    private TextView title;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("支出細項");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_my_wallet_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.tvWalletDeatilsTitle);
        rvMyWalletDetails = view.findViewById(R.id.rvWalletDetails);
        rvMyWalletDetails.setLayoutManager(new LinearLayoutManager(activity));

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
        Bundle bundle = this.getArguments();
        String json = bundle.get("WalletData").toString();

        Type listType = new TypeToken<List<MyWallet>>() {}.getType();
        myMemberOrderDetailsList = gson.fromJson(json, listType);

        title.setText(myMemberOrderDetailsList.get(0).getCategory());
        showWalletDetailsList(myMemberOrderDetailsList);

    }

    private void showWalletDetailsList(List<MyWallet> myMemberOrderDetailsList) {
        if (myMemberOrderDetailsList == null || myMemberOrderDetailsList.isEmpty()) {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
        MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter myWalletAdapter = (MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter) rvMyWalletDetails.getAdapter();
        if (myWalletAdapter == null) {
            rvMyWalletDetails.setAdapter(new MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter(activity, myMemberOrderDetailsList));
        } else {
            myWalletAdapter.setMyWallets(myMemberOrderDetailsList);
            myWalletAdapter.notifyDataSetChanged();
        }
    }

    private class MyWalletDetailsAdapter extends RecyclerView.Adapter<MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<MyWallet> myMemberOrderDetailsList;

        MyWalletDetailsAdapter(Context context, List<MyWallet> myMemberOrderDetailsList) {
            layoutInflater = LayoutInflater.from(context);
            this.myMemberOrderDetailsList = myMemberOrderDetailsList;
        }

        void setMyWallets(List<MyWallet> myMemberOrderDetailsList) {
            this.myMemberOrderDetailsList = myMemberOrderDetailsList;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView groupTitleWallet, nameWallet,priceWallet,dateWallet,number;

            MyViewHolder(View itemView) {
                super(itemView);
                groupTitleWallet = itemView.findViewById(R.id.groupTitleWallet);
                nameWallet = itemView.findViewById(R.id.nameWallet);
                priceWallet = itemView.findViewById(R.id.priceWallet);
                dateWallet = itemView.findViewById(R.id.dateWallet);
                number = itemView.findViewById(R.id.numberWallet);
            }
        }

        @Override
        public int getItemCount() {
            return myMemberOrderDetailsList == null ? 0 : myMemberOrderDetailsList.size();
        }

        @NonNull
        @Override
        public MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_mywallet, parent, false);
            return new MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder myViewHolder, int position) {
            final MyWallet myMemberOrderDetail = myMemberOrderDetailsList.get(position);
            myViewHolder.dateWallet.setText(myMemberOrderDetail.getUpdateTime().toString());
            myViewHolder.priceWallet.setText(myMemberOrderDetail.getTotalPrice() + getString(R.string.text_money_ntd));
            myViewHolder.nameWallet.setText(myMemberOrderDetail.getMerchTitle());
            myViewHolder.groupTitleWallet.setText(myMemberOrderDetail.getGroupTitle());
            myViewHolder.number.setText(String.valueOf(myMemberOrderDetail.getQuantity()));


        }
    }
}