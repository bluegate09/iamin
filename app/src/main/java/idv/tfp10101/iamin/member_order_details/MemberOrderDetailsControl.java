package idv.tfp10101.iamin.member_order_details;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
}
