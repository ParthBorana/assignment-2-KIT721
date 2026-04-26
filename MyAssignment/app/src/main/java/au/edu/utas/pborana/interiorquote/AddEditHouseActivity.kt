package au.edu.utas.pborana.interiorquote

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import androidx.appcompat.app.AlertDialog

class AddEditHouseActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var houseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_house)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtHouseFormTitle)
        val txtHouseName = findViewById<EditText>(R.id.txtHouseName)
        val txtHouseAddress = findViewById<EditText>(R.id.txtHouseAddress)
        val txtCustomerName = findViewById<EditText>(R.id.txtCustomerName)
        val btnSaveHouse = findViewById<Button>(R.id.btnSaveHouse)
        val btnDeleteHouse = findViewById<Button>(R.id.btnDeleteHouse)

        btnBack.setOnClickListener {
            finish()
        }


        houseId = intent.getStringExtra("HOUSE_ID")
        val houseName = intent.getStringExtra("HOUSE_NAME")
        val houseAddress = intent.getStringExtra("HOUSE_ADDRESS")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")

        if (houseId != null) {
            txtTitle.text = "Edit House"
            btnSaveHouse.text = "Update House"
            btnDeleteHouse.visibility = View.VISIBLE

            txtHouseName.setText(houseName)
            txtHouseAddress.setText(houseAddress)
            txtCustomerName.setText(customerName)
        }

        btnSaveHouse.setOnClickListener {
            val newHouseName = txtHouseName.text.toString().trim()
            val newHouseAddress = txtHouseAddress.text.toString().trim()
            val newCustomerName = txtCustomerName.text.toString().trim()

            if (newHouseName.isEmpty() || newHouseAddress.isEmpty() || newCustomerName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val house = House(
                name = newHouseName,
                address = newHouseAddress,
                customerName = newCustomerName
            )

            if (houseId == null) {
                // Add mode
                db.collection("houses")
                    .add(house)
                    .addOnSuccessListener {
                        Toast.makeText(this, "House saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save house", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Edit mode
                db.collection("houses")
                    .document(houseId!!)
                    .set(house)
                    .addOnSuccessListener {
                        Toast.makeText(this, "House updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update house", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDeleteHouse.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete House")
                .setMessage("Are you sure you want to delete this house?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "House deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete house", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}