package org.openhab.binding.sma.internal.hardware.devices;

import java.io.IOException;
import java.net.InetAddress;

import org.openhab.binding.sma.internal.SmaBinding;

public class EthernetSolarInverter extends SolarInverter {

	public EthernetSolarInverter(SmaBinding.Device dev) {
		super(dev);
	}

	@Override
	public void init() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getValueAsString(LRIDefinition element) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected String getInverterData(InverterDataType energyproduction) {
		return "";
		// TODO Auto-generated method stub
	}

	@Override
	public void logon(SmaUserGroup userGroup, String password)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logoff() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
