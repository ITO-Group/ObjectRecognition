package com.example.zhoujianyu.objectrecognition;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

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
    public int touchPoints[][] = {{1,0},{15,0},{30,0}};
    public int diffs[] = new int[3];
    public TextView[] textViews = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViews[0]=findViewById(R.id.touch1);
        textViews[1]=findViewById(R.id.touch2);
        textViews[2]=findViewById(R.id.touch3);
        readDiffStart();
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
                    if(diffs[i]>30)textViews[i].setText(Integer.toString(diffs[i]));
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
