package org.openhab.binding.sma.internal.hardware.devices;

public class SmaSerial {
	protected short suSyID;
	protected long serial;

	public SmaSerial(short suSyID, long serial) {
		this.suSyID = suSyID;
		this.serial = serial;
	}

	@Override
	public String toString() {
		return "SmaSerial [suSyID=" + suSyID + ", serial=" + serial + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (serial ^ (serial >>> 32));
		result = prime * result + suSyID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmaSerial other = (SmaSerial) obj;
		if (serial != other.serial)
			return false;
		if (suSyID != other.suSyID)
			return false;
		return true;
	}
	
	
}
