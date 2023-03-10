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
import com.deecto.callsmsmarketing.model.Message

class MessageAdapter(private val context: Context, private val listener: MessageItemClickListener) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messageList = ArrayList<Message>()
    private val fullList = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(context).inflate(R.layout.message_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]
        holder.message.text = currentMessage.message
        holder.title.text = currentMessage.title
        holder.created.text = currentMessage.created_at
        holder.title.isSelected = true
        holder.currentMsg.isChecked = currentMessage.status

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

    fun updateList(newList: List<Message>) {
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

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message_layout = itemView.findViewById<CardView>(R.id.messageCard)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val message = itemView.findViewById<TextView>(R.id.tv_message)
        val deleteBtn = itemView.findViewById<TextView>(R.id.tv_message)
        val editBtn = itemView.findViewById<TextView>(R.id.tv_message)
        val created = itemView.findViewById<TextView>(R.id.tv_created)
        val currentMsg = itemView.findViewById<Switch>(R.id.currentMsg)
    }

    interface MessageItemClickListener {
        fun onItemClicked(message: Message)
        fun onDeleteClicked(message: Message)
        fun onEditClicked(message: Message)
        fun onSwitchClicked(message: Message)
        fun onLongItemClicked(message: Message, cardView: CardView)
    }
}