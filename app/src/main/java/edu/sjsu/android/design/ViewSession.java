package edu.sjsu.android.design;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class ViewSession extends AppCompatActivity {

    Toolbar tb;
    TextView SessName, SessDesc, SessAct;
    TextView sess_start, sess_end, sess_length;
    TextView calories_burned, distance_travelled, step_counter;
    public static final String DATE_FORMAT = "E, dd MMM HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_session);
        Bundle bund = this.getIntent().getExtras();
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        tb = (Toolbar)findViewById(R.id.toolbar);
        SessName = (TextView) findViewById(R.id.sess_name);
        SessDesc = (TextView) findViewById(R.id.sess_desc);
        SessAct = (TextView) findViewById(R.id.sess_act);
        sess_start = (TextView) findViewById(R.id.start_time);
        sess_end = (TextView) findViewById(R.id.end_time);
        sess_length = (TextView) findViewById(R.id.session_length);
        calories_burned = (TextView) findViewById(R.id.cal_burned);
        distance_travelled = (TextView) findViewById(R.id.distance);
        step_counter = (TextView) findViewById(R.id.step_count_value);

        tb.setTitle("Fitness App");
        tb.setBackgroundColor(Color.WHITE);
        setSupportActionBar(tb);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        Session ses = (Session) bund.get("session");
        SessName.setText(ses.getName());
        SessDesc.setText(ses.getDescription());
        SessAct.setText(ses.getActivity());
        Date start = new Date();
        start.setTime(ses.getStartTime(TimeUnit.MILLISECONDS));
        sess_start.setText(sdf.format(start));
        Date end = new Date();
        end.setTime(ses.getEndTime(TimeUnit.MILLISECONDS));
        sess_end.setText(sdf.format(end));
        int diff = (int) (ses.getEndTime(TimeUnit.SECONDS) - ses.getStartTime(TimeUnit.SECONDS));
        int hour = diff / 3600;
        diff = diff - hour * 3600;
        int minute = diff / 60;
        diff = diff - minute * 60;
        int second = diff;
        if(hour > 0)
        {
            sess_length.setText(""+hour+" h, "+minute+" m, "+second+" s");
        }
        else if(minute > 0){
            sess_length.setText(""+minute+" m, "+second+" s");
        }
        else{
            sess_length.setText(""+second+" s");
        }

        new RetrieveCalorie().execute(ses);
        new RetrieveDistance().execute(ses);
        new RetrieveStep().execute(ses);
    }

    public class RetrieveCalorie extends AsyncTask<Session, Void, Void> {

        @Override
        public Void doInBackground(Session... params){
            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .setTimeRange(params[0].getStartTime(TimeUnit.MILLISECONDS),
                            params[0].getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .build();

            Fitness.getHistoryClient(getBaseContext(),
                    GoogleSignIn.getLastSignedInAccount(getBaseContext()))
                    .readData(readRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            double sum = 0.0;
                            if(!dataReadResponse.getDataSets().get(0).isEmpty()) {
                                DataSet data = dataReadResponse.getDataSets().get(0);
                                for(int i = 0; i < data.getDataPoints().size(); i++)
                                {
                                    DataPoint calorieData = data.getDataPoints().get(0);
                                    Field calorie = calorieData.getDataType().getFields().get(0);
                                    String value = calorieData.getValue(calorie).toString();
                                    double convertedValue = Double.parseDouble(value);
                                    sum += convertedValue;
                                }
                                calories_burned.setText(""+Math.round(sum));
                            }
                        }
                    });
            return null;
        }
    }

    public class RetrieveDistance extends AsyncTask<Session, Void, Void> {

        @Override
        public Void doInBackground(Session... params){

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .setTimeRange(params[0].getStartTime(TimeUnit.MILLISECONDS),
                            params[0].getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .build();

            Fitness.getHistoryClient(getBaseContext(),
                    GoogleSignIn.getLastSignedInAccount(getBaseContext()))
                    .readData(readRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            if(!dataReadResponse.getDataSets().get(0).isEmpty())
                            {
                                DataSet data = dataReadResponse.getDataSets().get(0);
                                DataPoint distanceData = data.getDataPoints().get(0);
                                Field distance = distanceData.getDataType().getFields().get(0);
                                String value = distanceData.getValue(distance).toString();
                                double convertedValue = Double.parseDouble(value);
                                distance_travelled.setText(String.format("%.2f mi", convertedValue));
                            }
                            else{
                                distance_travelled.setText(String.format("%.2f mi", 0.00));
                            }
                        }
                    });
            return null;
        }
    }

    public class RetrieveStep extends AsyncTask<Session, Void, Void> {

        @Override
        public Void doInBackground(Session... params){
            DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setType(DataSource.TYPE_DERIVED)
                    .setStreamName("estimated_steps")
                    .setAppPackageName("com.google.android.gms")
                    .build();


            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .setTimeRange(params[0].getStartTime(TimeUnit.MILLISECONDS),
                            params[0].getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                    .read(ESTIMATED_STEP_DELTAS)
                    .build();

            Fitness.getHistoryClient(getBaseContext(),
                    GoogleSignIn.getLastSignedInAccount(getBaseContext()))
                    .readData(readRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            Log.d("Steps" , dataReadResponse.getDataSets().toString());
                            double sum = 0.0;
                            if(!dataReadResponse.getDataSets().get(0).isEmpty()) {
                                DataSet data = dataReadResponse.getDataSets().get(0);
                                for(int i = 0; i < data.getDataPoints().size(); i++)
                                {
                                    DataPoint stepData = data.getDataPoints().get(i);
                                    Field step = stepData.getDataType().getFields().get(0);
                                    String value = stepData.getValue(step).toString();
                                    double convertedValue = Double.parseDouble(value);
                                    sum += convertedValue;
                                }
                            }
                            step_counter.setText(""+Math.round(sum));
                        }
                    });
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.findItem(R.id.GRAPHSETTINGS).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.ACTIVITYLOG:
                Intent findActivityLog = new Intent(this, ActivityLog.class);
                startActivity(findActivityLog);
                return true;

            case R.id.FINDACTIVITY:
                Intent findActivityStart = new Intent(this, FindActivity.class);
                startActivity(findActivityStart);
                return true;

            case R.id.SETTINGS:
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                return true;

            default:
                super.onOptionsItemSelected(item);
        }
        return true;

    }
}
