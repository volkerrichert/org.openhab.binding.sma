/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.sma.internal.hardware.devices;

import java.io.IOException;
import java.util.Date;

import org.openhab.binding.sma.internal.SmaBinding.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SolarInverter extends AbstractSmaDevice {
	private static final Logger logger = LoggerFactory
			.getLogger(SolarInverter.class);

	protected Device device;
	protected Data data;
	
	protected short pcktID = 1;
	
	public SolarInverter(Device device) {
		super();
		this.device = device;
	}

	protected abstract String getInverterData(InverterDataType type);
	
	public SmaSerial getSerial() {
		return data.serial;
	}


	public void setSerial(SmaSerial serial) {
		this.data.serial = serial;
	}


	@Override
	public String getValueAsString(LRIDefinition element) {
		InverterDataType data = element.getData();
		/*
		if (!this.hasValidValues(data)) {
			this.getInverterData(data);
		}
		
		if ((flags & InverterDataType.EnergyProduction.getValue()) == 0)
			this.getInverterData(InverterDataType.EnergyProduction);
		*/

		try {
			
			this.logon(device.isLoginAsInstaller()?SmaUserGroup.Installer:SmaUserGroup.User, device.getPassword());
			this.getInverterData(element.getData());
			this.logoff();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return "";
	}

	private boolean hasValidValues(InverterDataType data) {
		return false; //((flags & data.getValue()) != 0);
	}
	
	public static abstract class Data {

		public Data() {}
		
		protected String deviceName;
		protected SmaSerial serial;
		protected int netID;
		
		//protected short suSyID;
		//protected int serial;
		protected float btSignal;
		protected Date inverterTime;
		protected Date wakeupTime;
		protected Date sleepTime;
		protected long pdc1, pdc2;
		protected long udc1, udc2;
		protected long idc1, idc2;
		protected long pmax1, pmax2, pmax3;
		protected long totalPac;
		protected long pac1, pac2, pac3;
		protected long uac1, uac2, uac3;
		protected long iac1, iac2, iac3;
		protected long gridFreq;
		protected long operationTime;
		protected long feedInTime;
		protected long eToday;
		protected long eTotal;
		protected short modelID;
		protected String deviceType;
		protected String deviceClass;
		protected DeviceClass devClass;
		protected String swVersion; // "03.01.05.R"
		protected int deviceStatus;
		protected int gridRelayStatus;
		
		// Flag to signal which data is already loaded
		protected int flags;

		public int getNetID() {
			return netID;
		}

		public void setNetID(int netID) {
			this.netID = netID;
		}


		public void setSerial(SmaSerial smaSerial) {
			this.serial = smaSerial;
		}
		
		public SmaSerial getSerial() {
			return this.serial;
		}
		
		@Override
		public String toString() {
			return "deviceName=" + deviceName + ", netID=" + netID
					+ ", serial=" + serial;
		}

	}
}
