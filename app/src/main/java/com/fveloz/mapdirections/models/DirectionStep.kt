package com.fveloz.mapdirections.models

import org.json.JSONObject

class DirectionStep (
    val step: String,
    val duration: Double,
    val distance : Double) {

    constructor(json: JSONObject) : this(
        json.getJSONObject("maneuver").getString("instruction"),
        if (json.has("duration")) json.getDouble("duration") else 0.0,
        if (json.has("distance")) json.getDouble("distance") else 0.0)
    }