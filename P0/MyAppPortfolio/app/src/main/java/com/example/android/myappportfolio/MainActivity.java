package com.example.android.myappportfolio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button) view;
            Toast.makeText(MainActivity.this, getString(R.string.project_msg,
                    btn.getText().toString().toLowerCase()), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button project1Button = (Button) findViewById(R.id.project1);
        project1Button.setOnClickListener(mButtonClickListener);

        Button project2Button = (Button) findViewById(R.id.project2);
        project2Button.setOnClickListener(mButtonClickListener);

        Button project3Button = (Button) findViewById(R.id.project3);
        project3Button.setOnClickListener(mButtonClickListener);

        Button project4Button = (Button) findViewById(R.id.project4);
        project4Button.setOnClickListener(mButtonClickListener);

        Button project5Button = (Button) findViewById(R.id.project5);
        project5Button.setOnClickListener(mButtonClickListener);

        Button project6Button = (Button) findViewById(R.id.project6);
        project6Button.setOnClickListener(mButtonClickListener);
    }
}
