package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detector.DetectionStatus
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.flow.StateFlow

@Composable
fun RowScope.DetectionView(
    preferences: Preferences,
    detectorManager: DetectorManager,
    statusByKey: Map<DetectorKey, DetectionStatus?>,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
    selectPreviousDetection: (status: DetectionStatus) -> Unit,
    selectNextDetection: (status: DetectionStatus) -> Unit,
) {
    Box(modifier = Modifier.weight(0.5f).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))) {
        Column {
            Text("Auto-detection", modifier = Modifier.padding(8.dp))
            Divider()
            Row {
                if (preferences.showExceptionDetection) {
                    ExceptionDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        statusByKey[DetectorKey.Exception],
                        selectPreviousDetection,
                        selectNextDetection
                    )

                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))
                }

                JsonDetectionView(
                    Modifier.width(200.dp).wrapContentHeight(),
                    statusByKey[DetectorKey.Json],
                    selectPreviousDetection,
                    selectNextDetection
                )

                Spacer(Modifier.width(8.dp))
                Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                Spacer(Modifier.width(8.dp))

                MarkDetectionView(
                    Modifier.width(200.dp).wrapContentHeight(),
                    statusByKey[DetectorKey.Mark],
                    selectPreviousDetection,
                    selectNextDetection,
                    openMarkedRowsTab = { openNewTab(detectorManager.markedRowsFlow) }
                )

                Spacer(Modifier.width(8.dp))
                Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
            }
        }
    }
}
