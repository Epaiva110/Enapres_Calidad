package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.*
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: Int,
    val user: String,
    val password: String,
    val active: Boolean,
    val user_name: String,
    val role: String,
    @Serializable(with = TimestampToLongSerializer::class)
    val last_connection: Long?
)

object TimestampToLongSerializer : KSerializer<Long?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TimestampToLong", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Long {
        val value = decoder.decodeString()

        return LocalDateTime.parse(value)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    override fun serialize(encoder: Encoder, value: Long?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }

        val iso = Instant.ofEpochMilli(value).toString()
        encoder.encodeString(iso)
    }
}
