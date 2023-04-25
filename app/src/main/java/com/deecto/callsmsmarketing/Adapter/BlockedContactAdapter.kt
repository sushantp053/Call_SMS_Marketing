package com.deecto.callsmsmarketing.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.deecto.callsmsmarketing.BlockedContactsActivity
import com.deecto.callsmsmarketing.R
import com.deecto.callsmsmarketing.model.BlockedContacts

class BlockedContactAdapter(
    private val context: Context,
    private val listener: BlockedContactsItemClickListener
) :
    RecyclerView.Adapter<BlockedContactAdapter.BlockedViewHolder>() {

    private val blockedContactList = ArrayList<BlockedContacts>()
    private val fullList = ArrayList<BlockedContacts>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        return BlockedViewHolder(
            LayoutInflater.from(context).inflate(R.layout.blocked_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val currentBlockedContacts = blockedContactList[position]
        holder.blockedNumber.text = currentBlockedContacts.phone
        holder.title.text = currentBlockedContacts.name

        holder.removeBtn.setOnClickListener {
            listener.onRemoveClicked(blockedContactList[holder.adapterPosition])
        }

    }

    override fun getItemCount(): Int {
        return blockedContactList.size
    }

    fun updateList(newList: List<BlockedContacts>) {
        fullList.clear()
        fullList.addAll(newList)
        blockedContactList.clear()
        blockedContactList.addAll(fullList)
        notifyDataSetChanged()
    }

    inner class BlockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val blocked_layout = itemView.findViewById<CardView>(R.id.blockedCard)
        val title = itemView.findViewById<TextView>(R.id.tv_blockedName)
        val blockedNumber = itemView.findViewById<TextView>(R.id.tv_blockedNumber)
        val removeBtn = itemView.findViewById<TextView>(R.id.removeBlockBtn)
    }

    interface BlockedContactsItemClickListener {
        fun onRemoveClicked(blockedContacts: BlockedContacts)
    }
}