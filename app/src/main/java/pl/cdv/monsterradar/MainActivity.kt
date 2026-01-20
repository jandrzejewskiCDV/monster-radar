package pl.cdv.monsterradar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import pl.cdv.monsterradar.tracker.LocationTrackerSystem

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tracker : LocationTrackerSystem

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val DEFAULT_ZOOM_LEVEL = 6f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupMap(savedInstanceState)
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        val adapter = MapViewLifecycleAdapter(mapView)
        lifecycle.addObserver(adapter)

        mapView.getMapAsync { map ->
            googleMap = map
            enableMyLocation()
            addAdvancedMarker()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            moveToUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        tracker.clear()
    }

    @SuppressLint("MissingPermission")
    private fun moveToUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
            }
        }

        LocationTrackerSystem(googleMap, fusedLocationClient)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted
                enableMyLocation()
            }
        }
    }

    private fun addAdvancedMarker() {
        val MARKER_POSITION = LatLng(-33.87365, 151.20689)

        val sizeInDp = 60
        val sizeInPx = (sizeInDp * resources.displayMetrics.density).toInt()

        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.monster)
            layoutParams = ViewGroup.LayoutParams(sizeInPx, sizeInPx)
        }

        val advancedMarkerOptions: AdvancedMarkerOptions = AdvancedMarkerOptions()
            .position(MARKER_POSITION)
            .iconView(imageView)

        googleMap.addMarker(advancedMarkerOptions)
    }
}
