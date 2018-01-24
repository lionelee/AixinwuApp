package com.aixinwu.axw.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.VolunteerListAdapter;
import com.aixinwu.axw.fragment.HomePage;
import com.aixinwu.axw.model.VolunteerActivity;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class VolActivityList extends AppCompatActivity {

    private List<VolunteerActivity> volList = new ArrayList<VolunteerActivity>();
    private RecyclerView volListGrid;
    private VolunteerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vol_activity_list);

        volListGrid = (RecyclerView) findViewById(R.id.volList);
        volListGrid.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new VolunteerListAdapter(VolActivityList.this);
        volListGrid.setAdapter(adapter);
        volListGrid.addOnItemTouchListener(new OnRecyclerItemClickListener(volListGrid) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                VolunteerActivity product = ((VolunteerListAdapter.ViewHolder)vh).getData();
                Intent intent = new Intent(VolActivityList.this, VolunteerApply.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volActivityId", product);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });
        new GetVolunteerTask().execute();
    }

    class GetVolunteerTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            volList = HomePage.getVolunteer();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(volList == null)return;
            int len = volList.size();
            for(int i = 0; i < len; ++i){
                adapter.addItem(volList.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        super.onBackPressed();
    }
}
