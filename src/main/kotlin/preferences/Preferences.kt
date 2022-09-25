package preferences

import Priority
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class Preferences(
    val fontSize: TextUnit,
    val colorByPriority: Map<Priority, Color>
) {
    companion object {
        val default = Preferences(
            14.sp,
            mutableMapOf(
                Priority.Verbose to Color(0xFFBBBBBB),
                Priority.Debug to Color(0xFFAAB895),
                Priority.Info to Color(0xFF3EDE66),
                Priority.Warning to Color(0xFFFF6B68),
                Priority.Error to Color(0xFFFF6B68),
            )
        )
    }
}
