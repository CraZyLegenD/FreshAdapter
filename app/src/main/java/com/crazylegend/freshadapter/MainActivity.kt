package com.crazylegend.freshadapter

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.freshadapter.databinding.ActivityMainBinding
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val personAdapter by lazy {
        PersonAdapter()
    }

    private val personList
        get() =
            listOf(
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}"),
                    Person(UUID.randomUUID().toString(), "Annotations are awesome %${Random.nextInt(0, 20)}")
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.recycler.adapter = personAdapter
        personAdapter.submitList(personList)


        personAdapter.onItemViewClickListener = PersonAdapter.forItemClickListener { position, item, view -> Log.d("CLICKED AT $position", "LONG ITEM VIEW CLICK ${item.name} at view ${view.id}") }
        personAdapter.surnameLongClickListener = PersonAdapter.forItemClickListener { position, item, view -> Log.d("CLICKED AT $position", "LONG SURNAME VIEW CLICK ${item.surname} at view ${view.id}") }
    }


}