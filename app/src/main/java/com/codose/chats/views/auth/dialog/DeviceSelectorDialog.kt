package com.codose.chats.views.auth.dialog

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codose.chats.R
import com.codose.chats.model.ConnectedDevice
import com.codose.chats.utils.BluetoothConstants.EXTRA_DEVICE_ADDRESS
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.views.auth.adapter.BluetoothClickListener
import com.codose.chats.views.auth.adapter.BluetoothDeviceAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_selector.*
import timber.log.Timber

class DeviceSelectorDialog : BottomSheetDialogFragment() {
    private lateinit var pairedAdapter : BluetoothDeviceAdapter
    private lateinit var availableAdapter : BluetoothDeviceAdapter
    private val foundDevices = arrayListOf<ConnectedDevice>()
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Timber.v("Receiver Started")
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                Timber.v("Receiver Found")

                // Get the BluetoothDevice object from the Intent
                val device = intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed
                // already
                if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                    Timber.v(device?.name)
                    if(device !=null){
                        try{
                            val newDevice = ConnectedDevice(device.name,device.address)

                            if(!foundDevices.contains(newDevice)){
                                foundDevices.add(newDevice)
                            }
                        }catch (t : Throwable){

                        }

                        availableAdapter.submitList(foundDevices)
                    }

                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                try {
                    scanProgress.hide()
                    scanDeviceBtn.isEnabled = true
                    scanDeviceBtn.text = "Scan for devices"
                }catch (e : Exception){
                    Timber.v(e)
                }
                Timber.v("Receiver Finished")
            }
        }
    }
    private lateinit var mBtAdapter : BluetoothAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_selector, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("onViewCreated")
        scanProgress.hide()
        //Adapter for the recyclerView
        availableAdapter = BluetoothDeviceAdapter(BluetoothClickListener {
            setDevice(it)
        })

        pairedAdapter = BluetoothDeviceAdapter(BluetoothClickListener {
            setDevice(it)
        })
        pairedDeviceRV.adapter = pairedAdapter
        availableDeviceRV.adapter = availableAdapter

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(mReceiver, filter)
//
//        // Register for broadcasts when discovery has finished
//        filter = IntentFilter()
//        requireActivity().registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get a set of currently paired devices
        val pairedDevices: Set<BluetoothDevice> = mBtAdapter.bondedDevices

        // If there are paired devices, add each one to the BluetoothAdapter
        if (pairedDevices.isNotEmpty()) {
            val allPaired = ArrayList<ConnectedDevice>()
            try {
                pairedDevices.forEach {
                    val device = ConnectedDevice(it.name, it.address)
                    allPaired.add(device)
                }
            }catch (t : Throwable){
                Timber.e(t)
            }

            pairedAdapter.submitList(allPaired)
        } else {
            val noDevices = "None Paired"
        }

        scanDeviceBtn.setOnClickListener {
            doDiscovery()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery()
        }

        // Unregister broadcast listeners

        // Unregister broadcast listeners
        requireActivity().unregisterReceiver(mReceiver)
    }

    private fun doDiscovery() {
        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering) {
            mBtAdapter.cancelDiscovery()
            Timber.v("Search Stop")
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery()
        scanProgress.show()
        scanDeviceBtn.isEnabled = false
        scanDeviceBtn.text = "Scanning"
        Timber.v("Search Starting : ${mBtAdapter.name} ${mBtAdapter.address}")
    }


    private fun setDevice(device: ConnectedDevice) {
        mBtAdapter.cancelDiscovery()
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, device.deviceAddress)
        targetFragment?.onActivityResult(targetRequestCode, RESULT_OK, intent)
        dismiss()
    }

    companion object {
        fun newInstance(): DeviceSelectorDialog =
            DeviceSelectorDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }
}