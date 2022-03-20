package com.fveloz.mapdirections.api

import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.fveloz.mapdirections.MapApplication
import com.fveloz.mapdirections.R
import com.fveloz.mapdirections.models.DirectionStep
import com.fveloz.mapdirections.utils.NetworkUtils
import com.fveloz.mapdirections.utils.ResultState
import com.fveloz.mapdirections.utils.VolleySingleton
import com.mapbox.geojson.Point
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapboxAPI {
    companion object{
        suspend fun getDirectionSteps(pointOrigin: Point, pointDestination: Point) = suspendCoroutine<ResultState<ArrayList<DirectionStep>>>{ cont ->
            if (NetworkUtils.isConnected()) {
                val stringRequest = object : StringRequest(
                    Method.GET, "https://api.mapbox.com/directions/v5/mapbox/driving/${pointOrigin.longitude()},${pointOrigin.latitude()};${pointDestination.longitude()},${pointDestination.latitude()}?steps=true&access_token=${MapApplication.getAppContext().getString(R.string.mapbox_access_token)}",
                    { response ->
                        try {
                            val dataList = ArrayList<DirectionStep>()
                            val json = JSONObject(response)
                            for (i in 0 until json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length()) {
                                val singleJson = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i)
                                dataList.add(DirectionStep(singleJson))
                            }
                            if (dataList.isEmpty()) cont.resume(ResultState.Empty())
                            else cont.resume(ResultState.Success(dataList))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            cont.resume(ResultState.Error(MapApplication.getAppContext().getString(
                                R.string.general_error)))
                        }
                    },
                    {
                        try {
                            if (it.networkResponse.statusCode == 404) cont.resume(ResultState.Empty())
                            else cont.resume(
                                ResultState.Error(
                                    MapApplication.getAppContext()
                                        .getString(R.string.general_error)
                                )
                            )
                        } catch (e: Exception) {
                            cont.resume(ResultState.Error(MapApplication.getAppContext()
                                .getString(R.string.general_error)))
                        }
                    }
                ){}
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                VolleySingleton.getInstance(MapApplication.getAppContext()).addToRequestQueue(stringRequest)
            } else cont.resume(ResultState.Error(MapApplication.getAppContext().getString(R.string.no_connection)))
        }
    }
}