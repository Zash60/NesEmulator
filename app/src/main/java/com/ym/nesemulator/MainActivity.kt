package com.ym.nesemulator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ym.library.sdk.EmulatorManager
import java.io.File

class MainActivity : AppCompatActivity() {

    private val btnStart: Button by lazy { findViewById(R.id.btnStart) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener {
            checkPermissionAndStart()
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11, 12, 13, 14+ (Usa a nova permissão de Acesso Total)
            if (Environment.isExternalStorageManager()) {
                startGame()
            } else {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = android.net.Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, 2000)
                    Toast.makeText(this, "Por favor, conceda acesso a todos os arquivos", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, 2000)
                }
            }
        } else {
            // Android 10 e anteriores (Usa o método antigo)
            // Mantive simples aqui, mas idealmente usaria o requestPermissions antigo
            startGame()
        }
    }

    // Verifica se o usuário voltou das configurações com a permissão ativada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show()
                    startGame()
                } else {
                    Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startGame() {
        // ATENÇÃO: Mudei o nome para 'jogo.nes' para facilitar
        // O arquivo deve estar na raiz do armazenamento interno (não no cartão SD externo)
        val path = Environment.getExternalStorageDirectory().path + "/jogo.nes"
        val romFile = File(path)

        if (!romFile.exists()) {
            Toast.makeText(this, "Arquivo não encontrado: $path", Toast.LENGTH_LONG).show()
            return
        }

        try {
            EmulatorManager.getInstance().startGame(this, MyEmulatorActivity::class.java, romFile)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao iniciar emulador: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
