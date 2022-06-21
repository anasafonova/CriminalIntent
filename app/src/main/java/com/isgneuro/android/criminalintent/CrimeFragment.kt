package com.isgneuro.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import getRotatedAndScaledBitmap
import java.io.File
import java.util.*


private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_ID"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val DIALOG_IMAGE = "ImageFragment"


class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    var imageWidth : Int = 0
    var imageHeight : Int = 0

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

//    fun getContactPhoneNumber(context: Context, contactId: Long): String? {
//        var phoneNumber: String? = null
//        val whereArgs = arrayOf(contactId.toString())
//        Log.d(TAG, contactId.toString())
//        val cursor: Cursor? = context.contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            null,
//            ContactsContract.CommonDataKinds.Phone._ID + " = ?", whereArgs, null
//        )
//        val phoneNumberIndex: Int = cursor?
//            .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
//        Log.d(TAG, java.lang.String.valueOf(cursor?.getCount()))
//        if (cursor != null) {
//            Log.v(TAG, "Cursor Not null")
//            try {
//                if (cursor.moveToNext()) {
//                    Log.v(TAG, "Moved to first")
//                    Log.v(TAG, "Cursor Moved to first and checking")
//                    phoneNumber = cursor.getString(phoneNumberIndex)
//                }
//            } finally {
//                Log.v(TAG, "In finally")
//                cursor.close()
//            }
//        }
//        Log.v(TAG, "Returning phone number")
//        return phoneNumber
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data

// Указать, для каких полей ваш запрос должен возвращать значения .
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID, ContactsContract.CommonDataKinds.Phone.NUMBER)
// Выполняемый здесь запрос — contactUri похож на предложение "where"
                val cursor = contactUri?.let {
                        requireActivity().contentResolver
                            .query(
                                it,
                                queryFields, null, null, null
                            )
                    }
                //cursor?.toString()?.let { Log.d(TAG, it) }
                cursor?.use {
// Verify cursor contains at least one result
                            if (it.count == 0) {
                                return
                            }
// Первый столбец первой строки данных —
// это имя вашего подозреваемого.
                    it.moveToFirst()
                    Log.d(TAG, it.getString(0))
                    Log.d(TAG, it.getString(1))
                    Log.d(TAG, it.getString(2))
                    val suspect = it.getString(0)
                    val id = it.getString(1)
                    val phone = it.getString(2)
                    crime.suspect = suspect
                    //Log.d(TAG, suspect)
                    //Log.d(TAG, id)
                    crime.phone = phone
                    Log.d(TAG, crime.phone)
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                    //callButton.text = phone
                }
            }
//            requestCode == REQUEST_PHONE && data != null -> {
//                Log.d(TAG, data.toString())
//                //getContactPhoneNumber(requireContext(), data)
//            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeID: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeID)
//        Log.d(TAG, "args bundle crime ID: $crimeID")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton  = view.findViewById(R.id.crime_suspect) as Button
        callButton  = view.findViewById(R.id.crime_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        val observer = photoView.viewTreeObserver

        observer?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                imageWidth = view.width
                imageHeight = view.height
                //val bitmap = getScaledBitmap(photoFile.path,
                //    view.width,
                //    view.height)
                //photoView.setImageBitmap(bitmap)
            }
        })

//        dateButton.apply {
//            text = crime.date.toString()
//            isEnabled = false
//        }

        return view
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            Log.d(TAG, photoFile.path)
            //val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            val bitmap = getRotatedAndScaledBitmap(photoFile.path, //getScaledBitmap(photoFile.path,
                                         imageWidth,
                                         imageHeight)
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    override fun onStart() {
        super.onStart()



        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?,
                                       start: Int,
                                       count: Int,
                                       after: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {

            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener { Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent -> val chooserIntent =
            Intent.createChooser(intent,
                getString(R.string.send_report))
            startActivity(chooserIntent) } //startActivity(intent) }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI) //ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            //Log.d(TAG, pickContactIntent.data.toString())

            //pickContactIntent.addCategory(Intent.CATEGORY_HOME)

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            Log.d(TAG, resolvedActivity.toString())
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        callButton.apply {
            val number = crime.phone.filter { it.isLetterOrDigit() }
            val call = Uri.parse("tel:$number")
            val pickCallIntent =
                Intent(Intent.ACTION_DIAL, call)
            setOnClickListener {
                startActivity(pickCallIntent) //, REQUEST_PHONE)
            }
        }

        photoView.apply {
            setOnClickListener {
                ImageFragment.newInstance(photoFile).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_PHOTO)
                    show(this@CrimeFragment.parentFragmentManager, DIALOG_IMAGE)
                }
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(), "com.isgneuro.android.criminalintent.fileprovider", photoFile)
                    updateUI()
                }
            }
        )
    }

    private fun timeStringFromDate(date: Date) : String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        val s = calendar.get(Calendar.SECOND)
        return "${h}:${m}:${s}"
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        val dateFormat = DateFormat.getDateFormat(context)
        val s = dateFormat.format(crime.date)
        dateButton.text = s.toString() //crime.date.toString()
        timeButton.text = timeStringFromDate(crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
        {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
                getString(R.string.crime_report_no_suspect)
            } else {
                getString(R.string.crime_report_suspect, crime.suspect)
            }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
        fun newInstance(crimeID: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeID)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}