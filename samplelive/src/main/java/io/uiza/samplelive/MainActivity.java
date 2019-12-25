package io.uiza.samplelive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.TooltipCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatButton btLoginFirebase;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.live_btn).setOnClickListener(this);
        findViewById(R.id.force_live_btn).setOnClickListener(this);
        btLoginFirebase = findViewById(R.id.bt_firebase_auth);
        TooltipCompat.setTooltipText(btLoginFirebase, "For Chat demo");
        btLoginFirebase.setOnClickListener(this);
        findViewById(R.id.setting_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_btn:
                startActivity(new Intent(MainActivity.this, LiveListActivity.class));
                break;
            case R.id.force_live_btn:
                startActivity(new Intent(MainActivity.this, InputActivity.class));
                break;
            case R.id.setting_btn:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.bt_firebase_auth:
                startActivity(new Intent(MainActivity.this, FirebaseAuthActivity.class));
                break;
            default:
                break;
        }
    }
}
