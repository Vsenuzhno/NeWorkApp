package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.TabsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentDetailUserBinding
import ru.netology.nework.util.AppConst
import ru.netology.nework.util.loadImage
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DetailUserFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private lateinit var binding: FragmentDetailUserBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var tabsAdapter: TabsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDetailUserBinding.inflate(inflater, container, false)

        val tabLayout = binding.tabLayout
        val pager = binding.pager

        val userId = arguments?.getInt(AppConst.USER_ID)
        if (userId != null) {
            userViewModel.getUser(userId)
        }

        tabsAdapter = TabsAdapter(this, userId)
        binding.pager.adapter = tabsAdapter

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setText(R.string.wall)
                }

                1 -> {
                    tab.setText(R.string.jobs)
                }
            }
        }.attach()

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userPhoto.isVisible = user.avatar != null
                binding.userPhoto.loadImage(user.avatar)
                binding.topAppBar.title = buildString {
                    append(user.name + " / " + user.login)
                }
            }
        }

        if (userId == authViewModel.dataAuth.value?.id) {
            binding.topAppBar.inflateMenu(R.menu.profile_menu)
        }

        binding.topAppBar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.exit -> {
                    authViewModel.logout()
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}
