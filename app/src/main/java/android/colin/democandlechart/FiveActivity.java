package android.colin.democandlechart;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FiveActivity extends AppCompatActivity {

    private CombinedChart mChartTop;
    private int itemcount;
    private List<CandleEntry> topCandleEntriesList = new ArrayList<>();
    private List<StockListBean.StockBean> topStockBeansList = new ArrayList<>();
    private LineData topLineData;
    private CombinedData topCombinedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);

        mChartTop = (CombinedChart) findViewById(R.id.chartTop);
        initTopChart();

    }

    private void initTopChart() {
        int colorHomeBg = getResources().getColor(R.color.home_page_bg);
        int colorLine = getResources().getColor(R.color.common_divider);
        int colorText = getResources().getColor(R.color.text_grey_light);
        int colorMa5 = getResources().getColor(R.color.ma5);
        int colorMa10 = getResources().getColor(R.color.ma10);
        int colorMa20 = getResources().getColor(R.color.ma20);

        mChartTop.setDescription("");
        mChartTop.setDrawGridBackground(true);
        mChartTop.setBackgroundColor(colorHomeBg);
        mChartTop.setGridBackgroundColor(colorHomeBg);
        mChartTop.setScaleYEnabled(false);
        mChartTop.setPinchZoom(true);
        mChartTop.setDrawValueAboveBar(false);
        mChartTop.setNoDataText("加载中...");
        mChartTop.setAutoScaleMinMaxEnabled(true);
        mChartTop.setDragEnabled(true);
        mChartTop.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});

        XAxis xAxis = mChartTop.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(colorLine);
        xAxis.setTextColor(colorText);
        xAxis.setSpaceBetweenLabels(4);

        YAxis leftAxis = mChartTop.getAxisLeft();
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setGridColor(colorLine);
        leftAxis.setTextColor(colorText);

        YAxis rightAxis = mChartTop.getAxisRight();
        rightAxis.setEnabled(false);

        int[] colors = {colorMa5, colorMa10, colorMa20};
        //String[] labels = {"MA5", "MA10", "MA20"};
        String[] labels = {"最高值", "中间值", "最低值"};
        Legend legend = mChartTop.getLegend();
        legend.setCustom(colors, labels);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        legend.setTextColor(Color.WHITE);

        mChartTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                Log.d("result", entry.toString());
                CandleEntry candleEntry = (CandleEntry) entry;
                float change = (candleEntry.getClose() - candleEntry.getOpen()) / candleEntry.getOpen();
                NumberFormat nf = NumberFormat.getPercentInstance();
                nf.setMaximumFractionDigits(2);
                String changePercentage = nf.format(Double.valueOf(String.valueOf(change)));
                Log.d("qqq", "最高" + candleEntry.getHigh() + " 最低" + candleEntry.getLow() +
                        " 开盘" + candleEntry.getOpen() + " 收盘" + candleEntry.getClose() +
                        " 涨跌幅" + changePercentage);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        loadChartData(colorMa5, colorMa10, colorMa20);
    }

    private ArrayList<String> xVals;

    private void loadChartData(int colorMa5, int colorMa10, int colorMa20) {
        mChartTop.resetTracking();
//        List<CandleEntry> candleEntries = Model.getCandleEntries();
        topCandleEntriesList = new ArrayList<>();
       topStockBeansList = Model.getData();
        itemcount = topStockBeansList.size();
        xVals = new ArrayList<>();
        for (int i = 0; i < topStockBeansList.size(); i++) {
            xVals.add(topStockBeansList.get(i).getDate());
            StockListBean.StockBean stockBean = topStockBeansList.get(i);
            topCandleEntriesList.add(new CandleEntry(i, stockBean.getHigh(), stockBean.getLow(), stockBean.getOpen(), stockBean.getClose()));

        }

        topCombinedData = new CombinedData(xVals);
//        getTopLine(colorMa5, colorMa10, colorMa20);
        getTopCandleData();
        mChartTop.setData(topCombinedData);//当前屏幕会显示所有的数据
        mChartTop.invalidate();
    }

    private void getTopLine(int colorMa5, int colorMa10, int colorMa20) {
        /**
         * 往折线里面填充数据
         */
        /*ma5*/
        ArrayList<Entry> ma5Entries = new ArrayList<Entry>();
        for (int index = 0; index < topStockBeansList.size(); index++) {
            ma5Entries.add(new Entry(topStockBeansList.get(index).getMa5(), index));
        }
        /*ma10*/
        ArrayList<Entry> ma10Entries = new ArrayList<Entry>();
        for (int index = 0; index < topStockBeansList.size(); index++) {
            ma10Entries.add(new Entry(topStockBeansList.get(index).getMa10(), index));
        }
        /*ma20*/
        ArrayList<Entry> ma20Entries = new ArrayList<Entry>();
        for (int index = 0; index < topStockBeansList.size(); index++) {
            ma20Entries.add(new Entry(topStockBeansList.get(index).getMa20(), index));
        }

        topLineData = generateTopMultiLineData(
                generateTopLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateTopLineDataSet(ma10Entries, colorMa10, "ma10"),
                generateTopLineDataSet(ma20Entries, colorMa20, "ma20"));

        topCombinedData.setData(topLineData);
    }


    private LineDataSet generateTopLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(1f);
        set.setDrawCubic(true);//圆滑曲线
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

    private LineData generateTopMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < itemcount; i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    /**
     * K线图设置属性
     *
     * @return
     */
    private void getTopCandleData() {

        CandleDataSet set = new CandleDataSet(topCandleEntriesList, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //上下的细线的宽度

        set.setShadowWidth(1.5f);
        //减小的是绿色
        set.setDecreasingColor(Color.RED);
        //设置减小方块是实心还是空心
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        //增长的是绿色
        set.setIncreasingColor(Color.GREEN);
        //设置增长方块是实心还是空心
        set.setIncreasingPaintStyle(Paint.Style.FILL);
//        set.setNeutralColor(Color.RED);
        set.setShadowColorSameAsCandle(true);
        //高亮十字架的宽度与颜色
        set.setHighlightLineWidth(0.5f);
        set.setHighLightColor(Color.WHITE);
        //true，显示每一个的值
        set.setDrawValues(false);
        //设置每一个的字的颜色
        set.setValueTextColor(getResources().getColor(R.color.common_white));

        CandleData candleData = new CandleData(xVals);
        candleData.addDataSet(set);

        topCombinedData.setData(candleData);
    }
}
