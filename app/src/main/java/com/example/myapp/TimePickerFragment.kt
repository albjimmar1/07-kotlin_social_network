package com.example.myapp

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(val listener: (hour: Int, minute: Int) -> Unit) :
    DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        listener(hour, minute)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c: Calendar = Calendar.getInstance()
        val hour: Int = c.get(Calendar.HOUR_OF_DAY)
        val minute: Int = c.get(Calendar.MINUTE)
        val picker = TimePickerDialog(
            activity as Context,
            R.style.pickerTheme,
            this,
            hour,
            minute,
            true
        )
        return picker
    }
}