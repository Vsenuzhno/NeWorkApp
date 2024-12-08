package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import ru.netology.nework.R
import ru.netology.nework.databinding.DialogSelectDateEventBinding
import ru.netology.nework.dto.EventType
import ru.netology.nework.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DialogSelectDateEventFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogSelectDateEventBinding
    private val eventViewModel: EventViewModel by activityViewModels()
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm", Locale.getDefault())
    private var selectedDate: LocalDateTime? = null
    private var dateTimeString = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogSelectDateEventBinding.inflate(inflater, container, false)

        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.select_date))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(System.currentTimeMillis())
            .build()

        binding.text.setEndIconOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            val instant = Instant.ofEpochMilli(timestamp)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            selectedDate = LocalDateTime.of(localDate, LocalDateTime.now().toLocalTime())
            showTimePicker()
        }

        binding.dateText.addTextChangedListener {
            dateTimeString = it.toString()
        }

        binding.radioButtonOnline.isChecked = true
        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.radio_button_online -> eventViewModel.setEventType(EventType.ONLINE)
                else -> eventViewModel.setEventType(EventType.OFFLINE)
            }
        }

        return binding.root
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()

        timePicker.show(childFragmentManager, "timePicker")

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            selectedDate = selectedDate?.withHour(hour)?.withMinute(minute)
            selectedDate?.let {
                dateTimeString = it.format(formatter)
                binding.dateText.setText(dateTimeString)
            }
        }
    }

    override fun onDestroy() {
        if (dateTimeString.isNotEmpty()) {
            try {
                val odt =
                    LocalDateTime.parse(dateTimeString, formatter).atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                eventViewModel.setDateTime(odt)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onDestroy()
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
