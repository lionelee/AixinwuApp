package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.aixinwu.axw.R;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.talkmessage;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;
import io.nats.client.ConnectionFactory;
import schoolapp.chat.Chat;
import schoolapp.chat.Messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by liangyuding on 2016/4/15.
 */
public class Chattoother extends AppCompatActivity{
    public int start = -1;
    private TextView chatt;
    ArrayList<HashMap<String,Object>> chatList=null;
    public ArrayList<String> OtherMsg = new ArrayList<String>();
    public ArrayList<String> cont = new ArrayList<String>();
    public ArrayList<Integer> who = new ArrayList<Integer>();
    String[] from={"name","text"};
    int[] to={R.id.chatlist_image_me,R.id.chatlist_text_me,R.id.chatlist_image_other,R.id.chatlist_text_other};
    int[] layout={R.layout.chat_listitem_me,R.layout.chat_listitem_other};
    String[] avatar={"",""};
    public String myWord;
    TypedValue typedValue = new TypedValue();

    public final static int OTHER=1;
    public final static int ME=0;

    private String otherName = "";

    protected ListView chatListView=null;
    protected TextView chatSendButton=null;
    protected EditText editText=null;
    public int num = 0;
    protected MyChatAdapter adapter=null;
    private int ItemID;
    private boolean pause = true;
    private int To;
    private int From;
    private boolean uploadSuccessful = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data=getIntent();
        GlobalParameterApplication.setAllowChatThread(false);
        ItemID = data.getIntExtra("itemID",0);
        From = data.getIntExtra("To",0);
        otherName = data.getStringExtra("ToName");
        avatar[0] = GlobalParameterApplication.getImgUrl();
        avatar[1] = data.getStringExtra("imgUrl");
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);

        To = GlobalParameterApplication.getUserID();
        pause = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chat);
        chatList=new ArrayList<HashMap<String,Object>>();

       /* if(exist){
        FileInputStream inStream = null;
        try {
            inStream = openFileInput(FileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();//

		int len=0;
		byte[] buffer = new byte[102400];
        try {
            while((len=inStream.read(buffer))!=-1){
                outStream.write(buffer, 0, len);//
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] content_byte = outStream.toByteArray();
		String content = new String(content_byte);
        try {
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] ss = content.split("\\$\\$");
		for (int i = 1; i < ss.length;i++)
		{
			if (ss[i].charAt(0)=='0')addTextToList(ss[i].substring(2),0);
            else addTextToList(ss[i].substring(2),1);
		}
        }*/
        List<talkmessage> re00 = GlobalParameterApplication.gettalk(To, From);

        num = re00.size();
        for (talkmessage tm: re00){
            cont.add(tm.getDoc());
            if (tm.getSender()== GlobalParameterApplication.getUserID()){
                addTextToList(tm.getDoc(),ME);who.add(ME);
            } else {addTextToList(tm.getDoc(),OTHER);who.add(OTHER);}

        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (pause) {
                    if (GlobalParameterApplication.getLogin_status() == 1) {

                        Message msg = new Message();
                        msg.what = 22234;
                        msg.arg1 = 0;
                        int UserID = GlobalParameterApplication.getUserID();
                        List<talkmessage> res= GlobalParameterApplication.gettalk(To, From);
                        int ss = res.size();
                        if (ss > num){
                            for (int i=num; i<ss; i++)

                                if (res.get(i).getSender() != GlobalParameterApplication.getUserID()){
                                    cont.add(res.get(i).getDoc());
                                    who.add(OTHER);}

                            num = ss;}
                        nHandler.sendMessage(msg);
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();

        chatt =(TextView)findViewById(R.id.chat_contact_name);
        chatt.setText(otherName);
        chatSendButton=(TextView)findViewById(R.id.chat_bottom_sendbutton);
        editText=(EditText)findViewById(R.id.chat_bottom_edittext);
        chatListView=(ListView)findViewById(R.id.chat_list);

        adapter=new MyChatAdapter(this,chatList,layout,from,to);
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(TextUtils.isEmpty(editText.getText().toString())){
                    chatSendButton.setTextColor(getResources().getColor(R.color.gray));
                    chatSendButton.setEnabled(false);
                } else{
                    chatSendButton.setTextColor(getResources().getColor(typedValue.resourceId));
                    chatSendButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //         String myWord=null;

                /**
                 * 这是一个发送消息的监听器，注意如果文本框中没有内容，那么getText()的返回值可能为
                 * null，这时调用toString()会有异常！所以这里必须在后面加上一个""隐式转换成String实例
                 * ，并且不能发送空消息。
                 */

                myWord=(editText.getText()+"").toString();
                if(myWord.length()==0)
                    return;
                editText.setText("");
                uploadSuccessful = false;
                GlobalParameterApplication.publish(myWord, From);
                Date date= new Date();//创建一个时间对象，获取到当前的时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置时间显示格式
                String str = sdf.format(date);//将当前时间格式化为需要的类型

                GlobalParameterApplication.add(To, From, myWord, 1,str);
                cont.add(myWord);
                who.add(ME);
                addTextToList(myWord, ME);
                adapter.notifyDataSetChanged();
                chatListView.setSelection(chatList.size() - 1);
                /*new Thread(new Runnable(){
                    @Override
                    public void run(){
                        int UserID = GlobalParameterApplication.getUserID();
                        JSONObject data = new JSONObject();
                        JSONObject chatinfo = new JSONObject();
                        try {
                            URL url = new URL(surl+"/item_add_chart");
                            try {
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                                chatinfo.put("publisher_id",To);
                                chatinfo.put("buyer_id",From);
                                chatinfo.put("content",myWord);
                                chatinfo.put("itemID",ItemID);
                                data.put("chat",chatinfo);
                                data.put("token",GlobalParameterApplication.getToken());
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);

                                conn.setRequestProperty("Content-Type", "application/json");
                                //conn.setRequestProperty("Content-Length", String.valueOf(data.toJSONString().length()));
                                OutputStream output=conn.getOutputStream();
                                output.write(data.toJSONString().getBytes());

                                String ostr = IOUtils.toString(conn.getInputStream());
                                System.out.println("Chat2"+ostr);

                                org.json.JSONObject outjson = null;
                                int  result = 1;
                                try {
                                    outjson = new org.json.JSONObject(ostr);
                                    result = outjson.getJSONObject("status").getInt("code");
                                    uploadSuccessful = result==0?true:false;
                                   // while (!GlobalParameterApplication.getChat_othermsg());
           //                         GlobalParameterApplication.setChat_othermsg(false);
                                    /*if (uploadSuccessful && !OtherMsg.isEmpty()){
                                        FileOutputStream fos = openFileOutput(FileName,MODE_APPEND);
                                        for (int i = 0; i < OtherMsg.size(); i++){
                                            fos.write(OtherMsg.get(i).getBytes());
                                        }
                                        fos.close();
                                        OtherMsg.clear();
                                        start = -1;
                                    }
       //                             GlobalParameterApplication.setChat_othermsg(true);
                                    Message msg = new Message();
                                    msg.what=233333;
                                  //  nHandler.sendMessage(msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }



                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();*/

            }
        });

        chatListView.setAdapter(adapter);
        chatListView.setSelection(chatList.size()-1);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
        super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        // GlobalParameterApplication.setEnd(true);
        //while (!GlobalParameterApplication.getEnd());
        // GlobalParameterApplication.setChat_othermsg(true);

     /*   if (!OtherMsg.isEmpty()){
                FileOutputStream out = null;
                try {
                    out = openFileOutput(FileName,MODE_APPEND);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < OtherMsg.size(); i++){
                    try {
                        out.write(OtherMsg.get(i).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        pause= false;
        //   GlobalParameterApplication.setPause(false);
        super.onDestroy();
    }





    public Handler nHandler = new Handler(){public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case 537485:
                chatt.setText(otherName);
                break;
            case 22234:
                /*if (msg.arg1 == 1){
                    if (OtherMsg.size()-1>=start+1){
                        for (int i = start+1; i < OtherMsg.size();i++){
                            addTextToList(OtherMsg.get(i).substring(4,OtherMsg.get(i).length()),OTHER);
                        }
                        start = OtherMsg.size()-1;
                    }

      //                  GlobalParameterApplication.setChat_othermsg(true);
                    adapter.notifyDataSetChanged();
                    chatListView.setSelection(chatList.size()-1);

                    Toast.makeText(Chat.this,"You have new message!!!",Toast.LENGTH_LONG);
                }*/
                chatList.clear();
                for (int i = 0; i < cont.size();i++){
                    addTextToList(cont.get(i),who.get(i));
                }
                adapter.notifyDataSetChanged();
                //chatListView.setSelection(chatList.size()-1);
                break;
            case 233333:
                if(uploadSuccessful) {
                   /* FileOutputStream fos = null;
                    try {
                        fos = openFileOutput(FileName,MODE_APPEND);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.write(("$$0$"+myWord).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    addTextToList(myWord, ME);}


                /**
                 * 更新数据列表，并且通过setSelection方法使ListView始终滚动在最底端
                 */
                adapter.notifyDataSetChanged();
                chatListView.setSelection(chatList.size()-1);

                break;
        }
    }

    };
    protected void addTextToList(String text, int who){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("person",who );
        map.put("name", who==ME?"Me":"用户" + From);
        map.put("text", text);
        chatList.add(map);
    }

    private class MyChatAdapter extends BaseAdapter {

        public MyChatAdapter(Context context,
                             ArrayList<HashMap<String, Object>> chatList, int[] layout,
                             String[] from, int[] to) {
            super();
        }

        @Override
        public int getCount() {
            return chatList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder{
            public CircleImageView imgView;
            public TextView nameView=null;
            public TextView textView=null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int who=(Integer)chatList.get(position).get("person");
            if(convertView == null){
                convertView= LayoutInflater.from(Chattoother.this).inflate(
                        layout[who==ME?0:1], null);
                holder=new ViewHolder();
                holder.nameView=(TextView)convertView.findViewById(to[who*2+0]);
                holder.textView=(TextView)convertView.findViewById(to[who*2+1]);
                holder.imgView =(CircleImageView)convertView.findViewById(R.id.img_activity_product);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.nameView.setText(chatList.get(position).get(from[0]).toString());
            holder.textView.setText(chatList.get(position).get(from[1]).toString());
            String url = avatar[who==ME?0:1];
            if(!url.equals(""))
                ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+url, holder.imgView);
            return convertView;
        }

    }

}
