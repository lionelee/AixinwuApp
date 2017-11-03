package com.aixinwu.axw.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.LoginActivity;
import com.aixinwu.axw.activity.SendToAXw;
import com.aixinwu.axw.activity.SendToPeople;
import com.aixinwu.axw.tools.GlobalParameterApplication;

/**
 * Created by liangyuding on 2016/4/6.
 */
public class SubmitThings extends Fragment {
    private int iii;
    private View axw;
    private View people;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.submit_things,null);
        axw = view.findViewById(R.id.button3);
        people = view.findViewById(R.id.button);
        axw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                ImageView img1 = (ImageView) v.findViewById(R.id.imageAixinwu);
                ImageView img2 = (ImageView) v.findViewById(R.id.imageAixinwu1);

                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    img1.setVisibility(View.VISIBLE);
                    img2.setVisibility(View.GONE);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        axw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GlobalParameterApplication.getLogin_status()==1){
                    if (GlobalParameterApplication.whtherBindJC == 1){
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), SendToAXw.class);
                        startActivityForResult(intent,12);
                    }else {
                        Toast.makeText(getActivity(),"请先绑定jaccount",Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), LoginActivity.class);
                    startActivity(intent);

                }
            }
        });
        people.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ImageView img1 = (ImageView) v.findViewById(R.id.imageDeal1);
                ImageView img2 = (ImageView) v.findViewById(R.id.imageDeal);

                if ((event.getAction()) == MotionEvent.ACTION_DOWN){
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.VISIBLE);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    img1.setVisibility(View.VISIBLE);
                    img2.setVisibility(View.GONE);
                }



                return false;
            }
        });
        people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GlobalParameterApplication.getLogin_status()==1) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), SendToPeople.class);
                    startActivityForResult(intent,11);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), LoginActivity.class);
                    startActivity(intent);

                }
            }
        });




        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11){
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "人人商品发布成功", Toast.LENGTH_LONG);

            }

        }    else if (requestCode==12){
            if (resultCode == Activity.RESULT_OK){
                
            Toast.makeText(getActivity(),"爱心屋捐赠成功",Toast.LENGTH_LONG);}
        }

    }



}
