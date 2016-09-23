package android.colin.democandlechart;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private String TAG = "qqq";
    private CombinedChart mChart;
    private Button btn;
    private int itemcount;
    private LineData lineData;
    private CandleData candleData;
    private CombinedData combinedData;
    private ArrayList<String> xVals;
    private List<CandleEntry> candleEntries = new ArrayList<>();
    private List<StockListBean.StockBean> stockBeans;
    private int colorHomeBg;
    private int colorLine;
    private int colorText;
    private int colorMa5;
    private int colorMa10;
    private int colorMa20;

    XAxis xAxisBar;
    YAxis axisLeftBar;
    YAxis axisRightBar;
    private BarDataSet barDataSet;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            barChart.setAutoScaleMinMaxEnabled(true);
            barChart.notifyDataSetChanged();
            barChart.invalidate();

        }
    };
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine);

        mChart = (CombinedChart) findViewById(R.id.chart);
        barChart = (BarChart) findViewById(R.id.barchart);
        initChart();
        loadChartData();
    }

    private void initChart() {
        colorHomeBg = getResources().getColor(R.color.home_page_bg);
        colorLine = getResources().getColor(R.color.common_divider);
        colorText = getResources().getColor(R.color.text_grey_light);
        colorMa5 = getResources().getColor(R.color.ma5);
        colorMa10 = getResources().getColor(R.color.ma10);
        colorMa20 = getResources().getColor(R.color.ma20);


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
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});

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

        int[] colors = {colorMa5, colorMa10, colorMa20};
        String[] labels = {"MA5", "MA10", "MA20"};
        Legend legend = mChart.getLegend();
        legend.setCustom(colors, labels);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        legend.setTextColor(Color.WHITE);
        Log.d("qqq","2222/**/");

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                Log.d("qqq","1111");
                CandleEntry candleEntry = (CandleEntry) entry;
                float change = (candleEntry.getClose() - candleEntry.getOpen()) / candleEntry.getOpen();
                NumberFormat nf = NumberFormat.getPercentInstance();
                nf.setMaximumFractionDigits(2);
                String changePercentage = nf.format(Double.valueOf(String.valueOf(change)));
                Log.d("qqq", "最高" + candleEntry.getHigh() + " 最低" + candleEntry.getLow() +
                        " 开盘" + candleEntry.getOpen() + " 收盘" + candleEntry.getClose() +
                        " 涨跌幅" + changePercentage);
//                barChart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
//                barChart.highlightValue(null);
            }
        });


        barChart.setDrawBorders(true);
        barChart.setBorderWidth(1);
        barChart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        barChart.setDescription("");
        barChart.setDragEnabled(true);
        barChart.setScaleYEnabled(false);
        barChart.setBackgroundColor(colorHomeBg);
        Legend barChartLegend = barChart.getLegend();
        barChartLegend.setEnabled(false);

        //BarYAxisFormatter  barYAxisFormatter=new BarYAxisFormatter();
        //bar x y轴
        xAxisBar = barChart.getXAxis();
        xAxisBar.setDrawLabels(true);
        xAxisBar.setDrawGridLines(false);
        xAxisBar.setDrawAxisLine(false);
        xAxisBar.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setGridColor(getResources().getColor(R.color.minute_grayLine));

        axisLeftBar = barChart.getAxisLeft();
        axisLeftBar.setAxisMinValue(0);
        axisLeftBar.setDrawGridLines(false);
        axisLeftBar.setDrawAxisLine(false);
        axisLeftBar.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        axisLeftBar.setDrawLabels(true);
        axisLeftBar.setSpaceTop(0);
        axisLeftBar.setShowOnlyMinMax(true);
        axisRightBar = barChart.getAxisRight();
        axisRightBar.setDrawLabels(false);
        axisRightBar.setDrawGridLines(false);
        axisRightBar.setDrawAxisLine(false);


        //设置是否在抬起手之后继续滑动
        mChart.setDragDecelerationEnabled(true);
        barChart.setDragDecelerationEnabled(true);
        //设置摩擦系数，就是抬手后滑动的速度，0是停止，越大越好，要小于1
        mChart.setDragDecelerationFrictionCoef(0.2f);
        barChart.setDragDecelerationFrictionCoef(0.2f);


        // 将K线控的滑动事件传递给交易量控件
        mChart.setOnChartGestureListener(new CoupleChartGestureListener(mChart, new Chart[]{barChart}));
        // 将交易量控件的滑动事件传递给K线控件
        barChart.setOnChartGestureListener(new CoupleChartGestureListener(barChart, new Chart[]{mChart}));

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.e("%%%%", h.getXIndex() + "");
                mChart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                mChart.highlightValue(null);
            }
        });
