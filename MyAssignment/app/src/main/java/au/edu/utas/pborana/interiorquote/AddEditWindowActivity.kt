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
import kotlin.math.ceil

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

            selectedProductName = intent.getStringExtra("WINDOW_PRODUCT_NAME") ?: ""
            selectedProductPrice = intent.getDoubleExtra("WINDOW_PRODUCT_PRICE", 50.0)
            selectedProductColour = intent.getStringExtra("WINDOW_PRODUCT_COLOUR") ?: ""

            if (selectedProductName.isNotEmpty()) {
                btnSelectProduct.text = "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
            }
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val height = txtHeight.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || height == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedProductName.isEmpty()) {
                Toast.makeText(this, "Select a product first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validateSelectedProduct(width, height)) {
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val extra = btnSelectProduct.tag as? Pair<String, String>

            val window = Window(
                name = name,
                width = width,
                height = height,
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
            "Modular Vertical Slat",
            "Plantation Shutter"
        )

        val descriptions = arrayOf(
            "Basic roller blind suitable for small windows",
            "Premium curtain with extended width range",
            "Modular slat system for large windows",
            "Fixed size shutter panels"
        )

        val prices = arrayOf(50.0, 80.0, 95.0, 120.0)

        val imageUrls = arrayOf(
            "https://example.com/roller.jpg",
            "https://example.com/curtain.jpg",
            "https://example.com/slat.jpg",
            "https://example.com/shutter.jpg"
        )

        val colourOptions = arrayOf(
            arrayOf("White", "Grey"),
            arrayOf("Beige", "Black"),
            arrayOf("White", "Black"),
            arrayOf("Oak", "Walnut")
        )

        AlertDialog.Builder(this)
            .setTitle("Select Product")
            .setItems(productNames) { _, which ->

                val width = findViewById<EditText>(R.id.txtWidth).text.toString().toDoubleOrNull()
                val height = findViewById<EditText>(R.id.txtHeight).text.toString().toDoubleOrNull()

                if (width == null || height == null) {
                    Toast.makeText(this, "Enter width and height first", Toast.LENGTH_SHORT).show()
                    return@setItems
                }

                val productName = productNames[which]

                if (!validateProduct(productName, width, height)) {
                    return@setItems
                }

                // second dialog for colour selection
                AlertDialog.Builder(this)
                    .setTitle("Select Colour")
                    .setItems(colourOptions[which]) { _, colourIndex ->

                        selectedProductName = productName
                        selectedProductPrice = prices[which]
                        selectedProductColour = colourOptions[which][colourIndex]

                        val selectedDescription = descriptions[which]
                        val selectedImageUrl = imageUrls[which]

                        btnSelectProduct.text =
                            "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"

                        // store extra data using tags (simple method)
                        btnSelectProduct.tag = Pair(selectedDescription, selectedImageUrl)
                    }
                    .show()
            }
            .show()
    }

    private fun validateSelectedProduct(width: Double, height: Double): Boolean {
        return validateProduct(selectedProductName, width, height)
    }

    private fun validateProduct(productName: String, width: Double, height: Double): Boolean {
        if (height < 500.0 || height > 2500.0) {
            Toast.makeText(this, "Height must be 500–2500 mm", Toast.LENGTH_SHORT).show()
            return false
        }

        return when (productName) {
            "Standard Roller Blind" -> validateDirectFit(width, 500.0, 1200.0, productName)
            "Premium Curtain" -> validateDirectFit(width, 500.0, 2000.0, productName)
            "Modular Vertical Slat" -> validateMultiPanel(width, 600.0, 1000.0, 4, productName)
            "Plantation Shutter" -> validateRigidIncrement(width, 800.0, 3, productName)
            else -> {
                Toast.makeText(this, "Select a valid product", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun validateDirectFit(width: Double, minWidth: Double, maxWidth: Double, productName: String): Boolean {
        if (width < minWidth || width > maxWidth) {
            Toast.makeText(this, "$productName width must be ${minWidth.toInt()}–${maxWidth.toInt()} mm", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateMultiPanel(width: Double, minWidth: Double, maxWidth: Double, maxPanels: Int, productName: String): Boolean {
        if (width < minWidth) {
            Toast.makeText(this, "$productName width must be at least ${minWidth.toInt()} mm", Toast.LENGTH_SHORT).show()
            return false
        }

        if (width <= maxWidth) {
            return true
        }

        val requiredPanels = ceil(width / maxWidth).toInt()

        if (requiredPanels > maxPanels) {
            Toast.makeText(this, "$productName needs $requiredPanels panels, max allowed is $maxPanels", Toast.LENGTH_SHORT).show()
            return false
        }

        val panelWidth = width / requiredPanels

        if (panelWidth < minWidth || panelWidth > maxWidth) {
            Toast.makeText(this, "$productName cannot split into valid panel widths", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validateRigidIncrement(width: Double, panelWidth: Double, maxPanels: Int, productName: String): Boolean {
        for (panels in 1..maxPanels) {
            if (width == panelWidth * panels) {
                return true
            }
        }

        Toast.makeText(this, "$productName only supports 800, 1600, or 2400 mm width", Toast.LENGTH_SHORT).show()
        return false
    }
}