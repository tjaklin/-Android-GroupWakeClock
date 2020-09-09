package com.tjaklin.groupwakeclock.Models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.ArrayList;

public class UserRVAdapter extends RecyclerView.Adapter<UserRVAdapter.UserVH> {

    private static final String TAG = UserRVAdapter.class.getSimpleName();

    private FBObjectManager fbO;

    private Context context;
    private ArrayList<User> users;

    public UserRVAdapter(Context c, ArrayList<User> f) {
        context = c;
        users = f;

        fbO = FBObjectManager.getFbObjectManager(context);
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, null, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new UserVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH holder, final int position) {
        holder.tv_username.setText(users.get(position).getEmail());
        holder.cl_item.setOnLongClickListener(v -> {
            Log.d(TAG, "cl_item.clicked!");
            AlertDialog ad = spawnDeleteFriendshipDialog(position);
            if (ad != null) {
                ad.show();
            } else {
                Log.e(TAG, "Alert creation Failed!");
            }

            return false;
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * QOL Methods
     */
    private AlertDialog spawnDeleteFriendshipDialog(final int position) {

        User selectedUser = users.get(position);
        Friendship friendshipInQuestion = null;
        for (Friendship f : fbO.getMyOwnFriendships()) {
            if (f.getUserID2().equals(selectedUser.getUserID())) {
                Log.d(TAG, "Selected user found!");
                friendshipInQuestion = f;
            }
        }

        if (friendshipInQuestion == null) {
            Log.e(TAG, "Couldn't find that friendship!");
            return null;
        }

        Friendship finalFriendshipInQuestion = friendshipInQuestion;
        String title = "Remove '" + selectedUser.getEmail() + "' from friends?";

        // Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder .setTitle(title)
                .setNegativeButton("Cancel", (dialog, id) -> Log.d(TAG, "Changes dismissed!"))
                .setPositiveButton("REMOVE", (dialog, which) -> fbO.deleteFriendship(finalFriendshipInQuestion, new FBObjectManager.AsyncListenerFriendship() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart()");
                    }

                    @Override
                    public void onSuccess(Friendship friendship) {
                        Log.d(TAG, "onSuccess()");
                        Toast.makeText(context, "Friendship with " + selectedUser.getEmail() + " ended!", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
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
     *  ViewHolder
     */
    class UserVH extends RecyclerView.ViewHolder {

        TextView tv_username;
        ConstraintLayout cl_item;

        UserVH(View view ) {
            super(view);
            tv_username = view.findViewById(R.id.tv_username);
            cl_item = view.findViewById(R.id.cl_item);
        }
    }
}
