package pl.cdv.monsterradar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import android.view.View
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
    private lateinit var timerText: TextView
    private lateinit var warningImage: ImageView
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var resetButton: Button
    
    private val monsterViewModel: MonsterViewModel by viewModels()
    private val tickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val monsterMarkers = mutableMapOf<String, MonsterMarker>()
    private var monstersSpawned = false
    private var elapsedSeconds = 0
    private var isGameOver = false


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PLAYER_HIT_DISTANCE_METERS = 5f
        const val DEFAULT_ZOOM_LEVEL = 18f
        private const val WARNING_DISTANCE_METERS = 67f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupMap(savedInstanceState)

        warningImage = findViewById(R.id.warningImage)

        gameOverLayout = findViewById(R.id.gameOverLayout)
        resetButton = findViewById(R.id.resetButton)

        resetButton.setOnClickListener {
            resetGame()
        }
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
            tracker.startTrackingUser()
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
        if (::tracker.isInitialized) {
            tracker.startTrackingUser()
        }
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
                    spawnMonstersAt(userLatLng)
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
        @SuppressLint("DefaultLocale")
        override fun run() {

            if (isGameOver){
                println("Game over, ticking end")
                return
            }

            println("Ticking")

            elapsedSeconds++
            timerText.text = String.format(
                "%02d:%02d",
                elapsedSeconds / 60,
                elapsedSeconds % 60
            )

            val playerPos = tracker.lastLocation

            if(playerPos == null){
                Log.d("ZOMBIE", "Player position is null")
            }

            if (playerPos != null && monstersSpawned) {
                Log.d("ZOMBIE", "Player position in tick runnable: $playerPos")

                monsterViewModel.updateMonsters(playerPos, 1f)

                val showWarning = monsterViewModel.isMonsterNearPlayer(
                    playerPos,
                    WARNING_DISTANCE_METERS
                )
                warningImage.visibility =
                    if (showWarning) View.VISIBLE else View.GONE

                if (monsterViewModel.isMonsterTouchingPlayer(
                        playerPos,
                        PLAYER_HIT_DISTANCE_METERS
                    )
                ) {
                    endGame()
                    return
                }
            }

            if(playerPos != null && !monstersSpawned){
                spawnMonstersAt(playerPos)
                Log.d("ZOMBIE", "Spawning zombies")
            }

            tickHandler.postDelayed(this, 1000)
        }
    }

    private fun endGame() {
        isGameOver = true
        tickHandler.removeCallbacks(tickRunnable)

        if (::tracker.isInitialized) {
            tracker.clear()
        }

        gameOverLayout.visibility = View.VISIBLE
    }

    private fun resetGame() {
        // Hide Game Over UI
        gameOverLayout.visibility = View.GONE

        // Reset timer
        elapsedSeconds = 0
        timerText.text = "00:00"

        // Remove monsters from map and ViewModel
        clearMonsterMarkers()
        monsterViewModel.clearMonsters()

        // Reset flags
        isGameOver = false
        monstersSpawned = false

        //restart user tracker!
        tracker.startTrackingUser()

        // Start ticking
        tickHandler.post(tickRunnable)
    }

    private fun spawnMonstersAt(playerPos: LatLng) {
        repeat(3) {
            monsterViewModel.spawnMonsterNearPlayer(playerPos)
        }
        monstersSpawned = true
    }

    private fun clearMonsterMarkers() {
        monsterMarkers.values.forEach { it.removeFromMap() }
        monsterMarkers.clear()
    }


}
