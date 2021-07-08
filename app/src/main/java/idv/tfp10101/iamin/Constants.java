package idv.tfp10101.iamin;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 常用的方法or常數
 */
public class Constants {
    public static final String TAG = "Iamin_TAG ";

    /**
     * 設定RecyclerView的Item間距
     */
    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private String Offset;
        private int spacePx;

        public SpacesItemDecoration(String Offset, int spacePx) {
            this.Offset = Offset;
            this.spacePx = spacePx;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //outRect.left = space;
            //outRect.right = space;
            //outRect.bottom = space;

            switch (Offset) {
                case "bottom":
                    outRect.bottom = spacePx;
                    break;

                case "right":
                    outRect.right = spacePx;
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Covert dp to px
     */
    public static float convertDpToPixel(float dp, Context context){
        float px = dp * getDensity(context);
        return px;
    }

    /**
     * Covert px to dp
     */
    public static float convertPixelToDp(float px, Context context){
        float dp = px / getDensity(context);
        return dp;
    }

    /**
     * 取得螢幕密度
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     */
    public static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }
}
