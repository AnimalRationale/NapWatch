package pl.appnode.napwatch;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import java.util.ArrayList;
        import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        RecyclerView recList = (RecyclerView) findViewById(R.id.alarmList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        AlarmAdapter aa = new AlarmAdapter(createList(30));
        recList.setAdapter(aa);
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

    private List<AlarmInfo> createList(int size) {

        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i=1; i <= size; i++) {
            AlarmInfo ai = new AlarmInfo();
            ai.name = AlarmInfo.NAME_PREFIX + i;
            ai.description = AlarmInfo.DESCRIPTION_PREFIX + i;
            ai.duration = AlarmInfo.DURATION_PREFIX + i + " minutes";
            result.add(ai);
        }
        return result;
    }
}
