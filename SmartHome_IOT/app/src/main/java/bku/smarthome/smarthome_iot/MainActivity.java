package bku.smarthome.smarthome_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    MQTTHelper mqttHelper;
    TextView txtTemp,txtHumi;
    LabeledSwitch led_btn, door_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        led_btn = findViewById(R.id.btn_led);
        door_btn = findViewById(R.id.btn_door);

        // Firebase Authentication
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if(user == null)
        {
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }
        else
        {
            textView.setText(user.getEmail());
        }

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
                if(topic.contains("humi"))
                {
                    txtHumi.setText(message.toString()+ "RH");
                }
                else if(topic.contains("temp"))
                {
                    txtTemp.setText(message.toString() + "°C");
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

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}