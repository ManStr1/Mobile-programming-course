import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.math.abs



interface IMovable {
    val maxStep: Int
        get() = 1

    fun move(dx: Int, dy: Int): Boolean
}

fun ask(text: String): String? {
    print("$text: ")
    return readlnOrNull()?.trim()
}

// ITEMS
abstract class WorldItem(val name: String, x: Int, y: Int) {
    
    var x: Int = x
        protected set
    var y: Int = y
        protected set

    open fun getInfo(): String = "$name сейчас находится на ($x, $y)"
}

class Wardrobe(x: Int, y: Int) : WorldItem("Шкаф", x, y) {
    override fun getInfo(): String = super.getInfo()
}

class Pan(x: Int, y: Int) : WorldItem("Сковородка", x, y), IMovable {
    override val maxStep: Int = 20

    override fun move(dx: Int, dy: Int): Boolean {
        if (abs(dx) > maxStep || abs(dy) > maxStep) return false
        x += dx
        y += dy
        return true
    }

    override fun getInfo(): String = "${super.getInfo()}, если хочешь подвинь на $maxStep"
}

fun tryMove(item: WorldItem, dx: Int, dy: Int) {
    if (item is IMovable) {
        val ok = item.move(dx, dy)
        println("${item.name} ${if (ok) "сдвинут на координаты" else "не удалось подвинуть, она всё еще на "}(${item.x}, ${item.y})")
    } else {
        println("${item.name} не может двигаться, ты чё, чудик")
    }
}

fun addItem(world: World) {
    val names = itemFactories.keys.joinToString(", ")
    val type = ask("Тип предмета ($names)")?.lowercase() ?: return
    val factory = itemFactories[type]
    if (factory == null) {
        println("Такое мы не можем создать")
        return
    }
    val x = ask("X (по умолчанию 0)")?.toIntOrNull() ?: 0
    val y = ask("Y (по умолчанию 0)")?.toIntOrNull() ?: 0
    val item = factory(x, y)
    val id = world.add(item)
    println("Добавлено под айди - [$id], ${item.getInfo()}")
}

fun moveItem(world: World) {
    val id = ask("ID предмета")?.toIntOrNull()
    val item = if (id == null) null else world.get(id)
    if (item == null) {
        println("Предмет не найден")
        return
    }
    val dx = ask("Насколько сдвинуть по X?")?.toIntOrNull()
    val dy = ask("Насколько сдвинуть по Y?")?.toIntOrNull()
    if (dx == null || dy == null) {
        println("Ввод неверный")
        return
    }
    tryMove(item, dx, dy)
}
//End of items


// WORLD
class World {
    private val items: MutableMap<Int, WorldItem> = linkedMapOf()
    private var nextId = 1

    fun add(item: WorldItem): Int {
        val id = nextId++
        items[id] = item
        return id
    }

    fun get(id: Int): WorldItem? = items[id]

    fun all(): Map<Int, WorldItem> = items
}

val itemFactories: Map<String, (Int, Int) -> WorldItem> = mapOf(
    "шкаф" to { x, y -> Wardrobe(x, y) },
    "сковородка" to { x, y -> Pan(x, y) }
)

fun showWorld(world: World) {
    if (world.all().isEmpty()) {
        println("Пусто.")
        return
    }
    for ((id, item) in world.all()) {
        println("Предмет под айди - $id, ${item.getInfo()}")
    }
}
//


//PLAYER
data class Player(
    val id: Int,
    val nickname: String,
    val balance: Int,
    val registeredAt: LocalDate = LocalDate.now(),
    val items: List<WorldItem> = emptyList()
) {
    val rank: String
        get() = when {
            balance >= 10000 -> "Богач"
            balance >= 5000 -> "Средняк"
            else -> "Кепка"
        }

    fun shortInfo(): String =
        "#$id | $nickname | баланс: $balance | звание: $rank | кол-во предметов: ${items.size} | зареган $registeredAt"
}

sealed class RegistrationResult {
    data class Success(val player: Player) : RegistrationResult()
    data class InvalidNickname(val reason: String) : RegistrationResult()
    data class NicknameTaken(val nickname: String) : RegistrationResult()
}

class PlayerRegistry(private val startBalance: Int = 100) {
    private val usedNicknames: MutableSet<String> = mutableSetOf()
    private val players: MutableMap<Int, Player> = linkedMapOf()
    private var nextId = 1

