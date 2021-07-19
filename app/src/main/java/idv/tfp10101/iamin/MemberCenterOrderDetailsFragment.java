package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import idv.tfp10101.iamin.member_order_details.MemberOrderDetails;

public class MemberCenterOrderDetailsFragment extends Fragment {
    private Activity activity;
    private MemberOrderDetails memberOrderDetails;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_order_details, container, false);
    }
}