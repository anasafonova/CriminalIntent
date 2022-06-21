package com.isgneuro.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class TimePickerFragment : DialogFragment() {

//    interface Callbacks {
//        fun onTimeSelected(time: Time)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val timeListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
//            val resultTime : Date = GregorianCalendar(year, month, day).time
//            targetFragment?.let { fragment ->
//                (fragment as Callbacks).onDateSelected(resultDate)
//            }
//        }

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val is24Hour : Boolean = true

        return TimePickerDialog(
            requireContext(),
            null, //dateListener,
            initialHour,
            initialMinute,
            is24Hour
        )
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}