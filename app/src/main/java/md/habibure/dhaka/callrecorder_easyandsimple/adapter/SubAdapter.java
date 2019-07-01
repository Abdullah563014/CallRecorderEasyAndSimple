package md.habibure.dhaka.callrecorder_easyandsimple.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import md.habibure.dhaka.callrecorder_easyandsimple.PlayerActivity;
import md.habibure.dhaka.callrecorder_easyandsimple.database.Database;
import md.habibure.dhaka.callrecorder_easyandsimple.model.CallListModelClass;
import md.habibure.dhaka.callrecorder_easyandsimple.R;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.MyViewHolder> {

    ArrayList<CallListModelClass> arrayList;
    Context context;

    public SubAdapter(Context context, ArrayList<CallListModelClass> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public SubAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = (View) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.home_activity_record_list_model, viewGroup, false);
        SubAdapter.MyViewHolder vh = new SubAdapter.MyViewHolder(context, arrayList, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (arrayList.get(position).getCallIndicator().equalsIgnoreCase("incoming")) {
            holder.relativeLayout.setBackgroundResource(R.drawable.home_activity_record_list_incoming_call_background);
            holder.callIndicator.setImageResource(R.drawable.ic_call_incoming);
        } else {
            holder.relativeLayout.setBackgroundResource(R.drawable.home_activity_record_list_outgoing_call_background);
            holder.callIndicator.setImageResource(R.drawable.ic_call_outgoing);
        }
        holder.avatar.setImageResource(R.drawable.ic_account_circle);
        int duration = Integer.parseInt(arrayList.get(position).getDuration());
        holder.duration.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
        holder.name.setText(arrayList.get(position).getName());
        holder.time.setText(arrayList.get(position).getTime());
        holder.playButton.setImageResource(R.drawable.ic_play_circle);
        holder.shareButton.setImageResource(R.drawable.ic_share);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Context context;
        Database database;
        ArrayList<CallListModelClass> arrayList;
        RelativeLayout relativeLayout;
        ImageView avatar;
        ImageView callIndicator;
        TextView duration;
        TextView name;
        TextView time;
        ImageView playButton;
        ImageView shareButton;

        public MyViewHolder(Context context, ArrayList<CallListModelClass> arrayList, @NonNull View v) {
            super(v);

            this.context = context;
            this.arrayList = arrayList;
            relativeLayout = v.findViewById(R.id.homeActivityModelViewRelativeLayoutId);
            avatar = v.findViewById(R.id.homeActivityRecordListAccountCircleImageViewId);
            callIndicator = v.findViewById(R.id.homeActivityRecordListModelCallIndicatorImageViewId);
            duration = v.findViewById(R.id.homeActivityRecordListDurationTextViewId);
            name = v.findViewById(R.id.homeActivityRecordListModelNameTextViewId);
            time = v.findViewById(R.id.homeActivityRecordListTimeTextViewId);
            playButton = v.findViewById(R.id.homeActivityRecordListModelPlayIconImageViewId);
            shareButton = v.findViewById(R.id.homeActivityRecordListModelShareImageViewId);

            playButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.homeActivityRecordListModelShareImageViewId:
                    if (isFileExist(arrayList.get(getAdapterPosition()).getFile())) {
                        shareAudioFile(arrayList.get(getAdapterPosition()).getFile());
                    } else {
                        Toast.makeText(context, "File not exist", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.homeActivityRecordListModelPlayIconImageViewId:
                    Intent intent = new Intent(context, PlayerActivity.class);
                    if (isFileExist(arrayList.get(getAdapterPosition()).getFile())) {
                        intent.putExtra("uri", arrayList.get(getAdapterPosition()).getFile());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "File not exist", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }


        public void shareAudioFile(String filePath) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("*/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
            context.startActivity(Intent.createChooser(share, "Share Recorded File"));
        }


        public boolean isFileExist(String s) {
            File file = new File(s);
            if (file.exists()) {
                return true;
            } else {
                return false;
            }
        }
    }
}
