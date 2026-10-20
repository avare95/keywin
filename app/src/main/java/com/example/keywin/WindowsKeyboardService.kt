package com.example.keywin

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.inputmethodservice.InputMethodService
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

class WindowsKeyboardService : InputMethodService() {

    // Modifikatör Durumları (Yapışkan / Latch)
    private var isShiftActive = false
    private var isCapsActive = false
    private var isCtrlActive = false
    private var isAltActive = false
    private var isWinActive = false

    // Buton Referansları
    private var btnCtrl: Button? = null
    private var btnAlt: Button? = null
    private var btnWin: Button? = null
    private var btnShift: Button? = null
    private var btnCaps: Button? = null

    // Renkler
    private val colorActive = Color.parseColor("#0078D7")
    private val colorDefaultSpecial = Color.parseColor("#262626")
    private val colorDefaultStandard = Color.parseColor("#383838")

    // Boyut ve Mod Değişkenleri
    private var isCompactMode = true
    private var isNumpadActive = false
    private var layoutQwerty: ViewGroup? = null
    private var layoutNumpad: ViewGroup? = null
    private var btnToggleMode: Button? = null
    private var rows: List<ViewGroup> = emptyList()

    private val letterButtons = mutableMapOf<Int, Pair<String, String>>()
    private val symbolButtons = mutableMapOf<Int, Pair<String, String>>()
    private var rootView: View? = null

    override fun onCreateInputView(): View {
        val contextThemeWrapper = ContextThemeWrapper(this, R.style.Theme_KeyWin)
        val themedInflater = layoutInflater.cloneInContext(contextThemeWrapper)
        val view = themedInflater.inflate(R.layout.keyboard_view, null)
        rootView = view

        initKeyMappings()
        setupTopBar(view)
        setupModifierKeys(view)
        bindKeyEvents(view)
        bindNumpadAndMacros(view)
        updateLabels()

        return view
    }

    override fun onEvaluateInputViewShown(): Boolean = true
    override fun onEvaluateFullscreenMode(): Boolean = false

    private fun initKeyMappings() {
        val letters = listOf(
            R.id.btn_q to ("q" to "Q"), R.id.btn_w to ("w" to "W"), R.id.btn_e to ("e" to "E"),
            R.id.btn_r to ("r" to "R"), R.id.btn_t to ("t" to "T"), R.id.btn_y to ("y" to "Y"),
            R.id.btn_u to ("u" to "U"), R.id.btn_i to ("i" to "I"), R.id.btn_o to ("o" to "O"),
            R.id.btn_p to ("p" to "P"), R.id.btn_a to ("a" to "A"), R.id.btn_s to ("s" to "S"),
            R.id.btn_d to ("d" to "D"), R.id.btn_f to ("f" to "F"), R.id.btn_g to ("g" to "G"),
            R.id.btn_h to ("h" to "H"), R.id.btn_j to ("j" to "J"), R.id.btn_k to ("k" to "K"),
            R.id.btn_l to ("l" to "L"), R.id.btn_z to ("z" to "Z"), R.id.btn_x to ("x" to "X"),
            R.id.btn_c to ("c" to "C"), R.id.btn_v to ("v" to "V"), R.id.btn_b to ("b" to "B"),
            R.id.btn_n to ("n" to "N"), R.id.btn_m to ("m" to "M")
        )
        letters.forEach { letterButtons[it.first] = it.second }

        val symbols = listOf(
            R.id.btn_1 to ("1" to "!"), R.id.btn_2 to ("2" to "@"), R.id.btn_3 to ("3" to "#"),
            R.id.btn_4 to ("4" to "$"), R.id.btn_5 to ("5" to "%"), R.id.btn_6 to ("6" to "^"),
            R.id.btn_7 to ("7" to "&"), R.id.btn_8 to ("8" to "*"), R.id.btn_9 to ("9" to "("),
            R.id.btn_0 to ("0" to ")"), R.id.btn_comma to ("," to "<"), R.id.btn_period to ("." to ">")
        )
        symbols.forEach { symbolButtons[it.first] = it.second }
    }

