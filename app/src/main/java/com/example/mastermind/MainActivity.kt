package com.example.mastermind

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mastermind.ui.theme.MastermindTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_setup)
        val button = findViewById<Button>(R.id.btn_launch_game)
        button.setOnClickListener {
            val intent = Intent(this,GameBoard::class.java)

//            intent.putExtra("color_count",findViewById<SeekBar>(R.id.number_of_colors).progress)
//            intent.putExtra("code_length",findViewById<SeekBar>(R.id.length_of_code).progress)

            startActivity(intent)
        }
        val setupButton = findViewById<Button>(R.id.btn_go_to_settings)
        setupButton.setOnClickListener {
            val intent = Intent(this,SettingsActivity::class.java)

            startActivity(intent)
        }
        val instructionsButton = findViewById<Button>(R.id.btn_go_to_instructions)
        instructionsButton.setOnClickListener {
            val intent = Intent(this,Instructions::class.java)

            startActivity(intent)
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//            text = "Hello $name!",
//            modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MastermindTheme {
//        Greeting("Android")
//    }
//}