package com.example.mapbox_test

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import com.mapbox.mapboxsdk.style.expressions.Expression.get


private const val ICON_ID = "ICON_ID"
private const val LAYER_ID = "LAYER_ID"
private const val SOURCE_ID = "SOURCE_ID"
private var mapView: MapView? = null

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this,
            "pk.eyJ1Ijoic2tlbnRhcm8iLCJhIjoiY2s5dWU0ZXVsMDB4ZzNlbGFjZmV6bnQ1YyJ9._vEtIBnwm2xm9yYOc9ZpUw"
        )
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap : MapboxMap){
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        var data:JSONObject = JSONObject()

        //get covid patients data
        val httpAsync = "https://www.stopcovid19.jp/data/covid19japan.json"
            .httpGet()
            .responseJson{ request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        data = result.get().obj()
                        println(data)
                        val prefData = readPrefJson()

                        //analyze json to get patients data
                        for(i in 0 until data.getJSONArray("area").length()){
                            val prefName = data.getJSONArray("area").getJSONObject(i).getString("name_jp");
                            val patients = data.getJSONArray("area").getJSONObject(i).getInt("ncurrentpatients");

                            var lat:Double = 0.0
                            var lng:Double = 0.0

                            //get latitude and longitude from prefecture name
                            for (j in 0 until prefData.getJSONArray("marker").length()){
                                if(prefName ==  prefData.getJSONArray("marker").getJSONObject(i).getString("pref")){
                                    lat = prefData.getJSONArray("marker").getJSONObject(i).getDouble("lat")
                                    lng = prefData.getJSONArray("marker").getJSONObject(i).getDouble("lng")
                                }
                            }

                            // save to source list
                            var featureJson = JsonObject()
                            featureJson.addProperty("patients", patients)
                            symbolLayerIconFeatureList.add(
                                Feature.fromGeometry(
                                    Point.fromLngLat(lng, lat), featureJson
                                )
                            )
                        }

                        // setting the map style
                        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                            .withImage(ICON_ID, BitmapFactory.decodeResource(
                                resources, R.drawable.red_marker))
                            .withSource(GeoJsonSource(SOURCE_ID,
                                FeatureCollection.fromFeatures(symbolLayerIconFeatureList))
                            )
                            .withLayer(CircleLayer("CIRCLE_LAYER", SOURCE_ID)
                                .withProperties(
                                    circleColor(Color.WHITE),
                                    circleRadius(18f)
                                ))
                            .withLayer(SymbolLayer(LAYER_ID, SOURCE_ID)
                                .withProperties(
                                    textField(Expression.toString(get("patients"))),
                                    textSize(12f),
                                    textColor(Color.RED),
                                    textIgnorePlacement(true),
                                    textAllowOverlap(true)
                                ))

                        );
                    }
                }
            }
    }

    //read local prefecture-coordinates json
    private fun readPrefJson(): JSONObject {
        val assetManager = resources.assets
        val inputStream = assetManager.open("pref.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val str: String = bufferedReader.readText()
        return JSONObject(str)
    }

    public override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
