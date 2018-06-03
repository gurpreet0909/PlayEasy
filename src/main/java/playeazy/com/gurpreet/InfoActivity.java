package playeazy.com.gurpreet;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class InfoActivity extends AppCompatActivity {

    Button currentLocationButton;
    static TextView locationShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_layout);

        currentLocationButton = findViewById(R.id.locationButton);
        locationShow = findViewById(R.id.textView11);

        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            //    MapActivity.getAddressFromLocation(getApplicationContext(), new GeocoderHandler());

                Intent i = new Intent(InfoActivity.this,MapActivity.class);
                startActivity(i);

            }
        });


    }


    protected static class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            locationShow.setText(locationAddress);
        }
    }
}