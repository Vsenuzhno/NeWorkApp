package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.OnJobInteractionListener
import ru.netology.nework.databinding.FragmentJobsBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel

@AndroidEntryPoint
class JobsFragment : Fragment() {
    private lateinit var binding: FragmentJobsBinding
    private val authViewModel: AuthViewModel by activityViewModels()
    private val jobViewModel: JobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJobsBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()
        val userId = arguments?.getInt(AppConst.USER_ID)!!

        val jobAdapter = JobAdapter(object : OnJobInteractionListener {
            override fun onRemoveJob(job: Job) {
                jobViewModel.removeJob(job.id)
            }
        })
        binding.listJobs.adapter = jobAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                jobViewModel.setId(userId)
                jobViewModel.getJobs(userId)
                jobViewModel.data.collectLatest {
                    jobAdapter.submitList(it)
                }
            }
        }
        binding.buttonAddJob.isVisible = userId == authViewModel.dataAuth.value?.id
        binding.buttonAddJob.setOnClickListener {
            parentNavController?.navigate(R.id.action_detailUserFragment_to_newJobFragment)
        }

        return binding.root
    }
}