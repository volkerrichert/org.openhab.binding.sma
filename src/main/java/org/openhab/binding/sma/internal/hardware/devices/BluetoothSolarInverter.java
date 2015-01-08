package org.openhab.binding.sma.internal.hardware.devices;

import java.io.IOException;

import org.openhab.binding.sma.internal.SmaBinding.Device;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.InverterDataType;
import org.openhab.binding.sma.internal.layers.Bluetooth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothSolarInverter extends BluetoothSolarInverterPlant {
	private static final Logger logger = LoggerFactory
			.getLogger(BluetoothSolarInverter.class);
	
	public BluetoothSolarInverter(Device device) {
		super(device);
	}
/*
	public BluetoothSolarInverter(String address) {
		super(address);
	}
	*/
	@Override
	public void init() throws IOException {
		this.layer = new Bluetooth(device.getBTAdress());
	}
	
}
