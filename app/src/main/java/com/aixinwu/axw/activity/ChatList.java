package com.aixinwu.axw.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.talkmessage;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatList extends AppCompatActivity{

    private SwipeRefreshLayout layout;
    private ListView chatlist;
    private SimpleAdapter sim_adapter;
    private ArrayList<HashMap<String,String>> chatitem = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.chatlist_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("消息");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layout = (SwipeRefreshLayout) findViewById(R.id.chatlist_layout);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        layout.setColorSchemeResources(typedValue.resourceId);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetChatlistTask().execute();
            }
        });
        chatlist = (ListView)findViewById(R.id.chatlist);
        chatitem.clear();

        sim_adapter = new SimpleAdapter(this,chatitem,R.layout.item_chatlist,new String[]{"Name","Item","Doc","Time","Img"},new int[]{R.id.name,R.id.itemid,R.id.product,R.id.messageTime,R.id.img_activity_product});
        sim_adapter.setViewBinder(new SimpleAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                if (view instanceof TextView && o instanceof String){
                    TextView i = (TextView) view;
                    i.setText((String) o);
                    return true;
                }
                if (view instanceof ImageView && o instanceof String){
                    CircleImageView img = (CircleImageView) view;
                    String imgUrl = (String) o;
                    if (!o.equals(""))
                        ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+imgUrl,img);
                    return true;
                }
                return false;
            }
        });
        chatlist.setAdapter(sim_adapter);
        chatlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChatList.this,Chattoother.class);
                intent.putExtra("To",Integer.parseInt(chatitem.get(i).get("usrId")));
                intent.putExtra("itemID",Integer.parseInt(chatitem.get(i).get("Item")));
                intent.putExtra("ToName",chatitem.get(i).get("Name"));
                intent.putExtra("imgUrl",chatitem.get(i).get("Img"));
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
            }
        });
        layout.setRefreshing(true);
        new GetChatlistTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
    }

    class GetChatlistTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getChatlist();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            layout.setRefreshing(false);
            sim_adapter.notifyDataSetChanged();
        }
    }
    static public HashMap<String,String> getUserName(String userId){
        String usrName = "";
        HashMap<String, String> output = new HashMap<>();
        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/usr_get_by_id/"+userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            java.lang.String ostr ;
            org.json.JSONObject outjson = null;

            if (conn.getResponseCode() == 200){
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int i;
                while ((i = is.read()) != -1) baos.write(i);
                ostr = baos.toString();
                outjson = new org.json.JSONObject(ostr);

                String myUserName = outjson.getString("username");
                String myNickName = outjson.getString("nickname");
                if (myNickName.length() == 0)
                    usrName = myUserName;
                else usrName = myNickName;
                output.put("usrName",usrName);
                output.put("img",outjson.getString("image"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    public void getChatlist(){
        List<talkmessage> result = new ArrayList<>();
        ArrayList<Integer> chec = new ArrayList<Integer>();
        result = GlobalParameterApplication.gettalklist(GlobalParameterApplication.getUserID());
        chatitem.clear();
        for (int i = result.size()-1; i>= 0 ; --i){
            talkmessage re0 = result.get(i);
            HashMap<String,String> tt = new HashMap<String,String>();
            Integer ss = re0.getSender()+re0.getReceiver()-GlobalParameterApplication.getUserID();
            if (!chec.contains(ss)) {
                chec.add(ss);

                String usrName = null;
                GetNameThread getNameThread = new GetNameThread(ss.toString());
                getNameThread.start();
                try {
                    getNameThread.join();
                }catch (Exception e){
                    e.printStackTrace();
                }

                tt.put("usrId",ss.toString());
                tt.put("Name", getNameThread.getUsrName());
                tt.put("Item", String.valueOf(GlobalParameterApplication.query(ss)));
                tt.put("Doc",re0.getDoc());
                tt.put("Time",re0.getTime());
                tt.put("Img",getNameThread.getImgUrl());
                chatitem.add(tt);
            }
        }
    }

    class GetNameThread extends Thread{

        private String usrId;
        private String usrName;
        private String imgUrl;

        public GetNameThread(String usrId){
            this.usrId = usrId;
        }

        public String getUsrName(){
            return this.usrName;
        }

        public String getImgUrl() {
            return this.imgUrl;
        }
        @Override
        public void interrupt() {
            super.interrupt();
        }

        @Override
        public void run(){
            try{
                HashMap<String,String> info = getUserName(usrId);
                usrName = info.get("usrName");
                imgUrl = info.get("img");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
