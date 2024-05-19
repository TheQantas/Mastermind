package com.example.mastermind

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

fun<T> permutations(values: List<T>, size: Int, allowDuplicates: Boolean, default: T): MutableSet<List<T>> {
    val parentArray = MutableList(size) { default }
    val perms: MutableSet<List<T>> = mutableSetOf()

    fun buildList(index: Int, elements: MutableList<T>) {
        if (index == size) {
            perms.add(elements.toList())
            return
        }

        for (i in values.indices) {
            if (values[i] in elements && !allowDuplicates) {
                continue
            }

            val childElements = elements.toMutableList()
            childElements[index] = values[i]
            buildList(index + 1, childElements)
        }
    }

    buildList(0, parentArray)
    return perms
}

class Mastermind(numberOfColors: Int, codeLength: Int, allowDuplicates: Boolean) {
    private val secretCode: List<Int>
    private val allCodes: MutableSet<List<Int>> =
        permutations(List(numberOfColors) { it },codeLength,allowDuplicates,-1)

    init {
        val code = mutableListOf<Int>()
        while (code.size < codeLength) {
            val randInt = Random.nextInt(0, numberOfColors)
            if (randInt !in code || allowDuplicates) {
                code.add(randInt)
            }
        }
        secretCode = code.toList()
    }

    companion object {
        fun isMatch(guessOrig: List<Int>,feedback: List<Int>,feedbackFrom: List<Int>): Boolean {
            val guess = guessOrig.toMutableList()
            val value = feedbackFrom.toMutableList()

            if (guess.size != value.size) {
                return false
            }

            var blackCount = 0
            for (i in guess.indices) {
                if (guess[i] == value[i]) {
                    guess[i] = -1
                    value[i] = -1
                    blackCount++
                }
            }

            if (blackCount != feedback.count { it == 2 }) {
                return false
            }

            var whiteCount = 0
            for (i in guess.indices) {
                if (guess[i] == -1) continue
                if (guess[i] in value) {
                    whiteCount++
                    value[value.indexOf(guess[i])] = -1
                    guess[i] = -1
                }
            }

            return whiteCount == feedback.count { it == 1 }
        }

        fun calcFeedback(value: List<Int>, guessOrig: List<Int>): List<Int> {
            val feedback = MutableList(value.size) { 0 }
            val secretCode = value.toMutableList()
            val guessList = guessOrig.toMutableList()

            for (i in secretCode.indices) {
                if (guessList[i] == secretCode[i]) {
                    feedback[i] = 2
                    secretCode[i] = -1
                    guessList[i] = -1
                }
            }

            for (i in secretCode.indices) {
                for (j in guessList.indices) {
                    if (guessList[j] == secretCode[i] && guessList[j] != -1 && secretCode[i] != -1) {
                        feedback[j] = 1
                        secretCode[i] = -1
                        guessList[j] = -1
                    }
                }
            }

            return feedback.sorted().reversed()
        }
    }

    fun giveFeedback(guessOrig: List<Int>): List<Int> {
        val feedback = calcFeedback(secretCode,guessOrig)
        val valuesToRemove: MutableSet<List<Int>> = mutableSetOf()
        for (value in allCodes) {
            if (!isMatch(value,feedback,guessOrig)) {
//                allCodes.remove(value)
                valuesToRemove.add(value)
            }
        }
        for (deleteValue in valuesToRemove) {
            allCodes.remove(deleteValue)
        }
        return feedback
    }

    fun getRemainingCount(): Int {
        return allCodes.size
    }

    fun getCodeAsString(): String {
        return secretCode.toString()
    }
}

class GameBoard : ComponentActivity() {
    companion object {
        private const val outerPadding = 15
        private const val innerPadding = 10
        private val drawables = listOf(
            R.drawable.circle_red,
            R.drawable.circle_green,
            R.drawable.circle_blue,

            R.drawable.circle_yellow,
            R.drawable.circle_purple,
            R.drawable.circle_orange,

            R.drawable.circle_cyan,
            R.drawable.circle_magenta,
            R.drawable.circle_brown,
            R.drawable.circle_lavender
        )
        private val feedbackDrawables = listOf(
            R.drawable.circle_outline,
            R.drawable.circle_half,
            R.drawable.circle_full
        )
    }

