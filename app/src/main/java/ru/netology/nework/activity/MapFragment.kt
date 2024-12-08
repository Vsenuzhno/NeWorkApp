package ru.netology.nework.activity

import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding

@AndroidEntryPoint
class MapFragment : Fragment(), UserLocationObjectListener {
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var userLocation: UserLocationLayer
    private lateinit var placeMark: PlacemarkMapObject
    private val gson = Gson()
    private var inputListener: InputListener? = null

    private val zoomHandler = Handler(Looper.getMainLooper())
    private var isZoomingIn = false
    private var isZooming = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        MapKitFactory.initialize(requireContext())

        val imageProvider: ImageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on_24)

        inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                if (::placeMark.isInitialized) {
                    map.mapObjects.remove(placeMark)
                }
                placeMark = map.mapObjects.addPlacemark()
                placeMark.apply {
                    geometry = point
                    setIcon(imageProvider)
                }
                placeMark.isDraggable = true
            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) = Unit
        }

        mapView = binding.map.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            mapWindow.map.addInputListener(inputListener as InputListener)
            userLocation.setObjectListener(this@MapFragment)
        }

        setupZoomButtons()

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    if (::placeMark.isInitialized) {
                        setFragmentResult(
                            "mapFragment",
                            bundleOf("point" to gson.toJson(placeMark.geometry))
                        )
                    }
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

    private fun setupZoomButtons() {
        binding.zoomIn.setOnTouchListener { _, event ->
            handleZoomEvent(event, zoomIn = true)
            true
        }

        binding.zoomOut.setOnTouchListener { _, event ->
            handleZoomEvent(event, zoomIn = false)
            true
        }
    }

    private fun handleZoomEvent(event: MotionEvent, zoomIn: Boolean) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isZoomingIn = zoomIn
                isZooming = true
                startZooming()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isZooming = false
                stopZooming()
            }
        }
    }

    private fun startZooming() {
        zoomHandler.post(object : Runnable {
            override fun run() {
                if (isZooming) {
                    val currentPosition = mapView.mapWindow.map.cameraPosition
                    val newZoom = if (isZoomingIn) {
                        currentPosition.zoom + 0.1f
                    } else {
                        currentPosition.zoom - 0.1f
                    }
                    mapView.mapWindow.map.move(
                        CameraPosition(
                            currentPosition.target,
                            newZoom,
                            currentPosition.azimuth,
                            currentPosition.tilt
                        )
                    )
                    zoomHandler.postDelayed(this, 50) // Повтор каждые 50 мс
                }
            }
        })
    }

    private fun stopZooming() {
        zoomHandler.removeCallbacksAndMessages(null)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputListener?.let {
            mapView.mapWindow.map.removeInputListener(it)
            inputListener = null
        }
        zoomHandler.removeCallbacksAndMessages(null)
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onObjectAdded(view: UserLocationView) = Unit

    override fun onObjectRemoved(view: UserLocationView) = Unit

    override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
        userLocation.setAnchor(
            PointF(mapView.width * 0.5F, mapView.height * 0.5F),
            PointF(mapView.width * 0.5F, mapView.height * 0.5F)
        )
        mapView.mapWindow.map.move(
            CameraPosition(view.arrow.geometry, 5f, 0f, 0f)
        )
    }
}
