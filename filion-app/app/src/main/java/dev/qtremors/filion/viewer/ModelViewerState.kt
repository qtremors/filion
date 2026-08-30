package dev.qtremors.filion.viewer

enum class ModelViewerControl {
    None,
    Zoom,
    Brightness,
    Background,
    AutoRotate
}

enum class ModelViewerBackground {
    Theme,
    White,
    Black
}

data class ModelViewerState(
    val uiVisible: Boolean = true,
    val infoVisible: Boolean = false,
    val activeControl: ModelViewerControl = ModelViewerControl.None,
    val autoRotate: Boolean = false,
    val autoRotateSpeed: Float = 15f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val zoomScale: Float = 1f,
    val lightBrightness: Float = 1f,
    val backgroundMode: ModelViewerBackground = ModelViewerBackground.Theme,
    val loading: Boolean = true,
    val errorMessage: String? = null
)