    inner class BoardRow(i: Int, codeLength: Int, squareLengthInPx: Int, table: FrameLayout, context: Context) {
        private val row = LinearLayout(context)
        private val buttonList: ArrayList<Button> = ArrayList()
        private val guessList: IntArray = IntArray(codeLength) { -1 }
        private var active = false
        private var activeButtonIndex = -1

        init {
            if (i % 2 == 0) {
                row.setBackgroundResource(R.color.accent)
            } else {
                row.setBackgroundResource(R.color.offset_accent)
            }

            for (j in 0 until codeLength) {
                val button = Button(context)
                val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.circle_empty)
                button.background = backgroundDrawable
                button.layoutParams = LinearLayout.LayoutParams(squareLengthInPx,squareLengthInPx)

                button.setOnTouchListener { _, _ ->
                    if (this.active) {
                        activeButtonIndex = j
                        val colorSelectorView = findViewById<LinearLayout>(R.id.color_selector)
                        colorSelectorView.visibility = View.VISIBLE
                        val marginLayoutParams = colorSelectorView.layoutParams as FrameLayout.LayoutParams
                        marginLayoutParams.topMargin = (-(innerPadding * resources.displayMetrics.scaledDensity) +
                                (outerPadding * resources.displayMetrics.scaledDensity) +
                                (squareLengthInPx + (2 * innerPadding * resources.displayMetrics.scaledDensity)) * (i+1)).toInt()
                        colorSelectorView.layoutParams = marginLayoutParams
                        for (c in 1 until colorSelectorView.childCount) {
                            spaceEvenly(colorSelectorView.getChildAt(c) as LinearLayout)
                        }
                        val triangle = (colorSelectorView.getChildAt(0) as LinearLayout).getChildAt(0)
                        val triangleParams = triangle.layoutParams as LinearLayout.LayoutParams
                        triangleParams.leftMargin = button.marginLeft * (j+1) + squareLengthInPx * j + button.width / 2 - triangle.width / 2
                        triangle.layoutParams = triangleParams
                        colorSelectorView.bringToFront()
                    }
                    true
                }
                row.addView(button)
                buttonList.add(button)
            }

            //add enter button
            val enterButton = Button(context)
            val backgroundDrawable = ContextCompat.getDrawable(context,R.drawable.circle_enter)
            enterButton.background = backgroundDrawable
            enterButton.layoutParams = LinearLayout.LayoutParams(squareLengthInPx,squareLengthInPx)
            enterButton.setOnTouchListener { _, _ ->
                if (this.active) {
                    advanceBoard()
                }
                true
            }
            row.addView(enterButton)

            row.orientation = LinearLayout.HORIZONTAL
            table.addView(row)
            row.setPadding(
                0,
                (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
                0,
                (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
            )
            val params = FrameLayout.LayoutParams(
                resources.displayMetrics.widthPixels - (2 * outerPadding * resources.displayMetrics.scaledDensity).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = (outerPadding * resources.displayMetrics.scaledDensity).toInt()
            params.topMargin = (outerPadding * resources.displayMetrics.scaledDensity).toInt() +
                    (squareLengthInPx + (2 * innerPadding * resources.displayMetrics.scaledDensity).toInt()) * i
            row.layoutParams = params
            spaceEvenly(row)
        }

        fun activate() {
            active = true
            for (i in 0 until row.childCount) {
                if (i == row.childCount - 1) { //enter button
                    (row.getChildAt(i) as Button).setBackgroundResource(R.drawable.circle_enter_active)
                } else {
                    (row.getChildAt(i) as Button).setBackgroundResource(R.drawable.circle_empty_active)
                }
            }
        }

        private fun deactivate() {
            active = false
        }

        fun getGuess(): IntArray { return guessList }

        fun fullGuess(): Boolean {
            for (i in guessList) {
                if (i == -1) return false
            }
            return true
        }

        fun updateColor(color: Int) {
            if (activeButtonIndex == -1) return
            (row.getChildAt(activeButtonIndex) as Button).setBackgroundResource(drawables[color])
            guessList[activeButtonIndex] = color
            println("new: ${guessList.toList()}")
            findViewById<LinearLayout>(R.id.color_selector).visibility = View.GONE
            activeButtonIndex = -1
        }

        fun displayFeedback(feedback: IntArray, originalCount: Int, context: Context) {
            deactivate()
            row.getChildAt(row.childCount-1).visibility = View.GONE //hide enter button
            val feedbackView = FrameLayout(context)
//            feedbackView.setBackgroundResource(R.color.blue)
            feedbackView.layoutParams = FrameLayout.LayoutParams(squareLengthInPx,squareLengthInPx)
            val innerSize = squareLengthInPx * 0.35
            val radialSize = (squareLengthInPx * 0.28)
            for (i in feedback.indices) {
                val angle = 2 * Math.PI * i / feedback.size
                val circleView = TextView(context)
                val innerParams = FrameLayout.LayoutParams(radialSize.toInt(),radialSize.toInt())
                innerParams.topMargin = (squareLengthInPx.toDouble() / 2 - innerSize * cos(angle) - radialSize / 2 + 1.5).toInt()
                innerParams.leftMargin = (squareLengthInPx.toDouble() / 2 + innerSize * sin(angle) - radialSize / 2).toInt()
                circleView.layoutParams = innerParams
                circleView.setBackgroundResource(feedbackDrawables[feedback[i]])
                feedbackView.addView(circleView)
            }

            val textView = TextView(context)
            when (val valuesLeft = game.getRemainingCount()) {
                0 -> {
                    textView.text = "!"
                }
                1 -> {
                    textView.text = "W"
                }
                else -> {
                    val valuesEliminated = (originalCount - valuesLeft).toDouble()
                    textView.text = round(valuesEliminated / originalCount * 100).toInt().toString()
                }
            }
            textView.setTextColor(ContextCompat.getColor(context,R.color.triple_accent))
            feedbackView.addView(textView)
            textView.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = android.view.Gravity.CENTER
            textView.layoutParams = layoutParams

            row.addView(feedbackView)
            spaceEvenly(row)
        }
    }

    private val rowList: ArrayList<BoardRow> = ArrayList()
//    private val secretCode: ArrayList<Int> = ArrayList()
    private var numberOfColors: Int = 0
    private var codeLength: Int = 0
    private var allowDuplicates: Boolean = true
    private var rowCount: Int = 0
    private var activeRowIndex: Int = 0
    private var squareLengthInPx: Int = 0

    private var game = Mastermind(0,0,false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)
        val root = findViewById<FrameLayout>(R.id.game_board)

        val sharedPreferences = getSharedPreferences("com.example.mastermind_preferences",Context.MODE_PRIVATE)

        numberOfColors = max(sharedPreferences.getInt("number_of_colors",6),3)
        codeLength = max(sharedPreferences.getInt("length_of_code",4),2)
        allowDuplicates = sharedPreferences.getBoolean("allow_duplicates",true)
        rowCount = (0.75 * ln(
            numberOfColors.toDouble().pow(codeLength.toDouble())
        ) + 2).roundToInt()

        squareLengthInPx = (50 * resources.displayMetrics.scaledDensity).toInt()
        if (squareLengthInPx * (codeLength + 1) > resources.displayMetrics.widthPixels) {
            squareLengthInPx = resources.displayMetrics.widthPixels / (codeLength + 1)
        }

        //old secret code
//        while (secretCode.size < codeLength) {
//            val randomColor = Random.nextInt(numberOfColors)
//            if (allowDuplicates || !secretCode.contains(randomColor)) {
//                secretCode.add(randomColor)
//            }
//        }
        game = Mastermind(numberOfColors,codeLength,allowDuplicates)

        for (i in 0 until rowCount) { //build rows
            rowList.add(BoardRow(i,codeLength,squareLengthInPx,root,this))
        }

        val textView = TextView(this)
//        textView.text = secretCode.toString()
        textView.text = game.getCodeAsString()
        textView.setTextColor(resources.getColor(R.color.counter))
        root.addView(textView)

        //build color selector
        val colorSelectorView = findViewById<LinearLayout>(R.id.color_selector)
        var rowOneMax = numberOfColors
        var rowTwoMax = -1
        if (numberOfColors > 6) {
            rowOneMax = (numberOfColors + 1) / 2
            rowTwoMax = numberOfColors
        }

        //build first color row
        val colorRowOne = LinearLayout(this)
        colorRowOne.orientation = LinearLayout.HORIZONTAL
        colorRowOne.setBackgroundResource(R.color.counter_accent)
        colorRowOne.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            squareLengthInPx + (2 * innerPadding * resources.displayMetrics.scaledDensity).toInt(),
        )
        colorRowOne.setPadding(
            0,
            (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
            0,
            (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
        )
        for (i in 0 until rowOneMax) { //add buttons for colors
            val button = Button(this)
            button.layoutParams = LinearLayout.LayoutParams(squareLengthInPx,squareLengthInPx)
            val backgroundDrawable = ContextCompat.getDrawable(this,drawables[i])
            button.background = backgroundDrawable
            button.setOnTouchListener { _, _ ->
                rowList[activeRowIndex].updateColor(i)
                true
            }
            colorRowOne.addView(button)
        }
        colorSelectorView.addView(colorRowOne)

        if (rowTwoMax != -1) {
            //build first color row
            val colorRowTwo = LinearLayout(this)
            colorRowTwo.orientation = LinearLayout.HORIZONTAL
            colorRowTwo.setBackgroundResource(R.color.counter_accent)
            colorRowTwo.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                squareLengthInPx + (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
            )
            colorRowTwo.setPadding(
                0,
                0,
                0,
                (innerPadding * resources.displayMetrics.scaledDensity).toInt(),
            )
            for (i in rowOneMax until numberOfColors) { //add buttons for colors
                val button = Button(this)
                button.layoutParams = LinearLayout.LayoutParams(squareLengthInPx,squareLengthInPx)
                val backgroundDrawable = ContextCompat.getDrawable(this,drawables[i])
                button.background = backgroundDrawable
                button.setOnTouchListener { _, _ ->
                    rowList[activeRowIndex].updateColor(i)
                    true
                }
                colorRowTwo.addView(button)
            }
            colorSelectorView.addView(colorRowTwo)
        }

        rowList[0].activate()

        //hide color selector when deselected
        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Check if the touch event is outside the bounds of viewToHide
                val rect = Rect()
                colorSelectorView.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // The touch event is outside viewToHide, so hide it
                    colorSelectorView.visibility = View.GONE
                }
            }
            true // Consume the touch event
        }
    }

    override fun onBackPressed() {
        val inflater = LayoutInflater.from(this)
        val customDialogView = inflater.inflate(R.layout.alert_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(customDialogView)
        alertDialogBuilder.setMessage("Are you sure you want to go back?")
        alertDialogBuilder.setPositiveButton("Go Back") { _, _ ->
            // User clicked "Go Back"
            super.onBackPressed()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            // User clicked "Cancel", do nothing
            dialog.dismiss()
        }
        val alertDialog: AppCompatDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun advanceBoard() {
        if (activeRowIndex >= rowList.size) return
        if (!rowList[activeRowIndex].fullGuess()) return
        val originalCount = game.getRemainingCount()
        val guess = rowList[activeRowIndex].getGuess()
//        val feedback = giveFeedback(ArrayList(secretCode),ArrayList(guess.toList())).sortedArrayDescending()
        val feedback = game.giveFeedback(guess.toList())
        this.rowList[activeRowIndex].displayFeedback(feedback.toIntArray(),originalCount,this)
        activeRowIndex++
        var solved = true
        for (peg in feedback) {
            if (peg != 2) {
                solved = false
                break
            }
        }
        if (activeRowIndex < rowList.size && !solved) {
            this.rowList[activeRowIndex].activate()
        } else {
            val inflater = LayoutInflater.from(this)
            val customDialogView = inflater.inflate(R.layout.alert_dialog, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(customDialogView)
            alertDialogBuilder.setMessage(if(solved){"You Won!"}else{"You Lost!"})
            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setPositiveButton("Play Again") { _, _ ->
                // User clicked "Go Back"
                this.finish()
                val intent = Intent(this,GameBoard::class.java)
                startActivity(intent)
            }
            alertDialogBuilder.setNegativeButton("Go to Main Page") { _, _ ->
                // User clicked "Cancel", do nothing
                this.finish()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            val alertDialog: AppCompatDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}

//    private fun giveFeedback(secretCode: ArrayList<Int>, guessList: ArrayList<Int>): IntArray {
//        val feedback = IntArray(secretCode.size)
//
//        // Mark black pegs (2)
//        for (i in secretCode.indices) {
//            if (guessList[i] == secretCode[i]) {
//                feedback[i] = 2
//                secretCode[i] = -1 // Mark the digit as already used in the feedback
//                guessList[i] = -1 // Mark the digit as already used in the feedback
//            }
//        }
//
//        // Mark white pegs (1)
//        for (i in secretCode.indices) {
//            for (j in guessList.indices) {
//                if (guessList[j] == secretCode[i] && guessList[j] != -1 && secretCode[i] != -1) {
//                    feedback[j] = 1
//                    secretCode[i] = -1 // Mark the digit as already used in the feedback
//                    guessList[j] = -1 // Mark the digit as already used in the feedback
//                }
//            }
//        }
//        return feedback
//    }

    private fun spaceEvenly(layout: LinearLayout) {
        if (layout.childCount == 0) return
        layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                var totalWidth = 0f
                var visibleChildCount = layout.childCount
                for (i in 0 until layout.childCount) {
                    if (layout.getChildAt(i).visibility == View.GONE) {
                        visibleChildCount--
                        continue
                    }
                    totalWidth += layout.getChildAt(i).width
                }

                val spacing = (layout.width - totalWidth) / (visibleChildCount + 1)
                for (i in 0 until layout.childCount) {
                    val view = layout.getChildAt(i)
                    val params = view.layoutParams as LinearLayout.LayoutParams
                    params.leftMargin = spacing.toInt()
                    view.layoutParams = params
                }
            }
        })
    }

