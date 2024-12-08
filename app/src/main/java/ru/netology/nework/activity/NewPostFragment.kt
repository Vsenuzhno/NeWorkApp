package ru.netology.nework.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.loadImage
import ru.netology.nework.viewmodel.PostViewModel
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private lateinit var binding: FragmentNewPostBinding
    private val postViewModel: PostViewModel by activityViewModels()
    private val gson = Gson()
    private val pointToken = object : TypeToken<Point>() {}.type
    private val usersToken = object : TypeToken<List<Long>>() {}.type

    companion object {
        var Bundle.text by StringArg
    }

    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    val file = fileUri.toFile()

                    postViewModel.setAttachment(fileUri, file, AttachmentType.IMAGE)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    private val videoResultContract =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val file = uri.toFile(requireContext())!!
                val size = file.length()
                if (size > 15728640) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.attachment_15MB),
                        Toast.LENGTH_LONG
                    ).show()
                    return@registerForActivityResult
                }
                postViewModel.setAttachment(
                    uri,
                    file,
                    AttachmentType.VIDEO
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val arg = arguments?.getString("editPost")
        if (arg != null) {
            binding.textContent.setText(arg)
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    postViewModel.savePost(binding.textContent.text.toString())
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        binding.addPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent {
                    photoResultContract.launch(it)
                }
        }

        binding.addFile.setOnClickListener {
            videoResultContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.buttonRemoveImage.setOnClickListener {
            postViewModel.removePhoto()
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_mapFragment)
        }
        setFragmentResultListener("mapFragment") { _, bundle ->
            val point = gson.fromJson<Point>(bundle.getString("point"), pointToken)
            point.let { postViewModel.setCoords(it) }
        }

        binding.chooseUsers.setOnClickListener {
            findNavController().navigate(
                R.id.action_newPostFragment_to_usersFragment2,
                bundleOf("selectUser" to true)
            )
        }
        setFragmentResultListener("usersFragmentResult") { _, bundle ->
            val selectedUsers =
                gson.fromJson<List<Int>>(bundle.getString("selectUser"), usersToken)
            if (selectedUsers != null) {
                postViewModel.setMentionId(selectedUsers)
            }
        }

        postViewModel.attachmentData.observe(viewLifecycleOwner) { attachment ->
            when (attachment?.attachmentType) {
                AttachmentType.IMAGE -> {
                    binding.imageContent.loadImage(attachment.uri.toString())
                    binding.imageContent.isVisible = true
                    binding.imageContainer.isVisible = true
                }

                AttachmentType.VIDEO -> {}

                null -> {
                    binding.imageContent.isVisible = false
                    binding.imageContainer.isVisible = false
                }
            }
        }

        binding.contentContainer.setOnClickListener {
            binding.textContent.focusAndShowKeyboard()
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}

fun Uri.toFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this)
    val tempFile = File.createTempFile("temp", ".jpg")

    val outputStream = FileOutputStream(tempFile)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}