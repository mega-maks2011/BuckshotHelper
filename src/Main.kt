import javax.swing.*
import java.awt.*
import javax.swing.border.EmptyBorder

class BuckshotHelperGUI : JFrame("Buckshot Helper") {
    data class BulletVisualState(var knownType: Boolean?, var isFired: Boolean)
    private val shellLineupInput = JTextField("3/2", 10)
    private val submitRoundButton = JButton("Submit Round Lineup")
    private val shellKnowledgeInput = JTextField("", 10)
    private val submitShellKnowledgeButton = JButton("Submit Shell Knowledge")
    private val currentShellLineupLabel = JLabel("[]")
    private val liveShellChanceLabel = JLabel("Live shell chance: ?")
    private val blankShellChanceLabel = JLabel("Blank shell chance: ?")
    private val liveShellsRemainingLabel = JLabel("Live shells remaining: ?")
    private val blankShellsRemainingLabel = JLabel("Blank shells remaining: ?")
    private val liveFiredButton = JButton("Live Fired")
    private val blankCycledButton = JButton("Blank Cycled")
    private val messageLabel = JLabel("Enter shell lineup (e.g., 3/2).")
    private var allPossibleChambers: MutableList<MutableList<Boolean>> = mutableListOf()
    private var currentPossibleChambers: MutableList<MutableList<Boolean>> = mutableListOf()
    private var visualChamber: MutableList<BulletVisualState> = mutableListOf()
    private val BACKGROUND = Color(30, 33, 36)
    private val TEXT = Color(220, 221, 222)
    private val BORDER = Color(70, 73, 79)
    private val ACCENT_PRIMARY = Color(114, 137, 218)
    private val ACCENT_GREEN = Color(87, 242, 135)
    private val ACCENT_RED = Color(237, 66, 69)

