package com.musicaflight.hourglass

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.util.*

class DetailsActivity : AppCompatActivity() {

	lateinit var hourglass: Hourglass
	private lateinit var thread: Thread
	var sleepInterval: Long = 1000
	private lateinit var years: View
	private lateinit var days: View
	private lateinit var hours: View
	private lateinit var minutes: View
	private lateinit var seconds: View
	private lateinit var ago: TextView
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_details)

		val t = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(t)

		val i = intent
		hourglass = i.getParcelableExtra("hourglass")

		val ab = supportActionBar!!
		ab.setDisplayHomeAsUpEnabled(true)
		ab.title = hourglass.title

		years = (findViewById<ViewStub>(R.id.years)).inflate()
		days = (findViewById<ViewStub>(R.id.days)).inflate()
		hours = (findViewById<ViewStub>(R.id.hours)).inflate()
		minutes = (findViewById<ViewStub>(R.id.minutes)).inflate()
		seconds = (findViewById<ViewStub>(R.id.seconds)).inflate()

		ago = findViewById(R.id.ago)

		loadBackground()
	}

	override fun onDestroy() = super.onDestroy()

	override fun onStart() {
		super.onStart()
		updateCountdown()
		thread = object : Thread() {
			override fun run() {
				while (!isInterrupted) {
					updateCountdown()
					try {
						if (sleepInterval < 1000f / 60f) {
							sleepInterval = (1000f / 60f).toLong()
						}
						Thread.sleep(sleepInterval)
					} catch (e: InterruptedException) {
						break
					}

				}
			}
		}
		thread.start()

	}


	override fun onStop() {
		super.onStop()
		thread.interrupt()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.details, menu)
		return true
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_CANCELED) return
		val oldImageId = hourglass.imageId
		hourglass = data!!.getParcelableExtra("hourglass")
		supportActionBar!!.title = hourglass.title
		if (oldImageId!=hourglass.imageId) {
			loadBackground()
		}
	}

	private fun loadBackground() {
		if (hourglass.imageId != null) {
			val imageView = findViewById<ImageView>(R.id.background)
			val imgFile = File(Content.getImagePathFromImageId(this@DetailsActivity, hourglass.imageId!!))
			if (imgFile.exists()) {
				runOnUiThread {
					imageView.setImageBitmap(BitmapFactory.decodeFile(imgFile.absolutePath))
				}
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		when (id) {
			android.R.id.home -> if (!super.onOptionsItemSelected(item)) {
				finish()
				return true
			}
			R.id.action_edit -> {
				val i = Intent(this, EditHourglassActivity::class.java)
				i.putExtra("hourglass", hourglass)
				startActivityForResult(i, 1)
			}
			R.id.action_delete -> {
				Content.deleteHourglass(hourglass)
				finish()
			}
		}
		return super.onOptionsItemSelected(item)
	}


	fun updateCountdown() {
		var diff = Math.abs(hourglass.millisRemaining)

		diff -= updateTimeUnit(hourglass.showYears, Unit.YEARS, diff)
		diff -= updateTimeUnit(hourglass.showDays, Unit.DAYS, diff)
		diff -= updateTimeUnit(hourglass.showHours, Unit.HOURS, diff)
		diff -= updateTimeUnit(hourglass.showMinutes, Unit.MINUTES, diff)
		updateTimeUnit(hourglass.showSeconds, Unit.SECONDS, diff)

		if (hourglass.millisRemaining < 0)
			ago.visibility = View.VISIBLE
		else
			ago.visibility = View.GONE
		//        thread.setText(years + " years\n" + days + " days\n" + hours + " hours\n" + minutes + " minutes\n" + secondFormat.format(seconds) + " seconds");
	}


	private fun updateTimeUnit(
			show: Boolean, u: Unit, millisLeftover: Long): Long {

		val view: View
		val plural: Int
		val multiplier: Long
		val lastUnit: Boolean

		when (u) {
			Unit.YEARS -> {
				view = years
				plural = R.plurals.years
				multiplier = 1000L * 60L * 60L * 24L * 365L
				lastUnit = !hourglass.showSeconds && !hourglass.showMinutes && !hourglass.showHours && !hourglass.showDays
			}
			Unit.DAYS -> {
				view = days
				plural = R.plurals.days
				multiplier = 1000L * 60L * 60L * 24L
				lastUnit = !hourglass.showSeconds && !hourglass.showMinutes && !hourglass.showHours
			}
			Unit.HOURS -> {
				view = hours
				plural = R.plurals.hours
				multiplier = 1000L * 60L * 60L
				lastUnit = !hourglass.showSeconds && !hourglass.showMinutes
			}
			Unit.MINUTES -> {
				view = minutes
				plural = R.plurals.minutes
				multiplier = 1000L * 60L
				lastUnit = !hourglass.showSeconds
			}
			Unit.SECONDS -> {
				view = seconds
				plural = R.plurals.seconds
				multiplier = 1000L
				lastUnit = true
			}

		}
		val amount = view.findViewById<View>(R.id.amount) as TextView
		val unit = view.findViewById<View>(R.id.unit) as TextView
		val visible = show && Math.abs(hourglass.millisRemaining) >= multiplier

		val value = Math.abs(millisLeftover.toDouble() / multiplier.toDouble())
		//        if (u==Unit.SECONDS) {
		//            Log.d(getClass().getName(), "DIV = " + ((double) millisLeftover / (double) multiplier));
		//            Log.d(getClass().getName(), "MULTI = " + (double)multiplier);
		//            Log.d(getClass().getName(), "MILLIS = " + (double)millisLeftover);
		//        }

		runOnUiThread {
			if (visible) {
				if (view.visibility != View.VISIBLE)
					view.visibility = View.VISIBLE
				if (!lastUnit || hourglass.decimalPlaces == 0) {
					amount.text = String.format(Locale.getDefault(), "%d", value.toInt())
					unit.text = resources.getQuantityString(plural, value.toInt(),"").trim()
				} else {
					amount.text = hourglass.decimalFormat.format(value)
					val quantity: Int = if (value > 1.0) Math.ceil(value).toInt() else if (value < 1.0) Math.floor(value).toInt() else 1
					unit.text = resources.getQuantityString(plural, quantity,"").trim()
					sleepInterval = multiplier / Math.pow(10.0, hourglass.decimalPlaces.toDouble()).toLong()
				}
			} else {
				view.visibility = View.GONE
			}
		}

		return if (visible) value.toLong() * multiplier else 0
	}

	private enum class Unit {
		SECONDS,
		MINUTES,
		HOURS,
		DAYS,
		YEARS
	}
}
