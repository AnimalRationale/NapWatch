package pl.appnode.napwatch;

/**
 * Created by Monki on 2014-11-24.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

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
    public void onBindViewHolder(AlarmViewHolder alarmViewHolder, int i) {
        AlarmInfo ai = alarmList.get(i);
        alarmViewHolder.vDescription.setText(ai.description);
        alarmViewHolder.vTitle.setText(ai.name + ' ' +ai.duration);
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new AlarmViewHolder(itemView);
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {

        protected TextView vDescription;
        protected TextView vTitle;

        public AlarmViewHolder(View v) {
            super(v);
            vDescription = (TextView)  v.findViewById(R.id.txtDescription);
            vTitle = (TextView) v.findViewById(R.id.title);
        }
    }
}
