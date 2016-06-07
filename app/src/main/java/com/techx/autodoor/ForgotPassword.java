package com.techx.autodoor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by change on 5/23/2016.
 */
public class ForgotPassword extends AppCompatActivity  {


    EditText inputEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        //Grab the  edit text for Request Access

        inputEmail= (EditText) findViewById(R.id.forgot_email);
        Button btnRequest = (Button) findViewById(R.id.forgot_password_button);



        // Request  click event
        btnRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                // Check for empty data in the form
                if (!email.isEmpty()) {
                    // make a new access request
                    // Display the error message
                    Toast.makeText(getApplicationContext(),
                            "Sever error!!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    // Display the error message
                    Toast.makeText(getApplicationContext(),
                            "One or more fields are empty", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

    }



}
