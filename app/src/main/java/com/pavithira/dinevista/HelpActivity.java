package com.pavithira.dinevista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupDrawer();
        setupBottomNavigation(R.id.nav_settings); // Highlight settings menu as active
    }
}
