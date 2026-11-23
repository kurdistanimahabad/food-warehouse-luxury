package com.luxury.foodwarehouse.model

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    fun formatted(): String =
        String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
}

object JalaliCalendar {

    private val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    val monthNamesFa = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun daysInJalaliMonth(year: Int, month: Int): Int {
        val m = month.coerceIn(1, 12)
        return if (m == 12 && isJalaliLeapYear(year)) 30 else jDaysInMonth[m - 1]
    }

    fun isJalaliLeapYear(year: Int): Boolean {
        val rem = ((year - 979) % 33 + 33) % 33
        return rem in listOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        val jm = i + 1
        val jd = jDayNo + 1
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm.coerceIn(1, 12) - 1
        val maxDay = daysInJalaliMonth(jy, jm)
        val jd2 = jd.coerceIn(1, maxDay) - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + (((jy2 % 33) + 3) / 4)
        for (i in 0 until jm2) {
            jDayNo += jDaysInMonth[i]
        }
        jDayNo += jd2

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        var i = 0
        while (i < 11 && gDayNo >= gDaysInMonth[i] + (if (i == 1 && leap) 1 else 0)) {
            gDayNo -= gDaysInMonth[i] + (if (i == 1 && leap) 1 else 0)
            i++
        }
        val gm = i + 1
        val gd = gDayNo + 1
        return Triple(gy, gm, gd)
    }

    fun millisToJalali(millis: Long): JalaliDate {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = millis
        }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    fun jalaliToMillis(jalaliDate: JalaliDate, referenceMillis: Long = System.currentTimeMillis()): Long {
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val refCal = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = referenceMillis
        }
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, refCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, refCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, refCal.get(Calendar.SECOND))
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatJalali(millis: Long, includeMonthName: Boolean = false): String {
        val jd = millisToJalali(millis)
        val base = jd.formatted()
        return if (includeMonthName) {
            "$base (${monthNamesFa[jd.month - 1]})"
        } else {
            base
        }
    }
}
