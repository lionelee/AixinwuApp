package com.aixinwu.axw.tools;

/**
 * Created by dell1 on 2016/4/23.
 */

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.activity.PersonalCenter;
import com.aixinwu.axw.activity.ProductDetailActivity;
import com.aixinwu.axw.activity.RawPictureActivity;
import com.aixinwu.axw.activity.VolunteerApply;
import com.aixinwu.axw.activity.WelcomeActivity;
import com.aixinwu.axw.adapter.NotifyMessage;
import com.aixinwu.axw.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
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
    private static String imgUrl;
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
    private static int themeId = 0;
    public static int theme[]= {R.style.AppTheme_Aixinwu, R.style.AppTheme_Seiee, R.style.AppTheme_UltraViolet,
            R.style.AppTheme_ChiliOil, R.style.AppTheme_LittleBoyBlue,R.style.AppTheme_Arcadia,R.style.AppTheme_Emperador};
    public static int transtheme[] = {R.style.AppTheme_Aixinwu_TransBar,R.style.AppTheme_Seiee_TransBar,R.style.AppTheme_UltraViolet_TransBar,
            R.style.AppTheme_ChiliOil_TransBar,R.style.AppTheme_LittleBoyBlue_TransBar,R.style.AppTheme_Arcadia_TransBar,R.style.AppTheme_Emperador_TransBar};

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        themeId = Integer.parseInt(preferences.getString(getString(R.string.pref_theme_key),getString(R.string.pref_theme_default)));
        setTheme(theme[themeId]);
        listenActivity();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    private void listenActivity(){
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                if(activity instanceof WelcomeActivity)return;
                if(activity instanceof MainActivity || activity instanceof PersonalCenter || activity instanceof ProductDetailActivity
                        || activity instanceof VolunteerApply || activity instanceof RawPictureActivity){
                    activity.setTheme(transtheme[themeId]);
                }else{
                    activity.setTheme(theme[themeId]);
                }
            }

            @Override
            public void onActivityStarted(final Activity activity) {
                if(activity instanceof WelcomeActivity || activity instanceof MainActivity)return;
                if(NetInfo.checkNetwork(activity))return;
                Snackbar snackbar = Snackbar.make(((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0),
                        "网络未连接或不可用,请检查设置", Snackbar.LENGTH_LONG)
                        .setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_SETTINGS);
                                activity.startActivity(intent);
                            }
                        });
                View view = snackbar.getView();
                ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.white));
                snackbar.show();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
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
    public static void setPause(boolean _pause){ pause = _pause; }
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

    public static void setImgUrl(String s){imgUrl = s;}

    public static String getImgUrl(){return imgUrl;}

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

    public static void setThemeId(int id){themeId = id;}

    public static int getCurTheme(){return theme[themeId];}

}
