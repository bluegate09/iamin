package idv.tfp10101.iamin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberCenterFollowTestGround extends Fragment {
    private Activity activity;
    private ListView listView;
    private List<Member> allMember;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_center_follow_test_ground, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.test_listview);

        ArrayList<String> arrayList = new ArrayList<>();

        String url = RemoteAccess.URL_SERVER + "memberController";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "showAllMember");
        jsonObject.addProperty("member",new Gson().toJson(member));

        String jsonMemberList =  RemoteAccess.getRemoteData(url, jsonObject.toString());
        Type listType = new TypeToken<List<Member>>() {}.getType();
        allMember = new Gson().fromJson(jsonMemberList,listType);
        for(Member str: allMember){
            arrayList.add(str.getuUId());
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_1,arrayList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String  member_id = listView.getAdapter().getItem(position).toString();

                Member myMember = new Member();
                Member otherMember = new Member();

                myMember.setId(member.getId());
                otherMember.setId(Integer.parseInt(member_id));






            }
        });



    }
}