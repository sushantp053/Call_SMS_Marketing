package com.deecto.callsmsmarketing

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.deecto.callsmsmarketing.Adapter.BlockedContactAdapter
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityBlockedContactsBinding
import com.deecto.callsmsmarketing.model.BlockedContactViewModel
import com.deecto.callsmsmarketing.model.BlockedContacts


class BlockedContactsActivity : AppCompatActivity(),
    BlockedContactAdapter.BlockedContactsItemClickListener {
    private lateinit var database: MessageDatabase
    lateinit var viewModel: BlockedContactViewModel
    lateinit var adapter: BlockedContactAdapter
    lateinit var selectedBlockedContacts: BlockedContacts
    private lateinit var binding: ActivityBlockedContactsBinding

    private lateinit var addBlockedContact: BlockedContacts


    //contact permission code
    private val CONTACT_PERMISSION_CODE = 1;

    //contact pick code
    private val CONTACT_PICK_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedContactsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initUi()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(BlockedContactViewModel::class.java)

        viewModel.allContacts.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = MessageDatabase.getDatabase(this)

        binding.btnAddToBlock.setOnClickListener {
            val name = binding.editBlockNumberName.text.toString()
            val number = binding.editBlockNumber.text.toString()
            if (number.isNotEmpty()) {
                if (name.isEmpty()) {
                    val blockedContacts = BlockedContacts(null, number, "Unknown")
                    viewModel.insertContact(blockedContacts)
                } else {
                    val blockedContacts = BlockedContacts(null, number, name)
                    viewModel.insertContact(blockedContacts)
                }

            } else {
                Toast.makeText(this, "Please enter number", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initUi() {
        binding.blockedRecycler.setHasFixedSize(true)
        binding.blockedRecycler.layoutManager = LinearLayoutManager(applicationContext)
        adapter = BlockedContactAdapter(this, this)
        binding.blockedRecycler.adapter = adapter

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val blocked =
                        result.data?.getSerializableExtra("blocked") as? BlockedContacts
                    if (blocked != null) {
                        viewModel.insertContact(blocked)
                    }

                }
            }

        binding.fab.setOnClickListener {
            if (checkContactPermission()) {
                //allowed
                pickContact()
            } else {
                //not allowed, request
                requestContactPermission()
            }
        }

    }

    override fun onRemoveClicked(blockedContacts: BlockedContacts) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove")
        builder.setMessage("Do you really want to remove this contact from block list.")
        builder.setPositiveButton(R.string.remove) { dialog, which ->
            viewModel.removeContact(blockedContacts)
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
    }

    private fun checkContactPermission(): Boolean {
        //check if permission was granted/allowed or not, returns true if granted/allowed, false if not
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermission() {
        //request the READ_CONTACTS permission
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE)
    }

    private fun pickContact() {
        //intent ti pick contact
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //handle permission request results || calls when user from Permission request dialog presses Allow or Deny
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted, can pick contact
                pickContact()
            } else {
                //permission denied, cann't pick contact, just show message
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            //calls when user click a contact from contacts (intent) list
            if (requestCode == CONTACT_PICK_CODE) {

                val cursor1: Cursor?
                val cursor2: Cursor?
                val uri = data!!.data
                cursor1 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    val contractsWise =
                        cursor1?.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                }
                if (cursor1.moveToFirst()) {
                    //get contact details
                    val contactId =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val contactThumbnail =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResults =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()
                    //set details: contact id, contact name, image
//                    binding.contactTv.append("ID: $contactId")
//                    binding.contactTv.append("\nName: $contactName")
                    //set image, first check if uri/thumbnail is not null


                    //check if contact has a phone number or not
                    if (idResultHold == 1) {
                        cursor2 = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null
                        )
                        //a contact may have multiple phone numbers
                        while (cursor2!!.moveToNext()) {
                            //get phone number
                            val contactNumber =
                                cursor2.getString(cursor2.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            //set phone number
//                            binding.contactTv.append("\nPhone: $contactNumber")

                            val blockedContacts = BlockedContacts(
                                null,
                                contactNumber.replace(" ", "").replace("-", "").trim(),
                                contactName
                            )
                            viewModel.insertContact(blockedContacts)

                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }

    }
}