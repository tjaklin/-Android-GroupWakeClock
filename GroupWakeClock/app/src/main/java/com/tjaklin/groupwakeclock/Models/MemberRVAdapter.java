package com.tjaklin.groupwakeclock.Models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tjaklin.groupwakeclock.R;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.ArrayList;

public class MemberRVAdapter extends RecyclerView.Adapter<MemberRVAdapter.MemberVH> {

    private static final String TAG = MemberRVAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Member> members;

    private boolean isAdmin;
    private boolean showIfAwake;

    public MemberRVAdapter(Context c, boolean showResponseStatus, ArrayList<Member> u) {
        context = c;
        members = u;

        // This flag controls whether to display user's "isAwake" status or not.
        // We show this only when an Event has passed.
        showIfAwake = showResponseStatus;

        Log.d(TAG, "received ArrayList<>.users.size = " + members.size());
    }

    public void setIsUserAdmin(boolean flag) {
        isAdmin = flag;
    }

    @NonNull
    @Override
    public MemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context)
                .inflate(R.layout.item_member, null, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new MemberVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberVH holder, final int position) {

        holder.tv_name.setText(members.get(position).getEmail());

        if (holder.cl_item != null) {
            holder.cl_item.setOnClickListener(v -> {
                Log.d(TAG, "cl_item.clicked!");
                AlertDialog ad = spawnMemberInfoDialog(position);
                ad.show();

            });
        } else {
            Log.e(TAG, "[onBindViewHolder] cl_item == NULL!");
        }

        if (showIfAwake) {
            if (members.get(position).isAwake()) {
                holder.iv_status.setImageResource(R.drawable.ic_member_awake_24dp);
            } else {
                holder.iv_status.setImageResource(R.drawable.ic_member_asleep_24px);
            }
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public String getAwakeToSleepingRatio() {
        Log.e(TAG, "[[[getAwakeToSleepingRatio]]]");
        int membersAwakeCounter = 0;

        for (int i = 0; i < members.size(); i++)
            if (members.get(i).isAwake()) {
                membersAwakeCounter++;
            }

        return membersAwakeCounter + " / " + members.size() + " are awake!";
    }

    /**
     * Quality of Life methods
     */
    public void replaceData(ArrayList<Member> u) {
        members.clear();
        members = u;
    }

    private AlertDialog spawnMemberInfoDialog(final int userPos) {

        String email = members.get(userPos).getEmail();

        final AlarmTemplate alarmTemplate = new AlarmTemplate("math", 0);

        if (members.get(userPos).getAlarmTemplate() != null) {
            AlarmTemplate tempAT = members.get(userPos).getAlarmTemplate();
            alarmTemplate.setType(tempAT.getType());
            alarmTemplate.setComplexity(tempAT.getComplexity());
        } else {
            Log.e(TAG, "Cant find AlarmTemplate!");
        }

        // Possible Type and Complexity values:
        final String[] types = {"math", "image"};
        final String[] complexities = {"0", "1", "2", "3", "4", "5"};

        // User's initial AlarmTemplate values:
        int initialTypePos = -1;
        final int initialComplexityPos = alarmTemplate.getComplexity();


        Log.d(TAG, "[0] alarm.getType()= " + alarmTemplate.getType());
        Log.d(TAG, "[0] alarm.getComplexity()= " + alarmTemplate.getComplexity());

        final String currentType = alarmTemplate.getType();
        for (char i = 0; i < types.length; i++)
            if (currentType.equals(types[i]))
                initialTypePos = i;

        Log.d(TAG, "[1] initialTypePos= " + initialTypePos);
        Log.d(TAG, "[1] initialComplexityPos= " + initialComplexityPos);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View layoutView = LayoutInflater.from(context).inflate(R.layout.dialog_member, null);

        // Here we use Spinners to let the user choose between different types and complexities
        // to create an AlarmTemplate.

        // type
        final Spinner typeSpinner = layoutView.findViewById(R.id.spnr_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelection(initialTypePos);
        typeSpinner.setEnabled(isAdmin);

        // complexity
        Spinner complexitySpinner = layoutView.findViewById(R.id.spnr_complexity);
        ArrayAdapter<String> complexityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, complexities);
        complexityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complexitySpinner.setAdapter(complexityAdapter);
        complexitySpinner.setSelection(initialComplexityPos);
        complexitySpinner.setEnabled(isAdmin);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "typeSpinner.onItemSelected!");

                alarmTemplate.setType((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "typeSpinner.onNothingSelected!");
            }
        });

        complexitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "complexitySpinner.onItemSelected!");

                alarmTemplate.setComplexity(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "complexitySpinner.onNothingSelected!");
            }
        });

        TextView tv_username = layoutView.findViewById(R.id.tv_username);
        tv_username.setText(email);

        //Determine if user is a Friend. Display a command to add / remove from friends based on what you had determined.
        ImageButton ib_memberInfo = layoutView.findViewById(R.id.ib_memberInfo);

        FBObjectManager fbO = FBObjectManager.getFbObjectManager(context);

        User clickedUser = new User(members.get(userPos).getUserID(), email);

        for (int i = 0; i < fbO.getMyOwnFriends().size(); i++) {
            if (fbO.getMyOwnFriends().get(i).equals(clickedUser)) {
                // They are already friends! Display the RemoveFriend option:
                clickedUser.isFriendsWithClient(true);
                break;
            }
        }

        if (clickedUser.isFriendsWithClient())
            ib_memberInfo.setImageResource(R.drawable.ic_friend_remove_24px);

        ib_memberInfo.setOnClickListener(v -> {
            clickedUser.isFriendsWithClient(!clickedUser.isFriendsWithClient());
            changeFriendshipStatus(ib_memberInfo, clickedUser.isFriendsWithClient(), fbO.getThisUser().getUserID(), clickedUser.getUserID());
        });

        builder.setView(layoutView);

        if (isAdmin) {
            builder.setPositiveButton("Accept", (dialog, id) -> {
                members.get(userPos).setAlarmTemplate(alarmTemplate);
                Membership thisMembership = new Membership(members.get(userPos).getMembershipID(), alarmTemplate, false);

                FBObjectManager.getFbObjectManager(context).updateMembership(thisMembership, new FBObjectManager.AsyncListenerString() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart()!");
                    }

                    @Override
                    public void onSuccess(String id) {
                        Log.d(TAG, "onSuccess()!");
                        Log.d(TAG, "Membership " + id + " update finished succesfully!");
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                notifyDataSetChanged();
                Log.d(TAG, "Changes saved!");
            });
        }

        return builder.create();
    }

    /**
     * FRIENDSHIP STATUS MANIPULATOR
     */
    private void removeFromFriends(ImageButton ib, String thisUserID, String friendUserID) {

        FBObjectManager fbO = FBObjectManager.getFbObjectManager(context);

        Friendship friendshipToRemove = new Friendship(thisUserID, friendUserID);
        fbO.deleteFriendship(friendshipToRemove, new FBObjectManager.AsyncListenerFriendship() {
            @Override
            public void onStart() {
                Log.d(TAG, "[deleteFriendship] onStart");
            }

            @Override
            public void onSuccess(Friendship friendship) {
                Log.d(TAG, "[deleteFriendship] onSuccess");
                ib.setImageResource(R.drawable.ic_friend_add_24dp);
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[deleteFriendship] onFailed");
            }
        });
    }

    private void addToFriends(ImageButton ib, String thisUserID, String friendUserID) {

        FBObjectManager fbO = FBObjectManager.getFbObjectManager(context);

        Friendship friendshipToAdd = new Friendship(thisUserID, friendUserID);
        fbO.uploadFriendship(friendshipToAdd, new FBObjectManager.AsyncListenerFriendship() {
            @Override
            public void onStart() {
                Log.d(TAG, "[uploadFriendship] onStart");
            }

            @Override
            public void onSuccess(Friendship friendship) {
                Log.d(TAG, "[uploadFriendship] onSuccess");
                ib.setImageResource(R.drawable.ic_friend_remove_24px);
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[uploadFriendship] onFailed");
            }
        });
    }

    private void changeFriendshipStatus(ImageButton ib, boolean addAsFriend, String thisUserID, String friendUserID) {
        if (!addAsFriend) {
            removeFromFriends(ib, thisUserID, friendUserID);
        } else {
            addToFriends(ib, thisUserID, friendUserID);
        }
    }

    /**
     * ViewHolder
     */
    class MemberVH extends RecyclerView.ViewHolder {

        TextView tv_name;
        ConstraintLayout cl_item;
        ImageView iv_status;

        MemberVH(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            cl_item = view.findViewById(R.id.cl_item);
            iv_status = view.findViewById(R.id.iv_status);
        }
    }
}