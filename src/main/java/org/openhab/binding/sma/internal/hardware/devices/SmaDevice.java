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
import java.util.HashMap;
import java.util.List;

import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.LRIDefinition;
import org.openhab.core.events.EventPublisher;

import com.sun.swing.internal.plaf.synth.resources.synth;

public interface SmaDevice {

	public static final short NaN_S16 = (short) 0x8000;		// "Not a Number" representation for SHORT (converted to 0)
	public static final short NaN_U16 = (short) 0xFFFF;		// "Not a Number" representation for USHORT (converted to 0)
	public static final int NaN_S32 = 0x80000000;				// "Not a Number" representation for LONG (converted to 0)
	public static final int NaN_U32 = 0xFFFFFFFF;				// "Not a Number" representation for ULONG (converted to 0)
	public static final long NaN_S64 = 0x8000000000000000L;		// "Not a Number" representation for LONGLONG (converted to 0)
	public static final long NaN_U64 = 0xFFFFFFFFFFFFFFFFL;		// "Not a Number" representation for ULONGLONG (converted to 0)
	
	public static final String strWatt = "{}: {} (W)   {}";
	public static final String strVolt = "{}: {} (V)   {}";
	public static final String strAmp  = "{}: {} (A)   {}";
	public static final String strkWh  = "{}: {} (kWh) {}";
	public static final String strHour = "{}: {} (h)   {}";
    
	public enum InverterDataType {
		None                (0     , 0         , 0         , 0), // undefined
		EnergyProduction	(1 <<  0, 0x54000200, 0x00260100, 0x002622FF), // SPOT_ETODAY, SPOT_ETOTAL
		SpotDCPower			(1 <<  1, 0x53800200, 0x00251E00, 0x00251EFF), // SPOT_PDC1, SPOT_PDC2
		SpotDCVoltage		(1 <<  2, 0x53800200, 0x00451F00, 0x004521FF), // SPOT_UDC1, SPOT_UDC2, SPOT_IDC1, SPOT_IDC2
		SpotACPower			(1 <<  3, 0x51000200, 0x00464000, 0x004642FF), // SPOT_PAC1, SPOT_PAC2, SPOT_PAC3
		SpotACVoltage		(1 <<  4, 0x51000200, 0x00464800, 0x004652FF), // SPOT_UAC1, SPOT_UAC2, SPOT_UAC3, SPOT_IAC1, SPOT_IAC2, SPOT_IAC3
		SpotGridFrequency	(1 <<  5, 0x51000200, 0x00465700, 0x004657FF), // SPOT_FREQ
		MaxACPower			(1 <<  6, 0x51000200, 0x00411E00, 0x004120FF), // INV_PACMAX1, INV_PACMAX2, INV_PACMAX3
		MaxACPower2			(1 <<  7, 0x51000200, 0x00832A00, 0x00832AFF), // INV_PACMAX1_2
		SpotACTotalPower	(1 <<  8, 0x51000200, 0x00263F00, 0x00263FFF), // SPOT_PACTOT
		TypeLabel			(1 <<  9, 0x58000200, 0x00821E00, 0x008220FF), // INV_NAME, INV_TYPE, INV_CLASS
		OperationTime		(1 << 10, 0x54000200, 0x00462E00, 0x00462FFF), // SPOT_OPERTM, SPOT_FEEDTM
		SoftwareVersion		(1 << 11, 0x58000200, 0x00823400, 0x008234FF), // INV_SWVERSION
		DeviceStatus		(1 << 12, 0x51800200, 0x00214800, 0x002148FF), // INV_STATUS
		GridRelayStatus		(1 << 13, 0x51800200, 0x00416400, 0x004164FF), // INV_GRIDRELAY
		BatteryChargeStatus	(1 << 14, 0x51000200, 0x00295A00, 0x00295AFF), //
		BatteryInfo			(1 << 15, 0x51000200, 0x00491E00, 0x00495DFF), //
		InverterTemperature	(1 << 16, 0x52000200, 0x00237700, 0x002377FF); //
		
		private final int value;
		private final int command;
		private final int first;
		private final int last;

		private InverterDataType(int value) {
			this(value, 0, 0, 0);
		}

