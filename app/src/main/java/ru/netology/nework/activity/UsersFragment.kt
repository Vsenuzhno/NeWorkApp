package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.OnUserInteractionListener
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Int>>() {}.type

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()
        val arg = arguments?.containsKey("selectUser") ?: false
        val selectedUsers = mutableListOf<Int>()

        val listUsersConst = when {
            arguments?.containsKey(AppConst.LIKERS) == true -> {
                binding.topAppBar.title = getString(R.string.likers)
                gson.fromJson<List<Int>>(arguments?.getString(AppConst.LIKERS), typeToken)
            }

            arguments?.containsKey(AppConst.PARTICIPANT) == true -> {
                binding.topAppBar.title = getString(R.string.participants)
                gson.fromJson<List<Int>>(arguments?.getString(AppConst.PARTICIPANT), typeToken)
            }

            arguments?.containsKey(AppConst.MENTIONED) == true -> {
                binding.topAppBar.title = getString(R.string.mentioned)
                gson.fromJson<List<Int>>(arguments?.getString(AppConst.MENTIONED), typeToken)
            }

            else -> null
        }

        val userAdapter = UserAdapter(object : OnUserInteractionListener {

            override fun onSelectUser(userResponse: UserResponse) {
                if (selectedUsers.contains(userResponse.id)) {
                    selectedUsers.remove(userResponse.id)
                } else {
                    selectedUsers.add(userResponse.id)
                }
            }

            override fun onCardUser(userResponse: UserResponse) {
                parentNavController?.navigate(
                    R.id.action_mainFragment_to_detailUserFragment,
                    bundleOf(AppConst.USER_ID to userResponse.id)
                )
            }
        }, arg, selectedUsers)
        binding.listUsers.adapter = userAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.dataUser.collectLatest {
                    if (listUsersConst != null) {
                        userAdapter.submitData(
                            it.filter { item ->
                                item.id in listUsersConst
                            })
                    } else {
                        userAdapter.submitData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            userAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshUsers.isRefreshing =
                    it.refresh is LoadState.Loading

                if (it.refresh is LoadState.Error) {
                    Snackbar.make(
                        binding.root,
                        R.string.connection_error,
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(R.id.bottom_navigation)
                        .show()
                }
            }
        }

        binding.topAppBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                when {
                    arg -> {
                        menuInflater.inflate(R.menu.new_content_menu, menu)
                        binding.topAppBar.title = getString(R.string.select_users)
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        setFragmentResult(
                            "usersFragmentResult",
                            bundleOf("selectUser" to gson.toJson(selectedUsers))
                        )
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
            }
        })

        binding.swipeRefreshUsers.setOnRefreshListener {
            userAdapter.refresh()
        }

        binding.topAppBar.isVisible = arg || listUsersConst != null

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}