package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private lateinit var binding: FragmentNewJobBinding
    private val jobViewModel: JobViewModel by activityViewModels()
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
    private val emptyOffsetDateTime =
        OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    private var flagEmptyField = false
    var name = ""
    var position = ""
    var link = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNewJobBinding.inflate(inflater, container, false)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.name.addTextChangedListener {
            name = it.toString()
            flagEmptyField = false
            binding.apply {
                nameLayout.error = null
                buttonJobCreate.isChecked = updateStateButtonLogin()
            }
        }
        binding.position.addTextChangedListener {
            position = it.toString()
            flagEmptyField = false
            binding.apply {
                positionLayout.error = null
                buttonJobCreate.isChecked = updateStateButtonLogin()
            }
        }
        binding.link.addTextChangedListener {
            link = it.toString()
        }

        binding.buttonJobCreate.setOnClickListener {
            name.trim()
            position.trim()
            link.trim()

            if (name.isEmpty()) {
                binding.nameLayout.error = getString(R.string.empty_field)
                flagEmptyField = true
            }
            if (position.isEmpty()) {
                binding.positionLayout.error = getString(R.string.empty_field)
                flagEmptyField = true
            }

            if (flagEmptyField) {
                return@setOnClickListener
            }

            val startWork = binding.startWork.text.toString()
            val finishWork = binding.finishWork.text.toString()

            try {
                val dateStart = LocalDate.parse(startWork, formatter)
                    .atTime(0, 0)
                    .atOffset(ZoneOffset.UTC)
                val dateFinish =
                    if (finishWork == getString(R.string.present_time)) null else LocalDate.parse(
                        finishWork,
                        formatter
                    )
                        .atTime(0, 0)
                        .atOffset(ZoneOffset.UTC)

                jobViewModel.saveJob(
                    name,
                    position,
                    dateStart,
                    dateFinish ?: emptyOffsetDateTime,
                    link,
                )
                findNavController().navigateUp()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }

        dialogSelectDate()
        return binding.root
    }

    private fun dialogSelectDate() {
        binding.startWork.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_start))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { timeInMillis ->
                val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    .format(Date(timeInMillis))
                binding.startWork.text = date
                binding.finishWork.text = getString(R.string.present_time)
            }

            datePicker.show(parentFragmentManager, "START_DATE_PICKER")
        }

        binding.finishWork.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.date_finish)
                .setMessage(R.string.enter_dates)
                .setPositiveButton(R.string.select_date) { _, _ ->
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.date_finish))
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build()

                    datePicker.addOnPositiveButtonClickListener { timeInMillis ->
                        val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            .format(Date(timeInMillis))
                        binding.finishWork.text = date
                    }

                    datePicker.show(parentFragmentManager, "END_DATE_PICKER")
                }
                .setNegativeButton(R.string.present_time) { _, _ ->
                    binding.finishWork.text = getString(R.string.present_time)
                }
                .show()
        }
        if (binding.finishWork.text.isNullOrEmpty()) {
            binding.finishWork.text = getString(R.string.present_time)
        }
    }

    private fun updateStateButtonLogin(): Boolean {
        return name.isNotEmpty() && position.isNotEmpty()
    }
}