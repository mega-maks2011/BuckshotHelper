import javax.swing.*
import java.awt.*
import javax.swing.border.EmptyBorder
import java.util.prefs.Preferences
import kotlin.random.Random

class BuckshotHelperGUI : JFrame("Buckshot Helper") {

    data class BulletVisualState(var knownType: Boolean?, var isFired: Boolean)

    // Data class для сохранения состояния для Undo
    private data class AppState(
        val allPossibleChambers: MutableList<MutableList<Boolean>>,
        val currentPossibleChambers: MutableList<MutableList<Boolean>>,
        val visualChamber: MutableList<BulletVisualState>,
        val messageText: String
    )

    private val shellLineupInput = JTextField("3/2", 10)
    private val submitRoundButton = JButton()
    private val shellKnowledgeInput = JTextField("", 10)
    private val submitShellKnowledgeButton = JButton()

    private val currentShellLineupLabel = JLabel("[]")
    private val liveShellChanceLabel = JLabel()
    private val blankShellChanceLabel = JLabel()
    private val liveShellsRemainingLabel = JLabel()
    private val blankShellsRemainingLabel = JLabel()

    private val liveFiredButton = JButton()
    private val blankCycledButton = JButton()

    private val messageLabel = JLabel()

    // Пункт меню для Undo
    private val undoMenuItem = JMenuItem()

    private var allPossibleChambers: MutableList<MutableList<Boolean>> = mutableListOf()
    private var currentPossibleChambers: MutableList<MutableList<Boolean>> = mutableListOf()
    private var visualChamber: MutableList<BulletVisualState> = mutableListOf()

    // Стек для Undo
    private val undoStack: MutableList<AppState> = mutableListOf()

    private val BACKGROUND = Color(30, 33, 36)
    private val TEXT = Color(220, 221, 222)
    private val BORDER = Color(70, 73, 79)
    private val ACCENT_PRIMARY = Color(114, 137, 218)
    private val ACCENT_GREEN = Color(87, 242, 135)
    private val ACCENT_RED = Color(237, 66, 69)

    private var currentLanguage: String = "en"

    private val prefs: Preferences = Preferences.userNodeForPackage(BuckshotHelperGUI::class.java)

