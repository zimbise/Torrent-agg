package com.zim.tagg

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val pm = packageManager
        val testIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("magnet:?xt=urn:btih:test"))
        val handlers = pm.queryIntentActivities(testIntent, 0)

        val labels = handlers.map { it.loadLabel(pm).toString() }
        val packages = handlers.map { it.activityInfo.packageName }

        val spinner = findViewById<Spinner>(R.id.clientSpinner)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity)
                    .edit().putString("preferredclientpkg", packages[position]).apply()
                Toast.makeText(this@SettingsActivity, "Preferred client set", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.addProviderBtn).setOnClickListener {
            val txt = findViewById<EditText>(R.id.providerJsonInput).text.toString().trim()
            runCatching { JSONObject(txt) }.onSuccess { addProvider(it) }.onFailure {
                Toast.makeText(this, "Invalid provider JSON", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addProvider(obj: JSONObject) {
        val name = "providers-user.json"
        val root = runCatching { openFileInput(name).bufferedReader().readText() }
            .map { JSONObject(it) }.getOrElse { JSONObject("""{"providers":[]}""") }
        root.getJSONArray("providers").put(obj)
        openFileOutput(name, MODE_PRIVATE).use { it.write(root.toString().toByteArray()) }
        Toast.makeText(this, "Provider added", Toast.LENGTH_SHORT).show()
    }
}
