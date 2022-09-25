package ui

import DetectionResultFocus
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import detection.JsonDetection

@Composable
fun JsonDetectionView(
    detectionResultFocus: DetectionResultFocus?,
    moveToPreviousOccurrence: (DetectionResultFocus) -> Unit,
    moveToNextOccurrence: (DetectionResultFocus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        Box(modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))) {
            Column(Modifier.padding(8.dp)) {
                val title = buildAnnotatedString {
                    append("Json ")
                    withStyle(JsonDetection().detectedStyle) {
                        append("{ \"key\" : \"value\" }")
                    }
                }
                Text(title)
                detectionResultFocus?.let {
                    Column {
                        Row {
                            if (it.focusing == null) {
                                Text(
                                    "${it.results.size} results",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            } else {
                                Text(
                                    "${it.currentIndexInView} / ${detectionResultFocus.totalCount}",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                            IconButton(onClick = { moveToPreviousOccurrence(it) }) {
                                Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
                            }
                            IconButton(onClick = { moveToNextOccurrence(it) }) {
                                Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
                            }
                        }

                        // TODO cleanup ; don't cast
                        /*
                                        (it.focusing as? JsonDetectionResult)?.let {
                                            Text(it.jsonList) // TODO show summary of json
                                        }
                        */
                    }
                }
            }
        }
    }
}
