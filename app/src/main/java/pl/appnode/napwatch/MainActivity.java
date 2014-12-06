package pl.appnode.napwatch;

        import android.app.Activity;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import java.util.ArrayList;
        import java.util.List;


public class MainActivity extends Activity {

    public static final String ALARMS_PREFS_FILE = "AlarmsPrefsFile";
    private static final String TAG = "MainActivity";
    protected AlarmAdapter aa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        RecyclerView recList = (RecyclerView) findViewById(R.id.alarmList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        Log.i(TAG, "Before Setting Adapter.");
        aa = new AlarmAdapter(createList());
        recList.setAdapter(aa);
        Log.i(TAG, "After Setting Adapter.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences alarmsPrefs = getSharedPreferences(MainActivity.ALARMS_PREFS_FILE, 0);
        SharedPreferences.Editor editor = alarmsPrefs.edit();

        for (int i = 0; i <= 3; i++) {
            String alarmPrefix = "Alarm_" + (i + 1);
            AlarmInfo alarm = aa.alarmList.get(i);
            editor.putString(alarmPrefix, alarm.name);
            editor.putInt(alarmPrefix + "_Duration", alarm.duration);
            editor.putBoolean(alarmPrefix + "_State", alarm.isOn);
            Log.i(TAG, "Create SharedPrefs: " + alarmPrefix + ": " + alarm.duration + ": " + alarm.isOn);
        }

        editor.commit();
        Log.i(TAG, "COMMITED SharedPrefs.");
    }

    private List<AlarmInfo> createList() {
        Log.i(TAG, "Create list.");

        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, 0);
        String alarmPrefix;
        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i = 1; i <= 4; i++) {
            AlarmInfo ai = new AlarmInfo();
            alarmPrefix = "Alarm_" + i;
            ai.name = alarmsPrefs.getString(alarmPrefix, "Def Alarm " + i);
            ai.duration = alarmsPrefs.getInt(alarmPrefix + "_Duration", 12 + (i*4));
            ai.isOn = alarmsPrefs.getBoolean(alarmPrefix + "_State", false);
            Log.i(TAG, "before Result add #" + i);
            result.add(ai);
            Log.i(TAG, "Result add #" + i);
        }
        Log.i(TAG, "RETURN!");
        return result;
    }
}
