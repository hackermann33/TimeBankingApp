package it.polito.timebankingapp.ui.profile.editprofile


import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


/* Global lists of skills,
   Every-time a user add a new skill in his profile, if not present in this list, it will be added!  */


const val REQUEST_PIC = 1

class EditProfileFragment : Fragment(R.layout.fragment_editprofile) {

    private lateinit var ivProfilePic: ImageView
    private lateinit var pbProfilePic: ProgressBar

    private var usr: User = User()
    private lateinit var allSkills : List<String>
    private lateinit var skillsGroup: ChipGroup


    private lateinit var v : View

    private val vm by activityViewModels<ProfileViewModel>()
    private val timeSlotsVm by activityViewModels<TimeSlotsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        allSkills = timeSlotsVm.skillList.value ?: listOf<String>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        v = view
        usr = arguments?.getSerializable("profile") as User? ?: User()



        ivProfilePic = view.findViewById(R.id.fragment_show_profile_iv_profile_pic)

        pbProfilePic = view.findViewById<ProgressBar>(R.id.fragment_show_profile_pb_profile_pic)
        vm.user.observe(viewLifecycleOwner) {
            Helper.loadImageIntoView(ivProfilePic, pbProfilePic, it.profilePicUrl)
        }


        val sv = view.findViewById<ScrollView>(R.id.editScrollView2)
        setProfilePicSize(view, sv)

        val btnEditPic = view.findViewById<ImageButton>(R.id.uploadProfilePicButton)
        btnEditPic.setOnClickListener {

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

        val btnAddSkill = view.findViewById<Button>(R.id.addSkillButton)
        skillsGroup = view.findViewById(R.id.editSkillsGroup)

        val newSkillView = updateSkillsHints()

        btnAddSkill.setOnClickListener {
            var skillStr = newSkillView.text.toString()
            skillStr = skillStr.lowercase()
                .replace("\n", " ")
                .trim()
                .replaceFirstChar { it.uppercase() }
                .replace("\\s+".toRegex(), " ")
                .replaceFirstChar { it.uppercase() }

            if (skillStr.isNotEmpty()) {
                /*Adding to the global list of skill hints*/
                if (!allSkills.contains(skillStr))
                    timeSlotsVm.addNewSkill(skillStr).addOnSuccessListener { Log.d("EditProfile", "skill add success") }
                        .addOnFailureListener { Log.d("EditProfile", "skill add failure: $it") }
                if (!usr.skills.contains(skillStr)) {
                    usr.skills.add(skillStr)
                    addSkillChip(skillStr)
                }
                newSkillView.text.clear()
                updateSkillsHints()
            }
        }

        btnAddSkill.textSize = (4 * resources.displayMetrics.density)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleProfileConfirmation()
        }

        showProfile(view)

    }

    private fun setProfilePicSize(view: View, sv: ScrollView) {
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
            R.layout.chip_layout_edit,
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
            allSkills.filter { sk -> !usr.skills.contains(sk) }
        )
        val newSkillView = v.findViewById<View>(R.id.editNewSkill) as AutoCompleteTextView
        newSkillView.setAdapter(adapter)
        return newSkillView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleProfileConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var editedImagePath = ""
        if (requestCode == REQUEST_PIC && resultCode == RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                //vm.editUserImage(imageBitmap)
                //editedImagePath = saveToInternalStorage(imageBitmap)
            } else {
                try {
                    val imageUri: Uri = data?.data as Uri
                    val ins = requireActivity().contentResolver.openInputStream(imageUri)
                    imageBitmap = BitmapFactory.decodeStream(ins)
                    //vm.editUserImage(imageBitmap)
                    //editedImagePath = saveToInternalStorage(imageBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            /*val ei = ExifInterface(editedImagePath)
            ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )*/
            pbProfilePic.visibility = View.VISIBLE
            val rotatedBitmap: Bitmap? = when (attr.orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap!!, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap!!, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap!!, 270)
                ExifInterface.ORIENTATION_NORMAL -> imageBitmap
                else -> imageBitmap
            }
            /*ivProfilePic.setImageBitmap(rotatedBitmap)*/


            vm.editUserImage(rotatedBitmap)
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

    private fun handleProfileConfirmation() {

        /* Intent in order to save state and send it to showprofile*/
        retrieveUserData()

        if (usr.isValid()) {
            retrieveUserData()
            //var path = saveToInternalStorage(profilePic.drawable.toBitmap(), true)

            vm.editUser(usr)
            //setFragmentResult("profile", b)
            //returnIntent.putExtra("it.polito.timebankingapp.EditProfileActivity.user", usr)

            // Use the Kotlin extension in the fragment-ktx artifact
            setFragmentResult("editProfile", bundleOf("editProfileConfirm" to true))
            findNavController().navigateUp()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Profile NOT edited!")
                .setMessage("Profile modification was not performed because some fields were not correctly filled. Please, try again.")
                .setPositiveButton("Ok") { _, _ ->
                    evidenceWrongFields()
                }
                .show()
        }

        return
    }

    private fun evidenceWrongFields() {

        val nameEditLay = v.findViewById<TextInputLayout>(R.id.editFullNameLay)
        val nameEdit = v.findViewById<TextInputEditText>(R.id.editFullName)

        when {
            nameEdit.text?.isEmpty() == true -> nameEditLay.error = "Field cannot be empty!"
            nameEdit.text?.length ?: 0 > 45  -> nameEditLay.error = "Required field too long!"
            else -> nameEditLay.error = null
        }

        val nickEditLay = v.findViewById<TextInputLayout>(R.id.editNicknameLay)
        val nickEdit = v.findViewById<TextInputEditText>(R.id.editNickname)
        when {
            nickEdit.text?.isEmpty() == true -> nickEditLay.error = "Field cannot be empty!"
            nickEdit.text?.length ?: 0 > 20 -> nickEditLay.error = "Required field too long!"
            else -> nickEditLay.error = null
        }

        val emailEditLay = v.findViewById<TextInputLayout>(R.id.editEmailLay)
        val emailEdit = v.findViewById<TextInputEditText>(R.id.editEmail)
        when {
            emailEdit.text?.isEmpty() == true -> emailEditLay.error = "Field cannot be empty!"
            !Patterns.EMAIL_ADDRESS.matcher(emailEdit.text.toString()).matches() -> emailEditLay.error = "Insert a valid e-mail!"
            emailEdit.text?.length ?: 0 > 45 -> emailEditLay.error = "Required field too long!"
            else -> emailEditLay.error = null
        }

        val locationEditLay = v.findViewById<TextInputLayout>(R.id.editLocationLay)
        val locationEdit = v.findViewById<TextInputEditText>(R.id.editLocation)
        when {
            locationEdit.text?.isEmpty() == true -> locationEditLay.error = "Field cannot be empty!"
            locationEdit.text?.length ?: 0 > 50 -> locationEditLay.error = "Required field too long!"
            else -> locationEditLay.error = null
        }

        val descriptionEditLay = v.findViewById<TextInputLayout>(R.id.editDescriptionLay)
        val descriptionEdit = v.findViewById<TextInputEditText>(R.id.editDescription)
        when {
            descriptionEdit.text?.isEmpty() == true -> descriptionEditLay.error = "Field cannot be empty!"
            descriptionEdit.text?.length ?: 0 > 200 -> descriptionEditLay.error = "Required field too long!"
            else -> descriptionEditLay.error = null
        }

    }

}

