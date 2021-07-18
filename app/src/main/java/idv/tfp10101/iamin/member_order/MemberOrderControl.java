package idv.tfp10101.iamin.member_order;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.R;
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
}
