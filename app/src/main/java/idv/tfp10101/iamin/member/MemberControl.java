package idv.tfp10101.iamin.member;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Timestamp;

import idv.tfp10101.iamin.network.RemoteAccess;

import static android.content.Context.MODE_PRIVATE;

public class MemberControl {
    private final static String TAG = "TAG_MemberControl";

    public static String memberRemoteAccess(Context context , Member member, String value) {
        if (RemoteAccess.networkConnected(context)) {

            String url = RemoteAccess.URL_SERVER + "memberServelt";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", value);
            jsonObject.addProperty("member", new Gson().toJson(member));

            return RemoteAccess.getRometeData(url, jsonObject.toString());
        } else {
            Toast.makeText(context, "沒有網路", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    //存mebmer_ID到app裡
    public static void storeMemberIdSharedPreference(Context context, String result){
        int mySqlMemberId;
        try {
            mySqlMemberId = Integer.parseInt(result);
            SharedPreferences pref = context.getSharedPreferences("member_ID",
                    MODE_PRIVATE);
            pref.edit()
                    .putInt("member_ID",mySqlMemberId)
                    .putBoolean("Login statue",true)
                    .apply();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            mySqlMemberId = 0;
        }
        Log.d(TAG,mySqlMemberId + "");
        if (mySqlMemberId == 0){
            Log.d(TAG,"執行失敗");
        }else{
            Log.d(TAG,"執行成功");
        }
    }

    public static void setMemberData(Member member, JsonObject setter){
        member.setEmail(setter.get("EMAIL").getAsString());
        member.setNickname(setter.get("NICKNAME").getAsString());
        member.setPassword(setter.get("PASSWORD").getAsString());
        member.setPhoneNumber(setter.get("PHONE").getAsString());
        member.setRating(Double.parseDouble(setter.get("RATING").getAsString()));
        member.setFollow_count(Integer.parseInt(setter.get("FOLLOW_COUNT").getAsString()));
        member.setUpdateDate(Timestamp.valueOf(setter.get("UPDATE_TIME").getAsString()));
    }

}
