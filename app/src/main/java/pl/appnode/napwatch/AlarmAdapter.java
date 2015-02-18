package pl.appnode.napwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.List;
import android.view.View.OnClickListener;

import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.SWITCHING;
import static pl.appnode.napwatch.StateConstants.ON;
import static pl.appnode.napwatch.StateConstants.SECOND;
import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.START;
import static pl.appnode.napwatch.StateConstants.STOP;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private static final String TAG = "AlarmAdapter";

    protected List<AlarmInfo> mAlarmList;

    private Context mContext;

    private long mLastClickTime = 0;

    public AlarmAdapter(List<AlarmInfo> alarmList, Context context) {
        this.mAlarmList = alarmList;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder alarmViewHolder, final int position) {
        final AlarmInfo ai = mAlarmList.get(position);
        alarmViewHolder.vTitle.setText(ai.mName);
        if (!ai.mIsOn) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button);
            alarmViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
            alarmViewHolder.vDuration.setText(ai.mDuration + ai.mTimeUnitSymbol);
        } else if (ai.mIsOn & MainActivity.isService) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_selected);
            alarmViewHolder.vMinutesBar.setVisibility(View.GONE);
            alarmViewHolder.vDuration.setText(ai.mDurationCounter + ai.mTimeUnitSymbol);
        } else if (ai.mIsOn & !MainActivity.isService) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button);
            alarmViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
            alarmViewHolder.vDuration.setText(ai.mDuration + ai.mTimeUnitSymbol);
            ai.mIsOn = false;
        }
        alarmViewHolder.vMinutesBar.setMax(100);
        alarmViewHolder.vMinutesBar.setProgress(ai.mDuration);

        alarmViewHolder.vTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ai.mIsOn) {editAlarm(ai);}
            }
        });
        alarmViewHolder.vDuration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "Alarm TAPPED: ai.mIsOn = " + ai.mIsOn + " // isService = " + MainActivity.isService);
                if (ai.mIsOn & MainActivity.isService & MainActivity.AlarmState[position] == ON) {
                    MainActivity.AlarmState[position] = ON;
                    stopAlarm(ai);
                } else if (!ai.mIsOn & MainActivity.AlarmState[position] == OFF) {
                    MainActivity.AlarmState[position] = ON;
                    startAlarm(ai);
                }
            }
        });
        alarmViewHolder.vMinutesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        TextView title = (TextView) promptView.findViewById(R.id.alarmEditTitle);
        title.setText(R.string.edit_title);
        title.append("" + (position + 1));
        final EditText input = (EditText) promptView.findViewById(R.id.alarmNameText);
        input.setText(ai.mName);
        final RadioButton rbSeconds = (RadioButton) promptView.findViewById(R.id.radio_seconds);
        RadioButton rbMinutes = (RadioButton) promptView.findViewById(R.id.radio_minutes);
        if (ai.mTimeUnit == SECOND) {
            rbSeconds.toggle();
        } else rbMinutes.toggle();
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.edit_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ai.mName = input.getText().toString();
                        if (rbSeconds.isChecked()) {
                            ai.mTimeUnit = SECOND;
                            ai.mTimeUnitSymbol = mContext.getResources().getString(R.string.time_unit_seconds);
                        } else {
                            ai.mTimeUnit = MINUTE;
                            ai.mTimeUnitSymbol = mContext.getResources().getString(R.string.time_unit_minutes);
                        }
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
        alarm.mIsOn = true;
        notifyItemChanged(position);
        Log.d(TAG, "Alarm ON.");
        int timeUnitFactor = 0;
        if (alarm.mTimeUnit == SECOND) { timeUnitFactor = 1000;} else {timeUnitFactor = (1000 * 60);}
        Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmId", position);
        serviceIntent.putExtra("AlarmName", alarm.mName);
        serviceIntent.putExtra("AlarmDuration", alarm.mDuration);
        serviceIntent.putExtra("AlarmUnit", alarm.mTimeUnitSymbol);
        serviceIntent.putExtra("AlarmFactor", timeUnitFactor);
        serviceIntent.putExtra("AlarmCommand", START);
        mContext.startService(serviceIntent);
        Log.d(TAG, "Service started.");
    }

    public void stopAlarm(AlarmInfo item) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmId", position);
        serviceIntent.putExtra("AlarmCommand", STOP);
        mContext.startService(serviceIntent); //TODO: active alarms recognition, stopping service if no running alarms
        alarm.mIsOn = false;
        MainActivity.AlarmState[position] = OFF;
        Log.d(TAG, "Alarm OFF.");
        notifyItemChanged(position);
    }

    public void updateTime (int position) {
        AlarmInfo alarm = mAlarmList.get(position);
        if (alarm.mIsOn) {
        }

    }

    public void setDuration(AlarmInfo item, int duration) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        alarm.mDuration = duration;
        alarm.mDurationCounter = duration;
        notifyItemChanged(position);
    }
}