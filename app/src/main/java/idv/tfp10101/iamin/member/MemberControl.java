package idv.tfp10101.iamin.member;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.group.Group;
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

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static Member getsellerByMemberId(Context context, Member seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("member", new Gson().toJson(seller));

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, jsonObject.toString());
           return new Gson().fromJson(jsonString, Member.class);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static Bitmap getsellerimageByMemberId(Context context, Member seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("member", new Gson().toJson(seller));

            return RemoteAccess.getRemoteImage(url, jsonObject.toString());
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static void followed(Context context, int buyer, int seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "follow");
            jsonObject.addProperty("member", new Gson().toJson(new Member()));
            jsonObject.addProperty("buyer_id", buyer);
            jsonObject.addProperty("follwer_id", seller);
            RemoteAccess.getRemoteData(url, jsonObject.toString());
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static int chackfollowed(Context context, int buyer, int seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "chackfollow");
            jsonObject.addProperty("member", new Gson().toJson(new Member()));
            jsonObject.addProperty("buyer_id", buyer);
            jsonObject.addProperty("follwer_id", seller);
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
        return -1;
    }
}
