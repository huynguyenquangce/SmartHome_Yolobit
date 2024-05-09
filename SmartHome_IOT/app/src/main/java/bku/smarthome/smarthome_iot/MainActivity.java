package bku.smarthome.smarthome_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.google.android.material.snackbar.Snackbar;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import androidx.appcompat.app.AlertDialog;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mqttMessageList = new ArrayList<>();
    FirebaseAuth auth;
    Button button,setting_btn,noti_btn;
    //    TextView textView;
    FirebaseUser user;

    MQTTHelper mqttHelper;
    TextView txtTemp,txtHumi;
    LabeledSwitch led_btn, door_btn,fan_btn,pump_btn;
    ProgressBar progTemp, progHumi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        led_btn = findViewById(R.id.btn_led);
        door_btn = findViewById(R.id.btn_door);
        fan_btn = findViewById(R.id.btn_fan);
        pump_btn = findViewById(R.id.btn_pump);
        progTemp = findViewById(R.id.progressBarTemp);
        progHumi = findViewById(R.id.progressBarHumi);
        setting_btn = findViewById(R.id.btn_settings);
        noti_btn = findViewById(R.id.notification);

        // Firebase Authentication
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
//        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if(user == null)
        {
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }
//        else
//        {
//            textView.setText(user.getEmail());
//        }

        // For settings
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Setting.class);
                startActivity(intent);
            }
        });


        // for notification button
        noti_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xây dựng dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Notification Messages");

                // Hiển thị danh sách thông điệp trong dialog
                StringBuilder notificationMessage = new StringBuilder();
                for (String message : mqttMessageList) {
                    notificationMessage.append(message).append("\n");
                }
                builder.setMessage(notificationMessage.toString());

                // Thêm nút đóng vào dialog
                builder.setPositiveButton("Close", null);

                // Tạo và hiển thị dialog
                AlertDialog dialogs = builder.create();
               dialogs.show();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });

        // led_btn
        led_btn.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true)
                {
                    sendDataMQTT("huynguyenk21ce/feeds/led-button","1");
                }
                else
                {
                    sendDataMQTT("huynguyenk21ce/feeds/led-button","0");
                }
            }
        });
        // door_btn
        door_btn.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true)
                {
                    sendDataMQTT("huynguyenk21ce/feeds/door-button","1");
                }
                else
                {
                    sendDataMQTT("huynguyenk21ce/feeds/door-button","0");
                }
            }
        });

        // fan_btn
        fan_btn.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true)
                {
                    sendDataMQTT("huynguyenk21ce/feeds/fan","2");
                }
                else
                {
                    sendDataMQTT("huynguyenk21ce/feeds/fan","0");
                }
            }
        });

        // pump_btn
        pump_btn.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true)
                {
                    sendDataMQTT("huynguyenk21ce/feeds/pump","1");
                }
                else
                {
                    sendDataMQTT("huynguyenk21ce/feeds/pump","0");
                }
            }
        });

        startMQTT();
    }


    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
        }
    }

    public void startMQTT()
    {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic +"***"+message.toString());
                String changeMsg = new String(message.toString());
//                showSnackbar("Adafruit Feed Update" + "Data on topic: " + topic + " has been updated to" + changeMsg);
                mqttMessageList.add("- Data on topic: " + topic.substring(21) + " has been updated to " + changeMsg);
                showNotification("Adafruit Feed Update", "Data on topic " + topic + " has been updated!");
                // Display notification
                if(topic.contains("humi"))
                {
                    txtHumi.setText(message.toString()+ "%");
                    // Cập nhật giá trị của Humi
                    int HumidityValue = Integer.parseInt(message.toString());
                    progHumi.setProgress(HumidityValue);
                }
                else if(topic.contains("temp"))
                {
                    txtTemp.setText(message.toString() + "°C");
                    // Cập nhật giá trị của ProgressBar nhiệt độ
                    int temperatureValue = Integer.parseInt(message.toString());
                    progTemp.setProgress(temperatureValue);
                }
                else if(topic.contains("led-button"))
                {
                    if(message.toString().equals("1"))
                    {
                        led_btn.setOn(true);
                    }
                    else
                    {
                        led_btn.setOn(false);
                    }
                }
                else if(topic.contains("door-button"))
                {
                    if(message.toString().equals("1"))
                    {
                        door_btn.setOn(true);
                    }
                    else
                    {
                        door_btn.setOn(false);
                    }
                }
                else if(topic.contains("fan"))
                {
                    if(message.toString().equals("2"))
                    {
                        fan_btn.setOn(true);
                    }
                    else
                    {
                        fan_btn.setOn(false);
                    }
                }
                else if(topic.contains("pump"))
                {
                    if(message.toString().equals("1"))
                    {
                        pump_btn.setOn(true);
                    }
                    else
                    {
                        pump_btn.setOn(false);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

//    private void showSnackbar(String message) {
//        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
//    }

}