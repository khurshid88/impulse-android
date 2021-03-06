package uz.impulse.impulse.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import uz.impulse.impulse.R
import uz.impulse.impulse.adapter.ContactItemAdapter
import uz.impulse.impulse.data.local.AppDatabase
import uz.impulse.impulse.data.local.entity.Contact
import uz.impulse.impulse.data.local.entity.Message
import uz.impulse.impulse.databinding.DialogContactMessageViewBinding
import uz.impulse.impulse.databinding.DialogDeleteMessageBinding
import uz.impulse.impulse.databinding.FragmentContactsBinding
import uz.impulse.impulse.utils.Logger
import uz.impulse.impulse.utils.SpacesItemDecoration
import uz.impulse.impulse.viewmodel.ContactsViewModel
import uz.impulse.impulse.viewmodel.factory.ContactsViewModelFactory
import uz.impulse.impulse.viewmodel.repository.ContactRepository
import uz.impulse.impulse.viewmodel.repository.MessageRepository


class ContactsFragment : BaseFragment() {
    private var _binding: FragmentContactsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val REQUEST_READ_CONTACTS_PERMISSION = 0
    private val REQUEST_CONTACT = 1

    private lateinit var contactAdapter: ContactItemAdapter
    private lateinit var appDatabase: AppDatabase
    private lateinit var contacts: ArrayList<Contact>
    private lateinit var viewModel: ContactsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val view = binding.root
        initViews()
        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION && grantResults.isNotEmpty()) {
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CONTACT && data != null) {
            val contactUri: Uri? = data.data

            // Perform your query - the contactUri
            // is like a "where" clause here
            val cursor: Cursor? = activity?.contentResolver!!
                .query(contactUri!!, null, null, null, null)
            val cursorPhone: Cursor?
            try {
                if (cursor!!.moveToFirst()) {
                    // get contact details
                    val contactId =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactThumbnail =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val name: String =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val idResults =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()

                    if (idResultHold == 1) {
                        cursorPhone = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null
                        )
                        //a contact may have multiple phone numbers
                        while (cursorPhone!!.moveToNext()) {
                            //get phone number
                            val contactNumber =
                                cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            //set phone number
                            val contact = Contact(name = name, number = contactNumber)
                            saveContactToDatabase(contact)
                        }
                        cursorPhone.close()
                    }

                }

                // Double-check that you
                // actually got results
                if (cursor.count == 0) return

            } finally {
                cursor?.close()
            }
        }
    }

    private fun initViews() {
        appDatabase = AppDatabase.getInstance(requireActivity())
        contacts = ArrayList()
        contacts = appDatabase.contactDao().getAllContacts() as ArrayList<Contact>
        setupViewModel()
        checkForContacts()
        // Intent to pick contacts
        val pickContact = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context)
            val decoration = SpacesItemDecoration(20)
            recyclerView.addItemDecoration(decoration)

            refreshAdapter(contacts)

            fab.setOnClickListener {
                vibrate()
                startActivityForResult(pickContact, REQUEST_CONTACT)
            }

            ivEdit.setOnClickListener {
                vibrate()
                setEditDialog()
            }
        }

        requestContactsPermission();
    }

    private fun checkForContacts() {
        binding.apply {
            if (appDatabase.contactDao().getAllContacts().isEmpty()) {
                llNoContacts.visibility = View.VISIBLE
                llContacts.visibility = View.GONE
                ivNoContacts.loop(true)
            } else {
                llNoContacts.visibility = View.GONE
                llContacts.visibility = View.VISIBLE
                ivNoContacts.pauseAnimation()
                ivNoContacts.loop(false)
            }
        }
    }

    private fun setEditDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogBinding = DialogContactMessageViewBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)
        val dialog = builder.create()
        dialogBinding.apply {
            mainDialogEditMessage.setOnClickListener {
                hideKeyboard(etMsg)
            }
        }

        if (appDatabase.messageDao().getMessage() != null &&
            appDatabase.messageDao().getMessage().trim() != ""
        ) {
            dialogBinding.apply {
                msgContainer.hint = ""
                etMsg.setText(
                    appDatabase.messageDao().getMessage(),
                    TextView.BufferType.EDITABLE
                )
            }
        } else {
            dialogBinding.msgContainer.hint = getString(R.string.str_enter_message)
        }
        dialogBinding.btnOk.setOnClickListener {
            val message = Message(dialogBinding.etMsg.text.toString().trim())
            Logger.d("@@@", message.message.toString())
            viewModel.deleteMessage()
            viewModel.saveMessage(message)
            dialogBinding.apply {
                msgContainer.hint = appDatabase.messageDao().getMessage()
            }
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshAdapter(contacts: ArrayList<Contact>) {
        contactAdapter = ContactItemAdapter(this@ContactsFragment, contacts)
        binding.recyclerView.adapter = contactAdapter
    }


    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Request contact permission if it
    // has not been granted already
    private fun requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS_PERMISSION
            )
        }
    }

    fun deleteContactFromDatabase(contact: Contact, position: Int) {
        Log.d("TAG", "deleteContactFromDatabase: ${contact.name}")
        Log.d("TAG", "deleteContactFromDatabase: ${contact.id}")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogBinding = DialogDeleteMessageBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)
        val dialog = builder.create()

        dialogBinding.btnOk.setOnClickListener {
            viewModel.deleteContact(contact)
            contacts.remove(contact)
            contactAdapter.notifyItemRemoved(position)
            refreshAdapter(contacts)
            checkForContacts()
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveContactToDatabase(contact: Contact) {
        viewModel.addContact(contact)
        contacts.add(contact)
        contactAdapter.notifyItemInserted(contacts.size - 1)
        refreshAdapter(contacts)
        checkForContacts()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ContactsViewModelFactory(
                ContactRepository(appDatabase.contactDao()),
                MessageRepository(appDatabase.messageDao())
            )
        )[ContactsViewModel::class.java]
    }

}