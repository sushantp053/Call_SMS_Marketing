package com.deecto.callsmsmarketing

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.deecto.callsmsmarketing.Adapter.MessageAdapter
import com.deecto.callsmsmarketing.database.MessageDatabase
import kotlinx.android.synthetic.main.activity_message.*

import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.model.MessageViewModel

class MessageActivity : AppCompatActivity(), MessageAdapter.MessageItemClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var database: MessageDatabase
    lateinit var viewModel: MessageViewModel
    lateinit var adapter: MessageAdapter
    lateinit var selectedMessage: Message

    private val updateMessage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val msg =
                    result.data?.getSerializableExtra("message") as com.deecto.callsmsmarketing.model.Message
                if (msg != null) {
                    viewModel.updateMessage(msg)
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        //initialize the UI
        initUi()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MessageViewModel::class.java)

        viewModel.allMessage.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = MessageDatabase.getDatabase(this)

    }
    private fun initUi() {
        messageRecycler.setHasFixedSize(true)
        messageRecycler.layoutManager = LinearLayoutManager(applicationContext)
        adapter = MessageAdapter(this, this)
         messageRecycler.adapter = adapter

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val msg =
                        result.data?.getSerializableExtra("message") as? com.deecto.callsmsmarketing.model.Message
                    if (msg != null) {
                        viewModel.insertMessage(msg)
                    }

                }
            }

         fab.setOnClickListener {
            val intent = Intent(this, AddMessage::class.java)
            getContent.launch(intent)
        }

    }

    override fun onItemClicked(message: Message) {

    }

    override fun onDeleteClicked(message: Message) {

        Toast.makeText(this, "Delete Clicked", Toast.LENGTH_SHORT).show()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Do you really want to delete this message")
        builder.setPositiveButton(R.string.delete) {dialog, which ->
            viewModel.deleteMessage(selectedMessage)
            dialog.dismiss()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onEditClicked(message: Message) {
        Toast.makeText(this, "Edit Button Clicked", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AddMessage::class.java)
        intent.putExtra("current_message", message)
        updateMessage.launch(intent)
    }

    override fun onSwitchClicked(message: Message) {
        Toast.makeText(this, "Switch Clicked", Toast.LENGTH_SHORT).show()
        viewModel.updateStatus(message)
    }

    override fun onLongItemClicked(
        message: Message,
        cardView: CardView
    ) {
        selectedMessage = message
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView) {
        val popupMenu = PopupMenu(this, cardView)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.inflate(R.menu.pop_up_menu)
        popupMenu.show()

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete_message) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete")
            builder.setMessage("Do you really want to delete this message")
            builder.setPositiveButton(R.string.delete) { dialog, which ->
                viewModel.deleteMessage(selectedMessage)
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            builder.show()
            return true
        }
        if (item?.itemId == R.id.edit_message) {

            return true
        }
        return false
    }
}