package au.edu.utas.pborana.interiorquote

data class FloorSpace(
    var id: String? = null,
    var name: String = "",
    var width: Double = 0.0,
    var depth: Double = 0.0,
    var productName: String = "",
    var productPricePerSqm: Double = 100.0,
    var productColour: String = ""
)