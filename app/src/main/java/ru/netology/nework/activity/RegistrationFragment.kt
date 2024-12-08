package ru.netology.nework.activity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.error.ApiErrorAuth
import ru.netology.nework.viewmodel.AuthViewModel


@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private var login = ""
    private var name = ""
    private var password = ""
    private var confirmPassword = ""

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                binding.preview.setImageURI(uri)
                uploadImage(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.userPhoto.setOnClickListener {
            pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()
            if (state.id != 0 && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        binding.loginText.addTextChangedListener {
            login = it.toString()
            binding.apply {
                login.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.nameText.addTextChangedListener {
            name = it.toString()
            binding.apply {
                binding.name.error = null
                binding.buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.passwordText.addTextChangedListener {
            password = it.toString()
            binding.password.error = null
            binding.apply {
                repeatPassword.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.repeatPasswordText.addTextChangedListener {
            confirmPassword = it.toString()
            binding.apply {
                password.error = null
                repeatPassword.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.buttonLogin.setOnClickListener {
            login.trim()
            name.trim()
            password.trim()
            confirmPassword.trim()
            val loginEmpty = login.isEmpty()
            val nameEmpty = name.isEmpty()
            val passwordsMatch = password == confirmPassword
            val passwordEmpty = password.isEmpty()
            val confirmPasswordEmpty = confirmPassword.isEmpty()

            binding.apply {
                login.error = if (loginEmpty) getString(R.string.empty_login) else null
                name.error = if (nameEmpty) getString(R.string.empty_name) else null
                password.error = if (!passwordsMatch || passwordEmpty) {
                    if (passwordEmpty) getString(R.string.empty_password) else getString(R.string.passwords_dont_match)
                } else null
                repeatPassword.error = if (!passwordsMatch || confirmPasswordEmpty) {
                    if (confirmPasswordEmpty) getString(R.string.empty_password) else getString(R.string.passwords_dont_match)
                } else null
            }

            if (loginEmpty || nameEmpty || !passwordsMatch || passwordEmpty || confirmPasswordEmpty) {
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    when (authViewModel.registration(login, name, password)) {
                        ApiErrorAuth.UserRegistered -> toastMsg(R.string.user_registered)
                        ApiErrorAuth.IncorrectPhotoFormat -> toastMsg(R.string.incorrect_photo_format)
                        ApiErrorAuth.Success -> findNavController().navigateUp()
                        else -> toastMsg(R.string.unknown_error)
                    }
                }
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun uploadImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file = ru.netology.nework.util.FileUtils.uriToFile(requireContext(), uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                authViewModel.mediaSaveMedia(body)
            } catch (e: Exception) {
                toastMsg(R.string.error_loading_photo)
            }
        }
    }

    private fun updateButtonState(): Boolean {
        return login.isNotEmpty() && name.isNotEmpty() &&
                password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

    private fun toastMsg(msg: Int) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}