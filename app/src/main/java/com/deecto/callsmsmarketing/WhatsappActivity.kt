package com.deecto.callsmsmarketing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.deecto.callsmsmarketing.Adapter.MessageAdapter
import com.deecto.callsmsmarketing.Adapter.WhatsappAdapter
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityWhatsappBinding
import com.deecto.callsmsmarketing.model.WhatsappMessage
import com.deecto.callsmsmarketing.model.WhatsappViewModel

class WhatsappActivity : AppCompatActivity(), WhatsappAdapter.WhatsappItemClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityWhatsappBinding
    private lateinit var database: MessageDatabase
    lateinit var viewModel: WhatsappViewModel
    lateinit var adapter: WhatsappAdapter
    lateinit var selectedMessage: WhatsappMessage



    private val updateWhatsapp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val msg =
                    result.data?.getSerializableExtra("message") as WhatsappMessage
                if (msg != null) {
                    viewModel.updateWhatsapp(msg)
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWhatsappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)



        //initialize the UI
        initUi()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(WhatsappViewModel::class.java)

        viewModel.allWhatsapp.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = MessageDatabase.getDatabase(this)
    }

    private fun initUi() {
        binding.whatsappRecycler.setHasFixedSize(true)
        binding.whatsappRecycler.layoutManager = LinearLayoutManager(applicationContext)
        adapter = WhatsappAdapter(this, this)
        binding.whatsappRecycler.adapter = adapter

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val msg =
                        result.data?.getSerializableExtra("message") as? WhatsappMessage
                    if (msg != null) {
                        viewModel.insertWhatsapp(msg)
                    }

                }
            }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddWhatsapp::class.java)
            getContent.launch(intent)
        }

    }

    override fun onItemClicked(whatsapp: WhatsappMessage) {

    }

    override fun onDeleteClicked(whatsapp: WhatsappMessage) {

        Toast.makeText(this, "Delete Clicked", Toast.LENGTH_SHORT).show()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Do you really want to delete this message")
        builder.setPositiveButton(R.string.delete) {dialog, which ->
            viewModel.deleteWhatsapp(selectedMessage)
            dialog.dismiss()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onEditClicked(whatsapp: WhatsappMessage) {
        Toast.makeText(this, "Edit Button Clicked", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AddWhatsapp::class.java)
        intent.putExtra("current_message", whatsapp)
        updateWhatsapp.launch(intent)
    }

    override fun onSwitchClicked(whatsapp: WhatsappMessage) {
        Toast.makeText(this, "Switch Clicked", Toast.LENGTH_SHORT).show()
        viewModel.updateStatus(whatsapp)
    }

    override fun onLongItemClicked(
        whatsapp: WhatsappMessage,
        cardView: CardView
    ) {
        selectedMessage = whatsapp
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
                viewModel.deleteWhatsapp(selectedMessage)
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