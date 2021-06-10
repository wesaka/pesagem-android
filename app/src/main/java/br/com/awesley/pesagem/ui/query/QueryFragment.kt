package br.com.awesley.pesagem.ui.query

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.awesley.pesagem.databinding.FragmentQueryBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class QueryFragment : Fragment() {

    private lateinit var queryViewModel: QueryViewModel
    private var _binding: FragmentQueryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        queryViewModel =
                ViewModelProvider(this).get(QueryViewModel::class.java)

        _binding = FragmentQueryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setting the behavior of the search button
        val buttonSearch = binding.buttonSearch
        buttonSearch.setOnClickListener {
            // Instantiate the recycler view and the data associated with it
            val recyclerQuery = binding.recyclerQuery

            // Get the tag number from the field
            val tagNumber = binding.editTextSearchTag.text.toString()
            var recyclerData: ArrayList<String> = ArrayList()

            if (tagNumber.isEmpty()) {
                Toast.makeText(context, "Por favor, digite um numero para pesquisar.", Toast.LENGTH_LONG).show()
            } else {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("listCollections")

                firestore.collection(tagNumber).get().addOnSuccessListener { result ->

                    for (document in result) {
                        val weight = document.data["weight"]
                        val date = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format((document.data["date"] as Timestamp).toDate())
                        val observation = document.data["observation"]

                        recyclerData.add("Brinco: $tagNumber - Peso: $weight - Data: $date - Observação: $observation")
                    }

                    val arrayAdapter = context?.let { it1 -> ArrayAdapter(it1, android.R.layout.simple_list_item_1, recyclerData) }
                    recyclerQuery.adapter = arrayAdapter
//                for (document in result) {
//                    Log.d("101", "${document.id} => ${document.data}")
//                }
                }
            }

            // Clear the tag number field
            binding.editTextSearchTag.text.clear()
        }

        val buttonExcel = binding.buttonExcel
        buttonExcel.setOnClickListener buttonExcelListener@ {
//            if (csvFile.toString().isEmpty()) {
//                Toast.makeText(context, "Por favor, primeiro pesquise um número.", Toast.LENGTH_SHORT).show()
//                return@buttonExcelListener
//            }

            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("listCollections").get().addOnSuccessListener { result ->
                // Setting the base for the csv file
                var csvFile = StringBuilder("Brinco,Peso,Data,Obs\r\n")

                for (document in result) {
                    var calculatedSize = 0
                    firestore.collection(document.id).get().addOnSuccessListener { innerResult ->

                        for (innerDocument in innerResult) {
                            val weight = innerDocument.data["weight"]
                            val date = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format((innerDocument.data["date"] as Timestamp).toDate())
                            val observation = innerDocument.data["observation"]

                            csvFile.append("${document.id},$weight,$date,$observation\r\n")
                            calculatedSize++

                            // If all documents were processed, write data to file
                            if(calculatedSize == result.size()) {
                                checkExternalMedia()
                                writeCSVFile(csvFile.toString())
                            }
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkExternalMedia() {
        var mExternalStorageAvailable = false
        var mExternalStorageWriteable = false
        val state = Environment.getExternalStorageState()
        when {
            Environment.MEDIA_MOUNTED == state -> {
                // Can read and write the media
                mExternalStorageWriteable = true
                mExternalStorageAvailable = mExternalStorageWriteable
            }
            Environment.MEDIA_MOUNTED_READ_ONLY == state -> {
                // Can only read the media
                mExternalStorageAvailable = true
                mExternalStorageWriteable = false
            }
            else -> {
                // Can't read or write
                mExternalStorageWriteable = false
                mExternalStorageAvailable = mExternalStorageWriteable
            }
        }
        Log.d("Media Write Status", "External Media: readable=$mExternalStorageAvailable writable=$mExternalStorageWriteable")
    }

    private fun writeCSVFile(csvContents: String) {

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
        // I know this is deprecated, but couldn't find a better way to do this
        val root = Environment.getExternalStorageDirectory()
        Log.d("File System", "\nExternal file system root: $root")

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        val dir = File("${root.absolutePath}/${Environment.DIRECTORY_DOWNLOADS}")
        dir.mkdirs()
        val fileName = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.ENGLISH).format(Date())
        val file = File(dir, "$fileName.csv")
        try {
            val f = FileOutputStream(file)
            val pw = PrintWriter(f)
            pw.print(csvContents)
            pw.flush()
            pw.close()
            f.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.d("File not found", "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(context, "Tabela exportada com sucesso para ${Environment.DIRECTORY_DOWNLOADS}/$fileName.csv", Toast.LENGTH_LONG).show()
        Log.d("Write Sucessful", "\n\nFile written to $file")
    }
}