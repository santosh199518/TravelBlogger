package com.example.travelblogger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener{


    TextInputEditText name,email, newPassword, confirmPassword;
    Button changePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initializeView();

    }

    public void initializeView(){
        name=findViewById(R.id.username);
        email=findViewById(R.id.email);
        newPassword =findViewById(R.id.password);
        confirmPassword =findViewById(R.id.confirm_password);
        changePassword=findViewById(R.id.change_password);
        changePassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_password) {
            DBHelper helper = new DBHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            String p = Objects.requireNonNull(newPassword.getText()).toString();
            String c = Objects.requireNonNull(confirmPassword.getText()).toString();
            String e = Objects.requireNonNull(email.getText()).toString();
            String n = new UserData().capitalize(Objects.requireNonNull(name.getText()).toString());
            if (checkCredentials(p, c, e, n, db)) {
                db.execSQL("UPDATE " + DBHelper.login_table_name + " SET " + DBHelper.password + " = '" + p + " ' WHERE " +
                        DBHelper.email + "='" + e + "' AND " + DBHelper.name + "='" + n);
                Toast.makeText(getApplicationContext(), "Password Changed Successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public boolean checkCredentials(String p, String c, String e, String n,SQLiteDatabase db){
        boolean result=true;
        Cursor cursor=db.rawQuery( "SELECT * FROM "+DBHelper.login_table_name+" WHERE " +
                DBHelper.email+"='"+e+"' AND "+DBHelper.name+"='" + n+"'",null);
        if(!p.equals(c)){
            Toast.makeText(getApplicationContext(),"Passwords must be same",Toast.LENGTH_SHORT).show();
            result=false;
        }
        else if(cursor.getCount()==0){
            Toast.makeText(getApplicationContext(),"User Not Found",Toast.LENGTH_SHORT).show();
            result=false;
        }
        cursor.close();
        return result;
    }
}