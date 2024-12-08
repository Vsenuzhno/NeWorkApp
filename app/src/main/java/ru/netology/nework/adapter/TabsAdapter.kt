package ru.netology.nework.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nework.activity.JobsFragment
import ru.netology.nework.activity.PostsFragment
import ru.netology.nework.error.TabsError
import ru.netology.nework.util.AppConst

class TabsAdapter(fragment: Fragment, private val userId: Int?) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PostsFragment().apply {
                arguments = bundleOf(AppConst.USER_ID to userId)
            }

            1 -> JobsFragment().apply {
                arguments = bundleOf(AppConst.USER_ID to userId)
            }

            else -> throw TabsError
        }
    }
}