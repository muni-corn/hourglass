package com.musicaflight.hourglass

import android.content.ContentValues
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Hourglass(var title: String, timestamp: Long) : Parcelable, Comparable<Hourglass> {
	var timestamp: Long = 0
		set(value) {
			calendar.timeInMillis = value
			field = value
		}
	private val calendar: Calendar = Calendar.getInstance()


	init {
		this.timestamp = timestamp
	}

	var decimalPlaces: Int = 0
		set(value) {
			field = value
			var s = "0"
			if (value > 0) {
				s += "."
				for (i in 0 until value) {
					s += "0"
				}
			}
			val nf = NumberFormat.getNumberInstance()
			secondFormat = nf as DecimalFormat
			secondFormat.applyPattern(s)
		}
	private var secondFormat = DecimalFormat("0")
	var imageId: String? = null
	var showYears = true
	var showDays = true
	var showHours = true
	var showMinutes = true
	var showSeconds = true

	var id: Long = -1
		set(value) {
			if (id == -1L) {
				field = value
			} else
				throw IllegalStateException("ID ($id) of Hourglass \"$title\" has already been assigned.")
		}

	constructor() : this("", (Math.ceil((System.currentTimeMillis() / (24 * 60 * 60 * 1000)).toDouble()) * 24 * 60 * 60 * 1000).toLong())

	private constructor(`in`: Parcel) : this(`in`.readString(), `in`.readLong()) {
		id = `in`.readLong()
		decimalPlaces = `in`.readInt()
		val booleans = BooleanArray(5)
		`in`.readBooleanArray(booleans)
		showYears = booleans[0]
		showDays = booleans[1]
		showHours = booleans[2]
		showMinutes = booleans[3]
		showSeconds = booleans[4]
		imageId = `in`.readString()
	}

	val millisRemaining: Long
		get() = timestamp - System.currentTimeMillis()

	fun getShortDescription(context: Context): String {
		val res = context.resources
		var millis = millisRemaining
		val past: Boolean
		if (millis <= 0) {
			past = true
			millis *= -1
		} else past = false
		val v:Int
		return when (millis) {
			in 0 until minuteInMillis -> if (past) res.getString(R.string.now) else res.getString(R.string.less_than_minute_remaining)
			in minuteInMillis until hourInMillis -> {
				v = Math.ceil(millis.toDouble() / minuteInMillis.toDouble()).toInt()
				if (past) res.getString(R.string.time_ago, res.getQuantityString(R.plurals.minutes, v, v)) else res.getString(R.string.remaining, res.getQuantityString(R.plurals.minutes, v, v))
			}
			in hourInMillis until dayInMillis -> {
				v = Math.ceil(millis.toDouble() / hourInMillis.toDouble()).toInt()
				if (past) res.getString(R.string.time_ago, res.getQuantityString(R.plurals.hours, v, v)) else res.getString(R.string.remaining, res.getQuantityString(R.plurals.hours, v, v))
			}
			in dayInMillis until yearInMillis -> {
				v = Math.ceil(millis.toDouble() / dayInMillis.toDouble()).toInt()
				if (past) res.getString(R.string.time_ago, res.getQuantityString(R.plurals.days, v,v)) else res.getString(R.string.remaining, res.getQuantityString(R.plurals.days, v,v))
			}
			else -> {
				v=Math.ceil(millis.toDouble() / yearInMillis.toDouble()).toInt()
				if (past) res.getString(R.string.more_than_ago, res.getQuantityString(R.plurals.years, v, v)) else res.getString(R.string.less_than_remaining, res.getQuantityString(R.plurals.years, v, v))
			}
		}
	}

	private val secondInMillis = 1000L
	private val minuteInMillis = secondInMillis * 60L
	private val hourInMillis = minuteInMillis * 60L
	private val dayInMillis = hourInMillis * 24L
	private val yearInMillis = dayInMillis * 365L

	override fun describeContents(): Int = 0

	override fun writeToParcel(out: Parcel, flags: Int) {
		out.writeString(title)
		out.writeLong(timestamp)
		out.writeLong(id)
		out.writeInt(this.decimalPlaces)
		out.writeBooleanArray(booleanArrayOf(showYears, showDays, showHours, showMinutes, showSeconds))
		out.writeString(imageId)
	}

	val decimalFormat: DecimalFormat
		get() {
			secondFormat.roundingMode = RoundingMode.FLOOR
			return secondFormat
		}

	val contentValues: ContentValues
		get() {
			if (id == -1L) throw IllegalStateException("This hourglass does not have an id.")
			return ContentValues().apply {
				put(HourglassContract._ID, id)
				put(HourglassContract.COLUMN_TITLE, title)
				put(HourglassContract.COLUMN_TIMESTAMP, timestamp)
				put(HourglassContract.COLUMN_SHOW_YEARS, showYears)
				put(HourglassContract.COLUMN_SHOW_DAYS, showDays)
				put(HourglassContract.COLUMN_SHOW_HOURS, showHours)
				put(HourglassContract.COLUMN_SHOW_MINUTES, showMinutes)
				put(HourglassContract.COLUMN_SHOW_SECONDS, showSeconds)
				put(HourglassContract.COLUMN_DECIMAL_PLACES, decimalPlaces)
				put(HourglassContract.COLUMN_IMAGE_ID, imageId)
			}
		}


	val date: Date
		get() = Date(timestamp)

	val hourOfDay: Int
		get() = calendar.get(Calendar.HOUR_OF_DAY)

	val minute: Int
		get() = calendar.get(Calendar.MINUTE)

	val year: Int
		get() = calendar.get(Calendar.YEAR)

	val month: Int
		get() = calendar.get(Calendar.MONTH)

	val dayOfMonth: Int
		get() = calendar.get(Calendar.DAY_OF_MONTH)


	override fun compareTo(other: Hourglass): Int {
		return when {
			Math.abs(this.millisRemaining) > Math.abs(other.millisRemaining) -> 1
			else -> -1
		}
	}

	companion object CREATOR : Parcelable.Creator<Hourglass> {
		override fun createFromParcel(parcel: Parcel): Hourglass = Hourglass(parcel)

		override fun newArray(size: Int): Array<Hourglass?> = arrayOfNulls(size)

		val DATE_FORMAT = SimpleDateFormat("MMMM d, y", Locale.getDefault())
		val TIME_FORMAT = SimpleDateFormat("h:mm aa", Locale.getDefault())
	}
}
