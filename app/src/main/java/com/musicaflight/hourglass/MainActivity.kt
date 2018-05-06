package com.musicaflight.hourglass

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity(), HourglassListAdapter.OnHourglassItemClickListener, Content.ContentChangeListener {


	override fun onHourglassItemClick(h: Hourglass) = startActivity(Intent(this, DetailsActivity::class.java).putExtra("hourglass", h))

	private var adapter = HourglassListAdapter(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)

		adapter.addOnHourglassItemClickListener(this)

		val fab = findViewById<FloatingActionButton>(R.id.fab)
		fab.setOnClickListener { startActivity(Intent(this@MainActivity, NewHourglassActivity::class.java)) }

		Content.load(this)
		Content.addContentChangeListener(this)

		val recyclerView = findViewById<RecyclerView>(R.id.hourglass_list) as RecyclerView
		val llm = object : LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
			override fun supportsPredictiveItemAnimations(): Boolean = true
		}


		recyclerView.layoutManager = llm
		recyclerView.adapter = adapter
	}

	override fun onDestroy() {
		Content.close()
		super.onDestroy()
	}

	override fun onPause() = super.onPause()

	override fun onStop() {
		Content.save()
		super.onStop()
	}

	override fun onStart() {
		Content.save()
		super.onStart()
	}

	override fun onResume() = super.onResume()

	override fun onHourglassAdded(hourglass: Hourglass, toList: MutableList<Hourglass>, position: Int) {}

	override fun onHourglassRemoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, position: Int) =
			Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.hourglass_deleted, hourglass.title), Snackbar.LENGTH_LONG).setAction(R.string.undo) { Content.addHourglass(hourglass) }.show()

	override fun onHourglassMoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, fromPosition: Int, toList: MutableList<Hourglass>, toPosition: Int) = Unit
}
