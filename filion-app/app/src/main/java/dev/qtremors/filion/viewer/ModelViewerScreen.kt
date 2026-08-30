package dev.qtremors.filion.viewer

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.qtremors.filion.R
import dev.qtremors.filion.ui.ModelInfoDialog
import dev.qtremors.filion.ui.ViewerErrorCard
import dev.qtremors.filion.ui.ViewerStatusCard
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberFillLightNode
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive

@Composable
fun ModelViewerScreen(
    reference: String,
    title: String,
    sizeBytes: Long,
    mimeType: String?,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    onOpenWith: () -> Unit
) {
    var viewerState by remember(reference) { mutableStateOf(ModelViewerState()) }
    var autoRotationAngle by remember(reference) { mutableFloatStateOf(0f) }

    // Smooth turntable rotation loop for screensaver mode
    LaunchedEffect(viewerState.autoRotate, viewerState.autoRotateSpeed) {
        if (viewerState.autoRotate) {
            var lastTime = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { currentTime ->
                    val dt = (currentTime - lastTime) / 1_000_000_000f
                    lastTime = currentTime
                    autoRotationAngle = (autoRotationAngle + dt * viewerState.autoRotateSpeed) % 360f
                }
            }
        }
    }

    val animatedZoomScale by animateFloatAsState(
        targetValue = viewerState.zoomScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "modelZoomScale"
    )
    val animatedLightBrightness by animateFloatAsState(
        targetValue = viewerState.lightBrightness,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "modelLightBrightness"
    )

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader, isOpaque = false)
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = MAIN_LIGHT_INTENSITY * animatedLightBrightness
    }
    val fillLightNode = rememberFillLightNode(engine) {
        intensity = FILL_LIGHT_INTENSITY * animatedLightBrightness
    }
    val cameraManipulator = rememberCameraManipulator()
    var modelInstance by remember(reference) { mutableStateOf<ModelInstance?>(null) }
    val modelViewerError = stringResource(R.string.model_viewer_error)
    val backgroundColor = when (viewerState.backgroundMode) {
        ModelViewerBackground.Theme -> MaterialTheme.colorScheme.surface
        ModelViewerBackground.White -> Color.White
        ModelViewerBackground.Black -> Color.Black
    }

    SideEffect {
        environment.indirectLight?.intensity = MAIN_LIGHT_INTENSITY * animatedLightBrightness
    }

    val context = LocalContext.current
    val activity = remember(context) {
        generateSequence(context) { if (it is ContextWrapper) it.baseContext else null }
            .filterIsInstance<Activity>()
            .firstOrNull()
    }

    DisposableEffect(activity, viewerState.uiVisible) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (!viewerState.uiVisible) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val resetView = {
        autoRotationAngle = 0f
        viewerState = viewerState.copy(
            zoomScale = 1f,
            rotationX = 0f,
            rotationY = 0f,
            rotationZ = 0f,
            panX = 0f,
            panY = 0f
        )
    }

    BackHandler {
        when {
            viewerState.infoVisible -> {
                viewerState = viewerState.copy(infoVisible = false)
            }
            viewerState.activeControl != ModelViewerControl.None -> {
                viewerState = viewerState.copy(activeControl = ModelViewerControl.None)
            }
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(reference, modelLoader) {
        viewerState = viewerState.copy(loading = true, errorMessage = null)
        modelInstance = null
        try {
            modelInstance = loadSceneViewModelInstance(context, modelLoader, reference)
            viewerState = viewerState.copy(loading = false)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            viewerState = viewerState.copy(
                loading = false,
                errorMessage = error.localizedMessage ?: modelViewerError
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Box(modifier = Modifier.fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                surfaceType = SurfaceType.TextureSurface,
                engine = engine,
                modelLoader = modelLoader,
                cameraManipulator = cameraManipulator,
                isOpaque = false,
                environment = environment,
                mainLightNode = mainLightNode,
                fillLightNode = fillLightNode,
                autoFitContent = true,
                onGestureListener = rememberOnGestureListener(
                    onSingleTapConfirmed = { _, _ ->
                        viewerState = if (viewerState.activeControl != ModelViewerControl.None) {
                            viewerState.copy(activeControl = ModelViewerControl.None)
                        } else {
                            viewerState.copy(uiVisible = !viewerState.uiVisible)
                        }
                    },
                    onDoubleTap = { _, _ ->
                        resetView()
                    }
                )
            ) {
                modelInstance?.let { instance ->
                    Node(
                        rotation = Rotation(0f, autoRotationAngle, 0f),
                        scale = Scale(animatedZoomScale)
                    ) {
                        ModelNode(
                            modelInstance = instance,
                            autoAnimate = true,
                            scaleToUnits = 2f,
                            centerOrigin = Position(0f, 0f, 0f)
                        )
                    }
                }
            }

            if (viewerState.loading) {
                ViewerStatusCard(
                    title = stringResource(R.string.model_viewer_loading),
                    detail = title.ifBlank { reference.substringAfterLast('/') },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            viewerState.errorMessage?.let { message ->
                ViewerErrorCard(
                    message = message.ifBlank { modelViewerError },
                    onOpenWith = onOpenWith,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            ModelViewerTopOverlay(
                visible = viewerState.uiVisible && !viewerState.infoVisible,
                title = title.ifBlank { reference.substringAfterLast('/') },
                modifier = Modifier.align(Alignment.TopCenter)
            )
            ModelViewerBottomOverlay(
                visible = viewerState.uiVisible && !viewerState.infoVisible,
                state = viewerState,
                onStateChange = { viewerState = it },
                onResetView = resetView,
                onShare = onShare,
                onOpenWith = onOpenWith,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (viewerState.infoVisible) {
                ModelInfoDialog(
                    title = title,
                    reference = reference,
                    sizeBytes = sizeBytes,
                    mimeType = mimeType,
                    onDismiss = { viewerState = viewerState.copy(infoVisible = false) }
                )
            }
        }
    }
}

private const val MAIN_LIGHT_INTENSITY = 10_000f
private const val FILL_LIGHT_INTENSITY = 3_000f