    fun register(rawNickname: String): RegistrationResult {
        val nickname = rawNickname.trim()

        val error = validate(nickname)
        if (error != null) return RegistrationResult.InvalidNickname(error)

        if (!usedNicknames.add(nickname.lowercase())) {
            return RegistrationResult.NicknameTaken(nickname)
        }

        val player = Player(id = nextId++, nickname = nickname, balance = startBalance)
        players[player.id] = player
        return RegistrationResult.Success(player)
    }

    private fun validate(nickname: String): String? = when {
        nickname.isEmpty() -> "Слишком пусто.."
        nickname.length < 3 -> "Ну хотя б на 3 символа раскошелься!"
        nickname.length > 16 -> "Ну не так же длинно, уложись в 16 символов пожалуйста!"
        !nickname.all { it.isLetterOrDigit() || it == '_' } ->
            "разрешены только буквы и цифры, В КРАЙНЕМ случае подчеркивание"
        else -> null
    }

    fun update(player: Player) {
        require(players.containsKey(player.id)) { "Тело под номером #${player.id} не найдено" }
        players[player.id] = player
    }

    fun topUp(id: Int, amount: Int): Player? {
        val player = players[id] ?: return null
        val updated = player.copy(balance = player.balance + amount)
        players[id] = updated
        return updated
    }

    fun all(): List<Player> = players.values.toList()
}

suspend fun handleResult(
    result: RegistrationResult,
    registry: PlayerRegistry,
    creator: CharacterCreator,
    prefix: String = ""
) {
    when (result) {
        is RegistrationResult.Success -> {
            println("${prefix} успешно зареган! ID: ${result.player.id}")
            val created = creator.create(result.player) { status -> printStatus(status, prefix) }
            registry.update(created)
        }
        is RegistrationResult.InvalidNickname -> println("${prefix}Ошибка: ${result.reason}")
        is RegistrationResult.NicknameTaken -> println("${prefix}Ошибка: кто-то уже успел себя назвать '${result.nickname}'")
    }
}

object PlayerStats {
    fun totalBalance(players: List<Player>): Int =
        if (players.isEmpty()) 0 else players.map { it.balance }.reduce { acc, b -> acc + b }

    fun select(players: List<Player>, predicate: (Player) -> Boolean): List<Player> =
        players.filter(predicate)

    fun richest(players: List<Player>, count: Int): List<Player> =
        players.sortedByDescending { it.balance }.take(count)

    fun countByRank(players: List<Player>): Map<String, Int> =
        players.groupBy { it.rank }.mapValues { (_, group) -> group.size }
}
//


// CHARACTERS
sealed class CreationStatus {
    data class InProgress(val message: String, val percent: Int) : CreationStatus()
    data class Completed(val player: Player) : CreationStatus()
}

class CharacterCreator(private val stepDelayMs: Long = 400) {
    suspend fun create(player: Player, onStatus: (CreationStatus) -> Unit): Player = coroutineScope {
        val steps = listOf(
            "Создаём красоту...",
            "Готовим предметы для него...",
            "Настраиваем профиль..."
        )

        val starterItems = async {
            delay(stepDelayMs)
            listOf<WorldItem>(Pan(0, 0))
        }

        for ((index, message) in steps.withIndex()) {
            onStatus(CreationStatus.InProgress(message, (index + 1) * 100 / steps.size))
            delay(stepDelayMs)
        }

        val created = player.copy(items = starterItems.await())
        onStatus(CreationStatus.Completed(created))
        created
    }
}

fun printStatus(status: CreationStatus, prefix: String = "") {
    when (status) {
        is CreationStatus.InProgress -> println("$prefix[${status.percent}%] ${status.message}...")
        is CreationStatus.Completed ->
            println("${prefix}Персонаж '${status.player.nickname}' создан! Выдано предметов: ${status.player.items.size}")
    }
}

// 

fun showPlayers(registry: PlayerRegistry) {
    val players = registry.all()
    if (players.isEmpty()) {
        println("Игроков пока нет.")
        return
    }
    players.forEach { player ->
        println(player.shortInfo())
        player.items.forEach { item -> println("    - ${item.getInfo()}") }
    }
}

