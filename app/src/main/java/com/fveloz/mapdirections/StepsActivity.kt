package com.fveloz.mapdirections

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.fveloz.mapdirections.adapters.DirectionStepsAdapter
import com.fveloz.mapdirections.databinding.ActivityRecyclerBinding
import com.fveloz.mapdirections.models.DirectionStep
import com.fveloz.mapdirections.models.Viewpoint
import com.fveloz.mapdirections.utils.ResultState
import com.fveloz.mapdirections.viewmodels.MainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class StepsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecyclerBinding
    private lateinit var adapter : DirectionStepsAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val typePost = object : TypeToken<Viewpoint>() {}.type
        val origin: Viewpoint = Gson().fromJson(intent.extras!!.getString("origin"), typePost)
        val destination: Viewpoint = Gson().fromJson(intent.extras!!.getString("destination"), typePost)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "${getString(R.string.destination)} ${destination.name}"

        lifecycleScope.launchWhenCreated {
            viewModel.isLoading.collect{
                binding.progressView.isVisible = it
                binding.contentView.isVisible = !it
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.stepList.collect {
                adapter = DirectionStepsAdapter(viewModel.stepList.value)
                binding.recyclerView.layoutManager = LinearLayoutManager(this@StepsActivity)
                binding.recyclerView.adapter = adapter
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getStepListState.collect{
                when (it) {
                    is ResultState.Empty ->{
                        binding.textEmpty.text = resources.getString(R.string.no_data)
                        binding.layEmpty.isVisible = true
                    }
                    is  ResultState.Error ->{
                        binding.textEmpty.text = it.message
                        binding.layEmpty.isVisible = true
                    }
                    is ResultState.Success -> {
                        binding.layEmpty.isVisible = false
                    }
                }
            }
        }
        viewModel.getDirectionSteps(origin.point, destination.point)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}