    init {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(300, 450)
        isResizable = false

        try {
            val iconPath = "assets/icon.png"
            val iconImage = Toolkit.getDefaultToolkit().getImage(iconPath)
            setIconImage(iconImage)
        } catch (e: Exception) {
            System.err.println("Error loading icon: ${e.message}")
        }

        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.background = BACKGROUND // Устанавливаем фон
        mainPanel.border = EmptyBorder(10, 10, 10, 10)

        val lineupPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)) // FlowLayout для горизонтального расположения
        lineupPanel.background = BACKGROUND
        lineupPanel.alignmentX = CENTER_ALIGNMENT
        shellLineupInput.columns = 5
        lineupPanel.add(shellLineupInput)
        lineupPanel.add(submitRoundButton)
        mainPanel.add(lineupPanel)
        mainPanel.add(Box.createVerticalStrut(15))

        val knowledgePanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)) // FlowLayout для горизонтального расположения
        knowledgePanel.background = BACKGROUND
        knowledgePanel.alignmentX = CENTER_ALIGNMENT
        shellKnowledgeInput.columns = 5
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

        messageLabel.alignmentX = CENTER_ALIGNMENT
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER)
        mainPanel.add(messageLabel)

        add(mainPanel, BorderLayout.CENTER)

        applyColorsToComponents()

        submitRoundButton.addActionListener { startNewRound() }
        submitShellKnowledgeButton.addActionListener { handleShellKnowledge() }
        liveFiredButton.addActionListener { handleShotConfirmation(true) }
        blankCycledButton.addActionListener { handleShotConfirmation(false) }

        updateDisplay()
        updateButtonStates()
        pack()
        setLocationRelativeTo(null)
    }

    fun createAndShowGUI() {
        isVisible = true
        messageLabel.text = "Enter shell lineup (e.g., 3/2)."
    }

    private fun applyColorsToComponents() {

        contentPane.background = BACKGROUND
        rootPane.background = BACKGROUND
        background = BACKGROUND

        UIManager.put("Panel.background", BACKGROUND)
        UIManager.put("Label.foreground", TEXT)
        UIManager.put("TextField.background", BACKGROUND)
        UIManager.put("TextField.foreground", TEXT)
        UIManager.put("TextField.caretForeground", TEXT)
        UIManager.put("TextField.border", BorderFactory.createLineBorder(BORDER, 1, true))
        UIManager.put("Button.background", ACCENT_PRIMARY)
        UIManager.put("Button.foreground", Color.WHITE) // Кнопки всегда с белым текстом
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(8, 15, 8, 15)) // Отступы кнопок

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

        messageLabel.foreground = TEXT
        currentShellLineupLabel.foreground = TEXT
        liveShellChanceLabel.foreground = TEXT
        blankShellChanceLabel.foreground = TEXT
        liveShellsRemainingLabel.foreground = TEXT
        blankShellsRemainingLabel.foreground = TEXT

        SwingUtilities.updateComponentTreeUI(this)

        for (panel in listOf(contentPane, rootPane.contentPane, shellLineupInput.parent, submitRoundButton.parent, shellKnowledgeInput.parent, submitShellKnowledgeButton.parent, currentShellLineupLabel.parent, liveShellChanceLabel.parent, blankShellChanceLabel.parent, liveShellsRemainingLabel.parent, blankShellsRemainingLabel.parent, liveFiredButton.parent, blankCycledButton.parent, messageLabel.parent)) { // ИЗМЕНЕНО: messageLabel ВОЗВРАЩЕНА в список
            panel.background = BACKGROUND
        }
        revalidate()
        repaint()
        updateDisplay()
    }

    private fun startNewRound() {
        val input = shellLineupInput.text.trim()
        val parts = input.split("/").mapNotNull { it.toIntOrNull() }

        if (parts.size != 2) {
            messageLabel.text = "Error: Invalid format. Use 'Live/Blank' (e.g., 3/2)."
            return
        }

        val live = parts[0]
        val blank = parts[1]
        val totalBullets = live + blank

        if (totalBullets == 0) {
            messageLabel.text = "Error: Total bullets cannot be zero."
            return
        }
        if (live < 0 || blank < 0) {
            messageLabel.text = "Error: Bullet counts cannot be negative."
            return
        }
        // Добавлено ограничение на 10 патронов
        if (totalBullets > 10) {
            messageLabel.text = "Error: Maximum 10 bullets allowed (Live + Blank)."
            return
        }

        allPossibleChambers = generatePermutations(live, blank)
        currentPossibleChambers = allPossibleChambers.map { it.toMutableList() }.toMutableList()
        visualChamber = MutableList(totalBullets) { BulletVisualState(null, false) }

        messageLabel.text = "Round ready! Initial: ${live}L, ${blank}B."
        updateDisplay()
        updateButtonStates()
    }

    private fun handleShotConfirmation(wasLive: Boolean) {
        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = "No possible bullet combinations. Start a new round."
            return
        }

        val firstUnfiredIndex = visualChamber.indexOfFirst { !it.isFired }
        if (firstUnfiredIndex != -1) {
            visualChamber[firstUnfiredIndex].isFired = true
            visualChamber[firstUnfiredIndex].knownType = wasLive
        }

        messageLabel.text = "Confirmed: Shot was ${if (wasLive) "Live" else "Blank"}."
        currentPossibleChambers = filterChambersByShot(currentPossibleChambers, wasLive)
        updateDisplay()
        updateButtonStates()
    }

    private fun handleShellKnowledge() {
        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = "No possible bullet combinations. Start a new round."
            return
        }

        val input = shellKnowledgeInput.text.trim().lowercase()
        if (input.length < 2) {
            messageLabel.text = "Error: Invalid format. Use 'IndexType' (e.g., 4l or 3b)."
            return
        }

        val indexChar = input.substring(0, input.length - 1)
        val typeChar = input.last()
        val relativeIndex = indexChar.toIntOrNull()
        val typeIsLive = (typeChar == 'l')

        if (relativeIndex == null || relativeIndex <= 0) {
            messageLabel.text = "Error: Invalid bullet number. Enter a number greater than 0."
            return
        }
        if (typeChar != 'l' && typeChar != 'b') {
            messageLabel.text = "Error: Invalid bullet type. Enter 'l' for Live or 'b' for Blank."
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
            messageLabel.text = "Error: Bullet No. $relativeIndex does not exist among unfired bullets."
            return
        }

        if (visualChamber[actualIndex].isFired) {
            messageLabel.text = "Error: Bullet No. $relativeIndex is already fired."
            return
        }

        currentPossibleChambers = filterChambersByPhone(currentPossibleChambers, actualIndex + 1, typeIsLive)

        if (currentPossibleChambers.isEmpty()) {
            messageLabel.text = "Error: Shell knowledge contradicts previous data. No possible combinations remain. Start a new round." // ИЗМЕНЕНО: Более точное сообщение об ошибке
            allPossibleChambers.clear()
            currentPossibleChambers.clear()
            visualChamber.clear()
            updateDisplay()
            updateButtonStates()
            return
        }

        visualChamber[actualIndex].knownType = typeIsLive

        messageLabel.text = "Shell knowledge: Bullet No. $relativeIndex is ${if (typeIsLive) "Live (L)" else "Blank (B)"}." // ВОЗВРАЩЕНО: Метка сообщений
        updateDisplay()
        updateButtonStates()
    }

    private fun updateDisplay() {
        val (liveProb, blankProb) = calculateProbabilities(currentPossibleChambers)
        liveShellChanceLabel.text = "Live shell chance: ${"%.2f".format(liveProb * 100)}%"
        blankShellChanceLabel.text = "Blank shell chance: ${"%.2f".format(blankProb * 100)}%"
        val liveRemaining = currentPossibleChambers.firstOrNull()?.count { it } ?: 0
        val blankRemaining = currentPossibleChambers.firstOrNull()?.count { !it } ?: 0

        liveShellsRemainingLabel.text = "Live shells remaining: $liveRemaining"
        blankShellsRemainingLabel.text = "Blank shells remaining: $blankRemaining"

        val visualString = StringBuilder("[")
        if (visualChamber.isEmpty()) {
            visualString.append("Empty")
        } else {
            for (i in visualChamber.indices) {
                val bulletState = visualChamber[i]
                val char = when (bulletState.knownType) {
                    true -> "L"
                    false -> "B"
                    else -> "R"
                }
                val displayChar = char

                val grayColor = "#A0A0A0"
                if (bulletState.isFired) {
                    visualString.append("<font color='$grayColor'>$displayChar</font>")
                } else {
                    visualString.append(displayChar)
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

        submitShellKnowledgeButton.isEnabled = hasPossibleChambers
        shellKnowledgeInput.isEnabled = hasPossibleChambers
        liveFiredButton.isEnabled = hasPossibleChambers
        blankCycledButton.isEnabled = hasPossibleChambers
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
    } catch (e: Exception) {
        e.printStackTrace()
    }

    SwingUtilities.invokeLater {
        BuckshotHelperGUI().createAndShowGUI()
    }
}