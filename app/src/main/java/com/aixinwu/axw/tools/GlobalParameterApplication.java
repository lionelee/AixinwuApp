package com.aixinwu.axw.tools;

/**
 * Created by dell1 on 2016/4/23.
 */

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatDelegate;

import com.aixinwu.axw.adapter.NotifyMessage;
import com.aixinwu.axw.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import io.nats.client.ConnectionFactory;
import schoolapp.chat.Chat;

public class GlobalParameterApplication extends Application{
    public static int login_status = 0;
    private static String user_name;
    private static String token;
    private static boolean pause=true;
    private static String jaccount;
    public static String surl = "http://202.120.47.213:12301/api";
    public static String axwUrl = "http://202.120.47.213:12301/";
    public static String imgSurl = "http://202.120.47.213:12301/img/";
    public  static Chat chat = null;
    public static dbmessage DataBaseM;
    private static HashMap<String,Integer> newOldStringToInt = new HashMap<String, Integer>();
    private static HashMap<Integer,String> newOldIntToString = new HashMap<Integer, String>();
    public static int notifyid = 0;
    public static Queue<NotifyMessage> sentMessages = new LinkedList<NotifyMessage>();
    public static int nowchat = -1;
    public static int whtherBindJC = 0;
    public static boolean wetherHaveNewVersion = false;
    public static String versionName = "";

    // This flag should be set to true to enable VectorDrawable support for API < 21
    static{
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DataBaseM = new dbmessage(this);
        newOldIntToString.put(Integer.valueOf(1),"全新");
        newOldIntToString.put(Integer.valueOf(2),"九成新");
        newOldIntToString.put(Integer.valueOf(3),"七成新");
        newOldIntToString.put(Integer.valueOf(4),"六成新及以下");

        newOldStringToInt.put("全新",Integer.valueOf(1));
        newOldStringToInt.put("九成新",Integer.valueOf(2));
        newOldStringToInt.put("七成新",Integer.valueOf(3));
        newOldStringToInt.put("六成新及以下",Integer.valueOf(4));

        DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.icon_stub)
                .showImageForEmptyUri(R.drawable.icon_empty)
                .showImageOnFail(R.drawable.icon_error)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisc(false)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static void start(String _token){
        chat = new Chat(_token, ConnectionFactory.DEFAULT_URL);
        chat.addRecvCallBack(new Chat.ChatCallback() {
            @Override
            public void recv(org.json.JSONObject msgjson) {

                org.json.JSONArray msgs = null;
                try {

                    msgs = msgjson.getJSONArray("messages");
                    for (int i = 0; i < msgs.length(); i++){
                        int send = msgs.getJSONObject(i).getInt("from");
                        int recv = msgs.getJSONObject(i).getInt("to");
                        String time = msgs.getJSONObject(i).getString("time");
                        String content = msgs.getJSONObject(i).getString("content");
                        if (nowchat != send && recv == UserID && login_status == 1){
                            NotifyMessage ma = new NotifyMessage();
                            ma.setMessage(content);
                            ma.setWho(String.valueOf(send));
                            Date date= new Date();//创建一个时间对象，获取到当前的时间
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置时间显示格式
                            String str = sdf.format(date);//将当前时间格式化为需要的类型
                            ma.setTime(str);
                            sentMessages.add(ma);
                        }
                        DataBaseM.add(send, recv, content, 0,time);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //     System.out.println(messages.toString());
                //      for (int i = 0; i < messages.getMessages().size(); i++){
                //       int send = messages.getFrom(i);
                //         int recv = messages.getTo(i);
                //      String content = messages.getContent(i);
                //DataBaseM.add(send, recv, content);


            }
        });
        try {
            chat.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
    public static void stop(){

        try {
            if (chat !=null)
                chat.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<talkmessage> gettalklist(int mem){
        List<talkmessage> result = DataBaseM.getIntalk(Integer.toString(mem));
        return result;
    }
    public static void publish(String str, int dest){
        chat.publish(str, Integer.toString(dest));
        System.out.println("Send :"+str+dest);
    }
    public static void add(int sender, int recver, String doc, int isRead,String time){
        DataBaseM.add(sender, recver, doc, isRead,time);
    }
    public static List<talkmessage> gettalk(int sender, int recv){
        List<talkmessage> result = DataBaseM.getIn(Integer.toString(sender), Integer.toString(recv));
        return result;
    }
    public static void update(talkmessage st){
        DataBaseM.update(st.getMessageid());
    }
    public static int query(int recv){
        return DataBaseM.count(recv);
    }
    private static boolean AllowChatThread = true;
    private static int UserID;
    private static int Chat_Num = 0;
    private static int prename=-1;
    private static boolean end = true;
    private SharedPreferences sharedPreferences;
    public static void setPrename(int _prename){
        prename=_prename;
    }
    private static boolean chat_othermsg=true;
    public static void setChat_othermsg(boolean _chat_othermsg){
        chat_othermsg=_chat_othermsg;
    }
    public static boolean getChat_othermsg(){
        return chat_othermsg;
    }
    public static int getPrename(){
        return prename;
    }

    public static String getSurl () {
        return surl;
    }
    public static void setEnd(boolean _end){
        end = _end;
    }
    public static boolean getEnd(){
        return end;
    }
    public static void setAllowChatThread(boolean aa){
        AllowChatThread = aa;
    }
    public static int getChat_Num(){
        return Chat_Num;
    }
    public static void setChat_Num(int _Chat_Num){
        Chat_Num = _Chat_Num;
    }
    public static void setPause(boolean _pause){
        pause = _pause;

    }
    public static boolean getPause(){
        return pause;
    }
    public static boolean getAllowChatThread(){
        return AllowChatThread;
    }
    public static void setUserID(int _UserID){
        UserID = _UserID;
    }
    public static int getUserID(){
        return UserID;
    }

    public static void setToken (String _token) {
        token = _token;
    }

    public static String getToken () {
        return token;
    }
    public static int getLogin_status () {
        return login_status;
    }

    public static void setLogin_status (int s) {
        login_status = s;
    }


    public static void setUser_name (String s) {
        user_name = s;
    }

    public static String getUser_name () {
        return user_name;
    }

    public static String getNewOldString(int i){
        return newOldIntToString.get(Integer.valueOf(i));
    }

    public static int getNewOldInt(String name){
        return newOldStringToInt.get(name).intValue();
    }

    public static void setJaccount(String jc){
        jaccount = jc;
    }

    public static String getJaccount(){
        return jaccount;
    }

}
