package au.edu.utas.pborana.interiorquote

data class Product(
    var id: String = "",
    var name: String = "",
    var type: String = "",
    var description: String = "",
    var pricePerSqm: Double = 0.0,
    var imageUrl: String = "",
    var colours: List<String> = listOf(),
    var minWidth: Double = 0.0,
    var maxWidth: Double = 0.0,
    var minHeight: Double = 0.0,
    var maxHeight: Double = 0.0,
    var maxPanels: Int = 1
)