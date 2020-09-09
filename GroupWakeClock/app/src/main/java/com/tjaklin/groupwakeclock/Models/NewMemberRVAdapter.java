package com.tjaklin.groupwakeclock.Models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tjaklin.groupwakeclock.R;

import java.util.ArrayList;

public class NewMemberRVAdapter extends RecyclerView.Adapter<NewMemberRVAdapter.NewMemberVH> {

    // NewMemberRVAdapter is not just a copy of MemberRVAdapter.
    // It is used for holding potential new event participants in AddNewMembersActivity.

    // MemberRVAdapter, on the other hand, holds a list of current event participants
    // in EventActivity.

    private static final String TAG = NewMemberRVAdapter.class.getSimpleName();

    Context context;
    ArrayList<Member> members;

    public NewMemberRVAdapter(Context c, ArrayList<Member> u) {
        context = c;
        members = u;

        Log.d(TAG, "received ArrayList<>.users.size = " + members.size());
    }


    @NonNull
    @Override
    public NewMemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(context)
                .inflate(R.layout.item_member_selectable, null, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new NewMemberVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewMemberVH holder, final int position) {

        holder.tv_name.setText(members.get(position).getEmail());

        if (holder.cl_item != null) {
            holder.cl_item.setOnClickListener(v -> {
                Log.d(TAG, "cl_item.clicked!");

                holder.isChecked = !holder.isChecked;
                holder.changeCheckedStatus(holder.isChecked);
                members.get(position).isSelected(holder.isChecked);
            });
        } else {
            Log.e(TAG, "[onBindViewHolder] cl_item == NULL!");
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }


    /**
     * Quality of Life methods
     */
    public ArrayList<Member> returnSelectedItems() {

        ArrayList<Member> result = new ArrayList<>();
        for (Member u : members) {
            if (u.isSelected()) {
                result.add(u);
            }
        }
        Log.d(TAG, "returnSelectedItems.result.size = " + result.size());

        return result;
    }

    public void replaceData(ArrayList<Member> u) {
        members.clear();
        members = u;
    }

    /**
     *  ViewHolder
     */
    class NewMemberVH extends RecyclerView.ViewHolder {

        TextView tv_name;
        CheckBox cb_selection;
        ConstraintLayout cl_item;

        boolean isChecked;

        NewMemberVH(View view ) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            cb_selection = view.findViewById(R.id.cb_selection);
            cl_item = view.findViewById(R.id.cl_item);
            isChecked = false;
        }

        private void changeCheckedStatus(boolean flag) {
            isChecked = flag;
            cb_selection.setChecked(isChecked);

            if (isChecked) {
                changeLayoutBackgroundTint(true);
            } else {
                changeLayoutBackgroundTint(false);
            }
        }

        private void changeLayoutBackgroundTint(boolean setToActiveTint) {
            if (setToActiveTint) {
                cl_item.setBackgroundTintList(null);
            } else {
                cl_item.setBackgroundTintList(context.getColorStateList(R.color.common_google_signin_btn_text_dark_disabled));
            }
        }
    }
}
