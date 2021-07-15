package idv.tfp10101.iamin.member;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

            return RemoteAccess.getRemoteData(url, jsonObject.toString());

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

    //將member資料存入
    public static void setMemberData(Member member2){
        Member.getInstance().setEmail(member2.getEmail());
        Member.getInstance().setNickname(member2.getNickname());
        Member.getInstance().setPassword(member2.getPassword());
        Member.getInstance().setPhoneNumber(member2.getPhoneNumber());
        Member.getInstance().setRating(member2.getRating());
        Member.getInstance().setFollow_count(member2.getFollow_count());
        Member.getInstance().setUpdateTime(member2.getUpdateTime());

    }

    public static void firebasedbAddOrReplace(Context context,FirebaseFirestore db,final Member member) {
        // 如果Firestore沒有該ID的Document就建立新的，已經有就更新內容
        db.collection("members").document(member.getId()+"").set(member)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Log.d(TAG, "Inserted with ID: " + member.getId());
                        Toast.makeText(context, "Inserted with ID: " + member.getId(), Toast.LENGTH_SHORT).show();
                        // 新增完畢回上頁
                    } else {
                        Log.e(TAG, "message: " + task1.getException().getMessage());
                        Toast.makeText(context, "message: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //取得會員資訊
    public static void getMemberData(Context context,Member member){

        final Gson gson2 = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

        SharedPreferences pref = context.getSharedPreferences("member_ID", MODE_PRIVATE);
        int mySqlMemberId = pref.getInt("member_ID", -1);
        member.setId(mySqlMemberId);

        String jsonIn = memberRemoteAccess(context, member, "findById");
        Member memberObject = gson2.fromJson(jsonIn,Member.class);
        MemberControl.setMemberData(memberObject);
    }


}
