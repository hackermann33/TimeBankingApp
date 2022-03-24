package it.polito.timebankingapp

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    lateinit var picBox: ImageView
    lateinit var usr: User
    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)

        val i = intent
        usr = intent.getSerializableExtra("it.polito.timebankingapp.ShowProfileActivity.user") as User

        val picEdit: ImageView
        picBox = findViewById(R.id.profile_pic)
        picEdit = findViewById<ImageButton>(R.id.uploadProfilePicButton)
        picEdit.setOnClickListener {
            dispatchTakePictureIntent()
        }
        displayUser(usr)

    }

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            picBox.setImageBitmap(imageBitmap)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val df: java.text.DateFormat? =
            android.text.format.DateFormat.getDateFormat(applicationContext)
        val timeStamp: String =
            android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", Date()).toString()

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return storageDir ?: File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
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

    /*Save the */
    private fun saveProfilePicture(pic: ImageView) {

        val resolver = applicationContext.contentResolver
        // Create the File where the photo should go
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File

            null
        }

        val out: FileOutputStream = FileOutputStream(photoFile)
        pic.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.android.fileprovider",
                it
            )

            val newImageDetails = ContentValues().apply{
                put(MediaStore.Images.Media.DISPLAY_NAME, "new pic details")
            }

            resolver.insert(photoURI, newImageDetails)
        }
    }


    /*To trigger back button pressed */
    override fun onBackPressed() {

        // Intent in order to save state and send it to showprofile

        retrieveUserData()

        if(usr.isGood()){
            val returnIntent : Intent = Intent()
            returnIntent.putExtra("it.polito.timebankingapp.EditProfileActivity.user", usr)
            setResult(RESULT_OK,returnIntent)
        }
        super.onBackPressed()
        return
    }

    private fun displayUser(usr: User) {
        val nameEdit = findViewById<EditText>(R.id.editFullName)
        nameEdit.setText(usr.fullName)

        val nickEdit = findViewById<EditText>(R.id.editNickname)
        nickEdit.setText(usr.nick)

        val emailEdit = findViewById<EditText>(R.id.editEmail)
        emailEdit.setText(usr.email)

        val locationEdit = findViewById<EditText>(R.id.editLocation)
        locationEdit.setText(usr.location)

    }

    private fun retrieveUserData() {
        val nameEdit = findViewById<EditText>(R.id.editFullName)
        this.usr.fullName = nameEdit.text.toString()

        val nickEdit = findViewById<EditText>(R.id.editNickname)
        usr.nick = nickEdit.text.toString()

        val emailEdit = findViewById<EditText>(R.id.editEmail)
        usr.email = emailEdit.text.toString()

        val locationEdit = findViewById<EditText>(R.id.editLocation)
        usr.location = locationEdit.text.toString()

    }

}
