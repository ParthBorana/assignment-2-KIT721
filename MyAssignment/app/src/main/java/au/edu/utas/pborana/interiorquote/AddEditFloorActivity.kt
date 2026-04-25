package au.edu.utas.pborana.interiorquote

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddEditFloorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var floorId: String? = null

    private var selectedProductName = ""
    private var selectedProductPrice = 100.0
    private var selectedProductColour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_floor)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        floorId = intent.getStringExtra("FLOOR_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        val txtName = findViewById<EditText>(R.id.txtFloorName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtDepth = findViewById<EditText>(R.id.txtDepth)
        val btnSelectProduct = findViewById<Button>(R.id.btnSelectProduct)
        val btnSave = findViewById<Button>(R.id.btnSaveFloor)
        val btnDelete = findViewById<Button>(R.id.btnDeleteFloor)

        btnBack.setOnClickListener { finish() }

        btnSelectProduct.setOnClickListener {
            showProductDialog(btnSelectProduct)
        }

        if (floorId != null) {
            txtTitle.text = "Edit Floor Space"
            btnSave.text = "Update Floor Space"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("FLOOR_NAME"))
            txtWidth.setText(intent.getDoubleExtra("FLOOR_WIDTH", 0.0).toString())
            txtDepth.setText(intent.getDoubleExtra("FLOOR_DEPTH", 0.0).toString())

            selectedProductName = intent.getStringExtra("FLOOR_PRODUCT_NAME") ?: ""
            selectedProductPrice = intent.getDoubleExtra("FLOOR_PRODUCT_PRICE", 100.0)
            selectedProductColour = intent.getStringExtra("FLOOR_PRODUCT_COLOUR") ?: ""

            if (selectedProductName.isNotEmpty()) {
                btnSelectProduct.text =
                    "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
            }
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val depth = txtDepth.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || depth == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedProductName.isEmpty()) {
                Toast.makeText(this, "Select a product first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val extra = btnSelectProduct.tag as? Triple<String, String, String>

            val floor = FloorSpace(
                name = name,
                width = width,
                depth = depth,
                productName = selectedProductName,
                productDescription = extra?.first ?: "",
                productPricePerSqm = selectedProductPrice,
                productImageUrl = extra?.second ?: "",
                productColour = selectedProductColour
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("floors")

            if (floorId == null) {
                ref.add(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving floor space", Toast.LENGTH_SHORT).show()
                    }
            } else {
                ref.document(floorId!!)
                    .set(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating floor space", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDelete.setOnClickListener {
            if (houseId == null || roomId == null || floorId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Floor Space")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("floors")
                        .document(floorId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Floor space deleted", Toast.LENGTH_SHORT).show()
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
            "Budget Carpet",
            "Vinyl Flooring",
            "Premium Timber"
        )

        val descriptions = arrayOf(
            "Affordable soft carpet flooring",
            "Durable waterproof vinyl flooring",
            "High quality natural timber finish"
        )

        val prices = arrayOf(100.0, 130.0, 180.0)

        val imageUrls = arrayOf(
            "https://example.com/carpet.jpg",
            "https://example.com/vinyl.jpg",
            "https://example.com/timber.jpg"
        )

        val colourOptions = arrayOf(
            arrayOf("Beige", "Brown"),
            arrayOf("Grey", "Black"),
            arrayOf("Oak", "Walnut")
        )

        AlertDialog.Builder(this)
            .setTitle("Select Floor Product")
            .setItems(productNames) { _, which ->

                AlertDialog.Builder(this)
                    .setTitle("Select Colour")
                    .setItems(colourOptions[which]) { _, colourIndex ->

                        selectedProductName = productNames[which]
                        selectedProductPrice = prices[which]
                        selectedProductColour = colourOptions[which][colourIndex]

                        val desc = descriptions[which]
                        val img = imageUrls[which]

                        btnSelectProduct.text =
                            "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"

                        btnSelectProduct.tag = Triple(desc, img, selectedProductColour)
                    }
                    .show()
            }
            .show()
    }
}