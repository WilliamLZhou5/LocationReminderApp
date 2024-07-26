package com.example.locationreminderapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val context: Context,
    private val reminders: MutableList<Reminder>,
    private val itemClickListener: OnItemClickListener,
    private val onToggleActive: (Reminder, Boolean) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(reminder: Reminder)
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.reminder_title)
        private val description: TextView = itemView.findViewById(R.id.reminder_description)
        private val location: TextView = itemView.findViewById(R.id.reminder_location)
        private val activeSwitch: SwitchCompat = itemView.findViewById(R.id.reminder_active_switch)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(reminders[position])
                }
            }
        }

        fun bind(reminder: Reminder) {
            title.text = reminder.getTitle()
            description.text = reminder.getDescription()
            location.text = reminder.getLocationName()
            activeSwitch.isChecked = reminder.isActive()

            if (reminder.isActive()) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                title.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                description.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                location.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                title.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                description.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                location.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }

            activeSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleActive(reminder, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val currentReminder = reminders[position]
        holder.bind(currentReminder)
    }

    override fun getItemCount(): Int {
        return reminders.size
    }
}
