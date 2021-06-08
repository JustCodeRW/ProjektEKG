package com.example.ekg_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registration : AppCompatActivity() {
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private lateinit var registeredName: TextInputLayout
    private lateinit var registeredEmail: TextInputLayout
    private lateinit var registeredPhoneNo: TextInputLayout
    private lateinit var registeredPassword: TextInputLayout
    private lateinit var registeredUsername: TextInputLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Hooks
        registeredName = findViewById(R.id.name)
        registeredUsername = findViewById(R.id.username)
        registeredEmail = findViewById(R.id.email)
        registeredPhoneNo = findViewById(R.id.phoneNo)
        registeredPassword = findViewById(R.id.password)

//        val login_registerBtn: Button = findViewById(R.id.login_registrationBtn)


//        val intent = Intent(this@Registration, UserProfile::class.java)
//        startActivity(intent)

    }

    private fun validateName(): Boolean {
        val value = registeredName.editText?.text.toString()

        return if (value.isEmpty()) {
            registeredName.error = "Field cannot be empty"
            false
        } else {
            registeredName.error = null
            registeredName.isErrorEnabled = false
            true
        }
    }

    private fun validateUserName(): Boolean {
        val value: String = registeredUsername.editText?.text.toString()
        val noWhiteSpace = "\\A\\w{4,20}\\z"

        if (value.isEmpty()) {
            registeredUsername.error = "Field cannot be empty"
            return false
        } else if (value.length >= 15) {
            registeredUsername.error = "Username too long"
            return false
        } else if (!value.matches(noWhiteSpace.toRegex())) {
            registeredUsername.error = "White Spaces are not allowed"
            return false
        } else {
            registeredUsername.error = null
            registeredUsername.isErrorEnabled = false
            return true
        }
    }

    private fun validateEmail(): Boolean {
        val value = registeredEmail.editText?.text.toString()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (value.isEmpty()) {
            registeredEmail.error = "Field cannot be empty"
            return false
        } else if (!value.matches(emailPattern.toRegex())) {
            registeredEmail.error = "Invalid Email"
            return false
        } else {
            registeredEmail.error = null
            registeredEmail.isErrorEnabled = false
            return true
        }
    }

    private fun validatePhoneNo(): Boolean {
        val value: String = registeredPhoneNo.editText?.text.toString()

        if (value.isEmpty()) {
            registeredPhoneNo.error = "Field cannot be empty"
            return false
        } else {
            registeredPhoneNo.error = null
            registeredPhoneNo.isErrorEnabled = false
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val value: String = registeredPassword.editText?.text.toString()
        val passwordPattern = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$"

        if (value.isEmpty()) {
            registeredPassword.error = "Field cannot be empty"
            return false
        } else if (!value.matches(passwordPattern.toRegex())) {
            registeredPassword.error = "Password is to weak"
            return false
        } else {
            registeredPassword.error = null
            registeredPassword.isErrorEnabled = false
            return true
        }
    }


    fun registerUser(view: View) {
        if (!validateName() or !validateUserName() or  !validateEmail() or  !validatePhoneNo() or  !validatePassword()) {
            return
        }

        //save data in firebase (database) on button click
        rootNode = FirebaseDatabase.getInstance("https://ekg-app-f4d09-default-rtdb.europe-west1.firebasedatabase.app/")
        reference = rootNode.getReference("users")

        val name = registeredName.editText?.text.toString()
        val userName = registeredUsername.editText?.text.toString()
        val email = registeredEmail.editText?.text.toString()
        val phoneNo = registeredPhoneNo.editText?.text.toString()
        val password = registeredPassword.editText?.text.toString()

        val dataModel = UserDataModel(name, userName, email, phoneNo, password)

        reference.child(phoneNo).setValue(dataModel)

    }
}