    private val translations = mapOf(
        "en" to mapOf(
            "windowTitle" to "Buckshot Helper",
            "submitRoundButton" to "Start Round",
            "submitShellKnowledgeButton" to "Phone",
            "liveShellChance" to "Live shell chance:",
            "blankShellChance" to "Blank shell chance:",
            "liveShellsRemaining" to "Live shells remaining:",
            "blankShellsRemaining" to "Blank shells remaining:",
            "liveFiredButton" to "Live (L)",
            "blankCycledButton" to "Blank (B)",
            "messageInitial" to "Enter shell lineup (Live/Blank).",
            "errorInvalidFormat" to "Error: Invalid format. Use 'Live/Blank' (e.g., 3/2).",
            "errorTotalBulletsZero" to "Error: Total bullets cannot be zero.",
            "errorBulletCountsNegative" to "Error: Bullet counts cannot be negative.",
            "errorMaxBullets" to "Error: Maximum 10 bullets allowed (Live + Blank).",
            "roundReady" to "Round ready! Initial: %1\$dL, %2\$dB.",
            "confirmedShot" to "Confirmed: Shot was %1\$s.",
            "errorNoPossibleCombinations" to "No possible bullet combinations. Start a new round.",
            "errorInvalidBulletNumber" to "Error: Invalid bullet number. Enter a number greater than 0.",
            "errorInvalidBulletType" to "Error: Invalid bullet type. Enter 'l' for Live or 'b' for Blank.",
            "errorBulletDoesNotExist" to "Error: Bullet No. %1\$d does not exist among unfired bullets.",
            "errorBulletAlreadyFired" to "Error: Bullet No. %1\$d is already fired.",
            "errorContradiction" to "Error: Shell knowledge contradicts previous data. No possible combinations remain. Start a new round.",
            "shellKnowledgeConfirmed" to "Shell knowledge: Bullet No. %1\$d is %2\$s.",
            "bulletTypeLive" to "Live (L)",
            "bulletTypeBlank" to "Blank (B)",
            "roundOver" to "Round Over! All bullets fired. Start a new round.",
            "settingsMenu" to "Settings",
            "englishMenuItem" to "English",
            "russianMenuItem" to "Russian",
            "aboutMenuItem" to "About",
            "aboutMessage" to "<html>Buckshot Helper v1.2.1<br><br>This application helps players track shell probabilities in Buckshot Roulette.<br><br>Developed by Gemini AI and M998__.</html>",
            "undoButton" to "Undo"
        ),
        "ru" to mapOf(
            "windowTitle" to "Buckshot Helper",
            "submitRoundButton" to "Начать раунд",
            "submitShellKnowledgeButton" to "Телефон",
            "liveShellChance" to "Шанс боевого патрона:",
            "blankShellChance" to "Шанс холостого патрона:",
            "liveShellsRemaining" to "Боевых патронов осталось:",
            "blankShellsRemaining" to "Холостых патронов осталось:",
            "liveFiredButton" to "Боевой (Б)",
            "blankCycledButton" to "Холостой (Х)",
            "messageInitial" to "Введите раскладку патронов (Боевой/Холостой).",
            "errorInvalidFormat" to "Ошибка: Неверный формат. Используйте 'Боевых/Холостых' (напр., 3/2).",
            "errorTotalBulletsZero" to "Ошибка: Общее количество патронов не может быть нулем.",
            "errorBulletCountsNegative" to "Ошибка: Количество патронов не может быть отрицательным.",
            "errorMaxBullets" to "Ошибка: Максимум 10 патронов (Боевых + Холостых).",
            "roundReady" to "Раунд готов! Начало: %1\$dБ, %2\$dХ.",
            "confirmedShot" to "Подтверждено: Выстрел был %1\$s.",
            "errorNoPossibleCombinations" to "Нет возможных комбинаций патронов. Начните новый раунд.",
            "errorInvalidBulletNumber" to "Ошибка: Неверный номер патрона. Введите число больше 0.",
            "errorInvalidBulletType" to "Ошибка: Неверный тип патрона. Введите 'l' для боевого или 'b' для холостого.",
            "errorBulletDoesNotExist" to "Ошибка: Патрон №%1\$d не существует среди не выстреленных патронов.",
            "errorBulletAlreadyFired" to "Ошибка: Патрон №%1\$d уже выстрелен.",
            "errorContradiction" to "Ошибка: Знание о патроне противоречит предыдущим данным. Нет возможных комбинаций. Начните новый раунд.",
            "shellKnowledgeConfirmed" to "Знание о патроне: Патрон №%1\$d - %2\$s.",
            "bulletTypeLive" to "Боевой (Б)",
            "bulletTypeBlank" to "Холостой (Х)",
            "roundOver" to "Раунд завершен! Все патроны выстрелены. Начните новый раунд.",
            "settingsMenu" to "Настройки",
            "englishMenuItem" to "English",
            "russianMenuItem" to "Русский",
            "aboutMenuItem" to "О программе",
            "aboutMessage" to "<html>Buckshot Helper v1.2.1<br><br>Это приложение помогает игрокам отслеживать вероятности патронов в Buckshot Roulette.<br><br>Разработано Gemini AI и M998__.</html>",
            "undoButton" to "Отменить"
        )
    )

