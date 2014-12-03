package pl.appnode.napwatch;

        import android.app.Activity;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.view.View;
        import android.widget.Toast;

        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.List;



public class MainActivity extends Activity {

    public static final String ALARMS_PREFS_FILE = "AlarmsPrefsFile";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        RecyclerView recList = (RecyclerView) findViewById(R.id.alarmList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        final AlarmAdapter aa = new AlarmAdapter(createList());
        recList.setAdapter(aa);

        View fab = findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Hardcoded!", Toast.LENGTH_SHORT).show();
                aa.addAlarm(aa.getItemCount()+1);
            }
        });
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

    private void checkAlarmsPrefs() {

        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, 0);
        if (!alarmsPrefs.contains("Alarm_1")) {
            String alarmsTitles[] = {"Alarm 1", "Alarm 2", "Alarm 3", "Alarm 4" };
            int alarmsDurations[] = {10, 15, 20, 30};
            String alarmPrefix;
            SharedPreferences.Editor editor = alarmsPrefs.edit();
            for (int i = 0; i <= 3; i++) {
                alarmPrefix = "Alarm_" + i + 1;
                editor.putString(alarmPrefix, alarmsTitles[i] );
                editor.putInt(alarmPrefix + "_Duration", alarmsDurations[i]);
                editor.putBoolean(alarmPrefix + "_State", false);
            }
            editor.commit();
        }
    }

    private List<AlarmInfo> createList() {
        Log.i(TAG, "Create list.");

        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, 0);
        checkAlarmsPrefs();

        String alarmPrefix;

        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i = 1; i <= 4; i++) {
            alarmPrefix = "Alarm_" + i;
            AlarmInfo ai = new AlarmInfo();
            ai.name = alarmsPrefs.getString(alarmPrefix, "Def " + i);
            ai.duration = alarmsPrefs.getInt(alarmPrefix + "_Duration", 12);
            ai.isOn = alarmsPrefs.getBoolean(alarmPrefix + "_State", true);
            Log.i(TAG, "before Result add #" + i);
            result.add(ai);
            Log.i(TAG, "Result add #" + i);
        }
        Log.i(TAG, "RETURN!");
        return result;
    }

    private List<AlarmInfo> createList(int size) {

        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i=1; i <= size; i++) {
            AlarmInfo ai = new AlarmInfo();
            ai.name = AlarmInfo.NAME_PREFIX + i;
            ai.duration = 10;
            ai.isOn = false;
            result.add(ai);
        }

        return result;
    }

}
