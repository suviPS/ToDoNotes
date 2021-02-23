package tk.httpksfdev.todo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tk.httpksfdev.todo.data.DbHelper
import tk.httpksfdev.todo.databinding.ActivityBackupBinding
import java.io.*
import java.util.*

class BackupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonExport.setOnClickListener { createFile() }
        binding.buttonImport.setOnClickListener { selectFile() }
    }

    //Activity result callback for create file intent
    private val createFileActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileUri: Uri? = result.data?.data
            if (fileUri == null) {
                displayMessage(getString(R.string.create_file_failed))
            } else {
                exportDatabaseToFile(fileUri)
            }
        }
    }

    private fun createFile() {
        val fileName = "todo_backup_" + Calendar.getInstance().timeInMillis + ".db"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.sqlite3"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createFileActivity.launch(intent)
    }

    private fun exportDatabaseToFile(fileUri: Uri) {
        //database path
        val inFileName = this.getDatabasePath(DbHelper.DATABASE_NAME).toString()

        //move to IO dispatcher
        GlobalScope.launch(Dispatchers.IO) {
            var msg = getString(R.string.db_backup_success)
            try {
                contentResolver.openFileDescriptor(fileUri, "w")?.use {
                    val dbFile = File(inFileName)
                    val fis = FileInputStream(dbFile)
                    val output: OutputStream = FileOutputStream(it.fileDescriptor)

                    val buffer = ByteArray(1024)
                    var length: Int = 1
                    length = fis.read(buffer)
                    while (length > 0) {
                        output.write(buffer, 0, length)
                        length = fis.read(buffer)
                    }
                    output.flush()
                    output.close()
                    fis.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                msg = getString(R.string.write_to_file_failed)
            }

            launch(Dispatchers.Main) {
                displayMessage(msg)
            }
        }
    }


    //Activity result callback for select file intent
    private val selectFileActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileUri: Uri? = result.data?.data
            if (fileUri == null) {
                displayMessage(getString(R.string.select_file_failed))
            } else {
                importDatabaseFromFile(fileUri)
            }
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
        }
        selectFileActivity.launch(intent)
    }

    private fun importDatabaseFromFile(fileUri: Uri) {
        //database path
        val outFileName = this.getDatabasePath(DbHelper.DATABASE_NAME).toString()

        //move to IO dispatcher
        GlobalScope.launch(Dispatchers.IO) {
            var msg = getString(R.string.db_import_success)
            try {
                val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
                        ?: throw RuntimeException("parcelFileDescriptor == null")
                val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor

                val fis = FileInputStream(fileDescriptor)
                val output: OutputStream = FileOutputStream(outFileName)

                val buffer = ByteArray(1024)
                var length: Int = 1
                length = fis.read(buffer)
                while (length > 0) {
                    output.write(buffer, 0, length)
                    length = fis.read(buffer)
                }
                output.flush()
                output.close()
                fis.close()

            } catch (e: Exception) {
                e.printStackTrace()
                msg = getString(R.string.write_to_file_failed)
            }

            launch(Dispatchers.Main) {
                displayMessage(msg)
            }
        }

    }

    private fun displayMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}