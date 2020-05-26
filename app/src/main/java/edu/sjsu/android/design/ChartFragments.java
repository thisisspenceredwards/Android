package edu.sjsu.android.design;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.Calendar.DAY_OF_WEEK;

public class ChartFragments extends Fragment
{
    private BarChart mBarChart;
    private int x_setting;
    private GetInfoForGraph graphInfo;
    private int time;
    private TimeUnit unit;
    private int bucketByTime;
    private int calendarSubtractionValue;
    private int metric;
    public ChartFragments(int xAxis, GetInfoForGraph graph, int tim, TimeUnit uni, int bucketBy, int amoun, int metri)
    {
        graphInfo = graph;
        x_setting = xAxis;
        time = tim;
        unit = uni;
        bucketByTime = bucketBy;
        calendarSubtractionValue = amoun;
        metric = metri;
    }

    private void setXAxis(int index)
    {

        x_setting = index;
    }

    public void updateXAxis(int index)
    {
        setXAxis(index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view =  inflater.inflate(R.layout.fragment_chart, container, false);
        mBarChart = view.findViewById(R.id.barChart);
        mBarChart.setScaleEnabled(false);
        getChart();
        return view;
    }
    private Calendar getCalendar(int index)
    {
        Calendar calendar = Calendar.getInstance();
        switch(this.x_setting)
        {
            case 0:
                calendar.add(Calendar.HOUR, -index);
                break;
            case 1:
                calendar.add(DAY_OF_WEEK, -index);
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, -index);
                break;
            case 3:
                calendar.add(Calendar.MONTH, -index);
                break;
            default:

        }
        return calendar;
    }

    private DateFormat getProperDateFormatter()
    {
        String format= "";
        switch(this.x_setting) {
            case 0:
                format = "hh aa";
                break;
            case 1:
                format = "EEE";
                break;
            case 2:
                format = "dd";
                break;
            case 3:
                format = "MMM";
                break;
            default:
        }

        return new SimpleDateFormat(format, new Locale("eng", "USA"));

    }
    public void updateGraph()
    {
        List<BarEntry> barEntries = new ArrayList<>();
        List<BarEntry> zeroValues = new ArrayList<>();
        List<TimeAndData> cal = graphInfo.getTimeAndDataArray();
        String[] x = new String[cal.size()];
        DateFormat format = getProperDateFormatter();

        for(int i = 0; i < cal.size(); i++)
        {
            Calendar calendar = getCalendar(i);
            Date then = calendar.getTime();
            x[cal.size()-1-i] = format.format(then);
            TimeAndData timeAndData = cal.get(i);
            float val = (float)(timeAndData.getValue());
            if((int)val == 0) zeroValues.add(new BarEntry(i, (0)));
            else barEntries.add(new BarEntry(i, (val)));
        }
        graphInfo.deleteTimeAndData();
        ArrayList<BarDataSet> barDataSet = new ArrayList<BarDataSet>();
        BarDataSet barSet = new BarDataSet(barEntries, "");
        barSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarDataSet zeroValueSet = new BarDataSet(zeroValues, "");
        barSet.setDrawValues(true);
        zeroValueSet.setDrawValues(false);
        barDataSet.add(barSet); barDataSet.add(zeroValueSet);

        mBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(x));

        mBarChart.getDescription().setEnabled(false);
        mBarChart.getLegend().setEnabled(false);
        BarData barData = new BarData(barDataSet.get(0), barDataSet.get(1));
        barData.setBarWidth(0.9f);
        mBarChart.getAxisLeft().setAxisMinimum(0);
        mBarChart.setVisibility(View.VISIBLE);
        mBarChart.setData(barData);
        mBarChart.setFitBars(true);
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText("Hours");
        Animation animate = AnimationUtils.loadAnimation(graphInfo.getContext(), R.anim.fade_in);
        mBarChart.startAnimation(animate);
    }

    public void getChart() //Add exercise type
    {
        updateXAxis(x_setting);
        graphInfo.retrieveCalorieByTime(this, time, unit, bucketByTime, calendarSubtractionValue, metric);
    }
}
