package com.fveloz.mapdirections.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fveloz.mapdirections.api.MapboxAPI
import com.fveloz.mapdirections.models.DirectionStep
import com.fveloz.mapdirections.utils.ResultState
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _stepList = MutableStateFlow<ArrayList<DirectionStep>>(ArrayList())
    val stepList: StateFlow<ArrayList<DirectionStep>> = _stepList
    private var _getStepListState = MutableStateFlow<ResultState<ArrayList<DirectionStep>>>(ResultState.Empty())
    val getStepListState: StateFlow<ResultState<ArrayList<DirectionStep>>> = _getStepListState

    fun getDirectionSteps(pointOrigin: Point, pointDestination: Point) = viewModelScope.launch{
        _isLoading.value = true
        val resultState = MapboxAPI.getDirectionSteps(pointOrigin, pointDestination)
        _getStepListState.value = resultState
        if (resultState is ResultState.Success<ArrayList<DirectionStep>>) {
            val temp =  ArrayList<DirectionStep>()
            resultState.data.forEach{
                temp.add(it)
            }
            _stepList.value = temp
        }
        _isLoading.value = false
    }
}