    private fun setupTopBar(view: View) {
        layoutQwerty = view.findViewById(R.id.layout_qwerty_container)
        layoutNumpad = view.findViewById(R.id.layout_numpad_macro_container)
        btnToggleMode = view.findViewById(R.id.btn_toggle_mode)

        // Android Panosundan Windows'a Metin Aktarma
        view.findViewById<Button>(R.id.btn_clipboard_paste)?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip

            if (clip != null && clip.itemCount > 0) {
                val textToPaste = clip.getItemAt(0).coerceToText(this).toString()
                if (textToPaste.isNotEmpty()) {
                    currentInputConnection?.commitText(textToPaste, 1)
                }
            } else {
                Toast.makeText(this, "Android panosu boş", Toast.LENGTH_SHORT).show()
            }
        }

        // QWERTY <-> Numpad/Makro Geçişi
        btnToggleMode?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            isNumpadActive = !isNumpadActive
            if (isNumpadActive) {
                layoutQwerty?.visibility = View.GONE
                layoutNumpad?.visibility = View.VISIBLE
                btnToggleMode?.text = "⌨ Qwerty"
            } else {
                layoutNumpad?.visibility = View.GONE
                layoutQwerty?.visibility = View.VISIBLE
                btnToggleMode?.text = "🔢 Numpad / Makro"
            }
        }

        view.findViewById<Button>(R.id.btn_hide_keyboard)?.setOnClickListener {
            requestHideSelf(0)
        }

        val rowF = view.findViewById<ViewGroup>(R.id.row_f_keys)
        val row1 = view.findViewById<ViewGroup>(R.id.row_numbers)
        val row2 = view.findViewById<ViewGroup>(R.id.row_letters_1)
        val row3 = view.findViewById<ViewGroup>(R.id.row_letters_2)
        val row4 = view.findViewById<ViewGroup>(R.id.row_letters_3)
        val row5 = view.findViewById<ViewGroup>(R.id.row_bottom)
        rows = listOf(row1, row2, row3, row4, row5)

        // Boyutlandırma Butonu
        view.findViewById<Button>(R.id.btn_toggle_size)?.setOnClickListener {
            isCompactMode = !isCompactMode
            val density = resources.displayMetrics.density
            val letterHeight = if (isCompactMode) (34 * density).toInt() else (46 * density).toInt()
            val fKeyHeight = if (isCompactMode) (28 * density).toInt() else (36 * density).toInt()

            rowF?.layoutParams?.height = fKeyHeight
            rowF?.requestLayout()

            rows.forEach { row ->
                row.layoutParams?.height = letterHeight
                row.requestLayout()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupModifierKeys(view: View) {
        btnCtrl = view.findViewById(R.id.btn_ctrl)
        btnAlt = view.findViewById(R.id.btn_alt)
        btnWin = view.findViewById(R.id.btn_win)
        btnShift = view.findViewById(R.id.btn_shift)
        btnCaps = view.findViewById(R.id.btn_caps)

        val modifierListener = View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                val button = v as Button

                when (button.id) {
                    R.id.btn_alt -> {
                        isAltActive = !isAltActive
                        setButtonColor(button, isAltActive)
                    }
                    R.id.btn_ctrl -> {
                        isCtrlActive = !isCtrlActive
                        setButtonColor(button, isCtrlActive)
                    }
                    R.id.btn_win -> {
                        isWinActive = !isWinActive
                        setButtonColor(button, isWinActive)
                    }
                    R.id.btn_shift -> {
                        isShiftActive = !isShiftActive
                        setButtonColor(button, isShiftActive)
                        updateLabels()
                    }
                    R.id.btn_caps -> {
                        isCapsActive = !isCapsActive
                        setButtonColor(button, isCapsActive)
                        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAPS_LOCK))
                        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CAPS_LOCK))
                        updateLabels()
                    }
                }
            }
            true
        }

        btnCtrl?.setOnTouchListener(modifierListener)
        btnAlt?.setOnTouchListener(modifierListener)
        btnWin?.setOnTouchListener(modifierListener)
        btnShift?.setOnTouchListener(modifierListener)
        btnCaps?.setOnTouchListener(modifierListener)
    }

    private fun setButtonColor(button: Button?, isActive: Boolean) {
        val color = if (isActive) colorActive else colorDefaultSpecial
        button?.backgroundTintMode = PorterDuff.Mode.SRC_OVER
        button?.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun updateLabels() {
        val root = rootView ?: return
        val isUpper = isShiftActive xor isCapsActive

        letterButtons.forEach { (id, pair) ->
            root.findViewById<Button>(id)?.text = if (isUpper) pair.second else pair.first
        }

        symbolButtons.forEach { (id, pair) ->
            root.findViewById<Button>(id)?.text = if (isShiftActive) pair.second else pair.first
        }
    }

    private fun bindKeyEvents(view: View) {
        bindTouch(view, R.id.btn_esc, KeyEvent.KEYCODE_ESCAPE, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f1, KeyEvent.KEYCODE_F1, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f2, KeyEvent.KEYCODE_F2, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f3, KeyEvent.KEYCODE_F3, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f4, KeyEvent.KEYCODE_F4, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f5, KeyEvent.KEYCODE_F5, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f6, KeyEvent.KEYCODE_F6, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f7, KeyEvent.KEYCODE_F7, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f8, KeyEvent.KEYCODE_F8, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f9, KeyEvent.KEYCODE_F9, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f10, KeyEvent.KEYCODE_F10, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f11, KeyEvent.KEYCODE_F11, colorDefaultSpecial)
        bindTouch(view, R.id.btn_f12, KeyEvent.KEYCODE_F12, colorDefaultSpecial)
        bindTouch(view, R.id.btn_del, KeyEvent.KEYCODE_FORWARD_DEL, colorDefaultSpecial)

        bindTouch(view, R.id.btn_1, KeyEvent.KEYCODE_1, colorDefaultStandard)
        bindTouch(view, R.id.btn_2, KeyEvent.KEYCODE_2, colorDefaultStandard)
        bindTouch(view, R.id.btn_3, KeyEvent.KEYCODE_3, colorDefaultStandard)
        bindTouch(view, R.id.btn_4, KeyEvent.KEYCODE_4, colorDefaultStandard)
        bindTouch(view, R.id.btn_5, KeyEvent.KEYCODE_5, colorDefaultStandard)
        bindTouch(view, R.id.btn_6, KeyEvent.KEYCODE_6, colorDefaultStandard)
        bindTouch(view, R.id.btn_7, KeyEvent.KEYCODE_7, colorDefaultStandard)
        bindTouch(view, R.id.btn_8, KeyEvent.KEYCODE_8, colorDefaultStandard)
        bindTouch(view, R.id.btn_9, KeyEvent.KEYCODE_9, colorDefaultStandard)
        bindTouch(view, R.id.btn_0, KeyEvent.KEYCODE_0, colorDefaultStandard)
        bindTouch(view, R.id.btn_backspace, KeyEvent.KEYCODE_DEL, colorDefaultSpecial)

        bindTouch(view, R.id.btn_tab, KeyEvent.KEYCODE_TAB, colorDefaultSpecial)
        bindTouch(view, R.id.btn_q, KeyEvent.KEYCODE_Q, colorDefaultStandard)
        bindTouch(view, R.id.btn_w, KeyEvent.KEYCODE_W, colorDefaultStandard)
        bindTouch(view, R.id.btn_e, KeyEvent.KEYCODE_E, colorDefaultStandard)
        bindTouch(view, R.id.btn_r, KeyEvent.KEYCODE_R, colorDefaultStandard)
        bindTouch(view, R.id.btn_t, KeyEvent.KEYCODE_T, colorDefaultStandard)
        bindTouch(view, R.id.btn_y, KeyEvent.KEYCODE_Y, colorDefaultStandard)
        bindTouch(view, R.id.btn_u, KeyEvent.KEYCODE_U, colorDefaultStandard)
        bindTouch(view, R.id.btn_i, KeyEvent.KEYCODE_I, colorDefaultStandard)
        bindTouch(view, R.id.btn_o, KeyEvent.KEYCODE_O, colorDefaultStandard)
        bindTouch(view, R.id.btn_p, KeyEvent.KEYCODE_P, colorDefaultStandard)

        bindTouch(view, R.id.btn_a, KeyEvent.KEYCODE_A, colorDefaultStandard)
        bindTouch(view, R.id.btn_s, KeyEvent.KEYCODE_S, colorDefaultStandard)
        bindTouch(view, R.id.btn_d, KeyEvent.KEYCODE_D, colorDefaultStandard)
        bindTouch(view, R.id.btn_f, KeyEvent.KEYCODE_F, colorDefaultStandard)
        bindTouch(view, R.id.btn_g, KeyEvent.KEYCODE_G, colorDefaultStandard)
        bindTouch(view, R.id.btn_h, KeyEvent.KEYCODE_H, colorDefaultStandard)
        bindTouch(view, R.id.btn_j, KeyEvent.KEYCODE_J, colorDefaultStandard)
        bindTouch(view, R.id.btn_k, KeyEvent.KEYCODE_K, colorDefaultStandard)
        bindTouch(view, R.id.btn_l, KeyEvent.KEYCODE_L, colorDefaultStandard)
        bindTouch(view, R.id.btn_enter, KeyEvent.KEYCODE_ENTER, colorDefaultSpecial)

        bindTouch(view, R.id.btn_z, KeyEvent.KEYCODE_Z, colorDefaultStandard)
        bindTouch(view, R.id.btn_x, KeyEvent.KEYCODE_X, colorDefaultStandard)
        bindTouch(view, R.id.btn_c, KeyEvent.KEYCODE_C, colorDefaultStandard)
        bindTouch(view, R.id.btn_v, KeyEvent.KEYCODE_V, colorDefaultStandard)
        bindTouch(view, R.id.btn_b, KeyEvent.KEYCODE_B, colorDefaultStandard)
        bindTouch(view, R.id.btn_n, KeyEvent.KEYCODE_N, colorDefaultStandard)
        bindTouch(view, R.id.btn_m, KeyEvent.KEYCODE_M, colorDefaultStandard)
        bindTouch(view, R.id.btn_comma, KeyEvent.KEYCODE_COMMA, colorDefaultStandard)
        bindTouch(view, R.id.btn_period, KeyEvent.KEYCODE_PERIOD, colorDefaultStandard)
        bindTouch(view, R.id.btn_up, KeyEvent.KEYCODE_DPAD_UP, colorDefaultSpecial)

        bindTouch(view, R.id.btn_space, KeyEvent.KEYCODE_SPACE, colorDefaultStandard)
        bindTouch(view, R.id.btn_left, KeyEvent.KEYCODE_DPAD_LEFT, colorDefaultSpecial)
        bindTouch(view, R.id.btn_down, KeyEvent.KEYCODE_DPAD_DOWN, colorDefaultSpecial)
        bindTouch(view, R.id.btn_right, KeyEvent.KEYCODE_DPAD_RIGHT, colorDefaultSpecial)
    }

    private fun bindNumpadAndMacros(view: View) {
        bindTouch(view, R.id.btn_num_0, KeyEvent.KEYCODE_NUMPAD_0, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_1, KeyEvent.KEYCODE_NUMPAD_1, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_2, KeyEvent.KEYCODE_NUMPAD_2, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_3, KeyEvent.KEYCODE_NUMPAD_3, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_4, KeyEvent.KEYCODE_NUMPAD_4, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_5, KeyEvent.KEYCODE_NUMPAD_5, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_6, KeyEvent.KEYCODE_NUMPAD_6, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_7, KeyEvent.KEYCODE_NUMPAD_7, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_8, KeyEvent.KEYCODE_NUMPAD_8, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_9, KeyEvent.KEYCODE_NUMPAD_9, colorDefaultStandard)

        bindTouch(view, R.id.btn_num_dot, KeyEvent.KEYCODE_NUMPAD_DOT, colorDefaultStandard)
        bindTouch(view, R.id.btn_num_enter, KeyEvent.KEYCODE_NUMPAD_ENTER, colorDefaultSpecial)
        bindTouch(view, R.id.btn_num_add, KeyEvent.KEYCODE_NUMPAD_ADD, colorDefaultSpecial)
        bindTouch(view, R.id.btn_num_sub, KeyEvent.KEYCODE_NUMPAD_SUBTRACT, colorDefaultSpecial)
        bindTouch(view, R.id.btn_num_mul, KeyEvent.KEYCODE_NUMPAD_MULTIPLY, colorDefaultSpecial)
        bindTouch(view, R.id.btn_num_div, KeyEvent.KEYCODE_NUMPAD_DIVIDE, colorDefaultSpecial)
        bindTouch(view, R.id.btn_num_lock, KeyEvent.KEYCODE_NUM_LOCK, colorDefaultSpecial)

        bindTouch(view, R.id.btn_nav_insert, KeyEvent.KEYCODE_INSERT, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_home, KeyEvent.KEYCODE_MOVE_HOME, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_end, KeyEvent.KEYCODE_MOVE_END, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_pgup, KeyEvent.KEYCODE_PAGE_UP, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_pgdn, KeyEvent.KEYCODE_PAGE_DOWN, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_del, KeyEvent.KEYCODE_FORWARD_DEL, colorDefaultStandard)
        bindTouch(view, R.id.btn_nav_prtscn, KeyEvent.KEYCODE_SYSRQ, colorDefaultSpecial)

        bindTouch(view, R.id.btn_macro_esc, KeyEvent.KEYCODE_ESCAPE, colorDefaultSpecial)
        bindTouch(view, R.id.btn_macro_backspace, KeyEvent.KEYCODE_DEL, colorDefaultSpecial)
        bindTouch(view, R.id.btn_macro_enter, KeyEvent.KEYCODE_ENTER, colorDefaultSpecial)

        bindMacro(view, R.id.btn_macro_taskmgr, listOf(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_ESCAPE))
        bindMacro(view, R.id.btn_macro_wind, listOf(KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_D))
        bindMacro(view, R.id.btn_macro_alttab, listOf(KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_TAB))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindMacro(view: View, id: Int, keyCodes: List<Int>) {
        val button = view.findViewById<Button>(id) ?: return
        button.setOnTouchListener { v, event ->
            val ic = currentInputConnection
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    button.backgroundTintMode = PorterDuff.Mode.SRC_OVER
                    button.backgroundTintList = ColorStateList.valueOf(colorActive)
                    v.scaleX = 0.93f
                    v.scaleY = 0.93f

                    for (k in keyCodes) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, k))
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    button.backgroundTintMode = PorterDuff.Mode.SRC_OVER
                    button.backgroundTintList = ColorStateList.valueOf(colorDefaultSpecial)
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f

                    for (k in keyCodes.reversed()) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, k))
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindTouch(view: View, id: Int, keyCode: Int, defaultColor: Int) {
        val button = view.findViewById<Button>(id) ?: return
        button.setOnTouchListener { v, event ->
            val ic = currentInputConnection
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    button.backgroundTintMode = PorterDuff.Mode.SRC_OVER
                    button.backgroundTintList = ColorStateList.valueOf(colorActive)
                    v.scaleX = 0.93f
                    v.scaleY = 0.93f

                    if (isAltActive) ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT))
                    if (isCtrlActive) ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT))
                    if (isShiftActive) ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT))
                    if (isWinActive) ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_META_LEFT))

                    ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    button.backgroundTintMode = PorterDuff.Mode.SRC_OVER
                    button.backgroundTintList = ColorStateList.valueOf(defaultColor)
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f

                    ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))

                    if (isAltActive) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT))
                        isAltActive = false
                        setButtonColor(btnAlt, false)
                    }
                    if (isCtrlActive) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT))
                        isCtrlActive = false
                        setButtonColor(btnCtrl, false)
                    }
                    if (isShiftActive) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
                        isShiftActive = false
                        setButtonColor(btnShift, false)
                        updateLabels()
                    }
                    if (isWinActive) {
                        ic?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_META_LEFT))
                        isWinActive = false
                        setButtonColor(btnWin, false)
                    }
                    true
                }
                else -> false
            }
        }
    }
}