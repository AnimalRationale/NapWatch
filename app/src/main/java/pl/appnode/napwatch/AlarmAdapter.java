package pl.appnode.napwatch;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.List;
import android.view.View.OnClickListener;

import static pl.appnode.napwatch.StateConstants.MINUTE_IN_MILLIS;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.SECOND_IN_MILLIS;
import static pl.appnode.napwatch.StateConstants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.napwatch.StateConstants.SWITCHING;
import static pl.appnode.napwatch.StateConstants.ON;
import static pl.appnode.napwatch.StateConstants.SECOND;
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
        } else if (ai.mIsOn & MainActivity.sIsService) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_selected);
            alarmViewHolder.vMinutesBar.setVisibility(View.GONE);
            alarmViewHolder.vDuration.setText(ai.mDurationCounter + ai.mTimeUnitSymbol);
        } else if (ai.mIsOn & !MainActivity.sIsService) {
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
                if (!ai.mIsOn) {
                    Intent settingsIntent = new Intent(mContext, AlarmSettingsActivity.class);
                    settingsIntent.putExtra("AlarmId", position);
                    settingsIntent.putExtra("AlarmName", ai.mName);
                    settingsIntent.putExtra("AlarmUnit", ai.mTimeUnit);
                    settingsIntent.putExtra("AlarmRingtoneUri", ai.mRingtoneUri);
                    settingsIntent.putExtra("AlarmRingtoneVol", ai.mRingtoneVolume);
                    ((MainActivity)mContext).startActivityForResult(settingsIntent, SETTINGS_INTENT_REQUEST);
                }
            }
        });
        alarmViewHolder.vDuration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "Alarm TAPPED: ai.mIsOn = " + ai.mIsOn + " // sIsService = " + MainActivity.sIsService);
                if (ai.mIsOn && MainActivity.sIsService && MainActivity.getAlarmState(position) == ON) {
                    MainActivity.setAlarmState(position, SWITCHING);
                    stopAlarm(ai);
                } else if (!ai.mIsOn && MainActivity.getAlarmState(position) == OFF) {
                    MainActivity.setAlarmState(position,SWITCHING);
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
            vDuration = (Button) v.findViewById(R.id.round_btn1);
            vMinutesBar = (SeekBar) v.findViewById(R.id.minutes_seek);
        }
    }

    public void startAlarm(AlarmInfo item) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        alarm.mIsOn = true;
        notifyItemChanged(position);
        Log.d(TAG, "Alarm ON.");
        int timeUnitFactor = 0;
        if (alarm.mTimeUnit == SECOND) { timeUnitFactor = SECOND_IN_MILLIS;} else {timeUnitFactor = (MINUTE_IN_MILLIS);}
        Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmId", position);
        serviceIntent.putExtra("AlarmName", alarm.mName);
        serviceIntent.putExtra("AlarmDuration", alarm.mDuration);
        serviceIntent.putExtra("AlarmUnit", alarm.mTimeUnitSymbol);
        serviceIntent.putExtra("AlarmFactor", timeUnitFactor);
        serviceIntent.putExtra("AlarmRingtone", alarm.mRingtoneUri);
        serviceIntent.putExtra("AlarmRingtoneVol", alarm.mRingtoneVolume);
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
        mContext.startService(serviceIntent);
        alarm.mDurationCounter = alarm.mDuration;
        alarm.mIsOn = false;
        notifyItemChanged(position);
        MainActivity.setAlarmState(position, OFF);
        Log.d(TAG, "Alarm OFF.");
    }

    public void setDuration(AlarmInfo item, int duration) {
        int position = mAlarmList.indexOf(item);
        AlarmInfo alarm = mAlarmList.get(position);
        alarm.mDuration = duration;
        alarm.mDurationCounter = duration;
        notifyItemChanged(position);
    }
}