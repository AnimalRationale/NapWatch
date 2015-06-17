package pl.appnode.napwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
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

import static pl.appnode.napwatch.IsDarkTheme.isDarkTheme;
import static pl.appnode.napwatch.StateConstants.BUTTON_PRESS_DELAY;
import static pl.appnode.napwatch.StateConstants.MINUTE_IN_MILLIS;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.RESTORE;
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

    Handler mAAHandler = new Handler();

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder alarmViewHolder, final int position) {
        final AlarmInfo alarm = mAlarmList.get(position);
        alarmViewHolder.vTitle.setText(alarm.mName);
        if (!alarm.mIsOn) {
            if (alarm.mDurationCounter == 0) {
                alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_red);
                alarmViewHolder.vMinutesBar.setVisibility(View.GONE);
                Log.d(TAG, "Alarm view #1: alarm = " + alarm.mName + " // duration = " + alarm.mDuration);
                alarmViewHolder.vDuration.setText(alarm.mDurationCounter + alarm.mTimeUnitSymbol);
            } else {
                alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_green);
                alarmViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
                alarmViewHolder.vDuration.setText(alarm.mDuration + alarm.mTimeUnitSymbol);
                Log.d(TAG, "Alarm view #2: alarm = " + alarm.mName + " // duration = " + alarm.mDuration);
            }
        } else if (alarm.mIsOn & MainActivity.isService() & MainActivity.getAlarmState(position) == ON) {
            if (alarm.mDurationCounter == 0) {
                alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_red);
            } else alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_orange);
            alarmViewHolder.vMinutesBar.setVisibility(View.GONE);
            Log.d(TAG, "Alarm view #3: alarm = " + alarm.mName + " // duration = " + alarm.mDuration);
            alarmViewHolder.vDuration.setText(alarm.mDurationCounter + alarm.mTimeUnitSymbol);
        } else if (alarm.mIsOn & MainActivity.getAlarmState(position) != ON & MainActivity.getAlarmState(position) != RESTORE) {
            alarmViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_green);
            alarmViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
            alarmViewHolder.vDuration.setText(alarm.mDuration + alarm.mTimeUnitSymbol);
            alarm.mIsOn = false;
            Log.d(TAG, "Alarm view #4: alarm = " + alarm.mName + " // duration = " + alarm.mDuration);
        } else if (alarm.mIsOn & MainActivity.getAlarmState(position) == RESTORE) {
            mAAHandler.post(new Runnable() {
                public void run() {
                    alarmAction(position);
                }
            });
            Log.d(TAG, "Alarm view #5: alarm = " + alarm.mName + " // duration = " + alarm.mDuration);
        }
        alarmViewHolder.vMinutesBar.setMax(100);
        alarmViewHolder.vMinutesBar.setProgress(alarm.mDuration);
        Log.d(TAG, "Alarm alarm = " + alarm + " // setting progress duration = " + alarm.mDuration);

        alarmViewHolder.vTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alarm.mIsOn) {
                    showAlarmSettings(position);
                }
            }
        });
        alarmViewHolder.vDuration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < BUTTON_PRESS_DELAY){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "Alarm TAPPED: alarm.mIsOn = " + alarm.mIsOn + " // sIsService = " + MainActivity.isService());
                alarmAction(position);
            }
        });
        alarmViewHolder.vMinutesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    alarm.mDuration = progress;
                    alarm.mDurationCounter = progress;
                    alarmViewHolder.vDuration.setText(progress + alarm.mTimeUnitSymbol);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setDuration(alarm);
                WidgetUpdate.buttonTime(position + 1, alarm.mDuration + alarm.mTimeUnitSymbol, mContext);
            }
        });
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
        } else card.setCardBackgroundColor(Color.WHITE);
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

    public void alarmAction (int position) {
        if (MainActivity.getAlarmState(position) == ON) {
            MainActivity.setAlarmState(position, SWITCHING);
            stopAlarm(position);
        } else if (MainActivity.getAlarmState(position) == OFF || MainActivity.getAlarmState(position) == RESTORE) {
            MainActivity.setAlarmState(position, SWITCHING);
            startAlarm(position);
        }
    }

    public void startAlarm(int position) {
        AlarmInfo alarm = mAlarmList.get(position);
        alarm.mIsOn = true;
        notifyItemChanged(position);
        Log.d(TAG, "Alarm ON.");
        int timeUnitFactor;
        if (alarm.mTimeUnit == SECOND) { timeUnitFactor = SECOND_IN_MILLIS;} else {timeUnitFactor = (MINUTE_IN_MILLIS);}
        alarm.mFinishTime = SystemClock.elapsedRealtime() + (alarm.mDurationCounter * timeUnitFactor);
        Log.d(TAG, "ERT: " + SystemClock.elapsedRealtime() + " # Finish Time: " + alarm.mFinishTime);
        Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmId", position);
        serviceIntent.putExtra("AlarmName", alarm.mName);
        serviceIntent.putExtra("AlarmFullscreenOff", alarm.mFullscreenOff);
        serviceIntent.putExtra("AlarmDuration", alarm.mDurationCounter);
        serviceIntent.putExtra("AlarmUnit", alarm.mTimeUnitSymbol);
        serviceIntent.putExtra("AlarmFactor", timeUnitFactor);
        serviceIntent.putExtra("AlarmRingtone", alarm.mRingtoneUri);
        serviceIntent.putExtra("AlarmRingtoneVol", alarm.mRingtoneVolume);
        serviceIntent.putExtra("AlarmCommand", START);
        mContext.startService(serviceIntent);
        Log.d(TAG, "Service started.");
    }

    public void stopAlarm(int position) {
        AlarmInfo alarm = mAlarmList.get(position);
        Intent serviceIntent = new Intent(mContext, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmId", position);
        serviceIntent.putExtra("AlarmCommand", STOP);
        mContext.startService(serviceIntent);
        if (alarm.mDuration > 0) {
            alarm.mDurationCounter = alarm.mDuration;
        } else alarm.mDurationCounter = 1;
        alarm.mIsOn = false;
        notifyItemChanged(position);
        MainActivity.setAlarmState(position, OFF);
        WidgetUpdate.setButtonOff(position + 1, mContext);
        WidgetUpdate.buttonTime(position + 1, alarm.mDuration + alarm.mTimeUnitSymbol, mContext);
        Log.d(TAG, "Alarm OFF.");
    }

    private void showAlarmSettings(int position) {
        AlarmInfo alarm = mAlarmList.get(position);
        Intent settingsIntent = new Intent(mContext, AlarmSettingsActivity.class);
        settingsIntent.putExtra("AlarmId", position);
        settingsIntent.putExtra("AlarmName", alarm.mName);
        settingsIntent.putExtra("AlarmUnit", alarm.mTimeUnit);
        settingsIntent.putExtra("AlarmRingtoneUri", alarm.mRingtoneUri);
        settingsIntent.putExtra("AlarmRingtoneVol", alarm.mRingtoneVolume);
        settingsIntent.putExtra("AlarmFullscreenOff", alarm.mFullscreenOff);
        ((MainActivity)mContext).startActivityForResult(settingsIntent, SETTINGS_INTENT_REQUEST);
    }

    public void setDuration(final AlarmInfo item) {

        mAAHandler.post(new Runnable() {
            public void run(){
                final int position = mAlarmList.indexOf(item);
                notifyItemChanged(position);
            }
        });
    }
}