package edu.sjsu.android.design;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.fitness.data.Session;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    List<Session> sessions;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView sessionName;
        public TextView sessionDesc;
        public TextView sessionTimeElapse;
        public TextView sessionActivity;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            sessionName = (TextView) v.findViewById(R.id.row_name);
            sessionDesc = (TextView) v.findViewById(R.id.row_desc);
            sessionTimeElapse = (TextView) v.findViewById(R.id.row_time);
            sessionActivity = (TextView) v.findViewById(R.id.row_activity);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SessionAdapter(List<Session> myDataset) {
        sessions = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SessionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.session_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Session ses = sessions.get(position);
        holder.sessionName.setText(ses.getName());
        holder.sessionName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewSession.class);
                intent.putExtra("session", sessions.get(position));
                v.getContext().startActivity(intent);
            }
        });
        holder.sessionDesc.setText(ses.getDescription());
        holder.sessionDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewSession.class);
                intent.putExtra("session", sessions.get(position));
                v.getContext().startActivity(intent);
            }
        });
        holder.sessionTimeElapse.setText(""+(ses.getEndTime(TimeUnit.MINUTES) -
                ses.getStartTime(TimeUnit.MINUTES))+" minutes");
        holder.sessionActivity.setText(ses.getActivity());
    }

    //Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sessions.size();
    }
}
