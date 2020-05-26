package edu.sjsu.android.design;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetInfoForGraph {
    private Context context;
    private Context baseContext;
    private ProgressBar bar;
    private ArrayList<TimeAndData> timeAndData;

    public GetInfoForGraph(Context cont, Context bContext, ProgressBar ba) {
        context = cont;
        baseContext = bContext;
        bar = ba;
        timeAndData = new ArrayList<>();
    }

    public List<TimeAndData> getTimeAndDataArray() { return timeAndData; }
    public void deleteTimeAndData()
    {
        timeAndData = null;
        timeAndData = new ArrayList<>();
    }

    public Context getContext() {
        return context;
    }

    public void sort() {
        TimeAndData.sort(timeAndData);
    }

    public void retrieveCalorieByTime(ChartFragments chart, int time, TimeUnit unit, int bucket, int amount, int metric) {
        GraphRetrieveExerciseInfo retrieveCalories = new GraphRetrieveExerciseInfo(this, chart, bar, time, unit, bucket, amount, metric);
        bar.setMax(0);
        resetProgress();
        makeProgressBarVisible();
        retrieveCalories.execute();
    }

    private void resetProgress() {
        bar.setProgress(0);
    }

    private void makeProgressBarVisible() {
        bar.setVisibility(View.VISIBLE);
    }

    public Context getBaseContext() {
        return baseContext;
    }
    public void setArray(ArrayList<TimeAndData> list)
    {
        timeAndData = list;
    }
}
