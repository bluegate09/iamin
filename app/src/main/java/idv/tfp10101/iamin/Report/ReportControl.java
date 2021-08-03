package idv.tfp10101.iamin.Report;

import android.content.Context;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.Report.Report;
import idv.tfp10101.iamin.network.RemoteAccess;


public class ReportControl {

    /**
     * 新增檢舉
     * @param context
     * @param report
     * @return
     */
    public static int insertReport(Context context, Report report) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Report";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "reportinsert");
            jsonObject.addProperty("report", new Gson().toJson(report));

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
}
