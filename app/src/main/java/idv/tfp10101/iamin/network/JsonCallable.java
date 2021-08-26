package idv.tfp10101.iamin.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import idv.tfp10101.iamin.Constants;

public class JsonCallable implements Callable<String> {
    private final String url;
    private final String requst;

    private HttpURLConnection http;

    public JsonCallable(String url, String requst) {
        this.url = url;
        this.requst = requst;
    }

    @Override
    public String call() throws Exception {
        // 網路連線設定
        SetUpConnection();

        /** request
         * http.getOutputStream() - 會自動connect
         */
        try (
                //OutputStreamWriter: OutputStream(位元) -> Writer(純文字)
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(http.getOutputStream()));
        ) {
            bw.write(requst);
            Log.d(Constants.TAG, "JsonCallable -> requst: " + requst);
        }

        /** response */
        StringBuilder response = new StringBuilder();
        int responseCode = http.getResponseCode(); // 獲得 response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (
                    // InputStreamReader: InputStream(位元) -> Reader(純文字)
                    BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            ) {
                String line;

                // 一列一列讀入
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }catch (Exception e) {
                Log.e(Constants.TAG, "response: " + e.toString());
            }
        } else {
            Log.d(Constants.TAG, "response code" + responseCode);
        }

        /** return */
        if (http != null) {
            http.disconnect();
        }
        return response.toString();
    }

    private void SetUpConnection() throws Exception {
        try {
            //建立連線
            http = (HttpURLConnection) new URL(url).openConnection();
            //設定引數
            http.setDoInput(true); // 允許輸入
            http.setDoOutput(true); // 允許輸出
            // 不知道請求內容大小時可以呼叫此方法將請求內容分段傳輸，設定0代表使用預設大小
            http.setChunkedStreamingMode(0);
            http.setUseCaches(false); // 不要使用cached
            //設定請求屬性
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "application/json");
            http.setRequestProperty("charset", "UTF-8");
        }catch (Exception e) {
            Log.e(Constants.TAG, "Connection: " + e.toString());
        }
    }
}
