package com.performancebooster

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var output: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private val requestCode = 4201

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { code, result ->
        if (code == requestCode) {
            runOnUiThread {
                status.text = if (result == PackageManagerCompat.PERMISSION_GRANTED) {
                    "Shizuku autorizado"
                } else "Permissão Shizuku negada"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        buildUi()
        refreshPermission()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.rgb(13, 17, 28)) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 28)
        }
        val title = TextView(this).apply {
            text = "PERFORMANCE BOOSTER"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Painel de desempenho • ADB + Shizuku"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, 20)
        }
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(120, 220, 160))
            setPadding(0, 8, 0, 18)
        }
        page.addView(title); page.addView(subtitle); page.addView(status)
        addButton(page, "Conectar / autorizar Shizuku") { requestShizuku() }
        addButton(page, "Diagnóstico do aparelho") {
            runShell("getprop ro.product.manufacturer; getprop ro.product.model; getprop ro.build.version.release; uptime; free -h; df -h /data")
        }
        addButton(page, "Ver uso de memória") {
            runShell("cat /proc/meminfo | head -12; dumpsys meminfo")
        }
        addButton(page, "Ativar perfil visual leve (animações 0,5x)") {
            runShell("settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5")
        }
        addButton(page, "Desativar animações") {
            runShell("settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0")
        }
        addButton(page, "Restaurar animações padrão (1x)") {
            runShell("settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1")
        }
        addButton(page, "Otimizar compilação de um app (informar pacote)") {
            val input = EditText(this).apply {
                hint = "Ex.: com.exemplo.jogo"
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
            }
            val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 8, 12, 8) }
            box.addView(input)
            AlertDialogCompat.show(this, "Compilar app instalado", box) {
                val pkg = input.text.toString().trim()
                if (pkg.matches(Regex("[A-Za-z0-9_.]+"))) {
                    runShell("cmd package compile -m speed-profile -f $pkg")
                } else appendOutput("Nome de pacote inválido.")
            }
        }
        addButton(page, "Solicitar limpeza de caches recuperáveis") {
            runShell("pm trim-caches 2G")
        }
        output = TextView(this).apply {
            text = "Saída dos comandos aparecerá aqui."
            textSize = 12f
            setTextColor(Color.rgb(210, 218, 230))
            setPadding(0, 18, 0, 20)
        }
        page.addView(output)
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }
        parent.addView(b, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }

    private fun refreshPermission() {
        status.text = when {
            !Shizuku.pingBinder() -> "Shizuku não está ativo. Abra o app Shizuku e inicie o serviço."
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED -> "Shizuku autorizado"
            else -> "Shizuku ativo, aguardando autorização"
        }
    }

    private fun requestShizuku() {
        if (!Shizuku.pingBinder()) {
            appendOutput("Abra o Shizuku, inicie o serviço (wireless debugging ou root) e volte aqui.")
            refreshPermission()
            return
        }
        if (Shizuku.isPreV11()) {
            appendOutput("Versão do Shizuku incompatível. Atualize o Shizuku.")
            return
        }
        if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            refreshPermission()
        } else Shizuku.requestPermission(requestCode)
    }

    private fun runShell(command: String) {
        if (!Shizuku.pingBinder() ||
            Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            appendOutput("Ação bloqueada: autorize o Shizuku primeiro.")
            return
        }
        status.text = "Executando comando…"
        executor.execute {
            try {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
                val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
                val code = process.waitFor()
                val result = "Exit: $code\n$stdout\n$stderr".trim()
                runOnUiThread { status.text = "Comando concluído"; appendOutput(result) }
            } catch (e: Exception) {
                runOnUiThread { status.text = "Falha ao executar"; appendOutput(e.message ?: "Erro desconhecido") }
            }
        }
    }

    private fun appendOutput(value: String) {
        output.text = value
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        executor.shutdown()
        super.onDestroy()
    }
}

private object PackageManagerCompat { const val PERMISSION_GRANTED = android.content.pm.PackageManager.PERMISSION_GRANTED }

private object AlertDialogCompat {
    fun show(activity: Activity, title: String, content: View, onOk: () -> Unit) {
        android.app.AlertDialog.Builder(activity)
            .setTitle(title).setView(content)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Executar") { _, _ -> onOk() }
            .show()
    }
}
