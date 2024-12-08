package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.error.ApiErrorAuth
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    private var login = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.loginText.addTextChangedListener {
            login = it.toString()
            binding.apply {
                login.error = null
                buttonLogin.isChecked = updateStateButtonLogin()
            }
        }
        binding.passwordText.addTextChangedListener {
            password = it.toString()
            binding.apply {
                password.error = null
                buttonLogin.isChecked = updateStateButtonLogin()
            }
        }

        binding.buttonLogin.setOnClickListener {
            login.trim()
            password.trim()
            when {
                password.isEmpty() && login.isEmpty() -> {
                    binding.apply {
                        login.error = getString(R.string.empty_login)
                        password.error = getString(R.string.empty_password)
                    }
                }

                password.isEmpty() -> {
                    binding.password.error = getString(R.string.empty_password)
                }

                login.isEmpty() -> {
                    binding.login.error = getString(R.string.empty_login)
                }

                else -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            when (authViewModel.login(login, password)) {
                                ApiErrorAuth.IncorrectPassword -> toastMsg(R.string.incorrect_password)
                                ApiErrorAuth.UserNotFound -> toastMsg(R.string.user_not_found)
                                ApiErrorAuth.Success -> findNavController().navigateUp()
                                else -> toastMsg(R.string.unknown_error)
                            }
                        }
                    }
                }
            }
        }

        binding.buttonRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()
            if (state.id != 0 && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

    private fun updateStateButtonLogin(): Boolean {
        return login.isNotEmpty() && password.isNotEmpty()
    }

    private fun toastMsg(msg: Int) {
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_LONG
        ).show()
    }
}