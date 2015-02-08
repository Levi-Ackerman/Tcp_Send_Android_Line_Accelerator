package edu.scut.tcptest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;


public class MainActivity extends ActionBarActivity implements IoHandler {

    private IoSession ioSession;
    private NioSocketConnector connector;
    private Button btnSend;
    TextView tvToast;
    EditText etIP, etContent, etPort;
    private String strIP;
    //    private String strContent;
    private int port;

    SensorManager manager;
    Sensor sensor;
    boolean isOpening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        btnSend = (Button) findViewById(R.id.button);
        tvToast = (TextView) findViewById(R.id.toast);
        etIP = (EditText) findViewById(R.id.editText);
        etPort = (EditText) findViewById(R.id.editText3);
//        etContent = (EditText) findViewById(R.id.editText2);
        etIP.setText(getString("ip"));
        etPort.setText(getString("port"));
//        etContent.setText(getString("content"));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        if (!isOpening) {
                            try {
                                initData();
                                createConnection();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnSend.setText("停止发送");
                                    }
                                });
                                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

                                isOpening = !isOpening;
                            } catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "连接出错", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }

                        } else {
                            ioSession.close(true);
                            connector.dispose();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnSend.setText("发送");
                                    Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                                }
                            });
                            manager.unregisterListener(listener);
                            isOpening = !isOpening;
                        }
                    }
                }.start();
            }
        });
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent event) {
            try {

                final String value = event.timestamp + "," + event.values[2];
                final byte[] data = (value).getBytes("gbk");
                ioSession.write(IoBuffer.wrap(data));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvToast.setText(value);
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };



    public void initData() {
        connector = new NioSocketConnector(); // 创建连接客户端
        connector.setConnectTimeoutMillis(10000); // 设置连接超时
        // 添加处理器，主要负责收包
        connector.setHandler(this);
        strIP = etIP.getText().toString().trim();
//        strContent = etContent.getText().toString().trim();
        port = Integer.parseInt(etPort.getText().toString().trim());
        setConfig("ip", strIP);
        setConfig("port", port + "");
    }

    //将数据保存在历史记录中
    void setConfig(String key, String value) {
        getSharedPreferences("config", MODE_PRIVATE).edit().putString(key, value).commit();

    }

    //从历史记录存储中读数据
    String getString(String key) {
        return getSharedPreferences("config", MODE_PRIVATE).getString(key, null);
    }

    protected void createConnection() throws Exception {
        if (ioSession != null) {
            ioSession.close(true);
        }
        ConnectFuture future = connector
                .connect(new InetSocketAddress(
                        strIP, port));
        future.awaitUninterruptibly();// 等待连接创建成功
        ioSession = future.getSession();// 获取会话
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {

    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {

    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {

    }

    @Override
    public void messageReceived(IoSession ioSession, Object o) throws Exception {

    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {

    }
}
