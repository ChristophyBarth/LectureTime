package capps.lecturetime.room

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromString(value: String): List<UUID> {
        return value.split(",").map { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromList(list: List<UUID>?): String {
        return list?.joinToString(separator = ",") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toIntegerList(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromIntegerList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromPair(pair: Pair<Int, Int>): String {
        return "${pair.first},${pair.second}"
    }

    @TypeConverter
    fun toPair(value: String): Pair<Int, Int> {
        val parts = value.split(",")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }
}
