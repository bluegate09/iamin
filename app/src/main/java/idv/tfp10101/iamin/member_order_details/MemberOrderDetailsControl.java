package idv.tfp10101.iamin.member_order_details;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberOrderDetailsControl {

    /**
     * 新增訂單明細
     * @param context
     * @param orderDetails
     * @return
     */
    public static int insertMemberOrderDetails(Context context, List<MemberOrderDetails> orderDetails) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merchbrowse";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "insertMemberOrderDetails");
            jsonObject.addProperty("memberorderdetails", new Gson().toJson(orderDetails));

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    public static List<MemberOrderDetails> getMemberOrderDetailsByMemberOrders(
            Context context, List<MemberOrder> memberOrders, String purpose
    ) {
        List<MemberOrderDetails> memberOrderDetails;
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + purpose;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getMemberOrderDetailsByMemberOrders");
            JsonArray jsonArray = new JsonArray();
            for (MemberOrder memberOrder : memberOrders) {
                JsonObject jsonObjectMO = new JsonObject();
                jsonObjectMO.addProperty("memberOrder", new Gson().toJson(memberOrder));
                jsonArray.add(jsonObjectMO);
            }
            jsonObject.add("memberOrders", jsonArray);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            if (jsonString == null) {
                return null;
            }
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<MemberOrderDetails>>(){}.getType();
            memberOrderDetails = new Gson().fromJson(jsonString, listType);
            return memberOrderDetails;
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
