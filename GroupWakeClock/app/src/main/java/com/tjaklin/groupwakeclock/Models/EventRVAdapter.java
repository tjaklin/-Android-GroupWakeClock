package com.tjaklin.groupwakeclock.Models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tjaklin.groupwakeclock.R;
import com.tjaklin.groupwakeclock.Alarm.AlarmRegistrationManager;
import com.tjaklin.groupwakeclock.Event.EventActivity;
import com.tjaklin.groupwakeclock.Util.CustomSQLiteOpenHelper;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.ArrayList;
import java.util.Locale;

public class EventRVAdapter extends RecyclerView.Adapter<EventRVAdapter.GroupVH> {

    private static final String TAG = EventRVAdapter.class.getSimpleName();

    private FBObjectManager fbO;

    private Context context;
    private boolean allowOnHoldEvents;
    private ArrayList<Event> eventList;

    public EventRVAdapter(Context c, boolean isHoldable, ArrayList<Event> objectList) {
        context = c;
        eventList = objectList;
        allowOnHoldEvents = isHoldable;

        fbO = FBObjectManager.getFbObjectManager(context);
    }

    @NonNull
    @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context)
                .inflate(R.layout.item_event, null, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new GroupVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH holder, final int position) {
        holder.tv_name.setText(eventList.get(position).getEventname());

        holder.tv_datetime.setText(getReadableDate(eventList.get(position).getDatetimeInMillis()));

        holder.cl_item.setOnClickListener(v -> {
            Intent launchEventActivityIntent = new Intent(context, EventActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("event", eventList.get(position));
            launchEventActivityIntent.putExtras(bundle);

            context.startActivity(launchEventActivityIntent);
        });

        if (allowOnHoldEvents) {
            holder.cl_item.setOnLongClickListener(v -> {
                Log.d(TAG, "cl_item.clicked!");
                AlertDialog ad = spawnDeleteMembershipDialog(position);
                if (ad != null) {
                    ad.show();
                } else {
                    Log.e(TAG, "Alert creation Failed!");
                }

                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * QOL Methods
     */
    private String getReadableDate(long datetime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(datetime);

        SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        return sf.format(c.getTime());
    }

    private AlertDialog spawnDeleteMembershipDialog(final int position) {

        Event selectedEvent = eventList.get(position);
        String membershipID = selectedEvent.getCurrentUserMembershipID();

        if (membershipID == null) {
            Log.e(TAG, "Couldn't find event membership!");
            return null;
        }

        String title = "Leave event '" + selectedEvent.getEventname() + "'?";

        // Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder .setTitle(title)
                .setNegativeButton("Cancel", (dialog, id) -> Log.d(TAG, "Changes dismissed!"))
                .setPositiveButton("LEAVE", (dialog, which) -> fbO.deleteMembership(membershipID, new FBObjectManager.AsyncListenerString() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart()");
                    }

                    @Override
                    public void onSuccess(String id) {
                        Log.d(TAG, "onSuccess()");
                        Toast.makeText(context, "Group " + selectedEvent.getEventname() + " left!", Toast.LENGTH_SHORT).show();
                        fbO.getMyOwnEvents().remove(selectedEvent);
                        notifyDataSetChanged();
                        if (new CustomSQLiteOpenHelper(context).deleteAlarm(selectedEvent.getDatetimeInMillis(), fbO.getThisUser().getEmail())) {
                            // Disarm a registered AlarmClock
                            AlarmRegistrationManager.getInstance(context).unregisterAlarmClock(fbO.getThisUser().getEmail(), selectedEvent.getDatetimeInMillis(),
                                    selectedEvent.getEventname(), selectedEvent.getEventChatID(), selectedEvent.getCurrentUserMembershipID());

                            Log.d(TAG, "Succesfully deleted alarm from SQLite!");
                        } else {
                            Log.e(TAG, "Failed to delete alarm from SQLite!");
                        }
                    }

                    @Override
                    public void onFailed() {
                        Log.d(TAG, "onFailed()");
                    }
                }));

        // Create the AlertDialog object and return it
        AlertDialog result = builder.create();

        result.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                result.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            }
        });

        return result;
    }

    /**
     * VH
     */
    class GroupVH extends RecyclerView.ViewHolder {

        TextView tv_name, tv_datetime;
        ConstraintLayout cl_item;

        GroupVH(View view ) {
            super(view);
            cl_item = view.findViewById(R.id.cl_item);
            tv_name = view.findViewById(R.id.tv_name);
            tv_datetime = view.findViewById(R.id.tv_datetime);
        }
    }
}
