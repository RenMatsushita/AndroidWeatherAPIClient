package com.websarva.wings.android.asyncsample

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lvCityList = findViewById<ListView>(R.id.lvCityList)
        val cityList: MutableList<MutableMap<String, String>> = mutableListOf()
        var city = mutableMapOf("name" to "大阪", "id" to "270000")
        cityList.add(city)
        city = mutableMapOf("name" to "神戸", "id" to "280010")
        cityList.add(city)

        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)

        val adapter = SimpleAdapter(applicationContext, cityList, android.R.layout.simple_expandable_list_item_1, from, to)

        lvCityList.adapter = adapter
        lvCityList.onItemClickListener = ListItemClickListener()
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val item = parent?.getItemAtPosition(position) as Map<String, String>
            val cityName = item["name"]
            val cityId = item["id"]
            val tvCityName = findViewById<TextView>(R.id.tvCityName)
            tvCityName.setText(cityName + "の天気:")

            val receiver = WeatherInfoReceiver()
            receiver.execute(cityId)
        }
    }

    private inner class WeatherInfoReceiver(): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            var id = params[0]
            val urlString = "http://weather.livedoor.com/forecast/webservice/json/v1?city=${id}"
            Log.i("Debug", urlString)
            val url = URL(urlString)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()

            val stream = con.inputStream
            Log.i("Debug", stream.toString())
            val result = is2String(stream)
            con.disconnect()
            stream.close()

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val rootJSON = JSONObject(result)
            val descriptionJSON = rootJSON.getJSONObject("description")
            val desc = descriptionJSON.getString("text")

            val forecasts = rootJSON.getJSONArray("forecasts")
            val forecastsNow = forecasts.getJSONObject(0)
            val telop = forecastsNow.getString("telop")

            val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            tvWeatherTelop.text = telop
            tvWeatherDesc.text = desc

            Log.i("Debug", telop)
            Log.i("Debug", desc)
        }

        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
}
