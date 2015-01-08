package org.openhab.binding.sma.internal;

import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.LRIDefinition;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;


public class SmaBindingConfig implements BindingConfig {
	final private Class<? extends Item> itemType;
	final private LRIDefinition type;
	final private String deviceId;
	
	public SmaBindingConfig(Class<? extends Item> itemType, String deviceId, LRIDefinition type) {
		this.itemType = itemType;
		this.type = type;
		this.deviceId = deviceId;
	}
	
	public Class<? extends Item> getItemType() {
		return itemType;
	}

	public LRIDefinition getLRIDefinition() {
		return type;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
}