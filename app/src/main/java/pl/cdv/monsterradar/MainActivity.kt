package pl.cdv.monsterradar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import pl.cdv.monsterradar.markers.MonsterMarker
import pl.cdv.monsterradar.tracker.LocationTrackerSystem
import pl.cdv.monsterradar.viewmodels.MonsterViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tracker : LocationTrackerSystem
    private val monsterViewModel: MonsterViewModel by viewModels()
    private val tickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val monsterMarkers = mutableMapOf<String, MonsterMarker>()
    private var monstersSpawned = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val DEFAULT_ZOOM_LEVEL = 15f
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
            observeMonsters()
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

    private fun observeMonsters() {
        monsterViewModel.monsters.observe(this) { monsters ->
            monsters.forEach { monster ->
                val existingMarker = monsterMarkers[monster.id]

                if (existingMarker == null) {
                    monsterMarkers[monster.id] = MonsterMarker(this, googleMap, monster)
                } else {
                    existingMarker.updatePosition(monster.position)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tickHandler.post(tickRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (::tracker.isInitialized) {
            tracker.clear()
        }
        tickHandler.removeCallbacks(tickRunnable)
    }

    @SuppressLint("MissingPermission")
    private fun moveToUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
                if (!monstersSpawned) {
                    repeat(3) {
                        monsterViewModel.spawnMonsterNearPlayer(userLatLng)
                    }
                    monstersSpawned = true
                }
            }
        }
        tracker = LocationTrackerSystem(googleMap, fusedLocationClient)
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

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (::tracker.isInitialized) {
                monsterViewModel.updateMonsters(tracker.lastLocation, 1f)
            }
            tickHandler.postDelayed(this, 1000)
        }
    }
}
