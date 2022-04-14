package it.polito.timebankingapp.ui.editprofile


import android.R.attr
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.user.User
import java.io.*


/* Global lists of skills,
   Every-time a user add a new skill in his profile, if not present in this list, it will be added!  */
private var SKILLS = arrayOf(
    "Gardening",
    "Tutoring",
    "Baby sitting",
    "Driver",
    "C developer",
    "Grocery shopping",
    "Cleaning and organization",
    "Cooking",
    "Data analytics",
    "Microsoft Excel",
)

const val REQUEST_PIC = 1

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profilePic: CircleImageView
    private lateinit var usr: User
    private lateinit var skillsGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)

        usr =
            intent.getSerializableExtra("it.polito.timebankingapp.ShowProfileActivity.user") as User


        profilePic = findViewById(R.id.profile_pic)
        val sv = findViewById<ScrollView>(R.id.editScrollView2)
        val editPic = findViewById<FrameLayout>(R.id.editPic)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            sv.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val h = sv.height
                    val w = sv.width
                    editPic.post { editPic.layoutParams = LinearLayout.LayoutParams(w, h / 3) }
                    sv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        try {
            val f = File(usr.pic)
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
            profilePic.setImageBitmap(bitmap)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val picEdit = findViewById<ImageButton>(R.id.uploadProfilePicButton)
        picEdit.setOnClickListener {

            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val chooser = Intent(Intent.ACTION_CHOOSER)
            chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
            chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

            val intentArray = arrayOf(cameraIntent)
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooser, REQUEST_PIC)
        }

        val addSkillButton = findViewById<Button>(R.id.addSkillButton)
        skillsGroup = findViewById(R.id.editSkillsGroup)

        val newSkillView = updateSkillsHints()

        addSkillButton.setOnClickListener {
            var skillStr = newSkillView.text.toString()
            skillStr = skillStr.lowercase()
                .replace("\n", " ")
                .trim()
                .replaceFirstChar { it.uppercase() }
                .replace("\\s+".toRegex(), " ")
                .replaceFirstChar { it.uppercase() }

            if (skillStr.isNotEmpty()) {
                /*Adding to the global list of skill hints*/
                if (!SKILLS.contains(skillStr))
                    SKILLS = SKILLS.plus(skillStr)
                if (!usr.skills.contains(skillStr)) {
                    usr.skills.add(skillStr)
                    addSkillChip(skillStr)
                }
                newSkillView.text.clear()
                updateSkillsHints()
            }
        }

        addSkillButton.textSize = (4 * resources.displayMetrics.density)

        if (usr.isValid()) {
            displayUser()
        }
    }

    private fun updateSkillsHints(): AutoCompleteTextView {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            SKILLS.filter { sk -> !usr.skills.contains(sk) }
        )
        val newSkillView = findViewById<View>(R.id.editNewSkill) as AutoCompleteTextView
        newSkillView.setAdapter(adapter)
        return newSkillView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        retrieveUserData()
        outState.putSerializable("user", usr)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        usr = savedInstanceState.getSerializable("user") as User
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PIC && resultCode == RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                usr.pic = saveToInternalStorage(imageBitmap)
            } else {
                try {
                    val imageUri: Uri = data?.data as Uri
                    val ins = contentResolver.openInputStream(imageUri)
                    imageBitmap = BitmapFactory.decodeStream(ins)
                    usr.pic = saveToInternalStorage(imageBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val ei = ExifInterface(usr.pic)
            ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val rotatedBitmap: Bitmap? = when (attr.orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap!!, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap!!, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap!!, 270)
                ExifInterface.ORIENTATION_NORMAL -> imageBitmap
                else -> imageBitmap
            }
            profilePic.setImageBitmap(rotatedBitmap)
        }
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }


    /*To trigger back button pressed */
    override fun onBackPressed() {

        /* Intent in order to save state and send it to showprofile*/
        retrieveUserData()

        if (usr.isValid()) {
            val returnIntent = Intent()
            usr.pic = saveToInternalStorage(profilePic.drawable.toBitmap())
            returnIntent.putExtra("it.polito.timebankingapp.EditProfileActivity.user", usr)
            setResult(RESULT_OK, returnIntent)
            super.onBackPressed()
        } else {

            AlertDialog.Builder(this)
                .setTitle("Review Your Data")
                .setMessage("Please, check again the fields that have not been correctly filled.")
                .setPositiveButton("Ok") { _, _ ->
                    evidenceWrongFields()
                }
                .show()
        }
        return
    }

    private fun evidenceWrongFields() {

        val nameEditLay = findViewById<TextInputLayout>(R.id.editFullNameLay)
        val nameEdit = findViewById<TextInputEditText>(R.id.editFullName)
        if (nameEdit.text?.isEmpty() == true)
            nameEditLay.error = "Field cannot be empty!"
        else
            nameEditLay.error = null

        val nickEditLay = findViewById<TextInputLayout>(R.id.editNicknameLay)
        val nickEdit = findViewById<TextInputEditText>(R.id.editNickname)
        if (nickEdit.text?.isEmpty() == true)
            nickEditLay.error = "Field cannot be empty!"
        else
            nickEditLay.error = null

        val emailEditLay = findViewById<TextInputLayout>(R.id.editEmailLay)
        val emailEdit = findViewById<TextInputEditText>(R.id.editEmail)
        if (emailEdit.text?.isEmpty() == true)
            emailEditLay.error = "Field cannot be empty!"
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdit.text.toString()).matches())
            emailEditLay.error = "Insert a valid e-mail!"
        else
            emailEditLay.error = null

        val locationEditLay = findViewById<TextInputLayout>(R.id.editLocationLay)
        val locationEdit = findViewById<TextInputEditText>(R.id.editLocation)
        if (locationEdit.text?.isEmpty() == true)
            locationEditLay.error = "Field cannot be empty!"
        else
            locationEditLay.error = null

        val descriptionEditLay = findViewById<TextInputLayout>(R.id.editDescriptionLay)
        val descriptionEdit = findViewById<TextInputEditText>(R.id.editDescription)
        if (descriptionEdit.text?.isEmpty() == true)
            descriptionEditLay.error = "Field cannot be empty!"
        else
            descriptionEditLay.error = null

    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String {
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
        return directory.absolutePath + "/profile.jpg"
    }

    private fun displayUser() {
        val nameEdit = findViewById<EditText>(R.id.editFullName)
        nameEdit.setText(usr.fullName)

        val nickEdit = findViewById<EditText>(R.id.editNickname)
        nickEdit.setText(usr.nick)

        val emailEdit = findViewById<EditText>(R.id.editEmail)
        emailEdit.setText(usr.email)

        val locationEdit = findViewById<EditText>(R.id.editLocation)
        locationEdit.setText(usr.location)

        val descriptionEdit = findViewById<EditText>(R.id.editDescription)
        descriptionEdit.setText(usr.description)

        usr.skills.forEach { skill ->
            addSkillChip(skill)
        }


    }

    private fun addSkillChip(text: String) {
        val chip = layoutInflater.inflate(
            R.layout.chip_layout_editprofile,
            skillsGroup.parent.parent as ViewGroup,
            false
        ) as Chip
        chip.text = text
        chip.setOnCloseIconClickListener {
            val ch = it as Chip
            usr.skills.remove(ch.text)
            skillsGroup.removeView(ch)
            updateSkillsHints()
        }
        skillsGroup.addView(chip)
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

        val descriptionEdit = findViewById<EditText>(R.id.editDescription)
        usr.description = descriptionEdit.text.toString()

    }

}

