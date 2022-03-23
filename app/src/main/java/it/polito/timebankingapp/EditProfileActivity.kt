package it.polito.timebankingapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView


lateinit var nameEdit: EditText
lateinit var nickEdit: EditText
lateinit var emailEdit: EditText
lateinit var locationEdit: EditText
lateinit var balanceEdit: EditText
lateinit var picEdit: ImageView
lateinit var picBox : ImageView


class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)

        /*
        val i = intent //automatically initialized when the activity is created
        val v = i.getIntExtra("threshold", 0)
        tv3.text = "You reached $v"
         */
        picBox = findViewById(R.id.profile_pic)

        picEdit = findViewById<ImageButton>(R.id.uploadProfilePicButton)
        picEdit.setOnClickListener {
            dispatchTakePictureIntent()
        }

        nameEdit = findViewById<EditText>(R.id.editFullName)
        // nameEdit = quello che mi arriva da show profile
        //per recuperare il valore strValue = nameEdit.getText().toString();

        nickEdit = findViewById<EditText>(R.id.editNickname)
        // nickEdit = ...

        //emailEdit = findViewById<EditText>(R.id.editEmail) ??
        emailEdit = findViewById<EditText>(R.id.editEmail)
        // emailEdit = ...

        locationEdit = findViewById<EditText>(R.id.editLocation)
        // locationEdit = ...

        balanceEdit = findViewById<EditText>(R.id.editBalance)
        //balanceEdit.text = ...
    }

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            picBox.setImageBitmap(imageBitmap)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /*To trigger back button pressed */
    override fun onBackPressed() {
        super.onBackPressed();
        // Intent in order to save state and send it to showprofile

        //setResult(...) //????
        return
    }


}
