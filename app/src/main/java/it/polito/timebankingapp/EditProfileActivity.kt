package it.polito.timebankingapp

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageButton
import android.text.Editable

import android.R

import android.text.TextWatcher

import com.google.android.material.textfield.TextInputLayout
import android.R
import android.R.attr.data


lateinit var nameEdit: EditText
lateinit var nickEdit: EditText
lateinit var emailEdit: EditText
lateinit var locationEdit: EditText
lateinit var balanceEdit: EditText
lateinit var picEdit: ImageEdit



class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)



        picEdit = findViewById<ImageButton>(R.id.uploadProfilePicButton)
        picEdit.setOnClickListener {
            dispatchTakePictureIntent()
        }

        nameEdit = (EditText) findViewById(R.id.editFullName)
        // nameEdit = quello che mi arriva da show profile
        //per recuperare il valore strValue = nameEdit.getText().toString();

        nickEdit = (EditText) findViewById(R.id.editNickname)
        // nickEdit = ...

        emailEdit = (EditText)findViewById<TextView>(R.id.editEmail)
        // emailEdit = ...

        locationEdit = (EditText)findViewById<TextView>(R.id.editLocation)
        // locationEdit = ...

        balanceEdit = (EditText)findViewById<TextView>(R.id.editBalance)
        //balanceEdit.text = ...
    }

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data.extras.get("data") as Bitmap
            imgEdit.setImageBitmap(imageBitmap)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val takePic = startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /*To trigger back button pressed */
    override fun onBackPressed() {
        // Intent in order to save state and send it to showprofile

        return
    }


}
