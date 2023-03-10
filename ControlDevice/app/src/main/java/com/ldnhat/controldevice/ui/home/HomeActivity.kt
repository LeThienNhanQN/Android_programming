package com.ldnhat.controldevice.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ldnhat.controldevice.R
import com.ldnhat.controldevice.ui.sensor.LightActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity(){

    private lateinit var speechRecognizer: SpeechRecognizer
    private val RecordAudioRequestCode = 1
    private var light : DatabaseReference? = null
    private var fan : DatabaseReference? = null
    private var lightTime:DatabaseReference? = null
    private var fanTime:DatabaseReference? = null
    private var lightData:String? = null
    private var fanData:String? = null
    private var lastSelectedHour = -1
    private var lastSelectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        val database = Firebase.database
        light = database.getReference("light_state")
        fan = database.getReference("fan_state")
        lightTime = database.getReference("light_time")
        fanTime = database.getReference("fan_time")

        light!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                error.details
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                lightData = snapshot.value as String
                handleStateOfLight(lightData!!)

            }

        })

        lightTime!!.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(snapshot: DataSnapshot) {
                val lightTimeData:Long = snapshot.value as Long

                if (lightTimeData < 0){
                    btn_time_light.text = "?????t Gi???"
                    btn_destroy_time_light.visibility = View.GONE
                }else{
                    val simpleDateFormat: DateFormat = SimpleDateFormat("HH:mm")
                    val date:Date = Date(lightTimeData)
                    btn_time_light.text = simpleDateFormat.format(date).toString()
                    btn_destroy_time_light.visibility = View.VISIBLE

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        fan!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                error.details
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                fanData = snapshot.value as String
                handleStateOfFan(fanData!!)
            }

        })

        fanTime!!.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeFanData:Long = snapshot.value as Long

                if (timeFanData < 0){
                    btn_time_fan.text = "?????t Gi???"
                    btn_destroy_time_fan.visibility = View.GONE
                }else{
                    val simpleDateFormat:DateFormat = SimpleDateFormat("HH:mm")
                    val date:Date = Date(timeFanData)
                    btn_time_fan.text = simpleDateFormat.format(date).toString()
                    btn_destroy_time_fan.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        speechRecognizer =  SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        btn_open_voice.setOnClickListener {
            startActivityForResult(intent, 20)
        }

        //handle time light
        handleTimeLight()

        //handle time fan
        handleTimeFan()

        btn_sensor.setOnClickListener {
            val it = Intent(this, LightActivity::class.java)
            startActivity(it)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleTimeLight(){

        btn_time_light.setOnClickListener {
            buttonSelectTime("LIGHT")
            btn_destroy_time_light.visibility = View.VISIBLE
        }

        btn_destroy_time_light.setOnClickListener {
            btn_destroy_time_light.visibility = View.GONE
            lightTime?.setValue(-1)
            btn_time_light.text = "?????t Gi???"
            sw_turn_light.isChecked = false
            light?.setValue("turn_off_light")

        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleTimeFan(){

        btn_time_fan.setOnClickListener {
            buttonSelectTime("FAN")
            btn_destroy_time_fan.visibility = View.VISIBLE
        }

        btn_destroy_time_fan.setOnClickListener {
            btn_destroy_time_fan.visibility = View.GONE
            fanTime?.setValue(-1)
            btn_time_fan.text = "?????t Gi???"
            sw_turn_fan.isChecked = false
            fan?.setValue("turn_off_fan")
        }
    }

    private fun handleStateOfLight(data: String){
        sw_turn_light.isChecked = data == "turn_on_light"
        sw_turn_light.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                light!!.setValue("turn_on_light")
                Toast.makeText(this, "???? b???t ????n", Toast.LENGTH_SHORT).show()
            }else if(!isChecked){
                light!!.setValue("turn_off_light")
                Toast.makeText(this, "???? t???t ????n", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun handleStateOfFan(data: String){
        sw_turn_fan.isChecked = data == "turn_on_fan"

        sw_turn_fan.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                fan!!.setValue("turn_on_fan")
                Toast.makeText(this, "???? b???t qu???t", Toast.LENGTH_SHORT).show()
            }else{
                fan!!.setValue("turn_off_fan")
                Toast.makeText(this, "???? t???t qu???t", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun buttonSelectTime(device : String){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        val calendar:Calendar = Calendar.getInstance()
        if (this.lastSelectedHour == -1){

            this.lastSelectedHour = calendar.get(Calendar.HOUR_OF_DAY)
            this.lastSelectedMinute = calendar.get(Calendar.MINUTE)
        }

        val timeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            lastSelectedHour = hourOfDay
            lastSelectedMinute = minute

            val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val year: String = calendar[Calendar.YEAR].toString()
            val month = (calendar[Calendar.MONTH] + 1).toString()
            val day = calendar[Calendar.DAY_OF_MONTH].toString()
            val seconds = calendar[Calendar.SECOND].toString()

            val date:Date? = simpleDateFormatter.parse(year+"-"+month+"-"+day+" "+hourOfDay+":"+minute+":"+seconds)
            val millionSeconds: Long = date!!.time
            if (device.equals("LIGHT")){
                btn_time_light.text = "$hourOfDay : $minute"
                lightTime?.setValue(millionSeconds)
                light?.setValue("turn_on_light")
                sw_turn_light.isChecked = true
            }else if (device.equals("FAN")){
                btn_time_fan.text = hourOfDay.toString() +" : "+minute
                fanTime?.setValue(millionSeconds)
                fan?.setValue("turn_on_fan")
                sw_turn_fan.isChecked = true
            }
        }

        val timePickerDialog = TimePickerDialog(
            this,
            timeSetListener,
            lastSelectedHour,
            lastSelectedMinute,
            true
        )

        timePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 20 && data != null){
            val intent: ArrayList<String>? =  data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voice = intent?.get(0)

            if(voice.equals("turn on light") || voice.equals("Turn on Light") || voice.equals("B???t ????n")
                || voice.equals("b???t ????n")){
                light?.setValue("turn_on_light")
            }else if(voice.equals("T???t ????n") || voice.equals("Turn off Light")) {
                light?.setValue("turn_off_light")
            }else if(voice.equals("Turn on Fan") || voice.equals("B???t Qu???t") || voice.equals("b???t qu???t")){
                fan?.setValue("turn_on_fan")
            }else if(voice.equals("Turn off Fan") || voice.equals("T???t Qu???t") || voice.equals("t???t qu???t")){
                fan?.setValue("turn_off_fan")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                this.RecordAudioRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.RecordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(
                this,
                "Permission Granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}