package com.isgneuro.android.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import getRotatedAndScaledBitmap
import getScaledBitmap
import java.io.File
import java.util.*


private const val ARG_FILE = "photo"
private const val TAG = "ImageFragment"

class ImageFragment: DialogFragment() {
    private lateinit var imageView: ImageView
    //private var photoUri: String = ""
    private lateinit var photoFile: File
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoFile = arguments?.getSerializable(ARG_FILE) as File
        Log.d(TAG, photoFile.path)
        //val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_image, null)
        //val dialog = MaterialAlertDialogBuilder(context!!)
        //    .setView(dialogView)
        //    .create()
        Log.d(TAG, "onCreate")
        setStyle(STYLE_NO_TITLE, 0)
        //Log.d(TAG, )
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.isgneuro.android.criminalintent.R.layout.fragment_image, container)
        imageView = view.findViewById(R.id.detailed_img) as ImageView
        //view.setOnClickListener(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Decode/load YOUR IMAGE HERE  */
        Log.d(TAG, photoFile.path)
        val bitmap = getRotatedAndScaledBitmap(photoFile.path, requireActivity())
        imageView.setImageBitmap(bitmap)
        //imageView.setImageResource(R.drawable.image)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_image, null)
        //val dialog = MaterialAlertDialogBuilder(context!!)
        //    .setView(dialogView)
        //    .create()
        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
//        //not working
//        Log.d(TAG, photoFile.path)
//        val bitmap = getScaledBitmap(photoFile.path, requireActivity())
//        imageView.setImageBitmap(bitmap)
        Log.d(TAG, "onCreateDialog")
        //dialogView
        //dialog.setCanceledOnTouchOutside(true)
        return dialog
    }
//
//    /* (non-Javadoc)
//     * @see android.view.View.OnClickListener#onClick(android.view.View)
//     */
//    fun onClick(v: View) {
//        if (v !== imageView) dismissAllowingStateLoss()
//    }


    companion object {
        fun newInstance(photoFile: File): DialogFragment {
            val args = Bundle().apply {
                putSerializable(ARG_FILE, photoFile)
            }

            Log.d(TAG, photoFile.path)


//            if (photoFile.exists()) {
//                val bitmap = getScaledBitmap(photoFile.path, requireActivity())
//                photoView.setImageBitmap(bitmap)
//            }

            return ImageFragment().apply {
                arguments = args
            }
        }
    }
}