package com.san_pedrito.myapplication // Ajusta esto al paquete correcto de tu aplicación

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context.VIBRATOR_SERVICE
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private var soundId = 0
    private var soundLoaded = false
    private var currentSelectedItemId = -1 // Para rastrear el elemento actualmente seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Añadir un listener para saber cuándo se ha cargado el sonido
        soundPool.setOnLoadCompleteListener { _, sId, status ->
            if (status == 0) { // 0 significa éxito
                soundLoaded = true
                Toast.makeText(this, "Sonido cargado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al cargar el sonido", Toast.LENGTH_SHORT).show()
            }
        }
            
        // Cargar el sonido existente en la carpeta raw
        soundId = soundPool.load(this, R.raw.tap_sound, 1)
        
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        
        // Configurar la navegación personalizada
        setupCustomNavigation(navView)
        
        // Seleccionar Home por defecto sin reproducir sonido
        currentSelectedItemId = R.id.navigation_home
        applySelectedStyle(navView, navView.menu.findItem(R.id.navigation_home), R.color.nav_home)
        navView.selectedItemId = R.id.navigation_home
    }

    private fun setupCustomNavigation(navView: BottomNavigationView) {
        // Desactivar el cambio de color predeterminado
        navView.itemIconTintList = null
        navView.itemTextColor = null
        
        // Configurar el listener para cambiar los colores y fondos
        navView.setOnItemSelectedListener { item ->
            // Solo reproducir sonido si es un cambio real de selección
            if (item.itemId != currentSelectedItemId) {
                playSound()
                currentSelectedItemId = item.itemId
            }
            
            // Restablecer todos los elementos a su estado normal
            resetAllItems(navView)
            
            // Aplicar estilo al elemento seleccionado
            when (item.itemId) {
                R.id.navigation_home -> {
                    applySelectedStyle(navView, item, R.color.nav_home)
                    true
                }
                R.id.navigation_likes -> {
                    applySelectedStyle(navView, item, R.color.nav_likes)
                    true
                }
                R.id.navigation_search -> {
                    applySelectedStyle(navView, item, R.color.nav_search)
                    true
                }
                R.id.navigation_profile -> {
                    applySelectedStyle(navView, item, R.color.nav_profile)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun playSound() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.tap_sound)
            mediaPlayer.setVolume(1.0f, 1.0f)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun resetAllItems(navView: BottomNavigationView) {
        val menuView = navView.getChildAt(0) as BottomNavigationMenuView
        for (i in 0 until menuView.childCount) {
            val itemView = menuView.getChildAt(i) as BottomNavigationItemView
            
            // Eliminar cualquier fondo personalizado
            itemView.background = null
            
            // Restablecer padding a valores más pequeños
            itemView.setPadding(0, 0, 0, 0)
            
            // Restablecer colores de iconos y texto a gris
            val grayColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.nav_unselected))
            itemView.setIconTintList(grayColor)
            itemView.setTextColor(grayColor)
        }
    }
    
    private fun applySelectedStyle(navView: BottomNavigationView, item: MenuItem, colorResId: Int) {
        val menuView = navView.getChildAt(0) as BottomNavigationMenuView
        val index = getMenuItemIndex(navView, item.itemId)
        
        if (index >= 0) {
            val itemView = menuView.getChildAt(index) as BottomNavigationItemView
            
            // Crear fondo redondeado con solo bordes (sin relleno)
            val shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(resources.getDimension(R.dimen.nav_item_corner_radius))
                .build()
            
            val color = ContextCompat.getColor(this, colorResId)
            val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
                fillColor = ColorStateList.valueOf(Color.TRANSPARENT) // Fondo transparente
                strokeColor = ColorStateList.valueOf(color)
                strokeWidth = resources.getDimension(R.dimen.nav_item_stroke_width)
            }
            
            // Aplicar fondo
            itemView.background = shapeDrawable
            
            // Añadir padding para hacer el elemento más grande
            val padding = resources.getDimensionPixelSize(R.dimen.nav_item_padding)
            itemView.setPadding(padding, padding / 2, padding, padding / 2)
            
            // Cambiar color de icono y texto
            val colorStateList = ColorStateList.valueOf(color)
            itemView.setIconTintList(colorStateList)
            itemView.setTextColor(colorStateList)
        }
    }
    
    private fun getMenuItemIndex(navView: BottomNavigationView, itemId: Int): Int {
        val menu = navView.menu
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == itemId) {
                return i
            }
        }
        return -1
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos del SoundPool cuando se destruye la actividad
        soundPool.release()
    }
}