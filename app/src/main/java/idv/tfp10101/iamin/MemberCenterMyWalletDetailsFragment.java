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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.member.MyWallet;

public class MemberCenterMyWalletDetailsFragment extends Fragment {
    private final String TAG = "TAG_MCMDFragment";
    private Activity activity;
    private RecyclerView rvMyWalletDeatils;
    private List<MyWallet> myWalletListDetails;
    private TextView title;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
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
        rvMyWalletDeatils = view.findViewById(R.id.rvWalletDetails);
        rvMyWalletDeatils.setLayoutManager(new LinearLayoutManager(activity));

        Gson gson = new Gson();
        Bundle bundle = this.getArguments();
        String json = bundle.get("WalletData").toString();
        String str = bundle.get("myWalletCategory").toString();

        Type listType = new TypeToken<List<MyWallet>>() {}.getType();
        myWalletListDetails = gson.fromJson(json, listType);

        title.setText(str);

        showWalletDetailsList(myWalletListDetails);

    }

    private void showWalletDetailsList(List<MyWallet> myWalletListDetails) {
        if (myWalletListDetails == null || myWalletListDetails.isEmpty()) {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
        MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter MyWalletAdapter = (MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter) rvMyWalletDeatils.getAdapter();
        if (MyWalletAdapter == null) {
            rvMyWalletDeatils.setAdapter(new MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter(activity, myWalletListDetails));
        } else {
            MyWalletAdapter.setMyWallets(myWalletListDetails);
            MyWalletAdapter.notifyDataSetChanged();
        }
    }

    private class MyWalletDetailsAdapter extends RecyclerView.Adapter<MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<MyWallet> myWalletListDetails;

        MyWalletDetailsAdapter(Context context, List<MyWallet> myWalletListDetails) {
            layoutInflater = LayoutInflater.from(context);
            this.myWalletListDetails = myWalletListDetails;
        }

        void setMyWallets(List<MyWallet> myWalletDeatails) {
            this.myWalletListDetails = myWalletDeatails;
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
            return myWalletListDetails == null ? 0 : myWalletListDetails.size();
        }

        @NonNull
        @Override
        public MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_mywallet, parent, false);
            return new MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMyWalletDetailsFragment.MyWalletDetailsAdapter.MyViewHolder myViewHolder, int position) {
            final MyWallet myWalletDetails = myWalletListDetails.get(position);
            myViewHolder.dateWallet.setText(myWalletDetails.getUpdateTime().toString());
            myViewHolder.priceWallet.setText(myWalletDetails.getTotalPrice() + getString(R.string.text_money_ntd));
            myViewHolder.nameWallet.setText(myWalletDetails.getName());
            myViewHolder.groupTitleWallet.setText(myWalletDetails.getGroupName());
            myViewHolder.number.setText(String.valueOf(myWalletDetails.getQuantity()));


        }
    }
}