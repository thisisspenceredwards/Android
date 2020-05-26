package edu.sjsu.android.design;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.fitness.FitnessActivities;

import org.w3c.dom.Text;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class SessionInfo extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Toolbar tb;
    Spinner activity_spinner;
    EditText session_name, session_description;
    TextView session_activity;
    Button submit;
    String name, description, activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_info);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        activity_spinner = findViewById(R.id.activity_spinner);
        activity_spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Type_of_Activity, R.layout.support_simple_spinner_dropdown_item);
        activity_spinner.setAdapter(adapter);

        tb = (Toolbar)findViewById(R.id.toolbar);
        tb.setTitle("Fitness App");
        tb.setBackgroundColor(Color.WHITE);
        setSupportActionBar(tb);

        session_name = (EditText) findViewById(R.id.session_name);
        session_description = (EditText) findViewById(R.id.session_description);
        session_activity = (TextView) findViewById(R.id.session_activity);
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = session_name.getText().toString();
                description = session_description.getText().toString();
                activity = session_activity.getText().toString();
                Intent intent = new Intent(v.getContext(), ActivityLog.class);
                intent.putExtra("name", name);
                intent.putExtra("description", description);
                intent.putExtra("activity", activity);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(position == 0){session_activity.setText(FitnessActivities.WALKING);}
        if(position == 1){session_activity.setText(FitnessActivities.RUNNING_JOGGING);}
        if(position == 2){session_activity.setText(FitnessActivities.RUNNING);}
        if(position == 3){session_activity.setText(FitnessActivities.BIKING);}
        if(position == 4){session_activity.setText(FitnessActivities.HIKING);}
        if(position == 5){session_activity.setText(FitnessActivities.TEAM_SPORTS);}
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        session_activity.setText("No Activity Description");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.ACTIVITYLOG:
                startActivity(new Intent(this, ActivityLog.class));
                return true;

            case R.id.FINDACTIVITY:
                Intent findActivityStart = new Intent(this, FindActivity.class);
                startActivity(findActivityStart);
                return true;

            case R.id.SETTINGS:
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                return true;

            default:

                super.onOptionsItemSelected(item);

        }
        return true;

    }
}