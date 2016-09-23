package android.colin.democandlechart;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

public class FourActivity extends Activity {

    private CombinedChart mChartTop;
    private CombinedChart mChartBottom;

    private int colorHomeBg;
    private int colorLine;
    private int colorText;
    private int colorMa5;
    private int colorMa10;
    private int colorMa20;

    private YAxis leftAxis;

    private List<CandleEntry> topCandleEntriesList = new ArrayList<>();
    private List<StockListBean.StockBean> topStockBeansList = new ArrayList<>();
    private ArrayList<String> xVals;
    private CombinedData topCombinedData;
    private LineData topLineData;

    private List<StockListBean.StockBean> bottomStockBeansList;
    private CombinedData bottomCombinedData;
    private List<Float> floatList = new ArrayList<>();
    private float volmax;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mChartTop.setAutoScaleMinMaxEnabled(true);
            mChartTop.notifyDataSetChanged();
            mChartTop.invalidate();
            mChartBottom.setAutoScaleMinMaxEnabled(true);
            mChartBottom.notifyDataSetChanged();
            mChartBottom.invalidate();

        }
    };
    private RadioGroup rg_bottom;
    private RadioButton rb_bottom_left;
    private RadioButton rb_bottom_middle;
    private RadioButton rb_bottom_right;

    private boolean isVol=true,isMACD,isKDJ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);
        mChartTop = (CombinedChart) findViewById(R.id.chartTop);
        mChartBottom = (CombinedChart) findViewById(R.id.chartBottom);
        rg_bottom = (RadioGroup) findViewById(R.id.rg_bottom);
        rb_bottom_left = (RadioButton) findViewById(R.id.rb_bottom_left);
        rb_bottom_middle = (RadioButton) findViewById(R.id.rb_bottom_middle);
        rb_bottom_right = (RadioButton) findViewById(R.id.rb_bottom_right);
        rb_bottom_left.setChecked(true);

        rg_bottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_bottom_left:
                        isVol=true;
                        isMACD=false;
                        isKDJ=false;
                        loadBottomChartData();
                        break;
                    case R.id.rb_bottom_middle:
                        isVol=false;
                        isMACD=true;
                        isKDJ=false;
                        loadBottomChartData();

                        break;
                    case R.id.rb_bottom_right:
                        isVol=false;
                        isMACD=false;
                        isKDJ=true;
                        loadBottomChartData();

                        break;
                }
            }
        });

        initTopChart();
        initBottomChart();
        final ViewPortHandler viewPortHandlerCombin = mChartTop.getViewPortHandler();
        //最大放大倍数
        viewPortHandlerCombin.setMaximumScaleX(10);
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        //当前页面显示的数据是总数据的几分之几
        final float xscaleCombin = 3;
        matrixCombin.postScale(xscaleCombin, 1f);

        final ViewPortHandler viewPortHandlerCombin1 = mChartBottom.getViewPortHandler();
        //最大放大倍数
        viewPortHandlerCombin1.setMaximumScaleX(10);
        Matrix matrixCombin1 = viewPortHandlerCombin1.getMatrixTouch();
        final float xscaleCombin1 = 3;
        matrixCombin1.postScale(xscaleCombin, 1f);

        mChartTop.moveViewToX(topStockBeansList.size() - 1);
        mChartBottom.moveViewToX(bottomStockBeansList.size() - 1);
        mChartTop.invalidate();
        mChartBottom.invalidate();
        setOffset();

        handler.sendEmptyMessageDelayed(0, 300);
    }

    private void initBottomChart() {
        colorHomeBg = getResources().getColor(R.color.home_page_bg);
        colorLine = getResources().getColor(R.color.common_divider);
        colorText = getResources().getColor(R.color.text_grey_light);
        colorMa5 = getResources().getColor(R.color.ma5);
        colorMa10 = getResources().getColor(R.color.ma10);
        colorMa20 = getResources().getColor(R.color.ma20);

        mChartBottom.resetTracking();

        mChartBottom.setDrawBorders(true);
        mChartBottom.setBorderWidth(0.5f);
        mChartBottom.setBorderColor(getResources().getColor(R.color.common_white));
        //外边框，true的时候是上下左右都有，false的时候是左边跟下边有
        mChartBottom.setDrawBorders(true);
        mChartBottom.setDescription("");
        mChartBottom.setDrawGridBackground(true);
        mChartBottom.setBackgroundColor(colorHomeBg);
        mChartBottom.setGridBackgroundColor(colorHomeBg);
        //Y轴是否伸缩
        mChartBottom.setScaleYEnabled(false);
        //如果为true，可以用两个手指放大缩小
        mChartBottom.setPinchZoom(true);
        //如果为true，则值在bar的上面，否则，在下面
        mChartBottom.setDrawValueAboveBar(false);

        mChartBottom.setNoDataText("加载中...");
        mChartBottom.setAutoScaleMinMaxEnabled(true);
        mChartBottom.setDragEnabled(true);
        mChartBottom.setDrawOrder(new CombinedChart.DrawOrder[]
                {CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE});


        XAxis xAxis = mChartBottom.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(colorLine);
        xAxis.setTextColor(colorText);
        xAxis.setSpaceBetweenLabels(4);
        xAxis.setDrawLabels(false);


         leftAxis = mChartBottom.getAxisLeft();
//        leftAxis.setAxisMinValue(0);
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(colorLine);
        leftAxis.setTextColor(colorText);


        YAxis rightAxis = mChartBottom.getAxisRight();
        rightAxis.setEnabled(false);

      /*  Legend legend = mChartBottom.getLegend();
        legend.setEnabled(false);*/

        loadBottomChartData();
    }

    private void loadBottomChartData() {
        mChartBottom.resetTracking();
        List<StockListBean.StockBean> testStockBeans = Model.getData();
        floatList = FileUtils.getFloat(getAssets(), "othersine.txt");

        xVals = new ArrayList<>();
        bottomStockBeansList = new ArrayList<>();
        for (int i = 0; i < testStockBeans.size(); i++) {
            testStockBeans.get(i).setVal(floatList.get(i));
            bottomStockBeansList.add(testStockBeans.get(i));
            xVals.add(bottomStockBeansList.get(i).getDate());
        }
        bottomCombinedData = new CombinedData(xVals);
        Log.d("result_boolean","isVol="+isVol+" isMACD="+isMACD+" isKDJ="+isKDJ);
        if (isVol) {
            getBottomVOLBarData();
        }else if (isMACD) {

            getBottomMACDBarData();
            getBottomMACDLineData();
        }else if (isKDJ){
            getBottomKDJLineData();
        }
//        getBottomKDJLineData();
        mChartBottom.setData(bottomCombinedData);
        mChartBottom.invalidate();
        mChartBottom.notifyDataSetChanged();
    }

    private void getBottomKDJLineData(){
        int[] colors = {colorMa5, colorMa10};
        String[] labels = {"MA5", "MA10"};
        Legend legend = mChartBottom.getLegend();
        legend.setCustom(colors, labels);

        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        legend.setTextColor(Color.WHITE);
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
        LineData lineData= generateBottomKDJMultiLineData(
                generateBottomKDJLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateBottomKDJLineDataSet(ma10Entries, colorMa10, "ma10"),
                generateBottomKDJLineDataSet(ma20Entries, colorMa20, "ma20"));
        bottomCombinedData.setData(lineData);
    }

    /**
     * 折线图属性
     *
     * @param entries 显示的数据
     * @param color   颜色
     * @param label   名称
     * @return
     */
    private LineDataSet generateBottomKDJLineDataSet(List<Entry> entries, int color, String label) {
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
    private LineData generateBottomKDJMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < topStockBeansList.size(); i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private void getBottomMACDLineData(){
        int[] colors = {colorMa5, colorMa10, colorMa20};
        String[] labels = {"MA5", "MA10", "MA20"};
        Legend legend = mChartBottom.getLegend();
        legend.setCustom(colors, labels);

        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        legend.setTextColor(Color.WHITE);
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

        LineData lineData= generateBottomMACDMultiLineData(
                generateBottomMACDLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateBottomMACDLineDataSet(ma10Entries, colorMa10, "ma10"));
        bottomCombinedData.setData(lineData);
    }

    /**
     * 折线图属性
     *
     * @param entries 显示的数据
     * @param color   颜色
     * @param label   名称
     * @return
     */
    private LineDataSet generateBottomMACDLineDataSet(List<Entry> entries, int color, String label) {
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
    private LineData generateBottomMACDMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < topStockBeansList.size(); i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private void getBottomMACDBarData(){
        List<BarEntry> barEntryList=new ArrayList<>();
        for (int i = 0; i < bottomStockBeansList.size(); i++) {
            barEntryList.add(new BarEntry(bottomStockBeansList.get(i).getVal(),i));
        }
        BarDataSet barDataSet = new BarDataSet(barEntryList, "Bar DataSet");
        barDataSet.setBarSpacePercent(40f);
        List<Integer> colorList = new ArrayList<>();

        for (int i = 0; i < barEntryList.size(); i++) {
            if (barEntryList.get(i).getVal() >= 0) {
                colorList.add(Color.RED);
            } else {
                colorList.add(Color.GREEN);
            }
        }

//        barDataSet.setColor(Color.rgb(240, 120, 124));
        barDataSet.setColors(colorList);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarData data = new BarData(xVals);
        data.addDataSet(barDataSet);
        data.setValueTextSize(10f);
        data.setDrawValues(false);
        Log.d("result","xvals.size="+xVals.size()+" barEntryList="+barEntryList.size()+" bottomStockBeansList"+bottomStockBeansList.size());
        bottomCombinedData.setData(data);
    }

    private void getBottomVOLBarData() {
          Legend legend = mChartBottom.getLegend();
        legend.setEnabled(false);
        List<Integer> barList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < bottomStockBeansList.size(); i++) {
            //获取成交量的最大值
            volmax = Math.max(Float.parseFloat(bottomStockBeansList.get(i).getVolume()), volmax);
            //取出每一天的成交量
            barList.add(Integer.parseInt(bottomStockBeansList.get(i).getVolume()));
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
//       YAxis leftAxis = mChartBottom.getAxisLeft();
////        leftAxis.setAxisMinValue(0);
//        leftAxis.setLabelCount(4, false);
//        leftAxis.setDrawGridLines(true);
//        leftAxis.setDrawAxisLine(false);
//        leftAxis.setGridColor(colorLine);
//        leftAxis.setTextColor(colorText);

//        leftAxis.setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < bottomStockBeansList.size(); i++) {
            barEntries.add(new BarEntry(Float.parseFloat(bottomStockBeansList.get(i).getVolume()), i));

        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "成交量");
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
        Log.d("result","xvals.size="+xVals.size()+" barEntries="+barEntries.size()+" bottomStockBeansList="+bottomStockBeansList.size());
        bottomCombinedData.setData(barData);
    }

    /**
     * 实例化上面的chart
     */
    private void initTopChart() {

        colorHomeBg = getResources().getColor(R.color.home_page_bg);
        colorLine = getResources().getColor(R.color.common_divider);
        colorText = getResources().getColor(R.color.text_grey_light);
        colorMa5 = getResources().getColor(R.color.ma5);
        colorMa10 = getResources().getColor(R.color.ma10);
        colorMa20 = getResources().getColor(R.color.ma20);

        mChartTop.resetTracking();
        mChartTop.setDrawBorders(true);
        mChartTop.setBorderWidth(0.5f);
        mChartTop.setBorderColor(getResources().getColor(R.color.common_white));
        //外边框，true的时候是上下左右都有，false的时候是左边跟下边有
        mChartTop.setDrawBorders(true);
        mChartTop.setDescription("");
        mChartTop.setDrawGridBackground(true);
        mChartTop.setBackgroundColor(colorHomeBg);
        mChartTop.setGridBackgroundColor(colorHomeBg);
        //Y轴是否伸缩
        mChartTop.setScaleYEnabled(false);
        //如果为true，可以用两个手指放大缩小
        mChartTop.setPinchZoom(true);
        //如果为true，则值在bar的上面，否则，在下面
        mChartTop.setDrawValueAboveBar(false);

        mChartTop.setNoDataText("加载中...");
        mChartTop.setAutoScaleMinMaxEnabled(true);
        mChartTop.setDragEnabled(true);
        mChartTop.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});


        XAxis xAxis = mChartTop.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
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
        String[] labels = {"MA5", "MA10", "MA20"};
        Legend legend = mChartTop.getLegend();
        legend.setCustom(colors, labels);

        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        legend.setTextColor(Color.WHITE);

        mChartTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                CandleEntry candleEntry = (CandleEntry) entry;
                float change = (candleEntry.getClose() - candleEntry.getOpen()) / candleEntry.getOpen();
                NumberFormat nf = NumberFormat.getPercentInstance();
                nf.setMaximumFractionDigits(2);
                String changePercentage = nf.format(Double.valueOf(String.valueOf(change)));
                Log.d("qqq", "最高" + candleEntry.getHigh() + " 最低" + candleEntry.getLow() +
                        " 开盘" + candleEntry.getOpen() + " 收盘" + candleEntry.getClose() +
                        " 涨跌幅" + changePercentage);
                mChartBottom.highlightValues(new Highlight[]{highlight});
            }

            @Override
            public void onNothingSelected() {
                mChartBottom.highlightValue(null);
            }
        });


        //设置是否在抬起手之后继续滑动
        mChartTop.setDragDecelerationEnabled(true);
        mChartBottom.setDragDecelerationEnabled(true);
        //设置摩擦系数，就是抬手后滑动的速度，0是停止，越大越好，要小于1
        mChartTop.setDragDecelerationFrictionCoef(0.2f);
        mChartBottom.setDragDecelerationFrictionCoef(0.2f);

        // 将K线控的滑动事件传递给交易量控件
        mChartTop.setOnChartGestureListener(new CoupleChartGestureListener(mChartTop, new Chart[]{mChartBottom}));
        // 将交易量控件的滑动事件传递给K线控件
        mChartBottom.setOnChartGestureListener(new CoupleChartGestureListener(mChartBottom, new Chart[]{mChartTop}));

        mChartBottom.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.e("%%%%", h.getXIndex() + "");
                mChartTop.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                mChartTop.highlightValue(null);
            }
        });

        loadTopChartData();
    }

    /**
     * 加载数据
     */
    private void loadTopChartData() {
        mChartTop.resetTracking();

        topStockBeansList = Model.getData();
//        topCandleEntriesList = Model.getCandleEntries();
        xVals = new ArrayList<>();
        for (int i = 0; i < topStockBeansList.size(); i++) {
            StockListBean.StockBean stockBean = topStockBeansList.get(i);
            //X轴的值
            xVals.add(stockBean.getDate());

            topCandleEntriesList.add(new CandleEntry(i, stockBean.getHigh(), stockBean.getLow(), stockBean.getOpen(), stockBean.getClose()));

            //获取成交量的最大值
//            volmax = Math.max(Float.parseFloat(stockBeans.get(i).getVolume()), volmax);
//            //取出每一天的成交量
//            barList.add(Integer.parseInt(stockBeans.get(i).getVolume()));
        }
        topCombinedData = new CombinedData(xVals);
        /**
         * candela必须在line前面
         */
        getTopCandleData();
        getTopLine();


        mChartTop.setData(topCombinedData);


    }

    private void getTopLine() {
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

    /**
     * 折线图属性
     *
     * @param entries 显示的数据
     * @param color   颜色
     * @param label   名称
     * @return
     */
    private LineDataSet generateTopLineDataSet(List<Entry> entries, int color, String label) {
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
//        set.setDrawFilled(true);//是否允许填充
//        set.setFillColor(color);//填充的颜色

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
    private LineData generateTopMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < topStockBeansList.size(); i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return count;
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

    /*设置量表对齐*/
    private void setOffset() {
        float lineLeft = mChartTop.getViewPortHandler().offsetLeft();
        float barLeft = mChartBottom.getViewPortHandler().offsetLeft();
        float lineRight = mChartTop.getViewPortHandler().offsetRight();
        float barRight = mChartBottom.getViewPortHandler().offsetRight();
        float barBottom = mChartBottom.getViewPortHandler().offsetBottom();
        float offsetLeft, offsetRight;
        float transLeft = 0, transRight = 0;
 /*注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (barLeft < lineLeft) {
           /* offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            mChartBottom.setExtraLeftOffset(offsetLeft);*/
            transLeft = lineLeft;
        } else {
            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
            mChartTop.setExtraLeftOffset(offsetLeft);
            transLeft = barLeft;
        }

  /*注：setExtraRight...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (barRight < lineRight) {
          /*  offsetRight = Utils.convertPixelsToDp(lineRight);
            mChartBottom.setExtraRightOffset(offsetRight);*/
            transRight = lineRight;
        } else {
            offsetRight = Utils.convertPixelsToDp(barRight);
            mChartTop.setExtraRightOffset(offsetRight);
            transRight = barRight;
        }
        mChartBottom.setViewPortOffsets(transLeft, 15, transRight, barBottom);
    }
}