fun showStats(registry: PlayerRegistry) {
    val players = registry.all()
    if (players.isEmpty()) {
        println("Игроков пока нет.")
        return
    }
    println("Всего игроков: ${players.size}")
    println("Общий баланс: ${PlayerStats.totalBalance(players)}")
    println("Игроков среднего достатка и выше: ${PlayerStats.select(players) { it.balance >= 5000 }.size}")
    println("Наш топ богачей:")
    PlayerStats.richest(players, 3).forEachIndexed { index, p ->
        println("  ${index + 1}. ${p.nickname} - ${p.balance}")
    }
    println("По званиям:")
    for ((rank, count) in PlayerStats.countByRank(players)) {
        println("  $rank: $count")
    }
}

fun topUp(registry: PlayerRegistry) {
    val id = ask("ID игрока")?.toIntOrNull()
    val amount = ask("Сумма пополнения")?.toIntOrNull()
    if (id == null || amount == null || amount <= 0) {
        println("Нужно ввести ID и положительную сумму.")
        return
    }
    val updated = registry.topUp(id, amount)
    if (updated == null) println("Игрок #$id не найден.")
    else println("Новый баланс ${updated.nickname}: ${updated.balance} (${updated.rank})")
}



// MENU
fun printMenu() {
    println()
    println("1 - Зарегистрировать игрока")
    println("2 - Список игроков")
    println("3 - Статистика")
    println("4 - Пополнить баланс игроку")
    println("5 - Комната: показать предметы")
    println("6 - Комната: добавить предмет")
    println("7 - Комната: подвинуть предмет")
    println("0 - Выход")
}

suspend fun runMenu(registry: PlayerRegistry, creator: CharacterCreator, world: World) {
    println("\n Теперь ваше меню: ")
    while (true) {
        printMenu()
        val choice = ask("Ваш выбор")
        if (choice == null) {
            println("\nВвод недоступен или закончился — завершаю работу.")
            return
        }
        when (choice) {
            "1" -> {
                val nickname = ask("Введите ник")
                if (nickname != null) handleResult(registry.register(nickname), registry, creator)
            }
            "2" -> showPlayers(registry)
            "3" -> showStats(registry)
            "4" -> topUp(registry)
            "5" -> showWorld(world)
            "6" -> addItem(world)
            "7" -> moveItem(world)
            "0" -> {
                println("До свидания!")
                return
            }
            else -> println("Неизвестная команда: '$choice'")
        }
    }
}

//

suspend fun runDemo(registry: PlayerRegistry, creator: CharacterCreator, world: World) {

    println("Параллельно регистрируем игроков")
    val nicknames = listOf("manstr123", "Fasd_bear", "lionel#1", "ku ka re ku")
    println("Пробуем: ${nicknames.joinToString(", ")}")
    val results = nicknames.map { nick -> nick to registry.register(nick) }
    coroutineScope {
        results
            .map { (nick, result) -> async { handleResult(result, registry, creator, "[$nick] ") } }
            .awaitAll()
    }

    println("\n 2. Пополнение баланса \n")
    registry.topUp(1, 45000)?.let { println("${it.nickname}: баланс ${it.balance} (${it.rank})") }
    registry.topUp(2, 1000)?.let { println("${it.nickname}: баланс ${it.balance} (${it.rank})") }

    println("\n 3. Игроки \n")
    showPlayers(registry)

    println("\n 4. Статистика \n")
    showStats(registry)

    println("\n 5. Предметы окружения \n")
    world.add(Wardrobe(15, 5))
    world.add(Pan(10, 1))
    showWorld(world)

    println("Двигаем все предметы на (+4, +1):")
    world.all().values.forEach { tryMove(it, 4, 1) }
    println("Пробуем сдвинуть сковородку на (+100, 0):")
    world.all().values.filterIsInstance<Pan>().forEach { tryMove(it, 100, 0) }

    val movable = world.all().values.filter { it is IMovable }.map { it.name }
    println("Подвижные предметы: ${movable.joinToString(", ")}")

    println("")
}



fun main() {
    val registry = PlayerRegistry(startBalance = 100)
    val creator = CharacterCreator(stepDelayMs = 400)
    val world = World()

    runBlocking {
        runDemo(registry, creator, world)
        runMenu(registry, creator, world)
    }
}