package idv.tfp10101.iamin.member;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberControl {

    private static Member memberInstance;
    public static Member getInstance(){
        if(memberInstance == null){
            memberInstance = new Member();
        }
        return memberInstance;
    }

    public static void setMember(Member member){
        if(memberInstance == null){
            memberInstance = new Member();
        }
        memberInstance = member;
    }


    private final static String TAG = "TAG_MemberControl";

    /**
     *
     * @param context context
     * @param member member bean
     * @param value 欲執行的action
     * @return json String
     */
    public static String memberRemoteAccess(Context context , Member member, String value) {
        if (RemoteAccess.networkConnected(context)) {

            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", value);
            jsonObject.addProperty("member", new Gson().toJson(member));

            return RemoteAccess.getRemoteData(url, jsonObject.toString());
        } else {
            Toast.makeText(context, "沒有網路", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

}
