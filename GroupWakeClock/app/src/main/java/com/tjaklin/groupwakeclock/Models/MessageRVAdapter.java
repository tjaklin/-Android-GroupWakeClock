package com.tjaklin.groupwakeclock.Models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tjaklin.groupwakeclock.R;

import java.util.ArrayList;

public class MessageRVAdapter extends RecyclerView.Adapter<MessageRVAdapter.MessageVH> {

    private static final String TAG = MessageRVAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    Context context;
    String thisUser;
    ArrayList<Message> messages;

    public MessageRVAdapter(Context c, String u, ArrayList<Message> m) {
        context = c;
        thisUser = u;
        messages = m;

        Log.e(TAG, "list size = " + m.size());
    }

    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = null;

        if (viewType == VIEW_TYPE_LEFT) {
            layoutView = LayoutInflater.from(context).inflate(R.layout.item_message_received, null, false);
        } else {
            layoutView = LayoutInflater.from(context).inflate(R.layout.item_message_sent, null, false);
        }

        return new MessageVH(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageVH holder, final int position) {
        if (holder.tv_sender != null) {
            holder.tv_sender.setText(messages.get(position).getSenderID());
        }
        Log.e(TAG, "list size = " + messages.size());
        holder.tv_content.setText(messages.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderID().equals(thisUser) ? VIEW_TYPE_RIGHT : VIEW_TYPE_LEFT;
    }

    /**
     *  ViewHolder
     */
    class MessageVH extends RecyclerView.ViewHolder {

        TextView tv_sender, tv_content;

        MessageVH(View view ) {
            super(view);
            tv_sender = view.findViewById(R.id.tv_sender);
            tv_content = view.findViewById(R.id.tv_content);
        }
    }

}
