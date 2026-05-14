import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

const val JUBILEE_START = 1000
const val JUBILEE_STEP = 1000

data class JubileeEvent(val date: LocalDate, val employeeName: String, val jubileeDays: Int)

fun main() {
    val employees = createEmployeeArray()
    val today = LocalDate.now()
    val weekRange = getWeekRange(today)

    val resultArray = findJubileesForWeek(employees, today, weekRange)
    printResultArray(resultArray)
}

fun createEmployeeArray(): Array<Array<Any>> = arrayOf(
    arrayOf("Иван Иванов", 993),
    arrayOf("Пётр Петров", 994),
    arrayOf("Алексей Сидоров", 995),
    arrayOf("Дмитрий Смирнов", 999),
    arrayOf("Сергей Кузнецов", 1000),
    arrayOf("Никита Попов", 1001),
    arrayOf("Андрей Васильев", 1993),
    arrayOf("Михаил Новиков", 1994),
    arrayOf("Владимир Фёдоров", 1995),
    arrayOf("Егор Морозов", 1996),
    arrayOf("Максим Волков", 2000),
    arrayOf("Роман Алексеев", 2001),
    arrayOf("Артём Лебедев", 2002),
    arrayOf("Кирилл Семёнов", 2005),
    arrayOf("Олег Егоров", 2006),
    arrayOf("Иван Петров", 2007),
    arrayOf("Пётр Иванов", 2008)
)

fun findJubileesForWeek(
    employees: Array<Array<Any>>,
    today: LocalDate,
    weekRange: ClosedRange<LocalDate>
): Array<Array<String>> {
    val events = employees
        .mapNotNull { employee -> calculateNextJubilee(employee[0] as String, employee[1] as Int, today) }
        .filter { event -> event.date in weekRange }
        .sortedBy { it.date }
        .groupBy { it.date }

    val result = mutableListOf<Array<String>>()
    weekRange.forEach { date ->
        val daysEvents = events[date] ?: emptyList()
        daysEvents.forEachIndexed { index, event ->
            val dateStr = if (index == 0) date.format(DateTimeFormatter.ofPattern("dd.MM")) else ""
            result.add(arrayOf(dateStr, "${event.employeeName} – ${event.jubileeDays} дней"))
        }
        if (daysEvents.isEmpty()) {
            result.add(arrayOf(date.format(DateTimeFormatter.ofPattern("dd.MM")), ""))
        }
    }
    return result.toTypedArray()
}

fun calculateNextJubilee(name: String, daysWorked: Int, today: LocalDate): JubileeEvent? {
    val daysToNextJubilee = calculateDaysToNextJubilee(daysWorked).takeIf { it >= 0 } ?: return null
    val jubileeDate = today.plusDays(daysToNextJubilee.toLong())
    val jubileeValue = daysWorked + daysToNextJubilee

    return JubileeEvent(jubileeDate, name, jubileeValue)
}

fun calculateDaysToNextJubilee(currentDays: Int): Int {
    val remainder = currentDays % 1000
    return (1000 - remainder) % 1000
}

fun getWeekRange(today: LocalDate): ClosedRange<LocalDate> {
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    return startOfWeek..endOfWeek
}

inline fun ClosedRange<LocalDate>.forEach(action: (LocalDate) -> Unit) {
    var current = start
    while (!current.isAfter(endInclusive)) {
        action(current)
        current = current.plusDays(1)
    }
}

fun printResultArray(result: Array<Array<String>>) {
    println("Date\tText")
    result.forEach { row ->
        println("${row[0]}\t${row[1]}")
    }
}
