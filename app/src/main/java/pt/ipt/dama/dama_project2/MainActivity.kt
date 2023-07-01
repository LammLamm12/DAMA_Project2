package pt.ipt.dama.dama_project2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONObject


class MainActivity : AppCompatActivity() {


    // Initialize variables
    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    private lateinit var temperature: TextView
    private lateinit var cityName: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var weatherIcon: ImageView

    // To avoid constant
    val appID = "e48755deb95906783b53f634b4223e0a"
    private val weatherURL = "https://api.openweathermap.org/data/2.5/weather"
    private val minTime = 5000
    private val minDistance = 1000
    private val requestCode = 101
    private val locationProvider = LocationManager.GPS_PROVIDER


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect to layout elements
        temperature = findViewById(R.id.temperature)
        cityName = findViewById(R.id.cityName)
        weatherCondition = findViewById(R.id.weatherCondition)
        weatherIcon = findViewById(R.id.weatherIcon)

        val cityFinder: RelativeLayout = findViewById(R.id.cityFinder)

        cityFinder.setOnClickListener {
            val intent = Intent(this, CityFinder::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val mIntent = intent
        val city = mIntent.getStringExtra("City")
        if (city != null ) {
            getWeatherForNewCity(city)
        } else {
            getWeatherForCurrentLocation()
        }
    }

    private fun getWeatherForNewCity(city: String) {
        val params = RequestParams()
        params.put("q", city)
        params.put("appID", appID)
        letsDoSomeNetworking(params)
    }

        // Current location must be set in extended controls
    private fun getWeatherForCurrentLocation() {
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()
                val params = RequestParams()
                params.put("lat", latitude)
                params.put("lon", longitude)
                params.put("appID", appID)
                letsDoSomeNetworking(params)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderDisabled(provider: String) {

            }

            override fun onProviderEnabled(provider: String) {

            }


        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode
            )
            return


        }
        mLocationManager.requestLocationUpdates(
            locationProvider,
            minTime.toLong(),
            minDistance.toFloat(),
            mLocationListener
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getWeatherForCurrentLocation()
            } else {
                // user denied the permission
            }
        }
    }

    fun letsDoSomeNetworking(params: RequestParams) {
        val client = AsyncHttpClient()
        client.get(weatherURL, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject
            ) {

                //Toast.makeText(this@MainActivity, "Data Get Success", Toast.LENGTH_SHORT).show()
                val weatherD: WeatherDetails? = WeatherDetails.fromJson(response)
                if (weatherD != null) {
                    updateUI(weatherD)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?,
                throwable: Throwable?
            ) {
            }

        })
    }

    @SuppressLint("DiscouragedApi")
    fun updateUI(weather: WeatherDetails) {
        temperature.text = weather.getTemperature()
        cityName.text = weather.getCity()
        weatherCondition.text = weather.apiWeather
        val resourceId = resources.getIdentifier(weather.getIcon(), "drawable", packageName)
        weatherIcon.setImageResource(resourceId)
    }
}