    init {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(350, 450)
        isResizable = false

        try {
            val iconPath = "assets/icon.png"
            val iconImage = Toolkit.getDefaultToolkit().getImage(iconPath)
            setIconImage(iconImage)
        }
        catch (e: Exception) {
            System.err.println("Error loading icon: ${e.message}")
        }

        val savedX = prefs.getInt("window_x", -1)
        val savedY = prefs.getInt("window_y", -1)
        val savedWidth = prefs.getInt("window_width", preferredSize.width)
        val savedHeight = prefs.getInt("window_height", preferredSize.height)
        val savedLanguage = prefs.get("language_code", "en")

        if (savedX != -1 && savedY != -1) {
            bounds = Rectangle(savedX, savedY, savedWidth, savedHeight)
        }
        else {
            setLocationRelativeTo(null)
        }
        currentLanguage = savedLanguage

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                prefs.putInt("window_x", x)
                prefs.putInt("window_y", y)
                prefs.putInt("window_width", width)
                prefs.putInt("window_height", height)
                prefs.put("language_code", currentLanguage)
                prefs.flush()
                super.windowClosing(e)
            }
        })

        val menuBar = JMenuBar()
        menuBar.background = BACKGROUND

        // Добавляем кнопку Undo в JMenuBar
        menuBar.add(undoMenuItem)

        val settingsMenu = JMenu()
        val englishMenuItem = JMenuItem()
        val russianMenuItem = JMenuItem()
        val aboutMenuItem = JMenuItem()

        englishMenuItem.addActionListener { setLanguage("en") }
        russianMenuItem.addActionListener { setLanguage("ru") }
        aboutMenuItem.addActionListener {
            val currentTranslation = translations[currentLanguage] ?: translations["ru"]!!
            val icon = try {
                val originalImage = Toolkit.getDefaultToolkit().getImage("assets/icon.png")
                val scaledImage = originalImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)
                ImageIcon(scaledImage)
            }
            catch (e: Exception) {
                System.err.println("Error loading or scaling icon for About dialog: ${e.message}")
                null
            }
            JOptionPane.showMessageDialog(this, currentTranslation["aboutMessage"], currentTranslation["aboutMenuItem"], JOptionPane.INFORMATION_MESSAGE, icon)
        }

        settingsMenu.add(englishMenuItem)
        settingsMenu.add(russianMenuItem)
        settingsMenu.addSeparator()
        settingsMenu.add(aboutMenuItem)
        menuBar.add(settingsMenu)
        jMenuBar = menuBar

        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.background = BACKGROUND
        mainPanel.border = EmptyBorder(10, 10, 10, 10)

        val lineupPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        lineupPanel.background = BACKGROUND
        lineupPanel.alignmentX = CENTER_ALIGNMENT
        shellLineupInput.columns = 5
        lineupPanel.add(shellLineupInput)
        lineupPanel.add(submitRoundButton)
        mainPanel.add(lineupPanel)
        mainPanel.add(Box.createVerticalStrut(15))

        val knowledgePanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        knowledgePanel.background = BACKGROUND
        knowledgePanel.alignmentX = CENTER_ALIGNMENT
        shellKnowledgeInput.columns = 8
        knowledgePanel.add(shellKnowledgeInput)
        knowledgePanel.add(submitShellKnowledgeButton)
        mainPanel.add(knowledgePanel)
        mainPanel.add(Box.createVerticalStrut(15))

        val infoPanel = JPanel()
        infoPanel.background = BACKGROUND
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.alignmentX = CENTER_ALIGNMENT
        infoPanel.add(currentShellLineupLabel)
        infoPanel.add(liveShellChanceLabel)
        infoPanel.add(blankShellChanceLabel)
        infoPanel.add(liveShellsRemainingLabel)
        infoPanel.add(blankShellsRemainingLabel)
        mainPanel.add(infoPanel)
        mainPanel.add(Box.createVerticalStrut(15))

        val actionButtonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))
        actionButtonsPanel.background = BACKGROUND
        actionButtonsPanel.add(liveFiredButton)
        actionButtonsPanel.add(blankCycledButton)
        mainPanel.add(actionButtonsPanel)
        mainPanel.add(Box.createVerticalStrut(10))

        // Удалена панель для кнопки Undo из mainPanel
        // Кнопка Undo теперь находится в JMenuBar

        messageLabel.alignmentX = CENTER_ALIGNMENT
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER)
        mainPanel.add(messageLabel)
        add(mainPanel, BorderLayout.CENTER)

        applyColorsToComponents()
        setLanguage(currentLanguage)

        submitRoundButton.addActionListener { startNewRound() }
        submitShellKnowledgeButton.addActionListener { handleShellKnowledge() }
        liveFiredButton.addActionListener { handleShotConfirmation(true) }
        blankCycledButton.addActionListener { handleShotConfirmation(false) }
        undoMenuItem.addActionListener { undoLastAction() } // Слушатель для пункта меню Undo

        updateDisplay()
        updateButtonStates()

        pack()
    }

    fun createAndShowGUI() {
        isVisible = true
        messageLabel.text = translations[currentLanguage]?.get("messageInitial") ?: "Enter shell lineup (e.g., 3/2)."
    }

    private fun setLanguage(langCode: String) {
        currentLanguage = langCode
        val currentTranslation = translations[currentLanguage] ?: translations["en"]!!

        title = currentTranslation["windowTitle"] ?: "Buckshot Helper"
        // ИСПРАВЛЕНИЕ: jMenuBar.components[0] теперь undoMenuItem, settingsMenu находится по индексу 1
        (jMenuBar.components[1] as JMenu).text = currentTranslation["settingsMenu"]
        // Приведение типов для JMenu.menuComponents
        ((jMenuBar.components[1] as JMenu).menuComponents[0] as JMenuItem).text = currentTranslation["englishMenuItem"]
        ((jMenuBar.components[1] as JMenu).menuComponents[1] as JMenuItem).text = currentTranslation["russianMenuItem"]
        ((jMenuBar.components[1] as JMenu).menuComponents[3] as JMenuItem).text = currentTranslation["aboutMenuItem"]

        submitRoundButton.text = currentTranslation["submitRoundButton"]
        submitShellKnowledgeButton.text = currentTranslation["submitShellKnowledgeButton"]
        liveFiredButton.text = currentTranslation["liveFiredButton"]
        blankCycledButton.text = currentTranslation["blankCycledButton"]
        undoMenuItem.text = currentTranslation["undoButton"] // Установка текста для пункта меню Undo

        liveShellChanceLabel.text = currentTranslation["liveShellChance"] + " ?"
        blankShellChanceLabel.text = currentTranslation["blankShellChance"] + " ?"
        liveShellsRemainingLabel.text = currentTranslation["liveShellsRemaining"] + " ?"
        blankShellsRemainingLabel.text = currentTranslation["blankShellsRemaining"] + " ?"

        messageLabel.text = currentTranslation["messageInitial"]
        updateDisplay()
        updateButtonStates()

        prefs.put("language_code", currentLanguage)
    }

    private fun applyColorsToComponents() {
        contentPane.background = BACKGROUND
        rootPane.background = BACKGROUND
        background = BACKGROUND
        jMenuBar.background = BACKGROUND

        UIManager.put("Panel.background", BACKGROUND)
        UIManager.put("Label.foreground", TEXT)
        UIManager.put("TextField.background", BACKGROUND)
        UIManager.put("TextField.foreground", TEXT)
        UIManager.put("TextField.caretForeground", TEXT)
        UIManager.put("TextField.border", BorderFactory.createLineBorder(BORDER, 1, true))
        UIManager.put("Button.background", ACCENT_PRIMARY)
        UIManager.put("Button.foreground", Color.WHITE)
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(8, 15, 8, 15))
        // Цвета для JMenuItem
        UIManager.put("MenuItem.background", BACKGROUND)
        UIManager.put("MenuItem.foreground", TEXT)
        UIManager.put("Menu.foreground", TEXT) // Для текста "Settings"

        shellLineupInput.background = BACKGROUND
        shellLineupInput.foreground = TEXT
        shellLineupInput.caretColor = TEXT
        shellLineupInput.border = BorderFactory.createLineBorder(BORDER, 1, true)
        shellKnowledgeInput.background = BACKGROUND
        shellKnowledgeInput.foreground = TEXT
        shellKnowledgeInput.caretColor = TEXT
        shellKnowledgeInput.border = BorderFactory.createLineBorder(BORDER, 1, true)
        submitRoundButton.background = ACCENT_PRIMARY
        submitShellKnowledgeButton.background = ACCENT_PRIMARY
        liveFiredButton.background = ACCENT_GREEN
        blankCycledButton.background = ACCENT_RED
        // undoMenuItem не нуждается в явной установке цвета, так как он наследует от UIManager.put("MenuItem.background", ...)

        messageLabel.foreground = TEXT
        currentShellLineupLabel.foreground = TEXT
        liveShellChanceLabel.foreground = TEXT
        blankShellChanceLabel.foreground = TEXT
        liveShellsRemainingLabel.foreground = TEXT
        blankShellsRemainingLabel.foreground = TEXT

        SwingUtilities.updateComponentTreeUI(this)

        for (panel in listOf(contentPane, rootPane.contentPane, shellLineupInput.parent, submitRoundButton.parent, shellKnowledgeInput.parent, submitShellKnowledgeButton.parent, currentShellLineupLabel.parent, liveShellChanceLabel.parent, blankShellChanceLabel.parent, liveShellsRemainingLabel.parent, blankShellsRemainingLabel.parent, liveFiredButton.parent, blankCycledButton.parent, messageLabel.parent)) {
            panel.background = BACKGROUND
        }

        revalidate()
        repaint()
    }

    // Сохраняет текущее состояние приложения в стек undoStack
    private fun saveState() {
        // Создаем глубокие копии изменяемых списков
        val chambersCopy = allPossibleChambers.map { it.toMutableList() }.toMutableList()
        val currentChambersCopy = currentPossibleChambers.map { it.toMutableList() }.toMutableList()
        val visualChamberCopy = visualChamber.map { it.copy() }.toMutableList() // Используем data class copy

        undoStack.add(AppState(chambersCopy, currentChambersCopy, visualChamberCopy, messageLabel.text))
        // Ограничиваем размер стека, чтобы не потреблять слишком много памяти
        if (undoStack.size > 20) { // Например, сохраняем последние 20 состояний
            undoStack.removeAt(0)
        }
    }

    // Отменяет последнее действие
    private fun undoLastAction() {
        if (undoStack.isNotEmpty()) {
            val prevState = undoStack.removeAt(undoStack.lastIndex) // Получаем последнее состояние

            // Восстанавливаем состояние
            allPossibleChambers = prevState.allPossibleChambers.map { it.toMutableList() }.toMutableList()
            currentPossibleChambers = prevState.currentPossibleChambers.map { it.toMutableList() }.toMutableList()
            visualChamber = prevState.visualChamber.map { it.copy() }.toMutableList()
            messageLabel.text = prevState.messageText

            updateDisplay()
            updateButtonStates()
        }
    }

    private fun startNewRound() {
        saveState() // Сохраняем состояние перед изменением

        val input = shellLineupInput.text.trim()
        val parts = input.split("/").mapNotNull { it.toIntOrNull() }
        val currentTranslation = translations[currentLanguage] ?: translations["en"]!!

        if (parts.size != 2) {
            messageLabel.text = currentTranslation["errorInvalidFormat"]!!
            return
        }

        val live = parts[0]
        val blank = parts[1]
        val totalBullets = live + blank

        if (totalBullets == 0) {
            messageLabel.text = currentTranslation["errorTotalBulletsZero"]!!
            return
        }
        if (live < 0 || blank < 0) {
            messageLabel.text = currentTranslation["errorBulletCountsNegative"]!!
            return
        }
        if (totalBullets > 10) {
            messageLabel.text = currentTranslation["errorMaxBullets"]!!
            return
        }

        allPossibleChambers = generatePermutations(live, blank)
        currentPossibleChambers = allPossibleChambers.map { it.toMutableList() }.toMutableList()

        visualChamber = MutableList(totalBullets) { BulletVisualState(null, false) }

        messageLabel.text = String.format(currentTranslation["roundReady"]!!, live, blank)
        updateDisplay()
        updateButtonStates()
    }

    private fun handleShotConfirmation(wasLive: Boolean) {
        saveState() // Сохраняем состояние перед изменением

        val currentTranslation = translations[currentLanguage] ?: translations["en"]!!
        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = currentTranslation["errorNoPossibleCombinations"]!!
            return
        }

        val firstUnfiredIndex = visualChamber.indexOfFirst { !it.isFired }
        if (firstUnfiredIndex != -1) {
            visualChamber[firstUnfiredIndex].isFired = true
            visualChamber[firstUnfiredIndex].knownType = wasLive
        }

        messageLabel.text = String.format(currentTranslation["confirmedShot"]!!, if (wasLive) currentTranslation["bulletTypeLive"] else currentTranslation["bulletTypeBlank"])
        currentPossibleChambers = filterChambersByShot(currentPossibleChambers, wasLive)
        updateDisplay()
        updateButtonStates()

        if (visualChamber.all { it.isFired }) {
            messageLabel.text = currentTranslation["roundOver"]!!
            allPossibleChambers.clear()
            currentPossibleChambers.clear()
            visualChamber.clear()
            updateButtonStates()
        }
    }

    private fun handleShellKnowledge() {
        saveState() // Сохраняем состояние перед изменением

        val currentTranslation = translations[currentLanguage] ?: translations["en"]!!
        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = currentTranslation["errorNoPossibleCombinations"]!!
            return
        }

        val input = shellKnowledgeInput.text.trim().lowercase()
        if (input.length < 2) {
            messageLabel.text = currentTranslation["errorInvalidFormat"]!!
            return
        }

        val indexChar = input.substring(0, input.length - 1)
        val typeChar = input.last()

        val relativeIndex = indexChar.toIntOrNull()
        val typeIsLive = (typeChar == 'l')

        if (relativeIndex == null || relativeIndex <= 0) {
            messageLabel.text = currentTranslation["errorInvalidBulletNumber"]!!
            return
        }
        if (typeChar != 'l' && typeChar != 'b') {
            messageLabel.text = currentTranslation["errorInvalidBulletType"]!!
            return
        }

        var unfiredCount = 0
        var actualIndex = -1
        for (i in visualChamber.indices) {
            if (!visualChamber[i].isFired) {
                unfiredCount++
                if (unfiredCount == relativeIndex) {
                    actualIndex = i
                    break
                }
            }
        }

        if (actualIndex == -1) {
            messageLabel.text = String.format(currentTranslation["errorBulletDoesNotExist"]!!, relativeIndex)
            return
        }

        if (visualChamber[actualIndex].isFired) {
            messageLabel.text = String.format(currentTranslation["errorBulletAlreadyFired"]!!, relativeIndex)
            return
        }

        currentPossibleChambers = filterChambersByPhone(currentPossibleChambers, actualIndex + 1, typeIsLive)

        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = currentTranslation["errorContradiction"]!!
            allPossibleChambers.clear()
            currentPossibleChambers.clear()
            visualChamber.clear()
            updateDisplay()
            updateButtonStates()
            return
        }

        visualChamber[actualIndex].knownType = typeIsLive

        messageLabel.text = String.format(currentTranslation["shellKnowledgeConfirmed"]!!, relativeIndex, if (typeIsLive) currentTranslation["bulletTypeLive"] else currentTranslation["bulletTypeBlank"])
        shellKnowledgeInput.text = ""
        updateDisplay()
        updateButtonStates()
    }

    private fun updateDisplay() {
        val (liveProb, blankProb) = calculateProbabilities(currentPossibleChambers)
        val currentTranslation = translations[currentLanguage] ?: translations["en"]!!

        liveShellChanceLabel.text = "${currentTranslation["liveShellChance"]} ${"%.2f".format(liveProb * 100)}%"
        blankShellChanceLabel.text = "${currentTranslation["blankShellChance"]} ${"%.2f".format(blankProb * 100)}%"

        val liveRemaining = currentPossibleChambers.firstOrNull()?.count { it == true } ?: 0
        val blankRemaining = currentPossibleChambers.firstOrNull()?.count { it == false } ?: 0

        liveShellsRemainingLabel.text = "${currentTranslation["liveShellsRemaining"]} $liveRemaining"
        blankShellsRemainingLabel.text = "${currentTranslation["blankShellsRemaining"]} $blankRemaining"

        val visualString = StringBuilder("[")
        if (visualChamber.isEmpty()) {
            visualString.append("Empty")
        }
        else {
            for (i in visualChamber.indices) {
                val bulletState = visualChamber[i]
                val char = when {
                    bulletState.knownType == true -> "L"
                    bulletState.knownType == false -> "B"
                    else -> "R"
                }

                val grayColor = "#A0A0A0"
                if (bulletState.isFired) {
                    visualString.append("<font color='$grayColor'>$char</font>")
                }
                else {
                    visualString.append(char)
                }
                if (i < visualChamber.size - 1) {
                    visualString.append(", ")
                }
            }
        }
        visualString.append("]")
        currentShellLineupLabel.text = "<html>$visualString</html>"
    }

    private fun updateButtonStates() {
        val hasPossibleChambers = currentPossibleChambers.isNotEmpty()
        val allBulletsFired = visualChamber.all { it.isFired }

        submitShellKnowledgeButton.isEnabled = hasPossibleChambers && !allBulletsFired
        shellKnowledgeInput.isEnabled = hasPossibleChambers && !allBulletsFired

        liveFiredButton.isEnabled = hasPossibleChambers && !allBulletsFired
        blankCycledButton.isEnabled = hasPossibleChambers && !allBulletsFired

        submitRoundButton.isEnabled = true
        shellLineupInput.isEnabled = true

        undoMenuItem.isEnabled = undoStack.isNotEmpty() // Кнопка Undo активна, если есть состояния для отмены
    }

    private fun generatePermutations(live: Int, blank: Int): MutableList<MutableList<Boolean>> {
        val result = mutableSetOf<List<Boolean>>()
        fun backtrack(current: MutableList<Boolean>, l: Int, b: Int) {
            if (l == 0 && b == 0) {
                result.add(current.toList())
                return
            }
            if (l > 0) {
                current.add(true)
                backtrack(current, l - 1, b)
                current.removeAt(current.lastIndex)
            }
            if (b > 0) {
                current.add(false)
                backtrack(current, l, b - 1)
                current.removeAt(current.lastIndex)
            }
        }
        backtrack(mutableListOf(), live, blank)
        return result.map { it.toMutableList() }.toMutableList()
    }

    private fun calculateProbabilities(possibleChambers: MutableList<MutableList<Boolean>>): Pair<Double, Double> {
        if (possibleChambers.isEmpty()) {
            return Pair(0.0, 0.0)
        }
        var liveCount = 0
        for (chamber in possibleChambers) {
            if (chamber.isNotEmpty() && chamber[0]) {
                liveCount++
            }
        }
        val totalChambers = possibleChambers.size
        val liveProb = liveCount.toDouble() / totalChambers
        val blankProb = 1.0 - liveProb
        return Pair(liveProb, blankProb)
    }

    private fun filterChambersByShot(chambers: MutableList<MutableList<Boolean>>, wasLive: Boolean): MutableList<MutableList<Boolean>> {
        val filtered = mutableListOf<MutableList<Boolean>>()
        for (chamber in chambers) {
            if (chamber.isNotEmpty() && chamber[0] == wasLive) {
                val newChamber = chamber.toMutableList()
                newChamber.removeAt(0)
                filtered.add(newChamber)
            }
        }
        return filtered
    }

    private fun filterChambersByPhone(chambers: MutableList<MutableList<Boolean>>, index: Int, typeIsLive: Boolean): MutableList<MutableList<Boolean>> {
        val filtered = mutableListOf<MutableList<Boolean>>()
        val actualIndex = index - 1
        for (chamber in chambers) {
            if (chamber.size > actualIndex && chamber[actualIndex] == typeIsLive) {
                filtered.add(chamber)
            }
        }
        return filtered
    }
}

fun main() {
    try {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
    }
    catch (e: Exception) {
        e.printStackTrace()
    }

    SwingUtilities.invokeLater {
        BuckshotHelperGUI().createAndShowGUI()
    }
}
