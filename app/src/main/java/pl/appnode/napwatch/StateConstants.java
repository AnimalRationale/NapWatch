package pl.appnode.napwatch;

/** Set of constants used to recognize states of alarms: OFF/ON/SWITCHING. */
public final class StateConstants {

    private StateConstants() {} /** Private constructor of final class to prevent instantiating. */

    public static final int OFF = 0;
    public static final int SWITCHING = 1;
    public static final int ON = 2;
}
