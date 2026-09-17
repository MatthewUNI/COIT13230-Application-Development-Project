package au.edu.cqu.ai_basedsmartmealplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.ai_basedsmartmealplanner.database.SavedMealPlanEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedPlansAdapter(
    private val onPlanClicked: (SavedMealPlanEntity) -> Unit
) : ListAdapter<SavedMealPlanEntity, SavedPlansAdapter.PlanViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(entity: SavedMealPlanEntity) {
            text1.text = entity.title
            val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date(entity.timestamp))
            text2.text = "Saved on: $formattedDate"

            itemView.setOnClickListener { onPlanClicked(entity) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SavedMealPlanEntity>() {
        override fun areItemsTheSame(oldItem: SavedMealPlanEntity, newItem: SavedMealPlanEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SavedMealPlanEntity, newItem: SavedMealPlanEntity): Boolean =
            oldItem == newItem
    }
}