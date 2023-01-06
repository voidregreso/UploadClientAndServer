package com.ernesto.fileuploaderclient

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.rosuh.filepicker.config.FilePickerManager
import java.io.*
import java.net.*

class NetworkAsyncTask(private val listener: OnTaskCompleted) : AsyncTask<String, Void, String>() {

    private fun uploadFile(uploadUrl: String, srcPath: String) : String {
        val end = "\r\n"
        val twoHyphens = "--"
        val boundary = "******"
        try {
            val url = URL(uploadUrl)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.doInput = true
            httpURLConnection.doOutput = true
            httpURLConnection.useCaches = false
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive")
            httpURLConnection.setRequestProperty("Charset", "UTF-8")
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            val dos = DataOutputStream(httpURLConnection.outputStream)
            dos.writeBytes("$twoHyphens$boundary$end")
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${srcPath.substring(srcPath.lastIndexOf("/") + 1)}\"$end")
            dos.writeBytes(end)

            val fis = FileInputStream(srcPath)
            val buffer = ByteArray(8192) // 8k
            var count = 0
            while (fis.read(buffer).also { count = it } != -1) {
                dos.write(buffer, 0, count)
            }
            fis.close()

            dos.writeBytes(end)
            dos.writeBytes("$twoHyphens$boundary$twoHyphens$end")
            dos.flush()

            val isr = InputStreamReader(httpURLConnection.inputStream, "utf-8")
            val br = BufferedReader(isr)
            var result = br.readLine()
            if(result == null) result = "Success!"
            dos.close()
            isr.close()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            val result = e.toString()
            return result
        }
    }

    interface OnTaskCompleted {
        fun onTaskCompleted(result: String)
    }

    override fun doInBackground(vararg params: String): String {
        // Perform network operation here and return the result
        val url = params[0]
        val path = params[1]
        return uploadFile(url, path)
    }

    override fun onPostExecute(result: String) {
        listener.onTaskCompleted(result)
    }
}

class MainActivity : AppCompatActivity() {

    private var files : MutableList<String> = mutableListOf()
    private lateinit var edtWebAddr : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var fabSelect : FloatingActionButton = findViewById(R.id.fabSelect)
        var fabUpload : FloatingActionButton = findViewById(R.id.fabUpload)
        edtWebAddr = findViewById(R.id.edtWebAddr)
        edtWebAddr.setText("http://192.168.31.248:8080/upload_file")
        fabSelect.setOnClickListener {
            FilePickerManager
                .from(this)
                .forResult(FilePickerManager.REQUEST_CODE)
        }
        fabUpload.setOnClickListener {
            val ip_addr = edtWebAddr.text.trim().toString()
            if(!ip_addr.isEmpty() && !files.isEmpty()) {
                for(v in files) {
                    val task = NetworkAsyncTask(object : NetworkAsyncTask.OnTaskCompleted {
                        override fun onTaskCompleted(result: String) {
                            // Update the UI with the result here
                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Process completed")
                            builder.setMessage(result)
                            builder.setPositiveButton("OK", null)
                            val dlg = builder.create()
                            dlg.show()
                        }
                    })
                    task.execute(ip_addr, v)
                }
            } else Toast.makeText(this, "No files selected or no server address inputted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    // do your work
                    files = list
                }
            }
        }
    }
}