//        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
//            @Override
//            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
//
//                barChart.highlightValues(new Highlight[]{h});
//            }
//
//            @Override
//            public void onNothingSelected() {
//                barChart.highlightValue(null);
//            }
//        });
    }

    private float volmax;

    private void loadChartData() {


        mChart.resetTracking();
        candleEntries = Model.getCandleEntries();
        itemcount = candleEntries.size();
//        List<StockListBean.StockBean> stockBeans = Model.getData();
        stockBeans = Model.getData();
        xVals = new ArrayList<>();


        getBarDataSet();
        combinedData = new CombinedData(xVals);

        /*k line*/
        candleData = generateCandleData();
        combinedData.setData(candleData);

        /**
         * 往折线里面填充数据
         */
        /*ma5*/
        ArrayList<Entry> ma5Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma5Entries.add(new Entry(stockBeans.get(index).getMa5(), index));
        }
        /*ma10*/
        ArrayList<Entry> ma10Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma10Entries.add(new Entry(stockBeans.get(index).getMa10(), index));
        }
        /*ma20*/
        ArrayList<Entry> ma20Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma20Entries.add(new Entry(stockBeans.get(index).getMa20(), index));
        }

        lineData = generateMultiLineData(
                generateLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateLineDataSet(ma10Entries, colorMa10, "ma10"),
                generateLineDataSet(ma20Entries, colorMa20, "ma20"));

        combinedData.setData(lineData);
        mChart.setData(combinedData);//当前屏幕会显示所有的数据
//        List<StockListBean.StockBean> stocks = Model.getData();
        final ViewPortHandler viewPortHandlerCombin = mChart.getViewPortHandler();
        viewPortHandlerCombin.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        final float xscaleCombin = 3;
        matrixCombin.postScale(xscaleCombin, 1f);

        mChart.moveViewToX(stockBeans.size() - 1);
        barChart.moveViewToX(stockBeans.size() - 1);
        mChart.invalidate();

        setOffset();

        handler.sendEmptyMessageDelayed(0, 300);
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    /**
     * 成交量柱状图
     * @return
     */
    private BarDataSet getBarDataSet() {

        List<Integer> barList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();

        for (int i = 0; i < itemcount; i++) {
            //X轴的值
            xVals.add(stockBeans.get(i).getDate());
            //获取成交量的最大值
            volmax = Math.max(Float.parseFloat(stockBeans.get(i).getVolume()), volmax);
            //取出每一天的成交量
            barList.add(Integer.parseInt(stockBeans.get(i).getVolume()));
        }
        //判断后一个与前一个的大小
        for (int i = 0; i < barList.size() - 1; i++) {
            if (barList.get(i) <= barList.get(i + 1)) {
                //成交量增加，是红色，否则是绿色
                colorList.add(Color.RED);
            } else {
                colorList.add(Color.GREEN);
            }
        }
        //第一天的成交量是增加的
        colorList.add(0, Color.RED);

        /**
         * 设置成交量的单位的
         */
        String unit = MyUtils.getVolUnit(volmax);
        int u = 1;
        if ("万手".equals(unit)) {
            u = 4;
        } else if ("亿手".equals(unit)) {
            u = 8;
        }
        axisLeftBar.setValueFormatter(new VolFormatter((int) Math.pow(10, u)));

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < itemcount; i++) {
            barEntries.add(new BarEntry(Float.parseFloat(stockBeans.get(i).getVolume()), i));

        }
        barDataSet = new BarDataSet(barEntries, "成交量");
        barDataSet.setBarSpacePercent(50); //bar空隙
        //设置高亮线
        barDataSet.setHighlightEnabled(true);
//        barDataSet.setHighLightAlpha(255);
        //高亮线的颜色
        barDataSet.setHighLightColor(Color.WHITE);
        barDataSet.setDrawValues(false);
        //设置柱状图的颜色
        barDataSet.setColors(colorList);

        BarData barData = new BarData(xVals, barDataSet);
        barChart.setData(barData);
        final ViewPortHandler viewPortHandlerBar = barChart.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        final float xscale = 3;
        touchmatrix.postScale(xscale, 1f);
        return barDataSet;
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
    private CandleData generateCandleData() {

        CandleDataSet set = new CandleDataSet(candleEntries, "");
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

        return candleData;
    }

    /*设置量表对齐*/
    private void setOffset() {
        float lineLeft = mChart.getViewPortHandler().offsetLeft();
        float barLeft = barChart.getViewPortHandler().offsetLeft();
        float lineRight = mChart.getViewPortHandler().offsetRight();
        float barRight = barChart.getViewPortHandler().offsetRight();
        float barBottom = barChart.getViewPortHandler().offsetBottom();
        float offsetLeft, offsetRight;
        float transLeft = 0, transRight = 0;
 /*注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (barLeft < lineLeft) {
           /* offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            barChart.setExtraLeftOffset(offsetLeft);*/
            transLeft = lineLeft;
        } else {
            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
            mChart.setExtraLeftOffset(offsetLeft);
            transLeft = barLeft;
        }
  /*注：setExtraRight...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (barRight < lineRight) {
          /*  offsetRight = Utils.convertPixelsToDp(lineRight);
            barChart.setExtraRightOffset(offsetRight);*/
            transRight = lineRight;
        } else {
            offsetRight = Utils.convertPixelsToDp(barRight);
            mChart.setExtraRightOffset(offsetRight);
            transRight = barRight;
        }
        barChart.setViewPortOffsets(transLeft, 15, transRight, barBottom);
    }

}
