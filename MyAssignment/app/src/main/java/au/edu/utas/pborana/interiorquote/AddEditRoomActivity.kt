package au.edu.utas.pborana.interiorquote

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class AddEditRoomActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var houseId: String? = null
    private var roomId: String? = null
    private var imageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                findViewById<ImageView>(R.id.imgRoom).setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_room)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtRoomFormTitle)
        val txtRoomName = findViewById<EditText>(R.id.txtRoomName)
        val txtRoomType = findViewById<EditText>(R.id.txtRoomType)
        val txtRoomSize = findViewById<EditText>(R.id.txtRoomSize)
        val btnSaveRoom = findViewById<Button>(R.id.btnSaveRoom)
        val btnDeleteRoom = findViewById<Button>(R.id.btnDeleteRoom)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val imgRoom = findViewById<ImageView>(R.id.imgRoom)

        btnBack.setOnClickListener {
            finish()
        }

        btnTakePhoto.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (roomId != null) {
            txtTitle.text = "Edit Room"
            btnSaveRoom.text = "Update Room"
            btnDeleteRoom.visibility = View.VISIBLE

            txtRoomName.setText(intent.getStringExtra("ROOM_NAME"))
            txtRoomType.setText(intent.getStringExtra("ROOM_TYPE"))
            txtRoomSize.setText(intent.getStringExtra("ROOM_SIZE"))

            val savedImage = intent.getStringExtra("ROOM_IMAGE") ?: ""
            if (savedImage.isNotEmpty()) {
                imageUri = Uri.parse(savedImage)
                imgRoom.setImageURI(imageUri)
            }
        }

        btnSaveRoom.setOnClickListener {
            val roomName = txtRoomName.text.toString().trim()
            val roomType = txtRoomType.text.toString().trim()
            val roomSize = txtRoomSize.text.toString().trim()

            if (roomName.isEmpty() || roomType.isEmpty() || roomSize.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null) {
                Toast.makeText(this, "Missing house information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val room = Room(
                name = roomName,
                type = roomType,
                size = roomSize,
                imageUri = imageUri?.toString() ?: ""
            )

            val roomsCollection = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")

            if (roomId == null) {
                roomsCollection
                    .add(room)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Room saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save room", Toast.LENGTH_SHORT).show()
                    }
            } else {
                roomsCollection
                    .document(roomId!!)
                    .set(room)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Room updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update room", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDeleteRoom.setOnClickListener {
            if (houseId == null || roomId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Room")
                .setMessage("Are you sure you want to delete this room?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Room deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete room", Toast.LENGTH_SHORT).show()
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

    private fun openCamera() {
        val file = File(getExternalFilesDir("Pictures"), "room_photo_${System.currentTimeMillis()}.jpg")

        imageUri = FileProvider.getUriForFile(
            this,
            "au.edu.utas.pborana.interiorquote.provider",
            file
        )

        imageUri?.let {
            cameraLauncher.launch(it)
        }
    }
}