package com.tjaklin.groupwakeclock.Models;

import android.icu.util.Calendar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tjaklin.groupwakeclock.R;

import java.util.ArrayList;

public class AlarmSQLiteRVAdapter extends RecyclerView.Adapter<AlarmSQLiteRVAdapter.AlarmSQLiteVH> {

    private static final String TAG = AlarmSQLiteRVAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_INACTIVE = 0;
    private static final int VIEW_TYPE_ACTIVE = 1;

    private static final String COLOR_ACTIVE = "#FF9100";
    private static final String COLOR_INACTIVE = "#95111111";

    ArrayList<AlarmSQLite> alarms;

    public AlarmSQLiteRVAdapter(ArrayList<AlarmSQLite> objectList) {
        alarms = objectList;
    }

    @NonNull
    @Override
    public AlarmSQLiteRVAdapter.AlarmSQLiteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = null;

        if (viewType == VIEW_TYPE_ACTIVE) {
            layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_active, null, true);
        } else {
            layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_inactive, null, true);
        }

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new AlarmSQLiteVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmSQLiteRVAdapter.AlarmSQLiteVH holder, int position) {

        holder.tv_datetime.setText(alarms.get(position).getDatetimeFormatted());
        holder.tv_groupname.setText(alarms.get(position).getGroupname());
        holder.tv_type.setText(alarms.get(position).getType());
        holder.tv_complexity.setText(String.valueOf(alarms.get(position).getComplexity()));
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isAlarmActive(alarms.get(position).getDatetime()) ? VIEW_TYPE_ACTIVE : VIEW_TYPE_INACTIVE;
    }

    public boolean isAlarmActive(long aTime) {

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(aTime);

        if (alarmTime.before(Calendar.getInstance())) {
            Log.d(TAG, "[VH] alarm already passed!");
            return false;
        } else {
            Log.d(TAG, "[VH] alarm has not yet passed!");
            return true;
        }
    }

    class AlarmSQLiteVH extends RecyclerView.ViewHolder {

        TextView tv_datetime, tv_groupname, tv_type, tv_complexity;

        AlarmSQLiteVH(View view ) {
            super(view);
            tv_datetime = view.findViewById(R.id.tv_datetime);
            tv_groupname = view.findViewById(R.id.tv_groupname);
            tv_type = view.findViewById(R.id.tv_type);
            tv_complexity = view.findViewById(R.id.tv_complexity);

            if (tv_datetime == null || tv_groupname == null || tv_type == null || tv_complexity == null) {
                Log.e(TAG, "some tv's are null!");
            }
        }
    }
}
