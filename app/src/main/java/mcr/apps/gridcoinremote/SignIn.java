package mcr.apps.gridcoinremote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by cardo on 10/29/2016.
 */

public class SignIn extends AppCompatActivity {
    static String ipFieldString = null;
    static String portFieldString = null;
    static String UsernameFieldString = null;
    static String PasswordFieldString = null;
    static boolean SignInformationFilled = false;
    static boolean EditMode = false;
    static boolean RememberChecked = false;

    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        final TextView WelcomeText = findViewById(R.id.welcomeText);
        final TextView HowToEnableRPCLink = findViewById(R.id.HowToEnableRPC);
        final EditText ipField = findViewById(R.id.IPAddressText);
        final EditText portField = findViewById(R.id.PortText);
        final EditText UsernameField = findViewById(R.id.UsernameField);
        final EditText PasswordField = findViewById(R.id.PasswordTextBox);
        final CheckBox RememberBox = findViewById(R.id.RememberCheckBox);
        final Button button = findViewById(R.id.SaveSignInButton);
        if (EditMode)
            WelcomeText.setText("Wallet Settings");
        else
            WelcomeText.setText("Welcome!");
        if (!TextUtils.isEmpty(ipFieldString))
            ipField.setText(ipFieldString);
        if (!TextUtils.isEmpty(portFieldString))
            portField.setText(portFieldString);
        if (!TextUtils.isEmpty(UsernameFieldString))
            UsernameField.setText(UsernameFieldString);
        if (!TextUtils.isEmpty(PasswordFieldString))
            PasswordField.setText(PasswordFieldString);
        if (RememberChecked)
            RememberBox.setChecked(true);
        else
            RememberBox.setChecked(false);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                boolean ipFieldEmpty = false;
                boolean portFieldEmpty = false;
                boolean UsernameFieldEmpty = false;
                boolean PasswordFieldEmpty = false;
                if (ipField.getText().toString().equals(""))
                    ipFieldEmpty = true;
                else
                    ipFieldString = ipField.getText().toString();
                if (portField.getText().toString().equals(""))
                    portFieldEmpty = true;
                else
                    portFieldString = portField.getText().toString();
                if (UsernameField.getText().toString().equals(""))
                    UsernameFieldEmpty = true;
                else
                    UsernameFieldString = UsernameField.getText().toString();
                if (PasswordField.getText().toString().equals(""))
                    PasswordFieldEmpty = true;
                else
                    PasswordFieldString = PasswordField.getText().toString();
                if (ipFieldEmpty || portFieldEmpty || UsernameFieldEmpty || PasswordFieldEmpty ) {
                    SignInformationFilled = false;
                    String message = null, messageTitle = "Error";
                    message = "Please fill the following fields to proceed:" + System.getProperty("line.separator");
                    if (ipFieldEmpty)
                        message += "-IP Address Field" + System.getProperty("line.separator");
                    if (portFieldEmpty)
                        message += "-Port Field" + System.getProperty("line.separator");
                    if (UsernameFieldEmpty)
                        message += "-Username" + System.getProperty("line.separator");
                    if (PasswordFieldEmpty)
                        message += "-Password" + System.getProperty("line.separator");
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
                    builder.setTitle(messageTitle);
                    builder.setMessage(message)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    // Create the AlertDialog object and return it
                    AlertDialog notify = builder.create();
                    notify.show();
                } else {
                    SignInformationFilled = true;
                    if (RememberBox.isChecked()) {
                        RememberChecked = true;
                        SharedPreferences settings = getSharedPreferences("grcremote", MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("ip", ipFieldString);
                        editor.putString("port", portFieldString);
                        editor.putString("username", UsernameFieldString);
                        editor.putString("password", PasswordFieldString);
                        editor.apply();
                    } else {
                        RememberChecked = false;
                        try {
                            SharedPreferences settings = getSharedPreferences("grcremote", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove("ip");
                            editor.remove("port");
                            editor.remove("username");
                            editor.remove("password");
                            editor.apply();
                        } catch (Exception e) {
                            //nothing here
                        }

                    }
                    Intent Start = new Intent(SignIn.this, MainActivity.class);
                    startActivity(Start);
                }

            }
        });
        HowToEnableRPCLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gridcoinapp.xyz/EnableRPC"));
                startActivity(browserIntent);
            }
        });
    }
}
