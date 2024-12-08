package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AuthState
import ru.netology.nework.databinding.FragmentMainBinding
import ru.netology.nework.util.AppConst
import ru.netology.nework.util.loadAvatar
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val childNavHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val childNavController = childNavHostFragment.navController
        binding.bottomNavigation.setupWithNavController(childNavController)
        var token: AuthState? = null

        val menuItem = binding.topAppBar.menu.findItem(R.id.user)
        val actionView = menuItem.actionView
        val avatarView = actionView?.findViewById<ShapeableImageView>(R.id.avatar)

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
            avatarView?.loadAvatar(state.avatar)
        }

        binding.topAppBar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.user -> {
                    if (token?.id != 0 && token?.id.toString().isNotEmpty()) {
                        findNavController().navigate(
                            R.id.action_mainFragment_to_detailUserFragment,
                            bundleOf(AppConst.USER_ID to token?.id)
                        )
                    } else {
                        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                    }
                    true
                }

                else -> false
            }
        }
        return binding.root
    }
}


