@file:OptIn(ExperimentalUnitApi::class)

package serialization

import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Be careful: it assumes TextUnitType is always sp
object TextUnitAsFloatSerializer : KSerializer<TextUnit> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TextUnit", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: TextUnit) = encoder.encodeFloat(value.value)
    override fun deserialize(decoder: Decoder): TextUnit = TextUnit(decoder.decodeFloat(), TextUnitType.Sp)
}
