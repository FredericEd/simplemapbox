package com.fveloz.mapdirections

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.fveloz.mapdirections.databinding.ActivityMainBinding
import com.fveloz.mapdirections.models.Viewpoint
import com.fveloz.mapdirections.utils.LocationPermissionHelper
import com.google.gson.Gson
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var userLocation: Viewpoint? = null
    private var userDestination: Viewpoint? = null

    private val points = listOf(
        Viewpoint("Hitec Commercial Solutions", Point.fromLngLat(-117.127707, 32.836589)),
        Viewpoint("Sea World Flying Field", Point.fromLngLat(-117.214693, 32.762725)),
        Viewpoint("Black Mountain Flying Field", Point.fromLngLat(-117.130309, 32.987890)),
        Viewpoint("Poke OneNHalf", Point.fromLngLat(-117.128241, 32.830914)),
        Viewpoint("USS Midway", Point.fromLngLat(-117.175157, 32.714188)),
        Viewpoint("San Diego Zoo", Point.fromLngLat(-117.148280, 32.734679)),
        Viewpoint("Miramar Air Force Base", Point.fromLngLat(-117.144246, 32.873340)))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        binding.btnSteps.setOnClickListener{
            userLocation?.let{
                if (userDestination != null) {
                    val i = Intent(this@MainActivity, StepsActivity::class.java)
                    i.putExtra("origin", Gson().toJson(it))
                    i.putExtra("destination", Gson().toJson(userDestination))
                    startActivity(i)
                } else Toast.makeText(this@MainActivity, R.string.destination_required, Toast.LENGTH_LONG).show()
            }
        }
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            points.map { it.name }
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinSteps.adapter = adapter
            binding.spinSteps.onItemSelectedListener = this@MainActivity
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val selected: String = parent.getItemAtPosition(pos) as String
        val filtered = points.filter { it.name == selected }
        if (filtered.isNotEmpty()) {
            pointAnnotationManager?.let{
                it.deleteAll()
            }
            userDestination = filtered[0]
            binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(filtered[0].point).build())
            addAnnotationsToMap(filtered[0])
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>) { }

    private fun addAnnotationsToMap(viewpoint: Viewpoint) {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.red_marker
        )?.let {
            val annotationApi = binding.mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(viewpoint.point)
                .withIconImage(it)
            pointAnnotationManager!!.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun onMapReady() {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().zoom(10.0).build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable( this@MainActivity, R.drawable.mapbox_user_puck_icon, ),
                shadowImage = AppCompatResources.getDrawable( this@MainActivity, R.drawable.mapbox_user_icon_shadow, ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop { literal(0.0)
                        literal(0.6) }
                    stop { literal(20.0)
                        literal(1.0) }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(it)
        userLocation = Viewpoint(getString(R.string.location), it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) { onCameraTrackingDismissed() }
        override fun onMove(detector: MoveGestureDetector): Boolean { return false }
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun onCameraTrackingDismissed() {
        binding.mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}