package android.colin.democandlechart;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class ThirdActivity extends Activity {
    private int colorHomeBg;
    private int colorLine;
    private int colorText;
    private CombinedChart mChart;
    private CombinedData combinedData;
    private List<BarEntry> mSinusData;
    private List<Float> floatList = new ArrayList<>();
    private List<StockListBean.StockBean> stockBeans = new ArrayList<>();
    private List<BarEntry> barEntryList;
    private ArrayList<String> xVals;
    private BarData barData;
    private CandleData candleData;
    private List<CandleEntry> candleEntries = new ArrayList<>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.notifyDataSetChanged();
            mChart.invalidate();

        }
    };

    private LineData lineData;
    private int colorMa5;
    private int colorMa10;
    private int colorMa20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        mChart = (CombinedChart) findViewById(R.id.chart1);
        floatList = FileUtils.getFloat(getAssets(), "othersine.txt");
        List<StockListBean.StockBean> testStockBeans = Model.getData();
//        Log.d("result","size="+testStockBeans.size());
        for (int i = 0; i < testStockBeans.size(); i++) {
            testStockBeans.get(i).setVal(floatList.get(i));
            stockBeans.add(testStockBeans.get(i));
//            Log.d("result_stockBeans",stockBeans.get(i).toString());
        }
        colorHomeBg = getResources().getColor(R.color.home_page_bg);
        colorLine = getResources().getColor(R.color.common_divider);
        colorText = getResources().getColor(R.color.text_grey_light);
        colorMa5 = getResources().getColor(R.color.ma5);
        colorMa10 = getResources().getColor(R.color.ma10);
        colorMa20 = getResources().getColor(R.color.ma20);
        mChart.resetTracking();
        mChart.setDrawBorders(true);
        mChart.setBorderWidth(0.5f);
        mChart.setBorderColor(getResources().getColor(R.color.common_white));
        //外边框，true的时候是上下左右都有，false的时候是左边跟下边有
        mChart.setDrawBorders(true);
        mChart.setDescription("");
        mChart.setDrawGridBackground(true);
        mChart.setBackgroundColor(colorHomeBg);
        mChart.setGridBackgroundColor(colorHomeBg);
        //Y轴是否伸缩
        mChart.setScaleYEnabled(false);
        //如果为true，可以用两个手指放大缩小
        mChart.setPinchZoom(true);
        //如果为true，则值在bar的上面，否则，在下面
        mChart.setDrawValueAboveBar(false);

        mChart.setNoDataText("加载中...");
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDragEnabled(true);
//        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE,
        });


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(colorLine);
        xAxis.setTextColor(colorText);
        xAxis.setSpaceBetweenLabels(4);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setGridColor(colorLine);
        leftAxis.setTextColor(colorText);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        xVals = new ArrayList<>();
        barEntryList = new ArrayList<>();
        for (int i = 0; i < stockBeans.size(); i++) {
//            if (stockBeans.get(i).getVal()>=0){
//                barEntryList.add(new BarEntry(stockBeans.get(i).getVal()+7,i));
//            }else {
//                barEntryList.add(new BarEntry(stockBeans.get(i).getVal()-7,i));
//            }
            barEntryList.add(new BarEntry(stockBeans.get(i).getVal(), i));
//            barEntryList.add(new BarEntry(stockBeans.get(i).getOpen(),i));
//            barEntryList.add(new BarEntry(getRandom(15, 30), i));

            xVals.add(stockBeans.get(i).getDate() + "");
            StockListBean.StockBean stockBean = stockBeans.get(i);
            candleEntries.add(new CandleEntry(i,stockBean.getHigh(),stockBean.getLow(),stockBean.getOpen(),stockBean.getClose()));
            Log.d("result", "barEntryList=" + barEntryList.get(i));
        }
        CombinedData data = new CombinedData(xVals);

//        data.setData(generateLineData());
        data.setData(generateBarData());
//        data.setData(generateBubbleData());
//         data.setData(generateScatterData());
         data.setData(generateCandleData());


        /**
         * 往折线里面填充数据
         */
        /*ma5*/
        ArrayList<Entry> ma5Entries = new ArrayList<Entry>();
        for (int index = 0; index < stockBeans.size(); index++) {
            ma5Entries.add(new Entry(stockBeans.get(index).getMa5(), index));
        }
        /*ma10*/
        ArrayList<Entry> ma10Entries = new ArrayList<Entry>();
        for (int index = 0; index < stockBeans.size(); index++) {
            ma10Entries.add(new Entry(stockBeans.get(index).getMa10(), index));
        }
        /*ma20*/
        ArrayList<Entry> ma20Entries = new ArrayList<Entry>();
        for (int index = 0; index < stockBeans.size(); index++) {
            ma20Entries.add(new Entry(stockBeans.get(index).getMa20(), index));
        }

        lineData = generateMultiLineData(
                generateLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateLineDataSet(ma10Entries, colorMa10, "ma10"),
                generateLineDataSet(ma20Entries, colorMa20, "ma20"));

        data.setData(lineData);
//        data.setData(generateCandleData());
        mChart.setData(data);
        mChart.invalidate();

        final ViewPortHandler viewPortHandlerCombin = mChart.getViewPortHandler();
        viewPortHandlerCombin.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        final float xscaleCombin = 3;
        matrixCombin.postScale(xscaleCombin, 1f);

        mChart.moveViewToX(stockBeans.size() - 1);
        mChart.invalidate();
        handler.sendEmptyMessageDelayed(0, 300);

    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    /**
     * 折线图属性
     *
     * @param entries 显示的数据
     * @param color   颜色
     * @param label   名称
     * @return
     */
    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        if (label.equals("ma5")) {
            set.setHighlightEnabled(true);
            set.setDrawHorizontalHighlightIndicator(false);
            set.setHighLightColor(Color.WHITE);
        } else {/*此处必须得写*/
            set.setHighlightEnabled(false);
        }
        set.setColor(color);//折线的颜色
        set.setLineWidth(1f);//折线的宽度
        set.setDrawCubic(true);//圆滑曲线
        set.setDrawCircles(false);//是否画点
        set.setDrawCircleHole(false);//是否画圆环
        set.setDrawValues(false);//是否线上显示值

//        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }


    /**
     * 设置X轴的值
     *
     * @param lineDataSets
     * @return
     */
    private LineData generateMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < stockBeans.size(); i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private BarData generateBarData() {
        BarDataSet barDataSet = new BarDataSet(barEntryList, "Bar DataSet");
        barDataSet.setBarSpacePercent(40f);
        Log.d("result_data", "1111111111");
        List<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < floatList.size(); i++) {
            if (floatList.get(i) >= 0) {
                colorList.add(Color.RED);
            } else {
                colorList.add(Color.GREEN);
            }
        }

//        barDataSet.setColor(Color.rgb(240, 120, 124));
        barDataSet.setColors(colorList);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarData data = new BarData();
        data.addDataSet(barDataSet);
        data.setValueTextSize(10f);
//        data.setDrawValues(false);
        Log.d("result_data", data.toString());
        return data;
    }

    private float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    /**
     * K线图设置属性
     *
     * @return
     */
    private CandleData generateCandleData() {

        CandleDataSet set = new CandleDataSet(candleEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //上下的细线的宽度
//        set.setBarSpace(0.3f);
        set.setBodySpace(0.3f);
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

        CandleData candleData = new CandleData();
        candleData.addDataSet(set);

        return candleData;
    }
}
