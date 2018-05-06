package com.musicaflight.hourglass

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.ParseException
import java.util.*

open class EditHourglassActivity : AppCompatActivity() {
	private var date = Calendar.getInstance()
	private var time = Calendar.getInstance()
	lateinit var hourglass: Hourglass
	private lateinit var dec: TextView
	private lateinit var yearSwitch: Switch
	private lateinit var daySwitch: Switch
	private lateinit var hourSwitch: Switch
	private lateinit var minuteSwitch: Switch
	private lateinit var secondSwitch: Switch
	private lateinit var dateField: TextInputEditText
	private lateinit var timeField: TextInputEditText
	private lateinit var titleField: TextInputEditText


	override final fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_hourglass)

		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)

		initHourglass()

		titleField = findViewById(R.id.title)
		titleField.setText(hourglass.title)

		dateField = findViewById(R.id.date)
		dateField.setOnClickListener { v ->
			val c = Calendar.getInstance()

			val dialog = DatePickerDialog(this@EditHourglassActivity, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
				if (view.isShown) {
					c.set(year, month, dayOfMonth)
					(v as TextInputEditText).setText(Hourglass.DATE_FORMAT.format(c.time))
					(findViewById<TextInputLayout>(R.id.date_layout)).error = null
				}
			}, hourglass.year, hourglass.month, hourglass.dayOfMonth)

			dialog.show()
		}
		dateField.setText(Hourglass.DATE_FORMAT.format(hourglass.date))

		timeField = findViewById(R.id.time)
		timeField.setOnClickListener { v ->
			val c = Calendar.getInstance()

			val dialog = TimePickerDialog(this@EditHourglassActivity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				if (view.isShown) {
					c.set(Calendar.HOUR_OF_DAY, hourOfDay)
					c.set(Calendar.MINUTE, minute)
					(v as EditText).setText(Hourglass.TIME_FORMAT.format(c.time))
				}
			}, hourglass.hourOfDay, hourglass.minute, false)
			dialog.show()
		}
		timeField.setText(Hourglass.TIME_FORMAT.format(hourglass.date))

		yearSwitch = findViewById(R.id.show_years)
		daySwitch = findViewById(R.id.show_days)
		hourSwitch = findViewById(R.id.show_hours)
		minuteSwitch = findViewById(R.id.show_minutes)
		secondSwitch = findViewById(R.id.show_seconds)

		yearSwitch.isChecked = hourglass.showYears
		daySwitch.isChecked = hourglass.showDays
		hourSwitch.isChecked = hourglass.showHours
		minuteSwitch.isChecked = hourglass.showMinutes
		secondSwitch.isChecked = hourglass.showSeconds

		dec = findViewById(R.id.decimal_places)
		dec.text = hourglass.decimalPlaces.toString()
	}

	open fun initHourglass() {
		hourglass = intent.getParcelableExtra("hourglass")
		supportActionBar!!.title = "Edit \"" + hourglass.title + "\""
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.new_hourglass, menu)
		return super.onCreateOptionsMenu(menu)
	}

	@Suppress("UNUSED_PARAMETER")
	fun increaseDecimalPlaces(view: View) {
		val currentDec = hourglass.decimalPlaces
		if (currentDec >= MAX_DECIMAL_PLACES) {
			hourglass.decimalPlaces = MAX_DECIMAL_PLACES
			dec.text = hourglass.decimalPlaces.toString()
			return
		}
		hourglass.decimalPlaces = currentDec + 1
		dec.text = hourglass.decimalPlaces.toString()
	}

	@Suppress("UNUSED_PARAMETER")
	fun decreaseDecimalPlaces(view: View) {
		val currentDec = hourglass.decimalPlaces
		if (currentDec <= 0) {
			hourglass.decimalPlaces = 0
			dec.text = hourglass.decimalPlaces.toString()
			return
		}
		hourglass.decimalPlaces = currentDec - 1
		dec.text = hourglass.decimalPlaces.toString()
	}

	fun sendNotification(view: View) {
		val builder = NotificationCompat.Builder(this, "hourglass_channel")
		val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel("hourglass_channel", getString(R.string.countdowns_channel_name), NotificationManager.IMPORTANCE_HIGH)
			notificationManager.createNotificationChannel(notificationChannel)
		}
		builder.setContentTitle("Hello from ${hourglass.title}!").setDefaults(NotificationCompat.DEFAULT_ALL).setColor(ContextCompat.getColor(this, R.color.colorPrimary)).setPriority(NotificationCompat.PRIORITY_HIGH).setContentText("Date is set for ${DateFormat.getInstance().format(hourglass.date)}").setChannelId("hourglass_channel").setAutoCancel(true).setSmallIcon(R.drawable.ic_hourglass_empty_white_24dp)
		notificationManager.notify(hourglass.id.toInt(), builder.build());
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.done -> {
				hourglass.showYears = yearSwitch.isChecked
				hourglass.showDays = daySwitch.isChecked
				hourglass.showHours = hourSwitch.isChecked
				hourglass.showMinutes = minuteSwitch.isChecked
				hourglass.showSeconds = secondSwitch.isChecked
				if (hourglass.showYears || hourglass.showDays || hourglass.showHours || hourglass.showMinutes || hourglass.showSeconds) {
					var title = (titleField).text.toString()

					val dateString = (dateField).text.toString()
					val timeString = (timeField).text.toString()
					if (title.trim { it <= ' ' } == "") {
						title = dateString
					}
					try {
						date.time = Hourglass.DATE_FORMAT.parse(dateString)
						if (timeString.trim { it <= ' ' } != "") {
							time = Calendar.getInstance()
							time.time = Hourglass.TIME_FORMAT.parse(timeString)
						}
					} catch (e: ParseException) {

					}

					hourglass.title = title
					hourglass.timestamp = date.timeInMillis + (time.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000) + (time.get(Calendar.MINUTE) * 60 * 1000)

					if (hourglass.id == -1L) {
						Content.addHourglass(hourglass)
					} else {
						Content.updateHourglass(hourglass)
						setResult(Activity.RESULT_OK, Intent().putExtra("hourglass", hourglass))
					}
					finish()
				} else {
					AlertDialog.Builder(this).setMessage(R.string.show_at_least_one_unit).setNegativeButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }.show()
				}
			}
			android.R.id.home -> setResult(Activity.RESULT_CANCELED)
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onDestroy() = super.onDestroy()

	@Suppress("UNUSED_PARAMETER")
	fun changeBackground(view: View) {
		val intent = Intent(Intent.ACTION_GET_CONTENT)
		intent.type = "image/*"
		intent.addCategory(Intent.CATEGORY_OPENABLE)
		startActivityForResult(intent, REQUEST_OPEN_IMAGE)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		super.onActivityResult(requestCode, resultCode, intentData)
		if (requestCode == REQUEST_OPEN_IMAGE && resultCode == Activity.RESULT_OK) {
			if (intentData != null) {
				val millis = System.currentTimeMillis().toString()
				if (hourglass.imageId != null) {
					val oldImg = File(Content.getImagePathFromImageId(this, hourglass.imageId!!))
					if (oldImg.delete()) {
						Log.i(javaClass.name, "Image file successfully deleted")
					} else {
						Log.e(javaClass.name, "Image deletion failed! " + if (oldImg.exists()) "Image exists" else "Image reportedly does not exist")
						//                        return;
					}
				}
				val uri = intentData.data
				try {
					val `in` = contentResolver.openInputStream(uri!!)
					val out = FileOutputStream(File(Content.getImagePathFromImageId(this, millis)))
					assert(`in` != null)
					val data = ByteArray(`in`!!.available())
					`in`.read(data)
					out.write(data)
				} catch (e: java.io.IOException) {
					e.printStackTrace()
				}

				hourglass.imageId = millis
			}
		}
	}

	companion object {
		val MAX_DECIMAL_PLACES = 3
		val REQUEST_OPEN_IMAGE = 1
	}
}