package com.twiceyuan.envvariable.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.twiceyuan.envvariable.EnvVariable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val config by lazy { (application as EnvApplication).config }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_config.setOnClickListener { EnvVariable.openConfig(this) }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        val fruitServer = config.fruitServer()
        val animalServer = config.animalServer()

        tv_content.text = """
            ${fruitServer.name}: ${config.fruitServer().value}
            ${animalServer.name}: ${config.animalServer().value}
        """.trimIndent()
    }
}
