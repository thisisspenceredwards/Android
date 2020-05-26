package edu.sjsu.android.design;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TimeAndData
{
    private long t;
    private double l;
    public TimeAndData(long time, double value )
    {
        t = time;
        l = value;
    }
    public long getTime()
    {
        return t;
    }
    public double getValue()
    {
        return l;
    }
    public static ArrayList<TimeAndData> sort(ArrayList<TimeAndData> list)
    {
        Comparator<TimeAndData> compare = (TimeAndData o1, TimeAndData o2) -> Long.compare(o1.getTime(), o2.getTime());
        Collections.sort(list, compare);
        return list;
    }
}


class GraphRetrieveExerciseInfo extends AsyncTask<Void, Integer, Void>
{
    private GetInfoForGraph graph;
    private ChartFragments chart;
    private int count;
    private ProgressBar bar;
    private int time;
    private int bucketSize;
    private ArrayList<TimeAndData> list;
    private TimeUnit unit;
    private int bucket;
    private int amount;
    private DataType exercise;
    private DataType aggregate;
    private boolean fail;
    public GraphRetrieveExerciseInfo(GetInfoForGraph grap, ChartFragments cha, ProgressBar ba, int tim, TimeUnit uni, int buck, int amoun, int metric)
    {
        fail = false;
        graph = grap;
        chart = cha;
        count = 0;
        bar = ba;
        time = tim;
        bucketSize = Integer.MAX_VALUE;
        list = new ArrayList<>();
        unit = uni;
        bucket = buck;
        amount = amoun;
        Log.d("metric", Integer.toString(metric));
        if(metric == 0)
        {
           exercise = DataType.TYPE_CALORIES_EXPENDED;
           aggregate = DataType.AGGREGATE_CALORIES_EXPENDED;
        }
        else
        {
            exercise = DataType.TYPE_STEP_COUNT_DELTA;
            aggregate = DataType.AGGREGATE_STEP_COUNT_DELTA;
        }
    }
    private void setFail(boolean val)
    {
        fail = val;
    }
    private boolean getFail()
    {
        return fail;
    }

    private synchronized void incrementCount()
    {
        count++;
    }
    private synchronized int getCount()
    {
        return count;
    }
    private synchronized void setBucketSize(int size) { bucketSize = size;}
    private synchronized int getBucketSize() { return bucketSize;}
    private synchronized void addTimeAndData(TimeAndData td){ list.add(td);}
    private ArrayList<TimeAndData> getTimeAndDataArray() { return list;}

    @Override
    public void onPreExecute()
    {
        Log.d("test", "this is exercise: " + exercise);
        Log.d("test", "this is aggregate " + aggregate);
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(time, -(amount));
        long startTime = cal.getTimeInMillis();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(exercise, aggregate)
                .bucketByTime(bucket, unit)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        Log.d("Session", "Present");
        Fitness.getHistoryClient(graph.getBaseContext(),
                GoogleSignIn.getLastSignedInAccount(graph.getContext()))
                .readData(readRequest)
                .addOnSuccessListener( dataReadResponse ->
                {
                        if (dataReadResponse != null)
                        {
                            List<Bucket> bucket = dataReadResponse.getBuckets();
                            int size = bucket.size();
                            long lt = 0;

                            for (int i = 0; i < size; i++)
                            {

                                DataSet set = bucket.get(i).getDataSet(aggregate);
                                Log.d("Test", set.toString());
                                if (set != null)
                                {

                                    if (!set.getDataPoints().isEmpty()) {
                                        DataPoint point = set.getDataPoints().get(0);
                                        List<Field> field = point.getDataType().getFields();
                                        Value data = point.getValue(field.get(0));
                                        Log.d("Sessions", "4 " + point.toString());
                                        String dataValue = data.toString();
                                        Float calorieCount = Float.parseFloat(dataValue);
                                        long t = point.getEndTime(unit);
                                        addTimeAndData(new TimeAndData(t, calorieCount));
                                        Log.d("Sessions", "6 " + calorieCount);
                                        Log.d("Sessions", "5 " + t);
                                        incrementCount();
                                        lt = t;
                                    }
                                    else
                                    {
                                        incrementCount();
                                        addTimeAndData(new TimeAndData(lt+1, 0));
                                        lt = lt+1;
                                    }
                                }
                                setBucketSize(size);
                            }
                        }
                        else
                        {
                            Log.d("Session", "DataReadResponse is null uhoh");
                            setBucketSize(0);
                        }
                })
                .addOnFailureListener( e ->
                {
                        setFail(true);
                        Log.d("Fail", "Fail");
                });
    }
    //Wait for either 5000 milliseconds or the request for data, whichever is first
    @Override
    public Void doInBackground(Void... params)
    {
        int timeToWait = 5000;
        int max = time;
        Date timer = new Date();

        long ctime = timer.getTime();
        long loopTime = 0;
        while((getCount() < max && loopTime < timeToWait) && !getFail() )
        {
            loopTime= new Date().getTime() - ctime;
            max = getBucketSize();
             //A sub par solution to the async problem.  Busy waiting and such.
        }
        Log.d("stuck", "getFail:" + getFail());
        if(loopTime >= 5000 || getFail())
        {
            max = 24;
            ArrayList<TimeAndData> arr = getTimeAndDataArray();
            int val = 24-arr.size();
            for(int i = 0; i < val; i++)
            {
                incrementCount();
                addTimeAndData(new TimeAndData(0, 0));
            }
        }
        publishProgress(max);
    return null;
    }
    //spinner, when true just exit.
    @Override
    protected void onProgressUpdate(Integer... result)
    {
        if(getCount() >= result[0])
        {
            Animation animation = AnimationUtils.loadAnimation(graph.getContext(), R.anim.fade_out);
            bar.startAnimation(animation);
            bar.setVisibility(View.INVISIBLE);
            bar = null; //does this help with memory leak?
        }
    }
    //draw new graph and hopefully reduce memory leaks by setting to null (?).
    @Override
    protected void onPostExecute(Void result)
    {
        graph.setArray(getTimeAndDataArray());
        graph.sort();
        chart.updateGraph();
        chart = null;
        graph = null;
    }
}