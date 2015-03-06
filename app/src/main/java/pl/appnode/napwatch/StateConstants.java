package pl.appnode.napwatch;

/** Set of constants used to recognize states of alarms:
 *  DEFAULT duration values for SharedPreferences;
 *  OFF/ON/SWITCHING;
 *  time units SECOND/MINUTE;
 *  commands in intents send to service for START/STOP/UPDATE alarm timer. */

public final class StateConstants {

    private StateConstants() {} /** Private constructor of final class to prevent instantiating. */

    public static final int DEFAULT_TIMER_DURATION = 12;
    public static final int DEFAULT_TIMER_DURATION_MODIFIER = 4;
    public static final int OFF = 0;
    public static final int SWITCHING = 1;
    public static final int ON = 2;
    public static final int SECOND = 0;
    public static final int MINUTE = 1;
    public static final int SECOND_IN_MILLIS = 1000;
    public static final int MINUTE_IN_MILLIS = 1000 * 60;
    public static final int TIME_DEVIATION_FOR_LAST_TICK = 200;
    public static final int STOP = 0;
    public static final int START = 1;
    public static final int UPDATE = 2;
    public static final int EMPTY = 10;
    public static final int SETTINGS_INTENT_REQUEST = 501;
    public static final int RINGTONE_INTENT_REQUEST = 502;
    public static final int RINGTONE_MUTE = 0;
}
