package zsoltmester.bitle

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// source:
// https://fvilarino.medium.com/creating-a-rotating-card-in-jetpack-compose-ba94c7dd76fb

enum class CardFace(val angle: Float) {
    Front(0f),
    Back(180f)
}

enum class RotationAxis {
    AxisX,
    AxisY,
}

@ExperimentalMaterialApi
@Composable
fun FlipCard(
    cardFace: CardFace,
    animationSpec: AnimationSpec<Float>,
    modifier: Modifier = Modifier,
    axis: RotationAxis = RotationAxis.AxisX,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
) {
    val rotation = animateFloatAsState(
        targetValue = cardFace.angle,
        animationSpec = animationSpec
    )
    Card(
        modifier = modifier
            .graphicsLayer {
                if (axis == RotationAxis.AxisX) {
                    rotationX = rotation.value
                } else {
                    rotationY = rotation.value
                }
                cameraDistance = 12f * density
            }
    ) {
        if (rotation.value <= 90f) {
            Box(
                Modifier.fillMaxSize()
            ) {
                front()
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (axis == RotationAxis.AxisX) {
                            rotationX = 180f
                        } else {
                            rotationY = 180f
                        }
                    },
            ) {
                back()
            }
        }
    }
}