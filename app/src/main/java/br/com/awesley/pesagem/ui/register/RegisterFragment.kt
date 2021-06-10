package br.com.awesley.pesagem.ui.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.awesley.pesagem.databinding.FragmentRegisterBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*


class RegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        registerViewModel =
                ViewModelProvider(this).get(RegisterViewModel::class.java)

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setting the behavior of the datepicker
        val editTextDate = binding.editTextDate
        // Set the default date as today
        editTextDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()))
        editTextDate.setOnClickListener {
            // Check if there is a default date in the field
            var defaultDate = editTextDate.text.toString()
            if (defaultDate.isBlank()) {
                defaultDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())
            }

            val datePickerDialog = context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        val selectedDate = dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year
                        editTextDate.setText(selectedDate)
                    }, defaultDate.split("/")[2].toInt(), defaultDate.split("/")[1].toInt(), defaultDate.split("/")[0].toInt()
                )
            }

            datePickerDialog?.datePicker?.maxDate = System.currentTimeMillis()
            datePickerDialog?.show()
        }

        // Setting the behavior of the "OK" button
        val buttonOK = binding.buttonOk
        buttonOK.setOnClickListener okClickListener@ {
            // Validate the fields
            if (binding.editTextTag.text!!.isEmpty()) {
                Toast.makeText(context, "Por favor escreva o número do brinco do animal.", Toast.LENGTH_LONG).show()
                binding.editTextTag.requestFocus()
                return@okClickListener
            }

            if (binding.editTextWeight.text!!.isEmpty()) {
                Toast.makeText(context, "Por favor, insira o peso do animal.", Toast.LENGTH_LONG).show()
                binding.editTextWeight.requestFocus()
                return@okClickListener
            }

            if (binding.editTextDate.text!!.isEmpty()) {
                Toast.makeText(context, "Por favor, escolha uma data.", Toast.LENGTH_LONG).show()
                //binding.editTextDate.requestFocus()
                return@okClickListener
            }


            // Get data from the text fields
            val tag = binding.editTextTag.text.toString()
            val weight = binding.editTextWeight.text.toString()
            val date = binding.editTextDate.text.toString()
            val invertedDayDate = "${date.split("/")[1]}/${date.split("/")[0]}/${date.split("/")[2]}"
            val observation = binding.editTextObservation.text.toString()

            val data = hashMapOf(
                "weight" to weight.toDouble(),
                "date" to Timestamp(Date(invertedDayDate)),
                "observation" to observation
            )

            // Send the data to firebase
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(tag).add(data).addOnSuccessListener {
                firestore.collection("listCollections").document(tag).set(hashMapOf("exists" to true)).addOnSuccessListener {
                    Toast.makeText(context, "Numero atualizado.", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(context, "Pesagem inserida com sucesso.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Log.d("Firestore Error", it.stackTrace.toString())
                Toast.makeText(context, "Não foi possivel adicionar a pesagem. Tente novamente", Toast.LENGTH_LONG).show()
            }

            // Clear the text fields
            binding.editTextTag.text?.clear()
            binding.editTextWeight.text?.clear()
            binding.editTextDate.text?.clear()
            binding.editTextObservation.text?.clear()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}