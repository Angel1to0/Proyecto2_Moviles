package com.example.proyecto2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

const val REQUEST_ENABLE_BT = 1;

/**Variable que verifica si el dispositivo posee comunicacion bluetooth*/
class MainActivity : AppCompatActivity() {

    //Variables para comunicacion Bluetooth
    /**Bluetooth Adapter*/
    lateinit var mBtAdapter: BluetoothAdapter // Con los ' : ', especificamos que es de tipo BluetoothAdapter
    var mAddressDevice: ArrayAdapter<String>? = null //Se guardan las direcciones de los dispositivos bluetooth
    var mNameDevices: ArrayAdapter<String>? = null //Se guardan los nombres de los dispositivos bluetooth

    companion object{
        var m_myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //Con el ' = ', asignamos el identificador bluetooth del HC-05 o HC-06 a la variable
        private var m_bluetoothSocket: BluetoothSocket? = null //generamos una variable bluetooth socket y le damos un valor nulo

        var m_isConnected: Boolean = false
        lateinit var m_address: String //generamos una variable de tipo String, que le asignamremos un valor despues, por eso ocupamos lateinit
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAddressDevice = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)

        val jbtOnBT = findViewById<Button>(R.id.btence)
        val jbtOffBT = findViewById<Button>(R.id.btapag)
        val jbtConnect = findViewById<Button>(R.id.btcon)

        val jbtDispBT = findViewById<Button>(R.id.btdis)
        val jbtSpinDisp = findViewById<Spinner>(R.id.spdisp)

        /*variable del seekbar*/
        val sbv = findViewById<SeekBar>(R.id.sbv)
        val sbd = findViewById<SeekBar>(R.id.sbd)

        /** ----------------------------------------------------------------------------- */

        //Cuando se inicia la App entra aqui
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ){result ->
            if (result.resultCode == REQUEST_ENABLE_BT){
                Log.i("MainActivity","ACTIVIDAD REGISTRADA")
            }
        }

        /**Inicializacion del Bluetooth Adapter*/
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        /**Checar si esta encendido o apagado*/
        if (mBtAdapter == null){
            Toast.makeText(this,"Bluetooth no esta disponible en este dispositivo",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this,"Bluetooth esta disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }

        /** --------------------------------------------------------------------------------------- */

        /**Boton Encender bluetooth*/
        jbtOnBT.setOnClickListener{
            if (mBtAdapter.isEnabled){
                //Si ya esta activado
                Toast.makeText(this,"El Bluetooth ya esta activado", Toast.LENGTH_LONG).show()
            }else{
                //Encender Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,Manifest.permission.BLUETOOTH_CONNECT
                    )!=PackageManager.PERMISSION_GRANTED
                ){
                    Log.i("MainActivity","ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        }

        /**Boton apagar Bluetooth*/
        jbtOffBT.setOnClickListener{
            if(!mBtAdapter.isEnabled){
                //Si ya esta desactivado
                Toast.makeText(this,"Bluetooth ya se encuentra desactivado",Toast.LENGTH_LONG).show()
            }else{
                //Encender Bluetooth
                mBtAdapter.disable()
                Toast.makeText(this,"Se ha desactivado el bluetooth",Toast.LENGTH_LONG).show()
            }
        }

        /**Boton dispositivos emparejados*/
        jbtDispBT.setOnClickListener{
            if(mBtAdapter.isEnabled){
                //procedo a buscar a todos los dispositivos que han sido vinculados con el telefono
                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                //Se cargan las direcciones de un dispositivo
                mAddressDevice!!.clear()
                //nombre de un dispositivo
                mNameDevices!!.clear()

                //Separo el nombre y la direccion del dispositivo
                pairedDevices?.forEach{device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    mAddressDevice!!.add(deviceHardwareAddress)
                    mNameDevices!!.add(deviceName)
                }

                //Actualizo los dispositivos
                jbtSpinDisp.setAdapter(mNameDevices)
            }else{
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevice!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this,"Primero vincule un dispositivo Bluetooth",Toast.LENGTH_LONG).show()
            }
        }

        /**Boton Conectar*/
        jbtConnect.setOnClickListener{
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValSpin = jbtSpinDisp.selectedItemPosition
                    m_address = mAddressDevice!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_address, Toast.LENGTH_LONG).show()
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    //creamos un socket bluetooth, que permite intercambiar informacion
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this,"CONEXION EXITOSA",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","CONEXION EXITOSA")
            }catch (e: IOException){
                e.printStackTrace()
                Toast.makeText(this,"ERROR DE CONEXION",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","ERROR DE CONEXION")
            }
        }
        /** --------------------------------------------------------------------------------------------------------------------------- */

        /**Desde aqui empieza el codigo de las barras*/

        sbv.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
            var speed: Int = 0
            var output: String? = null

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                val maxSeekBarValue = sbv.max
                val maxMotorSpeed = 255

                speed = ((progress.toFloat()/maxSeekBarValue) * maxMotorSpeed).toInt()

                output = "M$speed"

                sendCommand(output!!)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        /**Codigo del slider de la intensidad de LED
         * se utiliza object, debido a que instancio una interfaz y no una clase como tal*/
        sbd.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
            var i: Int = 0
            var position: Int = 0
            var output: String? = null
            override fun onProgressChanged(sb: SeekBar?, n: Int, b: Boolean) {
                i = n
                /**Se realiza un mapeo de 0-100 a 0-180, que corresponde al randgo de posicion del servomotor. Creamos el comando en forma de cadena
                 * y lo enviamos mediante 'sendCommand'*/
                position = ((n.toFloat() / 100) * 180).toInt() // Mapear el valor de 0-100 a 0-180 para la posición del servomotor
                output = "F$position" // Crear el comando para enviar por Bluetooth
                sendCommand(output!!)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                sbd.progress = sbd.max/2
                position = ((50.00/100) * 180).toInt()
                output = "F$position"
                sendCommand(output!!)
            }

        })
    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}