		private InverterDataType(int value, int command, int first, int last) {
			this.value = value;
			this.command = command;
			this.first = first;
			this.last = last;
		}

		private static HashMap<Integer, SmaDevice.InverterDataType> map;
		public static InverterDataType fromOrdinal(int i) {
			if (map == null) {
				map = new HashMap<Integer, SmaDevice.InverterDataType>(
						SmaDevice.InverterDataType.values().length);
				for (SmaDevice.InverterDataType e : SmaDevice.InverterDataType
						.values()) {
					map.put(e.getValue(), e);
				}
			}
			return map.get(i);
		}

		public int getValue() {
			return value;
		}
		
		public int getCommand() {
			return command;
		}

		public int getFirst() {
			return first;
		}

		public int getLast() {
			return last;
		}
	}

	public enum DeviceClass {
		AllDevices(8000), // DevClss0
		SolarInverter(8001), // DevClss1
		WindTurbineInverter(8002), // DevClss2
		BatteryInverter(8007), // DevClss7
		Consumer(8033), // DevClss33
		SensorSystem(8064), // DevClss64
		ElectricityMeter(8065), // DevClss65
		CommunicationProduct(8128); // DevClss128

		private final int value;

		private DeviceClass(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum SmaUserGroup {
		// User Group
		User(0x07), Installer(0x0A);
		private final int value;

		private SmaUserGroup(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum LRIDefinition {
		OperationHealth			(0x00214800, "INV_STATUS", InverterDataType.DeviceStatus), // *08* Condition (aka INV_STATUS)
		CoolsysTmpNom			(0x00237700), // *40* Operating condition temperatures
		DcMsWatt1				(0x00251E00, "SPOT_PDC1", InverterDataType.SpotDCPower, 1), // *40* DC power input (aka SPOT_PDC1 / SPOT_PDC2)
		DcMsWatt2				(0x00251E00, "SPOT_PDC2", InverterDataType.SpotDCPower, 2), // *40* DC power input (aka SPOT_PDC1 / SPOT_PDC2)
		MeteringTotWhOut		(0x00260100, "SPOT_ETOTAL", InverterDataType.EnergyProduction), // *00* Total yield (aka SPOT_ETOTAL)
		MeteringDyWhOut			(0x00262200, "SPOT_ETODAY", InverterDataType.EnergyProduction),  // *00* Day yield (aka SPOT_ETODAY)
		GridMsTotW				(0x00263F00, "SPOT_PACTOTAL", InverterDataType.SpotACTotalPower), // *40* Power (aka SPOT_PACTOT)
		BatChaStt				(0x00295A00, "BAT_STATUS", InverterDataType.BatteryChargeStatus), // *00* Current battery charge status
		OperationHealthSttOk	(0x00411E00, "INV_PACMAX1", InverterDataType.MaxACPower), // *00* Nominal power in Ok Mode (aka INV_PACMAX1)
		OperationHealthSttWrn	(0x00411F00, "INV_PACMAX2", InverterDataType.MaxACPower), // *00* Nominal power in Warning Mode (aka INV_PACMAX2)
		OperationHealthSttAlm	(0x00412000, "INV_PACMAX3", InverterDataType.MaxACPower), // *00* Nominal power in Fault Mode (aka INV_PACMAX3)
		OperationGriSwStt		(0x00416400, "INV_GRIDRELAY", InverterDataType.GridRelayStatus), // *08* Grid relay/contactor (aka INV_GRIDRELAY)
		OperationRmgTms			(0x00416600), // *00* Waiting time until feed-in
		DcMsVol1				(0x00451F00, "SPOT_UDC1", InverterDataType.SpotDCVoltage, 1), // *40* DC voltage input (aka SPOT_UDC1  SPOT_UDC2)
		DcMsVol2				(0x00451F00, "SPOT_UDC1", InverterDataType.SpotDCVoltage, 2), // *40* DC voltage input (aka SPOT_UDC1  SPOT_UDC2)
		DcMsAmp1				(0x00452100, "SPOT_IDC1", InverterDataType.SpotDCVoltage, 1), // *40* DC current input (aka SPOT_IDC1 /SPOT_IDC2)
		DcMsAmp2				(0x00452100, "SPOT_IDC2", InverterDataType.SpotDCVoltage, 2), // *40* DC current input (aka SPOT_IDC1 /SPOT_IDC2)
		MeteringPvMsTotWhOut	(0x00462300), // *00* PV generation counter reading
		MeteringGridMsTotWhOut	(0x00462400), // *00* Grid feed-in counter reading
		MeteringGridMsTotWhIn	(0x00462500), // *00* Grid reference counter reading
		MeteringCsmpTotWhIn		(0x00462600), // *00* Meter reading consumption meter
		MeteringGridMsDyWhOut	(0x00462700), // *00* ?
		MeteringGridMsDyWhIn	(0x00462800), // *00* ?
		MeteringTotOpTms		(0x00462E00, "SPOT_OPERTM", InverterDataType.OperationTime), // *00* Operating time (aka SPOT_OPERTM)
		MeteringTotFeedTms		(0x00462F00, "SPOT_FEEDTM", InverterDataType.OperationTime), // *00* Feed-in time (aka SPOT_FEEDTM)
		MeteringGriFailTms		(0x00463100), // *00* Power outage
		MeteringWhIn			(0x00463A00), // *00* Absorbed energy
		MeteringWhOut			(0x00463B00), // *00* Released energy
		MeteringPvMsTotWOut		(0x00463500), // *40* PV power generated
		MeteringGridMsTotWOut	(0x00463600), // *40* Power grid feed-in
		MeteringGridMsTotWIn	(0x00463700), // *40* Power grid reference
		MeteringCsmpTotWIn		(0x00463900), // *40* Consumer power
		GridMsWphsA				(0x00464000, "SPOT_PAC1", InverterDataType.SpotACPower), // *40* Power L1 (aka SPOT_PAC1)
		GridMsWphsB				(0x00464100, "SPOT_PAC2", InverterDataType.SpotACPower), // *40* Power L2 (aka SPOT_PAC2)
		GridMsWphsC				(0x00464200, "SPOT_PAC3", InverterDataType.SpotACPower), // *40* Power L3 (aka SPOT_PAC3)
		GridMsPhVphsA			(0x00464800, "SPOT_UAC1", InverterDataType.SpotACVoltage), // *00* Grid voltage phase L1 (aka SPOT_UAC1)
		GridMsPhVphsB			(0x00464900, "SPOT_UAC2", InverterDataType.SpotACVoltage), // *00* Grid voltage phase L2 (aka SPOT_UAC2)
		GridMsPhVphsC			(0x00464A00, "SPOT_UAC3", InverterDataType.SpotACVoltage), // *00* Grid voltage phase L3 (aka SPOT_UAC3)
		GridMsAphsA_1			(0x00465000, "SPOT_IAC1", InverterDataType.SpotACVoltage), // *00* Grid current phase L1 (aka SPOT_IAC1)
		GridMsAphsB_1			(0x00465100, "SPOT_IAC2", InverterDataType.SpotACVoltage), // *00* Grid current phase L2 (aka SPOT_IAC2)
		GridMsAphsC_1			(0x00465200, "SPOT_IAC3", InverterDataType.SpotACVoltage), // *00* Grid current phase L3 (aka SPOT_IAC3)
		GridMsAphsA				(0x00465300), // *00* Grid current phase L1 (aka SPOT_IAC1_2)
		GridMsAphsB				(0x00465400), // *00* Grid current phase L2 (aka SPOT_IAC2_2)
		GridMsAphsC				(0x00465500), // *00* Grid current phase L3 (aka SPOT_IAC3_2)
		GridMsHz				(0x00465700, "FREQ", InverterDataType.SpotGridFrequency), // *00* Grid frequency (aka SPOT_FREQ)
		MeteringSelfCsmpSelfCsmpWh		(0x0046AA00), // *00* Energy consumed internally
		MeteringSelfCsmpActlSelfCsmp	(0x0046AB00), // *00* Current self-consumption
		MeteringSelfCsmpSelfCsmpInc		(0x0046AC00), // *00* Current rise in self-consumption
		MeteringSelfCsmpAbsSelfCsmpInc	(0x0046AD00), // *00* Rise in self-consumption
		MeteringSelfCsmpDySelfCsmpInc	(0x0046AE00), // *00* Rise in self-consumption today
		BatDiagCapacThrpCnt		(0x00491E00), // *40* Number of battery charge throughputs
		//TODO Check battery data assoc
		BatDiagTotAhIn			(0x00492600, "BAT_CHARGE", InverterDataType.BatteryChargeStatus), // *00* Amp hours counter for battery charge
		BatDiagTotAhOut			(0x00492700, "BAT_DISCHARGE", InverterDataType.BatteryChargeStatus), // *00* Amp hours counter for battery discharge
		BatTmpVal				(0x00495B00, "BAT_TEMP", InverterDataType.BatteryInfo), // *40* Battery temperature
		BatVol					(0x00495C00, "BAT_VOL", InverterDataType.BatteryInfo), // *40* Battery voltage
		BatAmp					(0x00495D00, "BAT_CUURENT", InverterDataType.BatteryInfo), // *40* Battery current
		NameplateLocation		(0x00821E00, "INV_NAME", InverterDataType.TypeLabel), // *10* Device name (aka INV_NAME)
		NameplateMainModel		(0x00821F00, "INV_CLASS", InverterDataType.TypeLabel), // *08* Device class (aka INV_CLASS)
		NameplateModel			(0x00822000, "INV_TYPE", InverterDataType.TypeLabel), // *08* Device type (aka INV_TYPE)
		NameplateAvalGrpUsr		(0x00822100), // * * Unknown
		NameplatePkgRev			(0x00823400, "INV_SWVERSION", InverterDataType.SoftwareVersion), // *08* Software package (aka INV_SWVER)
		InverterWLim			(0x00832A00); // *00* Maximum active power device (aka INV_PACMAX1_2) (Some inverters like SB3300/SB1200)

		private final int value;
		private final int cls;
		private final String code;
		private final InverterDataType data;
		
		private LRIDefinition(int value) {
			this(value, Integer.toString(value), InverterDataType.None, 0) ;
		}
		
		private LRIDefinition(int value, String code, InverterDataType data) {
			this(value, code, data, 0);
		}
		
		private LRIDefinition(int value, String code, InverterDataType data, int cls) {
			this.value = value;
			this.code = code.toUpperCase();
			this.data = data;
			this.cls = cls;
		}

		public int getValue() {
			return value + cls;
		}
		
		public String getCode() {
			return code;
		}

		public InverterDataType getData() {
			return data;
		}

		private static HashMap<Integer, SmaDevice.LRIDefinition> valueMap;
		private static HashMap<String, SmaDevice.LRIDefinition> codeMap;
		
		public static LRIDefinition fromOrdinal(int i) {
			if (valueMap == null) {
				prepareMap();
			}
			return valueMap.get(i);
		}
		public static LRIDefinition fromOrdinal(String code) {
			if (codeMap == null) {
				prepareMap();
			}
			return codeMap.get(code.toUpperCase());
		}
		
		public static boolean containsCode(String code) {
			if (codeMap == null) {
				prepareMap();
			}
			return codeMap.containsKey(code);
		}
		
		private static synchronized void prepareMap() {
			valueMap = new HashMap<Integer, SmaDevice.LRIDefinition>(
					SmaDevice.LRIDefinition.values().length);
			codeMap = new HashMap<String, SmaDevice.LRIDefinition>(
					SmaDevice.LRIDefinition.values().length);
			
			for (SmaDevice.LRIDefinition e : SmaDevice.LRIDefinition
					.values()) {
				valueMap.put(e.getValue(), e);
				codeMap.put(e.getCode(), e);
			}
		}
		
		
	}

	void init() throws IOException;
	
	public void logon(SmaUserGroup userGroup, String password) throws IOException;
	
	public void logoff() throws IOException;

	void setEventPublisher(EventPublisher eventPublisher);

	void unsetEventPublisher(EventPublisher eventPublisher);

	List<LRIDefinition> getValidLRIDefinitions();

	String getValueAsString(LRIDefinition lriDefinition);

}
