package capps.lecturetime.utils

import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun dateFormatter(date: Date): String {/*val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date).toInt()*/

        val daySuffix = when (val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()) {
            in 11..13 -> "th"
            else -> when (dayOfMonth % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

        return SimpleDateFormat("EEEE, d'$daySuffix' MMMM yyyy", Locale.getDefault()).format(date)
    }

    fun formatTime(number: Int): String {
        return String.format("%02d", number)
    }

    fun getDays(days: List<Int>): String {
        val daysText: String

        val stringBuilder = StringBuilder("")

        for (day in days) {
            when (day) {
                Calendar.MONDAY -> if (stringBuilder.isBlank()) stringBuilder.append("M")
                    .toString() else stringBuilder.append(", M").toString()

                Calendar.TUESDAY -> if (stringBuilder.isBlank()) stringBuilder.append("T")
                    .toString() else stringBuilder.append(", T").toString()

                Calendar.WEDNESDAY -> if (stringBuilder.isBlank()) stringBuilder.append("W")
                    .toString() else stringBuilder.append(", W").toString()

                Calendar.THURSDAY -> if (stringBuilder.isBlank()) stringBuilder.append("TH")
                    .toString() else stringBuilder.append(", TH").toString()

                Calendar.FRIDAY -> if (stringBuilder.isBlank()) stringBuilder.append("F")
                    .toString() else stringBuilder.append(", F").toString()

                Calendar.SATURDAY -> if (stringBuilder.isBlank()) stringBuilder.append("S")
                    .toString() else stringBuilder.append(", S").toString()

                Calendar.SUNDAY -> if (stringBuilder.isBlank()) stringBuilder.append("SU")
                    .toString() else stringBuilder.append(", SU").toString()
            }
        }


        daysText = stringBuilder.toString()

        return daysText
    }

    fun millisUntilNextTime(dayOfWeek: Int, hourOfDay: Int, minuteOfHour: Int): Long {
        val now = Calendar.getInstance()
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        val nextDay = now.clone() as Calendar
        nextDay.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        nextDay.set(Calendar.HOUR_OF_DAY, hourOfDay)
        nextDay.set(Calendar.MINUTE, minuteOfHour)
        nextDay.set(Calendar.SECOND, 0)

        if (currentDayOfWeek == dayOfWeek && (now.get(Calendar.HOUR_OF_DAY) < hourOfDay || (now.get(Calendar.HOUR_OF_DAY) <= hourOfDay && now.get(
                Calendar.MINUTE
            ) < minuteOfHour))
        ) {
            return nextDay.timeInMillis - now.timeInMillis
        } else if (dayOfWeek > currentDayOfWeek) {
            return nextDay.timeInMillis - now.timeInMillis
        }

        nextDay.add(Calendar.DAY_OF_YEAR, 7)

        return nextDay.timeInMillis - now.timeInMillis
    }
}