package pl.appnode.napwatch;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.List;
import android.view.View.OnClickListener;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private static final String TAG = "AlarmAdapter";

    protected List<AlarmInfo> mAlarmList;

    private Context mContext;

    public AlarmAdapter(List<AlarmInfo> alarmList, Context context) {
        this.mAlarmList = alarmList;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder alarmViewHolder, int position) {
        final AlarmInfo ai = mAlarmList.get(position);
        alarmViewHolder.vTitle.setText(ai.mName);
        alarmViewHolder.vDuration.setText(ai.mDuration + "");
        if (!ai.mIsOn) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button);
            alarmViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
        } else {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_selected);
            alarmViewHolder.vMinutesBar.setVisibility(View.GONE);
        }
        alarmViewHolder.vMinutesBar.setMax(100);
        alarmViewHolder.vMinutesBar.setProgress(ai.mDuration);
        alarmViewHolder.vTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editAlarm(ai);
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

    public void editAlarm(AlarmInfo item) {

        final int position = mAlarmList.indexOf(item);
        final AlarmInfo ai = mAlarmList.get(position);

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View promptView = layoutInflater.inflate(R.layout.name_edit_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.alarmNameText);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.edit_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        ai.mName = input.getText().toString();
                        notifyItemChanged(position);
                    }
                })
                .setNegativeButton(R.string.edit_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,	int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }

    public void startAlarm(AlarmInfo item) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        if (!alarm.mIsOn) {
            alarm.mIsOn = true;
            notifyItemChanged(position);
            Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
            serviceIntent.putExtra("AlarmId", "Alarm"+ position);
            serviceIntent.putExtra("AlarmName", alarm.mName);
            serviceIntent.putExtra("AlarmDuration", alarm.mDuration);
            mContext.startService(serviceIntent);
            // TODO: startService with proper values
            Log.d(TAG, "Started service.");
        } else {
            alarm.mIsOn = false;
            notifyItemChanged(position);
        }
    }

    public void setDuration(AlarmInfo item, int duration) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        alarm.mDuration = duration;
        notifyItemChanged(position);
    }
}
