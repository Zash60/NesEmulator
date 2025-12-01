package com.ym.nesemulator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import com.ym.library.base.BaseEmulatorActivity
import com.ym.library.sdk.EmulatorManager

class MyEmulatorActivity : BaseEmulatorActivity(), OnTouchListener {

    // Mapa de botões
    private val keyCompMap: HashMap<Int, Pair<EmulatorManager.Player, ArrayList<EmulatorManager.ControllerKey>>> =
        HashMap()

    // Vibrador para feedback tátil
    private var vibrator: Vibrator? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_nes_emulator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializa o vibrador
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        this.initKeyCompMap()
    }

    private fun vibrate() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Vibração curta e leve para Android 8+
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // Vibração antiga
                vibrator?.vibrate(30)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyCompMap() {
        // Função auxiliar para registrar botões
        fun registerBtn(id: Int, player: EmulatorManager.Player, keys: List<EmulatorManager.ControllerKey>) {
            findViewById<View>(id)?.apply {
                keyCompMap[this.id] = Pair(player, ArrayList(keys))
                this.setOnTouchListener(this@MyEmulatorActivity)
            }
        }

        val p1 = EmulatorManager.Player.PLAYER1

        // Direcionais
        registerBtn(R.id.keyLeft, p1, listOf(EmulatorManager.ControllerKey.LEFT))
        registerBtn(R.id.keyUp, p1, listOf(EmulatorManager.ControllerKey.UP))
        registerBtn(R.id.keyRight, p1, listOf(EmulatorManager.ControllerKey.RIGHT))
        registerBtn(R.id.keyDown, p1, listOf(EmulatorManager.ControllerKey.DOWN))
        
        // Diagonais (Combinam duas teclas)
        registerBtn(R.id.keyLeftUp, p1, listOf(EmulatorManager.ControllerKey.LEFT, EmulatorManager.ControllerKey.UP))
        registerBtn(R.id.keyRightUp, p1, listOf(EmulatorManager.ControllerKey.RIGHT, EmulatorManager.ControllerKey.UP))
        registerBtn(R.id.keyRightDown, p1, listOf(EmulatorManager.ControllerKey.RIGHT, EmulatorManager.ControllerKey.DOWN))
        registerBtn(R.id.keyLeftDown, p1, listOf(EmulatorManager.ControllerKey.LEFT, EmulatorManager.ControllerKey.DOWN))

        // Ações
        registerBtn(R.id.keySelect, p1, listOf(EmulatorManager.ControllerKey.SELECT))
        registerBtn(R.id.keyStart, p1, listOf(EmulatorManager.ControllerKey.START))
        registerBtn(R.id.keyA, p1, listOf(EmulatorManager.ControllerKey.A))
        registerBtn(R.id.keyB, p1, listOf(EmulatorManager.ControllerKey.B))
        
        // TURBO (Velocidade de tiro rápida)
        registerBtn(R.id.keyATurbo, p1, listOf(EmulatorManager.ControllerKey.A_TURBO))
        registerBtn(R.id.keyBTurbo, p1, listOf(EmulatorManager.ControllerKey.B_TURBO))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val viewId = v?.id ?: return false
        val keyData = keyCompMap[viewId] ?: return false
        
        val player = keyData.first
        val keys = keyData.second

        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Ao tocar: Vibra e pressiona a tecla no emulador
                vibrate()
                
                // Efeito visual de clique (opacidade)
                v?.alpha = 0.6f 
                
                keys.forEach { key ->
                    EmulatorManager.getInstance().pressKey(player, key)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                // Ao soltar: Restaura opacidade e solta tecla
                v?.alpha = 1.0f
                
                keys.forEach { key ->
                    EmulatorManager.getInstance().unPressKey(player, key)
                }
            }
        }
        return true
    }
}
