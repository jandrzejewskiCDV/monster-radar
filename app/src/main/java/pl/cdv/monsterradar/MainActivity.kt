package pl.cdv.monsterradar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import pl.cdv.monsterradar.location.PlayerLocationTracker
import pl.cdv.monsterradar.ui.lifecycle.MapViewLifecycleAdapter
import pl.cdv.monsterradar.ui.renderer.MonsterMapRenderer
import pl.cdv.monsterradar.util.ResourceProvider
import pl.cdv.monsterradar.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var timerText: TextView
    private lateinit var warningImage: ImageView
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var resetButton: Button
    private lateinit var shareButton: Button

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationTracker: PlayerLocationTracker? = null
    private var mapRenderer: MonsterMapRenderer? = null
    private lateinit var resourceProvider: ResourceProvider

    private val gameViewModel: GameViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM_LEVEL = 18f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resourceProvider = ResourceProvider(this)
        gameViewModel.initResources(resourceProvider)

        initializeViews()
        initializeLocation()
        setupMap(savedInstanceState)
        observeViewModel()
    }

    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        timerText = findViewById(R.id.timerText)
        warningImage = findViewById(R.id.warningImage)
        gameOverLayout = findViewById(R.id.gameOverLayout)
        resetButton = findViewById(R.id.resetButton)
        shareButton = findViewById(R.id.shareButton)

        resetButton.setOnClickListener { onResetClicked() }
        shareButton.setOnClickListener { onShareClicked() }
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        lifecycle.addObserver(MapViewLifecycleAdapter(mapView))

        mapView.getMapAsync { map ->
            googleMap = map
            mapRenderer = MonsterMapRenderer(this, map)
            checkLocationPermission()

            locationTracker?.startTracking()
            gameViewModel.startGame()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    gameViewModel.gameState.collect { state ->
                        updateUI(state)
                    }
                }
                launch {
                    gameViewModel.monsters.collect { monsters ->
                        mapRenderer?.render(monsters)
                    }
                }
            }
        }
    }

    private fun updateUI(state: pl.cdv.monsterradar.model.GameState) {
        timerText.text = state.formattedTime
        warningImage.visibility = if (state.showWarning) View.VISIBLE else View.GONE
        gameOverLayout.visibility = if (state.isGameOver) View.VISIBLE else View.GONE

        state.zombieStatusMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            enableLocationFeatures()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @Suppress("MissingPermission")
    private fun enableLocationFeatures() {
        googleMap.isMyLocationEnabled = true
        locationTracker = PlayerLocationTracker(googleMap, fusedLocationClient, DEFAULT_ZOOM_LEVEL)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
            }
        }

        gameViewModel.setPlayerLocationProvider { locationTracker?.lastLocation }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationFeatures()
        }
    }

    override fun onResume() {
        super.onResume()
        locationTracker?.startTracking()
        gameViewModel.startGame()
    }

    override fun onPause() {
        super.onPause()
        locationTracker?.stopTracking()
        gameViewModel.pauseGame()
    }

    private fun onResetClicked() {
        gameOverLayout.visibility = View.GONE
        mapRenderer?.clear()
        locationTracker?.startTracking()
        gameViewModel.resetGame()
    }

    private fun onShareClicked() {
        val message = gameViewModel.generateShareMessage(timerText.text.toString())
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val chooserTitle = resourceProvider.getString(R.string.broadcast_status)
        startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
