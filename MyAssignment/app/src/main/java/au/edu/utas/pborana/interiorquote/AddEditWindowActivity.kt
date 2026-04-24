package au.edu.utas.pborana.interiorquote

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddEditWindowActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var windowId: String? = null

    private var selectedProductName = ""
    private var selectedProductPrice = 50.0
    private var selectedProductColour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_window)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        windowId = intent.getStringExtra("WINDOW_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtName = findViewById<EditText>(R.id.txtWindowName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtHeight = findViewById<EditText>(R.id.txtHeight)
        val btnSelectProduct = findViewById<Button>(R.id.btnSelectProduct)
        val btnSave = findViewById<Button>(R.id.btnSaveWindow)
        val btnDelete = findViewById<Button>(R.id.btnDeleteWindow)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)

        btnBack.setOnClickListener {
            finish()
        }

        btnSelectProduct.setOnClickListener {
            showProductDialog(btnSelectProduct)
        }

        if (windowId != null) {
            txtTitle.text = "Edit Window"
            btnSave.text = "Update Window"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("WINDOW_NAME"))
            txtWidth.setText(intent.getDoubleExtra("WINDOW_WIDTH", 0.0).toString())
            txtHeight.setText(intent.getDoubleExtra("WINDOW_HEIGHT", 0.0).toString())
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val height = txtHeight.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || height == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val window = Window(
                name = name,
                width = width,
                height = height,
                productName = selectedProductName,
                productPricePerSqm = selectedProductPrice,
                productColour = selectedProductColour
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("windows")

            if (windowId == null) {
                ref.add(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving window", Toast.LENGTH_SHORT).show()
                    }
            } else {
                ref.document(windowId!!)
                    .set(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating window", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDelete.setOnClickListener {
            if (houseId == null || roomId == null || windowId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Window")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("windows")
                        .document(windowId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Window deleted", Toast.LENGTH_SHORT).show()
                            finish()
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

    private fun showProductDialog(btnSelectProduct: Button) {
        val productNames = arrayOf(
            "Standard Roller Blind",
            "Premium Curtain",
            "Luxury Shutter"
        )

        val prices = arrayOf(50.0, 80.0, 120.0)
        val colours = arrayOf("White", "Grey", "Black")

        AlertDialog.Builder(this)
            .setTitle("Select Product")
            .setItems(productNames) { _, which ->
                selectedProductName = productNames[which]
                selectedProductPrice = prices[which]
                selectedProductColour = colours[which]
                btnSelectProduct.text = "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
            }
            .show()
    }
}