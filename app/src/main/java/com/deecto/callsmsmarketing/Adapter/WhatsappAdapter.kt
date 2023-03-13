package com.deecto.callsmsmarketing.Adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.deecto.callsmsmarketing.R
import com.deecto.callsmsmarketing.model.WhatsappMessage

class WhatsappAdapter(private val context: Context, private val listener: WhatsappItemClickListener) :
    RecyclerView.Adapter<WhatsappAdapter.WhatsappViewHolder>() {

    private val messageList = ArrayList<WhatsappMessage>()
    private val fullList = ArrayList<WhatsappMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatsappViewHolder {
        return WhatsappViewHolder(
            LayoutInflater.from(context).inflate(R.layout.message_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WhatsappViewHolder, position: Int) {
        val currentWhatsapp = messageList[position]
        holder.message.text = currentWhatsapp.message
        holder.title.text = currentWhatsapp.title
        holder.created.text = currentWhatsapp.created_at
        holder.title.isSelected = true
        holder.currentMsg.isChecked = currentWhatsapp.status

        holder.currentMsg.setOnClickListener {
            listener.onSwitchClicked(messageList[holder.adapterPosition])
        }
        holder.message_layout.setOnClickListener {
            listener.onItemClicked(messageList[holder.adapterPosition])
        }
        holder.message_layout.setOnLongClickListener {
            listener.onLongItemClicked(messageList[holder.adapterPosition], holder.message_layout)
            true
        }
        holder.deleteBtn.setOnClickListener {
            listener.onDeleteClicked(messageList[holder.adapterPosition])
        }
        holder.editBtn.setOnClickListener {
            listener.onEditClicked(messageList[holder.adapterPosition])
        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateList(newList: List<WhatsappMessage>) {
        fullList.clear()
        fullList.addAll(newList)
        messageList.clear()
        messageList.addAll(fullList)
        notifyDataSetChanged()
    }

    fun filterList(search: String) {
        messageList.clear()
        for (item in fullList) {
            if (item.title?.lowercase()
                    ?.contains(search.lowercase()) == true || item.message?.lowercase()
                    ?.contains(search.lowercase()) == true
            ) {
                messageList.add(item)
            }
        }
        notifyDataSetChanged()
    }

    inner class WhatsappViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message_layout = itemView.findViewById<CardView>(R.id.messageCard)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val message = itemView.findViewById<TextView>(R.id.tv_message)
        val deleteBtn = itemView.findViewById<TextView>(R.id.deleteBtn)
        val editBtn = itemView.findViewById<TextView>(R.id.editBtn)
        val created = itemView.findViewById<TextView>(R.id.tv_created)
        val currentMsg = itemView.findViewById<Switch>(R.id.currentMsg)
    }

    interface WhatsappItemClickListener {
        fun onItemClicked(message: WhatsappMessage)
        fun onDeleteClicked(message: WhatsappMessage)
        fun onEditClicked(message: WhatsappMessage)
        fun onSwitchClicked(message: WhatsappMessage)
        fun onLongItemClicked(message: WhatsappMessage, cardView: CardView)
    }
}