package idv.tfp10101.iamin.member_order;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.R;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.location.Location;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberOrderControl {

    /**
     * 新增訂單主表
     * @param context
     * @param memberOrder
     * @return
     */
    public static int insertMemberOrder(Context context, MemberOrder memberOrder) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merchbrowse";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "insertMemberOrder");
            jsonObject.addProperty("memberorder", new Gson().toJson(memberOrder));

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 抓取會員訂單(GroupId)
     */
    public static List<MemberOrder> getMemberOrderByGroupId(Context context, int groupId, String purpose){
        List<MemberOrder> memberOrders;
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + purpose;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getMemberOrderByGroupId");
            jsonObject.addProperty("groupId", groupId);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            if (jsonString == null) {
                return null;
            }
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<MemberOrder>>(){}.getType();
            memberOrders = new Gson().fromJson(jsonString, listType);
            return memberOrders;
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 更新多筆會員訂單
     */
    public static int updateMemberOrders(Context context, List<MemberOrder> memberOrders, String purpose) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + purpose;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "updateMemberOrders");
            JsonArray jsonArray = new JsonArray();
            for (MemberOrder memberOrder : memberOrders) {
                JsonObject jsonObjectMO = new JsonObject();
                jsonObjectMO.addProperty("memberOrder", new Gson().toJson(memberOrder));
                jsonArray.add(jsonObjectMO);
            }
            jsonObject.add("memberOrders", jsonArray);

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 更新指定會員訂單ID的發貨狀態
     */
    public static int updateDeliverStatus(Context context, int memberOderId, String purpose) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + purpose;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "updateDeliverStatus");
            jsonObject.addProperty("memberOderId", memberOderId);
            jsonObject.addProperty("status", true);

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
}
