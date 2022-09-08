package com.example.ekg_app

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

/*the registration Activity is needed to create a
* new user and save the user into the database
*/
class Registration : AppCompatActivity() {
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private lateinit var registeredName: TextInputLayout
    private lateinit var registeredEmail: TextInputLayout
    private lateinit var registeredPhoneNo: TextInputLayout
    private lateinit var registeredPassword: TextInputLayout
    private lateinit var registeredUsername: TextInputLayout

    private lateinit var image: ImageView
    private lateinit var logoText: TextView
    private lateinit var infoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        //here the animation are created
        registeredName = findViewById(R.id.name)
        registeredUsername = findViewById(R.id.username)
        registeredEmail = findViewById(R.id.email)
        registeredPhoneNo = findViewById(R.id.phoneNo)
        registeredPassword = findViewById(R.id.password)

        image = findViewById(R.id.logo_image)
        logoText = findViewById(R.id.logo_text)
        infoText = findViewById(R.id.infoText)

        val loginBtn: Button = findViewById(R.id.backToLoginBtn)
        val registerBtn: Button = findViewById(R.id.startRegisterBtn)

        //this listener points to the login Screen
        loginBtn.setOnClickListener {
            backToLogin()
        }

        //this listener register a new User
        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    //the onBackPressed method defines what happen if the button is pressed
    override fun onBackPressed() {
        backToLogin()
    }

    //this method takes the user back to the login Screen
    private fun backToLogin() {
        val intent = Intent(this@Registration, Login::class.java)
        val pair1: Pair<View, String> =
            Pair.create(image, "logo_image_transition")
        val pair2: Pair<View, String> =
            Pair.create(logoText, "logo_text_transition")
        val pair3: Pair<View, String> =
            Pair.create(infoText, "logo_desc_transition")
        val pair4: Pair<View, String> =
            Pair.create(registeredUsername, "username_transition")
        val pair5: Pair<View, String> =
            Pair.create(registeredPassword, "password_transition")

        val options = ActivityOptions.makeSceneTransitionAnimation(
            this@Registration,
            pair1,
            pair2,
            pair3,
            pair4,
            pair5,
        )
        startActivity(intent, options.toBundle())
    }

    /*this method creates a new user and transfers the
    * data into the database
    */
    private fun registerUser() {
        if (!validateName() or !validateUserName() or !validateEmail() or !validatePhoneNo() or !validatePassword()) {
            return
        }

        //save data in firebase (database) on button click
        rootNode =
            FirebaseDatabase.getInstance("https://ekg-app-f4d09-default-rtdb.europe-west1.firebasedatabase.app/")
        reference = rootNode.getReference("users")

        val name = registeredName.editText?.text.toString()
        val userName = registeredUsername.editText?.text.toString()
        val email = registeredEmail.editText?.text.toString()
        val phoneNo = registeredPhoneNo.editText?.text.toString()
        val password = registeredPassword.editText?.text.toString()

        val dataModel = UserDataModel(name, userName, email, phoneNo, password)

        reference.child(userName).setValue(dataModel)

        dataTransferToProfileView(userName)
    }

    /*the next few methods checks if
    * the user input data is correct and
    * the user is not yet created  */
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

        return if (value.isEmpty()) {
            registeredUsername.error = "Field cannot be empty"
            false
        } else if (value.length >= 15) {
            registeredUsername.error = "Username too long"
            false
        } else if (!value.matches(noWhiteSpace.toRegex())) {
            registeredUsername.error = "White Spaces are not allowed or username too short"
            false
        } else {
            registeredUsername.error = null
            registeredUsername.isErrorEnabled = false
            true
        }
    }

    private fun validateEmail(): Boolean {
        val value = registeredEmail.editText?.text.toString()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        return if (value.isEmpty()) {
            registeredEmail.error = "Field cannot be empty"
            false
        } else if (!value.matches(emailPattern.toRegex())) {
            registeredEmail.error = "Invalid Email"
            false
        } else {
            registeredEmail.error = null
            registeredEmail.isErrorEnabled = false
            true
        }
    }

    private fun validatePhoneNo(): Boolean {
        val value: String = registeredPhoneNo.editText?.text.toString()

        return if (value.isEmpty()) {
            registeredPhoneNo.error = "Field cannot be empty"
            false
        } else {
            registeredPhoneNo.error = null
            registeredPhoneNo.isErrorEnabled = false
            true
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

        return if (value.isEmpty()) {
            registeredPassword.error = "Field cannot be empty"
            false
        } else if (!value.matches(passwordPattern.toRegex())) {
            registeredPassword.error =
                "Password is to weak need a at least 4 characters and one symbol"
            false
        } else {
            registeredPassword.error = null
            registeredPassword.isErrorEnabled = false
            true
        }
    }

    //here the data from the user is transferred to the userProfile Screen
    private fun dataTransferToProfileView(userName: String) {
        val userReference: Query = reference.orderByChild("userName").equalTo(userName)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val passwordFromDB: String =
                        snapshot.child(userName).child("password").getValue(String::class.java)
                            .toString()
                    val nameFromDB: String? =
                        snapshot.child(userName).child("name").getValue(String::class.java)
                    val usernameFromDB: String? =
                        snapshot.child(userName).child("userName").getValue(String::class.java)
                    val phoneNoFromDB: String? =
                        snapshot.child(userName).child("phoneNo").getValue(String::class.java)
                    val emailFromDB: String? =
                        snapshot.child(userName).child("email").getValue(String::class.java)

                    val intent = Intent(this@Registration, UserProfile::class.java)

                    intent.putExtra("name", nameFromDB)
                    intent.putExtra("userName", usernameFromDB)
                    intent.putExtra("email", emailFromDB)
                    intent.putExtra("phoneNo", phoneNoFromDB)
                    intent.putExtra("password", passwordFromDB)

                    startActivity(intent)

                } else {
                    val intent = Intent(this@Registration, UserProfile::class.java)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Error in registration")
            }
        })
    }
}
