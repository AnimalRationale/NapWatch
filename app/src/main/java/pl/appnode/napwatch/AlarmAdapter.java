package pl.appnode.napwatch;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.List;
import android.view.View.OnClickListener;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmInfo> alarmList;

    public AlarmAdapter(List<AlarmInfo> alarmList) {
        this.alarmList = alarmList;
    }


    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder alarmViewHolder, int position) {
        final AlarmInfo ai = alarmList.get(position);
        alarmViewHolder.vTitle.setText(ai.name);
        alarmViewHolder.vDuration.setText(ai.duration + "");
        alarmViewHolder.vMinutesBar.setMax(100);
        alarmViewHolder.vMinutesBar.setProgress(ai.duration);
        alarmViewHolder.vTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAlarm(ai);
            }
        });
        alarmViewHolder.vDuration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlarm(ai);
            }
        });
        alarmViewHolder.vMinutesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //change to progress
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setDuration(ai, progress);
            }

            //methods to implement but not necessary to amend
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new AlarmViewHolder(itemView);
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected Button vDuration;
        protected SeekBar vMinutesBar;

        public AlarmViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDuration = (Button) v.findViewById(R.id.roundBtn1);
            vMinutesBar = (SeekBar) v.findViewById(R.id.minutes_seek);
        }
    }

    public void addAlarm(int position) {
        alarmList.add(alarmList.get(0)); // add mock-up
        notifyItemInserted(position);
    }

    public void removeAlarm(AlarmInfo item) {
        int position = alarmList.indexOf(item);
        alarmList.remove(position);
        notifyItemRemoved(position);
    }

    public void startAlarm(AlarmInfo item) {
        int position = alarmList.indexOf(item); // TODO

    }

    public void setDuration(AlarmInfo item, int duration) {
        int position = alarmList.indexOf(item);
        AlarmInfo alarm = alarmList.get(position);
        alarm.duration = duration;
        notifyItemChanged(position);
    }
}
