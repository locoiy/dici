package com.example.locoiy.dici;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener {

    SensorManager sensorManager;
    TextView txv_location;
    TextView txv_cnt;
    Button btn_start;
    Button btn_stop;
    Button btn_clear;

    private long lastTime;
    private int count = 0;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float currentDegree = 0f;
    private ImageView image;
    private LinkedList<accObject> accList = new LinkedList<>();
    private LinkedList<magObject> magList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txv_location = (TextView)findViewById(R.id.textView);
        txv_cnt = (TextView)findViewById(R.id.textView_cnt);
        image = (ImageView) findViewById(R.id.imageView);
        image.setKeepScreenOn(true);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        btn_start = (Button)findViewById(R.id.button_start);
        btn_start.setOnClickListener(this);;
        btn_stop = (Button)findViewById(R.id.button_stop);
        btn_stop.setOnClickListener(this);
        btn_clear = (Button)findViewById(R.id.button_clear);
        btn_clear.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Sensor sensor;
        try {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if(null == sensor) {
                txv_location.setText("没有地磁传感器！");
                return;
            }
            sensorManager.registerListener(this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME);

            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(null == sensor) {
                txv_location.setText("没有加速度传感器！");
                return;
            }
            sensorManager.registerListener(this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }catch (RuntimeException e) {
            txv_location.setText(e.getMessage());
        }
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder sb = new StringBuilder();
        long curTime = System.currentTimeMillis();
        float[] values = new float[3];
        float[] R = new float[9];
        float[] m_values = new float[3];
        float sum_x = 0;
        float sum_y = 0;
        float sum_z = 0;
        double x, y, z;
        double sin_alpha,sin_beta,sin_gamma;
        double cos_alpha,cos_beta,cos_gamma;
        DecimalFormat df   = new DecimalFormat("###0.00");

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accObject acc = new accObject(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            if ( accList.size() > 32 ) {
                accList.pollFirst();
            }
            accList.addLast(acc);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magObject mag = new magObject(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            if ( magList.size() > 32 ) {
                magList.pollFirst();
            }
            magList.addLast(mag);
        } else {
            return;
        }

        if ( accList.size() < 32 || magList.size() < 32 )
        {
            return;
        }

        for (Iterator iter = accList.iterator(); iter.hasNext();)
        {
            accObject acc = (accObject)iter.next();
            sum_x += acc.getX();
            sum_y += acc.getY();
            sum_z += acc.getZ();
        }
        accelerometerValues[0]  = sum_x / accList.size();
        accelerometerValues[1]  = sum_y / accList.size();
        accelerometerValues[2]  = sum_z / accList.size();

        sum_x = 0;
        sum_y = 0;
        sum_z = 0;
        for (Iterator iter = magList.iterator(); iter.hasNext();)
        {
            magObject mag = (magObject)iter.next();
            sum_x += mag.getX();
            sum_y += mag.getY();
            sum_z += mag.getZ();
        }
        magneticFieldValues[0]  = sum_x / magList.size();
        magneticFieldValues[1]  = sum_y / magList.size();
        magneticFieldValues[2]  = sum_z / magList.size();

        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
//        values[0] = (float) Math.toDegrees(values[0]);
//        values[1] = (float) Math.toDegrees(values[1]);
//        values[2] = (float) Math.toDegrees(values[2]);
        values[0] = Float.parseFloat(df.format(values[0]));
        values[1] = Float.parseFloat(df.format(values[1]));
        values[2] = Float.parseFloat(df.format(values[2]));
        m_values[0] = Float.parseFloat(df.format(magneticFieldValues[0]));
        m_values[1] = Float.parseFloat(df.format(magneticFieldValues[1]));
        m_values[2] = Float.parseFloat(df.format(magneticFieldValues[2]));

        sb.append("地磁 x：").append(m_values[0]).append("， y：").append(m_values[1]).append("， z：").append(m_values[2]).append("\n");
        sb.append("角度 x：").append(df.format(Math.toDegrees(values[0]))).append("， y：").append(df.format(Math.toDegrees(values[1]))).append("， z：").append(df.format(Math.toDegrees(values[2]))).append("\n");
        sb.append("弧度 x：").append(values[0]).append("， y：").append(values[1]).append("， z：").append(values[2]).append("\n");

        sin_alpha = Math.sin(values[0]);
        sin_beta = Math.sin(values[1]);
        sin_gamma = Math.sin(values[2]);
        cos_alpha = Math.cos(values[0]);
        cos_beta = Math.cos(values[1]);
        cos_gamma = Math.cos(values[2]);
        x = (cos_alpha * cos_gamma - sin_alpha * sin_beta * sin_gamma) * m_values[0]
                + (sin_alpha * cos_beta) * m_values[1]
                + (cos_alpha * sin_gamma + sin_alpha * sin_beta * cos_gamma) * m_values[2];

        y = ((0-sin_alpha) * cos_gamma - cos_alpha * sin_beta * sin_gamma) * m_values[0]
                + (cos_alpha * cos_beta) * m_values[1]
                + ((0-sin_alpha) * sin_gamma + cos_alpha * sin_beta * cos_gamma) * m_values[2];

        z = ((0-cos_beta) * sin_gamma) * m_values[0]
                + (0-sin_beta) * m_values[1]
                + (cos_beta * cos_gamma) * m_values[2];

//        if ( curTime - lastTime < 400 )
//        {
//            return;
//        }
        lastTime = curTime;
        sb.append("x:").append(df.format(x)).append("\n");
        sb.append("y:").append(df.format(y)).append("\n");
        sb.append("z:").append(df.format(z)).append("\n");
        txv_location.setText(sb);

        float degree = (float)Math.toDegrees(values[0]);
        // 创建旋转动画（反向转过degree度）
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // 设置动画的持续时间
        ra.setDuration(200);
        // 设置动画结束后的保留状态
        ra.setFillAfter(true);
        // 启动动画
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_start:
                count++;
                txv_cnt.setText("第" + count + "组");
                break;
            case R.id.button_stop:
                break;
            case R.id.button_clear:
                break;
            default:
                break;
        }
    }
}