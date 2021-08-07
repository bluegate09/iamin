package idv.tfp10101.iamin.member;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;


public class MyLoadingBar {
    private static ProgressDialog loadingBar;
    /**
     * 進度條
     * @param titleText 標題
     * @param messageText 訊息
     */
    public static void setLoadingBar(Context context, String titleText, String messageText) {
        loadingBar = new ProgressDialog(context);
        loadingBar.setTitle(titleText);
        loadingBar.setMessage(messageText);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
    }

    public static void dismissLoadingBar(){
        loadingBar.dismiss();
    }
}
