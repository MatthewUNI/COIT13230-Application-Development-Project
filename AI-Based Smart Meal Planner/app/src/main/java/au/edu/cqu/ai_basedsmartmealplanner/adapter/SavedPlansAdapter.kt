package au.edu.cqu.ai_basedsmartmealplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.database.SavedMealPlanEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedPlansAdapter(
    private val onPlanClicked: (SavedMealPlanEntity) -> Unit,
    private val onDeleteClicked: (SavedMealPlanEntity) -> Unit
) : ListAdapter<SavedMealPlanEntity, SavedPlansAdapter.PlanViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(entity: SavedMealPlanEntity) {
            textTitle.text = entity.title
            val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entity.timestamp))
            textDate.text = "Saved on: $formattedDate"

            itemView.setOnClickListener { onPlanClicked(entity) }
            buttonDelete.setOnClickListener { onDeleteClicked(entity) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SavedMealPlanEntity>() {
        override fun areItemsTheSame(oldItem: SavedMealPlanEntity, newItem: SavedMealPlanEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SavedMealPlanEntity, newItem: SavedMealPlanEntity) = oldItem == newItem
    }
}