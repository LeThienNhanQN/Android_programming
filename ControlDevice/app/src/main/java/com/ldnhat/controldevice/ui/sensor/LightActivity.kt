package com.ldnhat.controldevice.ui.sensor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ldnhat.controldevice.R
import kotlinx.android.synthetic.main.activity_light.*

class LightActivity : AppCompatActivity() {

    private var lightSensorState:DatabaseReference? = null
    private var lightSensorValue:Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light)

        val database = Firebase.database

        lightSensorState = database.getReference("light_sensor_state")

        lightSensorState!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                lightSensorValue = snapshot.value as Long
                txt_light_value.text = lightSensorValue.toString()

                if (lightSensorValue!! > 500){
                    txt_light_state.text = "Đèn: tắt"
                }else{
                    txt_light_state.text = "Đèn: bật"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error.details
            }

        })

        previous.setOnClickListener {
            finish()
        }
    }
}