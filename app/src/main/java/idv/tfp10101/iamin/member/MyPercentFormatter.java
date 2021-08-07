package idv.tfp10101.iamin.member;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;


/**
 * 此類別用於自定義欲顯示於piechart的數值及標籤
 */

public class MyPercentFormatter extends ValueFormatter {

    public DecimalFormat mFormat;
    private PieChart pieChart;

    public MyPercentFormatter() {
        mFormat = new DecimalFormat("###,###,##0.0");
    }
    public MyPercentFormatter(PieChart pieChart) {
        this();
        this.pieChart = pieChart;
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value) + " %";
    }

    @Override
    public String getPieLabel(float value, PieEntry pieEntry) {
        if (pieEntry.getValue() <= 300) {
            return "";
        } else {
            // raw value, skip percent sign
            return mFormat.format(value)+"%";
        }
    }

}
