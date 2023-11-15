package com.example.forcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import com.example.forcast.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//e25c08ebe811f63f434c90af57d523e8 Application API

class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Lucknow")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName:String) {
        val retroFit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retroFit.getWeatherData(cityName, "e25c08ebe811f63f434c90af57d523e8", "metrics")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = (responseBody.main.temp - 273.00)
                    val humidity = (responseBody.main.humidity)
                    val windSpeed = (responseBody.wind.speed)
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                    val maxTemp = (responseBody.main.temp_max - 273)
                    val minTemp = (responseBody.main.temp_min - 273)

                    binding.Temperature.text = String.format("%.2f", temperature).toString() + "°C"
                    binding.Weather.text = condition
                    binding.MaxTemp.text = String.format("%.2f", maxTemp).toString() + "°C"
                    binding.MinTemp.text = String.format("%.2f", minTemp).toString() + "°C"
                    binding.Humidity.text = "$humidity %"
                    binding.WindSpeed.text = "$windSpeed m/sec"
                    binding.sunset.text = "${time(sunSet)} PM"
                    binding.Sunrise.text = "${time(sunRise)} AM"
                    binding.sea.text = "$seaLevel hPa"
                    binding.Condition.text = condition
                    binding.Day.text = dayName(System.currentTimeMillis())
                    binding.Date.text = date()
                    binding.CityName.text = "$cityName"
                    Log.d("TAG", "onResponse: $temperature")
                    Log.d("TAG", "onResponse: $maxTemp")
                    Log.d("TAG", "onResponse: $minTemp")

                    checkCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                print("No Data")
            }

        })

    }

    private fun dayName(timeStamp:Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun time(timeStamp:Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timeStamp*1000)))
    }

    private fun checkCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }
}
