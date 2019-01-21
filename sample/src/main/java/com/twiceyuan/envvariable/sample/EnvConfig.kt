package com.twiceyuan.envvariable.sample

import com.twiceyuan.envvariable.annotation.VariableProp
import com.twiceyuan.envvariable.model.Variable

interface EnvConfig {

    class FruitServer {
        class Debug : Variable.Item("调试", "https://fruit1.com", true)
        class Release : Variable.Item("生产", "https://fruit2.com")

        class Default : Variable.DefaultItemProvider {
            override fun provide() = if (BuildConfig.DEBUG) {
                Debug::class.java
            } else {
                Release::class.java
            }
        }
    }

    @VariableProp(
        name = "fruit", desc = "水果服务器",
        defaultValue = FruitServer.Default::class,
        selections = [FruitServer.Debug::class, FruitServer.Release::class]
    )
    fun fruitServer(): Variable

    class AnimalServer {
        class Debug : Variable.Item("调试", "https://animal1.com", true)
        class Release : Variable.Item("生产", "https://animal2.com")

        class Default : Variable.DefaultItemProvider {
            override fun provide() = Release::class.java
        }
    }

    @VariableProp(
        name = "animal", desc = "动物服务器",
        defaultValue = AnimalServer.Default::class,
        selections = [AnimalServer.Debug::class, AnimalServer.Release::class]
    )
    fun animalServer(): Variable
}
