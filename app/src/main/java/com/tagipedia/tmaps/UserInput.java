package com.tagipedia.tmaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;

/**
 * Created by tr on 3/8/18.
 */

public class UserInput extends AppCompatActivity implements Serializable {
    static EditText mapId;
    UserInput currentUser = this;
    public UserInput() {
    }
    public UserInput getcurrentUser(){
        return currentUser;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_input);
        mapId = (EditText) findViewById(R.id.map_id);
        Button goButton = (Button)  findViewById(R.id.search_map_btn);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                Intent intent = new Intent(UserInput.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mapId",mapId.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            } catch(Exception e) {
                e.printStackTrace();
            }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
