package edu.sjsu.android.design;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.WEEK_OF_YEAR;
import static java.util.Calendar.YEAR;


public class ActivityLog extends AppCompatActivity
{
    Toolbar tb;
    ProgressBar bar;
    TextView date, xLegend, yLegend, breakdown_text;
    Button start_activity, stop_activity;
    long start_time, end_time;
    GoogleSignInAccount account;
    String TAG = "Testing";
    String sessionName, sessionActivity, sessionDescription;
    GetInfoForGraph graph;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static final String DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss";
    public static final String DAY_HOUR_FORMAT = "MMM dd, HH:mm";
    TimeUnit pUnit = TimeUnit.DAYS;
    int pCalendar = DAY_OF_WEEK;
    int pIndex = 0;
    int pBucketByTime = 1;
    int calendarSubtractionValue = 1;
    int pMetric = 0;
    RadioButton clickedTimeRadioButton = null;
    RadioButton clickedMetricRadioButton= null;
    /**
     * Currently using a dummy graph for the graph portion. Still working on recording activity and
     * displaying the activity in graph form.
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.workout);

        tb = (Toolbar)findViewById(R.id.toolbar);
        date = (TextView) findViewById(R.id.dateText);
        breakdown_text = (TextView) findViewById(R.id.breakdownText);
        start_activity = (Button) findViewById(R.id.button);
        stop_activity = (Button) findViewById(R.id.button2);
        recyclerView = (RecyclerView) findViewById(R.id.session_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        bar = findViewById(R.id.progressBar);
        graph = new GetInfoForGraph(getApplicationContext(), getBaseContext(), bar);
        xLegend = findViewById(R.id.xAxisLegend);
        yLegend = findViewById(R.id.yAxisLegend);
                requestPermissions();
        //********Graph Initialization and associated UI elements ****////
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        tb.setTitle("StayFit");
        tb.setBackgroundColor(Color.WHITE);
        setSupportActionBar(tb);
        stop_activity.setVisibility(View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Timer dateTimer = new Timer("timeUpdate");
        dateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateDate();
            }
        }, 0, 1000); //every second
        date.setText(sdf.format(new Date()));

        start_activity.setOnClickListener(v -> {
                //check for permission acitivy_recognition
                if(ContextCompat.checkSelfPermission(ActivityLog.this,
                        Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(
                            ActivityLog.this,
                            new String[] { Manifest.permission.ACTIVITY_RECOGNITION},
                            1);
                }
                if(ContextCompat.checkSelfPermission(ActivityLog.this,
                        Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startASession();
                }
                Intent intent = new Intent(ActivityLog.this, SessionInfo.class);
                startActivityForResult(intent, 3100);
                Toast.makeText(v.getContext(), "Starting Session",
                        Toast.LENGTH_SHORT).show(); //Will allow users to enter name and description soon.
        });

        stop_activity.setOnClickListener( v -> {
                Toast.makeText(v.getContext(),
                        "Ending Session and inserting it into history",
                        Toast.LENGTH_SHORT).show();
                stopSession();
        });
        setCalendar(DAY_OF_WEEK);
        setTimeUnit(TimeUnit.HOURS);
        setBucketByTime(1);
        setCalendarSubtractionValue(1);
        setIndex(0);
        setMetric(0);
        setFragment(getIndex(), getTimeUnit(), getCalendar(), getBucketByTime(), getCalendarSubtractionValue(), getMetric());
        xLegend.setText(R.string.hours);
    }

    /**
     * This function is called at the start of the activity to check whether the user has
     * the required permissions.
     */

    private void requestPermissions() {

        if(ContextCompat.checkSelfPermission(ActivityLog.this, Manifest.permission.ACTIVITY_RECOGNITION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivityLog.this, new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, 3002);
        }
        if(ContextCompat.checkSelfPermission(ActivityLog.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivityLog.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 3002);
        }

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, 3001, account, fitnessOptions); }
        else{
            readSessions("Day");
        }
    }

    /**
     * This function reads any sessions the user created within the past 24 hours
     * and displays it at the bottom.
     */
    private SessionReadRequest buildSessionReadRequest(String view)
    {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        if(view.equals("Day")) {
            cal.set(Calendar.DATE, (cal.get(Calendar.DATE)-1));
        }
        else if(view.equals("Month")) {
            cal.set(cal.get(YEAR), cal.get(Calendar.MONTH), 1,
                    0,0 ,0);
        }
        else if(view.equals("Hour"))
        {
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }
        else
        {
            cal.set(cal.get(YEAR), Calendar.JANUARY, 1,
                    0,0 ,0);
        }
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        return new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .build();

    }
    /*****
    //Split this up into two methods because graph uses the above portion//
    *****/
    private void readSessions(String view) {

        SessionReadRequest readRequest = buildSessionReadRequest(view);
        Fitness.getSessionsClient(this, account)
                .readSession(readRequest)
                .addOnSuccessListener( sessionReadResponse ->
                {
                        // Get a list of the sessions that match the criteria to check the result.
                        final List<Session> sessions = sessionReadResponse.getSessions();
                        mAdapter = new SessionAdapter(sessions);
                        recyclerView.setAdapter(mAdapter);

                })
                .addOnFailureListener(e -> Log.i(TAG, "Failed to read session"));

    }

    /**
     * This function sets up Step count listener to record steps taken.
     * The recordingClient is used to record data in the background efficiently.
     */
    private void setUpDataCollection(){
        Fitness.getRecordingClient(this, account)
                .subscribe(DataType.TYPE_DISTANCE_DELTA);

        Fitness.getRecordingClient(this, account)
                .subscribe(DataType.TYPE_LOCATION_SAMPLE);

        Fitness.getRecordingClient(this, account)
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA);

    }


    private class RetrieveSessionsTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            readSessions(params[0]);
            return null;
        }
    }

    /**
     * This function simply records the start time of the session.
     */
    private void startASession(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        start_time = cal.getTimeInMillis();
    }

    /**
     * This function changes "Start Activity" into "Stop activity" and vice versa when the user clicks
     * the button. It also updates the sessions shown at the bottom.
     */
    private void updateUI() {
        if (stop_activity.getVisibility() == View.GONE)
        {
            start_activity.setVisibility(View.GONE);
            stop_activity.setVisibility(View.VISIBLE);
        } else
        {
            start_activity.setVisibility(View.VISIBLE);
            stop_activity.setVisibility(View.GONE);
            new RetrieveSessionsTask().execute("Day");
        }
    }

    /**
     * This function records the end time of the session and calls the insertSession function.
     */
    private void stopSession(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        end_time = cal.getTimeInMillis();
        insertSessionIntoHistory();
    }

    /**
     * This function makes a Session object and inserts it into the user history.
     */
    private void insertSessionIntoHistory(){
        Session test = new Session.Builder()
                .setName(sessionName)
                .setDescription(sessionDescription)
                .setActivity(sessionActivity)
                .setStartTime(start_time, TimeUnit.MILLISECONDS)
                .setEndTime(end_time, TimeUnit.MILLISECONDS)
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(test)
                .build();
        // At this point, the session has been inserted and can be read.
        Fitness.getSessionsClient(this, account)
                .insertSession(insertRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        dataCollectionRemoval();
                        updateUI();
                        setFragment(getIndex(), getTimeUnit(), getCalendar(), getBucketByTime(), getCalendarSubtractionValue(), getMetric());
                    }
                })
                .addOnFailureListener((e) -> {
                        Toast.makeText(getApplicationContext(), "Session not submitted. Please retry again",
                                Toast.LENGTH_LONG).show();
                    })
                .addOnFailureListener(e ->
                        Log.i("Not submitted", "There was a problem inserting the session: " + e.getLocalizedMessage()));
    }

    /**
     * Stop recording for steps and remove the listener until the user starts a new physical activity.
     */
    private void dataCollectionRemoval() {
        Fitness.getRecordingClient(this, account)
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA);

        Fitness.getRecordingClient(this, account)
                .unsubscribe(DataType.TYPE_DISTANCE_DELTA);

        Fitness.getRecordingClient(this, account)
                .unsubscribe(DataType.TYPE_LOCATION_SAMPLE);
    }

    /**
     * This function is used only for handling permissions. This function
     * is likely to be called also when user enters information about their session.
     * @param requestCode - A code retrieved from a started activity
     * @param resultCode - The status of the other activity
     * @param data - Any data included along with the result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 3001) {
                Log.d("Permissions", "Permissions have been granted now");
                new RetrieveSessionsTask().execute("Day");
            }
            else if(requestCode == 3002)
            {
                requestPermissions();
            }
            else if(requestCode == 3100)
            {
                Bundle myInput = data.getExtras();
                sessionName = myInput.getString("name");
                sessionActivity = myInput.getString("activity").toLowerCase();
                sessionDescription = myInput.getString("description");
                Log.d(TAG, sessionName + "," + sessionActivity + "," + sessionDescription);
                setUpDataCollection();
                startASession();
                updateUI();
            }
            else
            {
                new RetrieveSessionsTask().execute("Day");
            }
        }
    }

    /**
     * This function updates the date displayed on the activity.
     */
    public void updateDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        date.setText(sdf.format(new Date()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.findItem(R.id.GRAPHSETTINGS).setVisible(true);

        return true;
    }
    private void setCalendar(int cal)
    {
        pCalendar = cal;
    }
    private int getCalendar()
    {
        return pCalendar;
    }
    private void setIndex(int index)
    {
        pIndex = index;
    }
    private int getIndex()
    {
        return pIndex;
    }
    private void setTimeUnit(TimeUnit un)
    {
        pUnit = un;
    }
    private TimeUnit getTimeUnit()
    {
        return pUnit;
    }
    private void setBucketByTime(int t)
    {
        pBucketByTime = t;
    }
    private int getBucketByTime()
    {
        return pBucketByTime;
    }

    private void setCalendarSubtractionValue(int amoun)
    {
         calendarSubtractionValue= amoun;
    }

    private int getCalendarSubtractionValue()
    {
        return calendarSubtractionValue;
    }
    private void setMetric(int p)
    {
        pMetric = p;
    }
    private int getMetric()
    {
        return pMetric;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.ACTIVITYLOG:
                Toast toast = Toast.makeText(this,
                        "You are already on this page", Toast.LENGTH_SHORT);
                toast.show();
                return true;

            case R.id.FINDACTIVITY:
                Intent findActivityStart = new Intent(this, FindActivity.class);
                startActivity(findActivityStart);
                return true;

            case R.id.SETTINGS:
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                return true;

            case R.id.GRAPHSETTINGS:

                Dialog frag = new Dialog(this);
                frag.setContentView(R.layout.activity_settings);
                final RadioGroup time = frag.findViewById(R.id.timeRadioButtons);
                switch(getIndex())
                {
                    case 1:
                        time.check(R.id.sevenDays);
                        break;
                    case 2:
                        time.check(R.id.month);
                        break;
                    case 3:
                        time.check(R.id.year);
                        break;
                    default:
                        time.check(R.id.day);
                        break;
                }
                final RadioGroup metric = frag.findViewById(R.id.metricRadioButtons);
                switch(getMetric())
                {
                    case 1:
                        metric.check(R.id.stepsRB);
                        break;
                    default:
                        metric.check(R.id.caloriesRB);
                        break;
                }

                //metric.check(R.id.);




                time.setOnCheckedChangeListener( (group, checkedId) ->
                {
                        RadioButton rb = time.findViewById(checkedId);

                        clickedTimeRadioButton = rb;
                        int index = group.indexOfChild(rb);
                        Log.d("index", "index:" + index);
                        setIndex(index);
                        switch(index)
                        {
                            case 1:
                                setCalendar(DAY_OF_WEEK);
                                setTimeUnit(TimeUnit.DAYS);
                                setBucketByTime(1);
                                setCalendarSubtractionValue(7);
                                xLegend.setText(R.string.days);
                                new RetrieveSessionsTask().execute("Day");
                                breakdown_text.setText("Activity from the past Week:");
                                break;
                            case 2:
                                setCalendar(WEEK_OF_YEAR);
                                setTimeUnit(TimeUnit.DAYS);
                                setBucketByTime(1);
                                setCalendarSubtractionValue(4);
                                xLegend.setText(R.string.days);
                                new RetrieveSessionsTask().execute("Month");
                                breakdown_text.setText("Activity since the start of this month:");
                                break;
                            case 3:
                                setCalendar(MONTH);
                                setTimeUnit(TimeUnit.DAYS);
                                setBucketByTime(30);
                                setCalendarSubtractionValue(12);
                                xLegend.setText(R.string.months);
                                new RetrieveSessionsTask().execute("Year");
                                breakdown_text.setText("Activity since the start of this year:");
                                break;
                            default:
                                setCalendar(DAY_OF_WEEK);
                                setTimeUnit(TimeUnit.HOURS);
                                setBucketByTime(1);
                                setCalendarSubtractionValue(1);
                                xLegend.setText(R.string.hours);
                                new RetrieveSessionsTask().execute("Day");
                                breakdown_text.setText("Activity from the past 24 hours:");
                                break;
                        }
                    });
                metric.setOnCheckedChangeListener( (group, checkedId) ->
                {
                        RadioButton rb = metric.findViewById(checkedId);
                        int index = group.indexOfChild(rb);
                        setMetric(index);
                        switch(index) {
                            case 1:
                                setMetric(1);
                                yLegend.setText(R.string.steps);
                                break;
                            default:
                                setMetric(0);
                                yLegend.setText(R.string.calories);
                                break;
                        }
                });
                frag.setOnDismissListener( dialog -> setFragment(getIndex(), getTimeUnit(), getCalendar(), getBucketByTime(), getCalendarSubtractionValue(), getMetric()));
                frag.show();
                return true;
            default:
                super.onOptionsItemSelected(item);

        }
        return true;
    }

    private void setFragment(int x_axis, TimeUnit unit, int calendar, int bucketByTime, int amount, int metric)
    {
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            ChartFragments chartFragment = new ChartFragments(x_axis, graph, calendar, unit, bucketByTime, amount, metric);
            Bundle bundle = new Bundle();
            chartFragment.setArguments(bundle);
            tr.replace(R.id.fragment_container1, chartFragment);
            tr.commit();
    }

    @Override
    protected void onDestroy()
    {
        dataCollectionRemoval();
        super.onDestroy();
    }
}
