package com.deecto.callsmsmarketing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.deecto.callsmsmarketing.Adapter.MessageAdapter
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.model.MessageViewModel

class MainActivity : AppCompatActivity(), MessageAdapter.MessageItemClickListener,
    PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: ActivityMainBinding
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                963
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                111
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                977
            )
        }

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
        binding.messageRecycler.setHasFixedSize(true)
        binding.messageRecycler.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        adapter = MessageAdapter(this, this)
        binding.messageRecycler.adapter = adapter

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

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddMessage::class.java)
            getContent.launch(intent)
        }

    }

    override fun onItemClicked(message: com.deecto.callsmsmarketing.model.Message) {

    }

    override fun onDeleteClicked(message: com.deecto.callsmsmarketing.model.Message) {
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
        val intent = Intent(this, AddMessage::class.java)
        intent.putExtra("current_message", message)
        updateMessage.launch(intent)
    }

    override fun onLongItemClicked(
        message: com.deecto.callsmsmarketing.model.Message,
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

            viewModel.deleteMessage(selectedMessage)
            return true
        }
        if (item?.itemId == R.id.edit_message) {

            return true
        }
        return false
    }
}

