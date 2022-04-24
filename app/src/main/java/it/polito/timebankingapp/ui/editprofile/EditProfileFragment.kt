package it.polito.timebankingapp.ui.editprofile


import android.R.attr
import android.app.Activity.RESULT_OK
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
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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

class EditProfileFragment : Fragment(R.layout.fragment_editprofile) {

    private lateinit var profilePic: CircleImageView
    private var usr: User = User()
    private lateinit var skillsGroup: ChipGroup

    private lateinit var v : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        v = view
        usr = arguments?.getSerializable("profile") as User? ?: User()



        profilePic = view.findViewById(R.id.profile_pic)
        val sv = view.findViewById<ScrollView>(R.id.editScrollView2)
        val editPic = view.findViewById<FrameLayout>(R.id.editPic)

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

        val picEdit = view.findViewById<ImageButton>(R.id.uploadProfilePicButton)
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

        val addSkillButton = view.findViewById<Button>(R.id.addSkillButton)
        skillsGroup = view.findViewById(R.id.editSkillsGroup)

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

        showProfile(view)


    }

    private fun showProfile(view: View) {
        val nameEdit = view.findViewById<EditText>(R.id.editFullName)
        nameEdit.setText(usr.fullName)

        val nickEdit = view.findViewById<EditText>(R.id.editNickname)
        nickEdit.setText(usr.nick)

        val emailEdit = view.findViewById<EditText>(R.id.editEmail)
        emailEdit.setText(usr.email)

        val locationEdit = view.findViewById<EditText>(R.id.editLocation)
        locationEdit.setText(usr.location)

        val descriptionEdit = view.findViewById<EditText>(R.id.editDescription)
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

    private fun updateSkillsHints(): AutoCompleteTextView {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SKILLS.filter { sk -> !usr.skills.contains(sk) }
        )
        val newSkillView = v.findViewById<View>(R.id.editNewSkill) as AutoCompleteTextView
        newSkillView.setAdapter(adapter)
        return newSkillView
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
                    val ins = requireActivity().contentResolver.openInputStream(imageUri)
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

    private fun saveToInternalStorage(bitmapImage: Bitmap): String {
        val cw = ContextWrapper(requireContext())
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

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun retrieveUserData() {

        val nameEdit = v.findViewById<EditText>(R.id.editFullName)
        this.usr.fullName = nameEdit.text.toString()

        val nickEdit = v.findViewById<EditText>(R.id.editNickname)
        usr.nick = nickEdit.text.toString()

        val emailEdit = v.findViewById<EditText>(R.id.editEmail)
        usr.email = emailEdit.text.toString()

        val locationEdit = v.findViewById<EditText>(R.id.editLocation)
        usr.location = locationEdit.text.toString()

        val descriptionEdit = v.findViewById<EditText>(R.id.editDescription)
        usr.description = descriptionEdit.text.toString()

    }

    override fun onDetach() {

        /* Intent in order to save state and send it to showprofile*/
        retrieveUserData()

        if (usr.isValid()) {
            retrieveUserData()
            usr.pic = saveToInternalStorage(profilePic.drawable.toBitmap())
            val b = Bundle()
            b.putSerializable("user", usr)
            setFragmentResult("profile", b)
            //returnIntent.putExtra("it.polito.timebankingapp.EditProfileActivity.user", usr)

        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Profile NOT edited!")
                .setMessage("Profile modification was not performed because some fields were not correctly filled. Please, try again.")
                .setPositiveButton("Ok") { _, _ ->
                    evidenceWrongFields()
                }
                .show()

        }

        super.onDetach()
        return
    }

    private fun evidenceWrongFields() {

        val nameEditLay = v.findViewById<TextInputLayout>(R.id.editFullNameLay)
        val nameEdit = v.findViewById<TextInputEditText>(R.id.editFullName)
        if (nameEdit.text?.isEmpty() == true)
            nameEditLay.error = "Field cannot be empty!"
        else
            nameEditLay.error = null

        val nickEditLay = v.findViewById<TextInputLayout>(R.id.editNicknameLay)
        val nickEdit = v.findViewById<TextInputEditText>(R.id.editNickname)
        if (nickEdit.text?.isEmpty() == true)
            nickEditLay.error = "Field cannot be empty!"
        else
            nickEditLay.error = null

        val emailEditLay = v.findViewById<TextInputLayout>(R.id.editEmailLay)
        val emailEdit = v.findViewById<TextInputEditText>(R.id.editEmail)
        if (emailEdit.text?.isEmpty() == true)
            emailEditLay.error = "Field cannot be empty!"
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdit.text.toString()).matches())
            emailEditLay.error = "Insert a valid e-mail!"
        else
            emailEditLay.error = null

        val locationEditLay = v.findViewById<TextInputLayout>(R.id.editLocationLay)
        val locationEdit = v.findViewById<TextInputEditText>(R.id.editLocation)
        if (locationEdit.text?.isEmpty() == true)
            locationEditLay.error = "Field cannot be empty!"
        else
            locationEditLay.error = null

        val descriptionEditLay = v.findViewById<TextInputLayout>(R.id.editDescriptionLay)
        val descriptionEdit = v.findViewById<TextInputEditText>(R.id.editDescription)
        if (descriptionEdit.text?.isEmpty() == true)
            descriptionEditLay.error = "Field cannot be empty!"
        else
            descriptionEditLay.error = null

    }

}

