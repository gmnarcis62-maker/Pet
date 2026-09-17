package red.line.pet.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class JalaliDateHelperTest {

    @Test
    fun testGregorianToJalaliConversions() {
        // 2024-03-20 -> 1403/01/01 (Leap year)
        val j1 = JalaliDateHelper.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, j1.year)
        assertEquals(1, j1.month)
        assertEquals(1, j1.day)

        // 2024-03-21 -> 1403/01/02
        val j2 = JalaliDateHelper.gregorianToJalali(2024, 3, 21)
        assertEquals(1403, j2.year)
        assertEquals(1, j2.month)
        assertEquals(2, j2.day)

        // 2025-03-20 -> 1403/12/30 (1403 is leap, Esfand has 30 days)
        val j3 = JalaliDateHelper.gregorianToJalali(2025, 3, 20)
        assertEquals(1403, j3.year)
        assertEquals(12, j3.month)
        assertEquals(30, j3.day)

        // 2025-03-21 -> 1404/01/01
        val j4 = JalaliDateHelper.gregorianToJalali(2025, 3, 21)
        assertEquals(1404, j4.year)
        assertEquals(1, j4.month)
        assertEquals(1, j4.day)

        // 2026-09-14 (Current date) -> 1405/06/23
        val j5 = JalaliDateHelper.gregorianToJalali(2026, 9, 14)
        assertEquals(1405, j5.year)
        assertEquals(6, j5.month)
        assertEquals(23, j5.day)
    }

    @Test
    fun testJalaliToGregorianConversions() {
        // 1403/01/01 -> 2024-03-20
        val cal1 = JalaliDateHelper.jalaliToGregorian(1403, 1, 1)
        assertEquals(2024, cal1.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal1.get(Calendar.MONTH))
        assertEquals(20, cal1.get(Calendar.DAY_OF_MONTH))

        // 1404/01/01 -> 2025-03-21
        val cal2 = JalaliDateHelper.jalaliToGregorian(1404, 1, 1)
        assertEquals(2025, cal2.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal2.get(Calendar.MONTH))
        assertEquals(21, cal2.get(Calendar.DAY_OF_MONTH))

        // 1405/06/23 -> 2026-09-14
        val cal3 = JalaliDateHelper.jalaliToGregorian(1405, 6, 23)
        assertEquals(2026, cal3.get(Calendar.YEAR))
        assertEquals(Calendar.SEPTEMBER, cal3.get(Calendar.MONTH))
        assertEquals(14, cal3.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testLeapYearsAndMonthDays() {
        assertTrue(JalaliDateHelper.isJalaliLeapYear(1399))
        assertTrue(JalaliDateHelper.isJalaliLeapYear(1403))
        assertTrue(JalaliDateHelper.isJalaliLeapYear(1408))
        assertFalse(JalaliDateHelper.isJalaliLeapYear(1404))
        assertFalse(JalaliDateHelper.isJalaliLeapYear(1405))

        assertEquals(31, JalaliDateHelper.getDaysInMonth(1403, 1))
        assertEquals(31, JalaliDateHelper.getDaysInMonth(1403, 6))
        assertEquals(30, JalaliDateHelper.getDaysInMonth(1403, 7))
        assertEquals(30, JalaliDateHelper.getDaysInMonth(1403, 12)) // Leap year Esfand
        assertEquals(29, JalaliDateHelper.getDaysInMonth(1404, 12)) // Non-leap Esfand
    }

    @Test
    fun testParsingDigitsAndFormats() {
        // Standard Persian digits
        val p1 = JalaliDateHelper.parseJalali("۱۴۰۳/۰۶/۲۱")
        assertNotNull(p1)
        assertEquals(1403, p1!!.year)
        assertEquals(6, p1.month)
        assertEquals(21, p1.day)

        // Latin digits with dashes
        val p2 = JalaliDateHelper.parseJalali("1404-02-15")
        assertNotNull(p2)
        assertEquals(1404, p2!!.year)
        assertEquals(2, p2.month)
        assertEquals(15, p2.day)

        // Inverted DD/MM/YYYY format
        val p3 = JalaliDateHelper.parseJalali("10/05/1402")
        assertNotNull(p3)
        assertEquals(1402, p3!!.year)
        assertEquals(5, p3.month)
        assertEquals(10, p3.day)

        // Formatting
        assertEquals("۱۴۰۳/۰۶/۲۱", p1.toStandardString(true))
        assertEquals("1403/06/21", p1.toStandardString(false))
    }
}
