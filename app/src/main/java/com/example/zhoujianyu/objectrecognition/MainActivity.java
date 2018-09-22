package com.example.zhoujianyu.objectrecognition;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static final int ROW_NUM = 32;
    public static final int COL_NUM = 16;
    public static final short [][] capaData = new short[ROW_NUM][COL_NUM];
    public static final int BASE_THR = 50;
    public static final int HALF_BOTTOM_THR = 50;
    public static final int FILL_BOTTOM_THR = 150;
    public static final int COMP_THR = 200;
    public static final int BOOK_THR = 250;
    public static final int PHONE_THR = 300;

    public int seconds=0;
    public int minutes=0;
    public int hours=0;
    public int touchPoints[][] = {{2,0},{9,0},{22,0},{29,0}};
    public int diffs[] = new int[3];

    public TextView timer_view;
    public ImageView cup_view;
    public TextView water_amount_view;
    public RequestQueue myQueue;
    public Socket mSocket;
    public String socket_server_ip = "10.19.20.229";
    public String socket_port = "3000";
    public String display_server_ip="";
    public String display_port = "3000";
    public boolean schedule = false;
    public MediaPlayer mp1;
    public MediaPlayer mp2;
    public  void initSocket(String server_ip,String socket_port){
        try {
            mSocket = IO.socket("http://"+server_ip+":"+socket_port);
        } catch (URISyntaxException e) {}
        finally{
            mSocket.on("change",onNewMessageListener);
            mSocket.connect();
        }
    }
    private Emitter.Listener onNewMessageListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String type = (String)args[0];
            if(type.equals("come_back")){
                seconds = 0;
                minutes= 0;
                hours = 0;
                water_amount_view.setText("150ml");
                mp1.start();
                cup_view.setImageResource(R.drawable.water_150ml);
                if(!schedule) {
                    timer.schedule(task,0,1000);
                    schedule = true;
                }
                Log.e("bug","come_back received");
            }
            else if(type.equals("alarm")){
                Log.e("bug","alarm received");
                seconds = 55;
                minutes = 29;
            }
            else if(type.equals("no_cup")){
                cup_view.setVisibility(View.INVISIBLE);
                water_amount_view.setVisibility(View.INVISIBLE);
            }
            else if(type.equals("30ml")){
                Log.e("bug","30ml");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cup_view.setImageResource(R.drawable.water_30ml);
                        cup_view.setVisibility(View.VISIBLE);
                        water_amount_view.setText("30ml");
                        water_amount_view.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            seconds++;
            if(seconds>=60) {seconds = 0;minutes++;}
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timer_view.setText("00:"+(minutes<10?"0"+minutes:minutes)+":"+(seconds<10?"0"+seconds:seconds));
                    if(minutes==30 && seconds==0) mp2.start();
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mp1 = MediaPlayer.create(this,R.raw.welcome_back);
        mp2 = MediaPlayer.create(this,R.raw.drink_water);
        timer_view = findViewById(R.id.time);
        cup_view = findViewById(R.id.imageView);
        water_amount_view = findViewById(R.id.textView);
        initSocket(socket_server_ip,socket_port);
//        readDiffStart();
    }

    public void sendCloud(final String data_str,String server_ip,String port){
        /**
         * send the str to a remote server
         */
        String url = "http://"+server_ip+":"+port+"/";
        StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //nothing here
//                Log.e("bug",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("bug","reponse error!!!!!");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap data = new HashMap<String,String>();
                data.put("data",data_str);
                return data;
            }
        };
//        req.setRetryPolicy(new DefaultRetryPolicy(1000, 0, 1.0f));
        myQueue.add(req);
    }

    /**
     * callback method after everytime native_lib.cpp read an image of capacity data
     * The function first convert
     * @param data: 32*16 short array
     */
    public void processDiff(short[] data) throws InterruptedException{
        for(int k = 0;k<touchPoints.length;k++){
            int i = touchPoints[k][0];
            int j = touchPoints[k][1];
            int diff = data[i*COL_NUM+j]-capaData[i][j];
            capaData[i][j] = data[i*COL_NUM+j];
            diffs[k] = diff;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<diffs.length;i++){
                    if(diffs[i]>30){}
                }
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void readDiffStart ();
    public native void readDiffStop ();

}
