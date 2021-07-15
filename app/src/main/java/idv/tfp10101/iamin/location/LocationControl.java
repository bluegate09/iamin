package idv.tfp10101.iamin.location;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.network.RemoteAccess;

public class LocationControl {
    /**
     * 抓取此團購ID的所有發貨地址
     */
    public static List<Location> getLocationByGroupId(Context context, int groupId) {
        List<Location> locations = new ArrayList<>();
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Location";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllByGroupId");
            jsonObject.addProperty("groupId", groupId);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Location>>(){}.getType();
            locations = new Gson().fromJson(jsonString, listType);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
        return locations;
    }

    /**
     * 存入 取貨時間
     */
    public static int update(Context context, Location location) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Location";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "update");
            jsonObject.addProperty("location", new Gson().toJson(location));

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
}
