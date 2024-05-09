package bku.smarthome.smarthome_iot;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import java.nio.charset.Charset;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Setting extends AppCompatActivity{
    Button save_btn, back_main;
    TextInputLayout tempSet, humiSet;
    EditText temperatureEditText, humidityEditText;
    MQTTHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        save_btn = findViewById(R.id.save_btn);
        tempSet = findViewById(R.id.tempSet);
        humiSet = findViewById(R.id.humiSet);
        back_main = findViewById(R.id.mainNow);
        temperatureEditText = findViewById(R.id.temperatureEditText);
        humidityEditText = findViewById(R.id.humidityEditText);

        //    For back main button
        back_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        // For Save Button push data to feed set_temp and set_humi
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temperature = temperatureEditText.getText().toString();
                String humidity = humidityEditText.getText().toString();
                sendDataMQTT("huynguyenk21ce/feeds/set_temp",temperature);
                sendDataMQTT("huynguyenk21ce/feeds/set_humi",humidity);
            }
        });

        startMQTT();
    }

    // send data MQTT
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}
