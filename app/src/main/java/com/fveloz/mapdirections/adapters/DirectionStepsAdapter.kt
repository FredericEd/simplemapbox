package com.fveloz.mapdirections.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fveloz.mapdirections.R
import com.fveloz.mapdirections.models.DirectionStep
import kotlin.math.roundToLong

class DirectionStepsAdapter(private var postList: List<DirectionStep>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return StepHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false))
    }

    override fun onBindViewHolder(genericHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = genericHolder as StepHolder
        val post = postList[position]
        holder.fillFields(post)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}

class StepHolder(private val view: View):
    RecyclerView.ViewHolder(view) {

    private val textStep: TextView = view.findViewById(R.id.textStep)
    private val textDistance: TextView = view.findViewById(R.id.textDistance)

    @SuppressLint("SetTextI18n")
    fun fillFields(step: DirectionStep) {
        textStep.text = step.step
        textDistance.text = "${view.resources.getString(R.string.forspace)} ${getMiles(step.distance)} ${view.resources.getString(R.string.miles)}"//(${step.duration} ${view.resources.getString(R.string.seconds)})"
    }
    private fun getMiles(i: Double): Double {
        return  String.format("%.3f", i * 0.000621371192).toDouble()
    }
}