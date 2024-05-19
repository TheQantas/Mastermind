package com.example.mastermind

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings,SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button click here
                onBackPressed() // Simulate the default behavior of the back button
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val sharedPreferences = preferenceManager.sharedPreferences ?: return

            val numberOfColorsPreference = findPreference<SeekBarPreference>("number_of_colors")
            val lengthOfCodePreference = findPreference<SeekBarPreference>("length_of_code")
            val allowDuplicatesPreference = findPreference<SwitchPreferenceCompat>("allow_duplicates")

            var numberOfColors = sharedPreferences.getInt("number_of_colors",6)
            var lengthOfCode = sharedPreferences.getInt("length_of_code",4)
            var allowDuplicates = sharedPreferences.getBoolean("allow_duplicates",false)

            fun checkValidDuplicate() {
                println("loc: $lengthOfCode, noc: $numberOfColors, ad: $allowDuplicates, values: ${sharedPreferences.all}")
                if (lengthOfCode > numberOfColors && !allowDuplicates) {
//                    val context: Context = this@SettingsActivity // Replace with the appropriate context
//                    val message = "You cannot allow duplicated with these options"
//                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
//                    sharedPreferences.edit().putBoolean("allow_duplicates",true).apply()
                    allowDuplicatesPreference?.isChecked = true
                    allowDuplicatesPreference?.isEnabled = false
                } else {
                    allowDuplicatesPreference?.isEnabled = true
                }
            }

            numberOfColorsPreference?.setOnPreferenceChangeListener { _, newValue ->
                println("noc; $newValue")
//                sharedPreferences.edit().putInt("number_of_colors",newValue as Int).apply()
                numberOfColors = newValue as Int
                checkValidDuplicate()
                true
            }
            lengthOfCodePreference?.setOnPreferenceChangeListener { _, newValue ->
//                sharedPreferences.edit().putInt("length_of_code",newValue as Int).apply()
                lengthOfCode = newValue as Int
                checkValidDuplicate()
                true
            }
            allowDuplicatesPreference?.setOnPreferenceChangeListener { _, newValue ->
//                sharedPreferences.edit().putBoolean("allow_duplicates",newValue as Boolean).apply()
                allowDuplicates = newValue as Boolean
                checkValidDuplicate()
                true
            }
        }
    }
}