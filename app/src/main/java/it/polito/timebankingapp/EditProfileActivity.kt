package it.polito.timebankingapp

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profilePic: CircleImageView
    private lateinit var usr: User

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)

        usr = intent.getSerializableExtra("it.polito.timebankingapp.ShowProfileActivity.user") as User


        profilePic = findViewById(R.id.profile_pic)
        try {
            val f = File(usr.pic, "profile.jpg")
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
            profilePic.setImageBitmap(bitmap)
        }
        catch (e : FileNotFoundException){
            e.printStackTrace()
        }


        val picEdit = findViewById<ImageButton>(R.id.uploadProfilePicButton)
        picEdit.setOnClickListener {
            var selectFromGallery = true

            /*val sw = findViewById<Switch>(R.id.selectFromGallerySwitch)
            selectFromGallery = sw.isChecked*/

            when(selectFromGallery){
                true -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_IMAGE)
                }
                false -> {
                    dispatchTakePictureIntent()
                }
            }
        }
        displayUser(usr)
    }

    private val PICK_IMAGE = 2
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            profilePic.setImageBitmap(imageBitmap)
        }
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            try{
                val imageUri : Uri = data?.data as Uri
                val ins = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(ins)
                profilePic.setImageBitmap(bitmap)
            }
            catch(e : Exception){
                e.printStackTrace()
            }
        }
    }

    /*
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
    */

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /*Save the */
    /*
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
    */

    /*To trigger back button pressed */
    override fun onBackPressed() {

        // Intent in order to save state and send it to showprofile

        retrieveUserData()

        if(usr.isGood()){
            val returnIntent = Intent()
            usr.pic = saveToInternalStorage(profilePic.drawToBitmap())
            returnIntent.putExtra("it.polito.timebankingapp.EditProfileActivity.user", usr)
            setResult(RESULT_OK,returnIntent)
        }
        super.onBackPressed()
        return
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "profile.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
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
