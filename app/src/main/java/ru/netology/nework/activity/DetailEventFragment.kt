package ru.netology.nework.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.ListUsersAdapter
import ru.netology.nework.adapter.ListUsersOnClickListener
import ru.netology.nework.databinding.FragmentDetailEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.model.ListUsersType
import ru.netology.nework.util.AppConst
import ru.netology.nework.util.ViewDecor
import ru.netology.nework.util.loadAvatar
import ru.netology.nework.util.loadImage
import ru.netology.nework.viewmodel.EventViewModel
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailEventFragment : Fragment() {
    private lateinit var binding: FragmentDetailEventBinding
    private val eventViewModel: EventViewModel by activityViewModels()
    private var player: ExoPlayer? = null
    private var placeMark: PlacemarkMapObject? = null
    private var mapView: MapView? = null
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailEventBinding.inflate(inflater, container, false)
        MapKitFactory.initialize(requireContext())

        var event: Event? = null
        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on_24)
        val viewDecor = ViewDecor(36)

        eventViewModel.eventData.observe(viewLifecycleOwner) { eventItem ->
            event = eventItem
            with(binding) {
                authorAvatar.loadAvatar(eventItem.authorAvatar)
                authorName.text = eventItem.author
                lastWork.text = eventItem.authorJob ?: getString(R.string.non_working)

                when (eventItem.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        imageContent.loadImage(eventItem.attachment.url)
                        imageContent.isVisible = true
                    }

                    AttachmentType.VIDEO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(eventItem.attachment.url))
                        }
                        videoContent.player = player
                        videoContent.isVisible = true
                    }

                    null -> {
                        imageContent.isVisible = false
                        videoContent.isVisible = false
                        player?.release()
                    }
                }

                eventType.text = eventItem.type.toString()
                eventDate.text =
                    eventItem.datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                textContent.text = eventItem.content

                lifecycleScope.launch {
                    eventViewModel.getListUsers(eventItem.speakerIds, ListUsersType.SPEAKERS)
                }

                lifecycleScope.launch {
                    eventViewModel.getListUsers(eventItem.likeOwnerIds, ListUsersType.LIKERS)
                }

                lifecycleScope.launch {
                    eventViewModel.getListUsers(
                        eventItem.participantsIds,
                        ListUsersType.PARTICIPANT
                    )
                }

                buttonLike.text = eventItem.likeOwnerIds.size.toString()
                buttonLike.isChecked = eventItem.likedByMe
                buttonUsers.text = eventItem.participantsIds.size.toString()
                buttonUsers.isChecked = eventItem.participatedByMe

                val speakersAdapter = ListUsersAdapter(object : ListUsersOnClickListener {
                    override fun onOpenList() {
                        findNavController().navigate(
                            R.id.usersFragment2,
                            bundleOf(AppConst.SPEAKERS to gson.toJson(event?.speakerIds))
                        )
                    }
                })
                val likersAdapter = ListUsersAdapter(object : ListUsersOnClickListener {
                    override fun onOpenList() {
                        findNavController().navigate(
                            R.id.usersFragment2,
                            bundleOf(AppConst.LIKERS to gson.toJson(event?.likeOwnerIds))
                        )
                    }
                })
                val participantAdapter = ListUsersAdapter(object : ListUsersOnClickListener {
                    override fun onOpenList() {
                        findNavController().navigate(
                            R.id.usersFragment2,
                            bundleOf(AppConst.PARTICIPANT to gson.toJson(event?.participantsIds))
                        )
                    }
                })

                eventViewModel.listUsersData.observe(viewLifecycleOwner) { involved ->
                    speakersAdapter.submitList(involved.speakers)
                    likersAdapter.submitList(involved.likers)
                    participantAdapter.submitList(involved.participant)
                }

                binding.listSpeakers.apply {
                    addItemDecoration(viewDecor)
                    adapter = speakersAdapter
                }
                binding.listLikers.apply {
                    addItemDecoration(viewDecor)
                    adapter = likersAdapter
                }
                binding.listParticipant.apply {
                    addItemDecoration(viewDecor)
                    adapter = participantAdapter
                }

                map.setNoninteractive(true)
                mapView = binding.map.apply {
                    val point =
                        if (eventItem.coords != null) Point(
                            eventItem.coords.lat,
                            eventItem.coords.long
                        ) else null
                    if (point != null) {
                        if (placeMark == null) {
                            placeMark = mapWindow.map.mapObjects.addPlacemark()
                        }
                        placeMark?.apply {
                            geometry = point
                            setIcon(imageProvider)
                        }
                        mapWindow.map.move(
                            CameraPosition(
                                point,
                                17.0F,
                                0.0f,
                                0.0f
                            )
                        )
                    } else {
                        placeMark = null
                    }
                    isVisible = placeMark != null && point != null
                }
            }

        }

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.share -> {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, event?.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.share))
                    startActivity(shareIntent)
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

    override fun onStop() {
        super.onStop()
        player?.apply {
            stop()
        }
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }
}