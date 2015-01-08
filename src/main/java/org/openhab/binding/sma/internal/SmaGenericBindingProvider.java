/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.sma.SmaBindingProvider;
import org.openhab.binding.sma.internal.hardware.devices.BluetoothSolarInverter;
import org.openhab.binding.sma.internal.hardware.devices.BluetoothSolarInverterPlant;
import org.openhab.binding.sma.internal.hardware.devices.EthernetSolarInverter;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.InverterDataType;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.LRIDefinition;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Volker Richert
 * @since 1.5.0
 */
public class SmaGenericBindingProvider extends AbstractGenericBindingProvider
		implements SmaBindingProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(SmaBinding.class);

	private Map<String, SmaDevice> devices;

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "sma";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		if (!(item instanceof SwitchItem || item instanceof NumberItem || item instanceof ContactItem || item instanceof StringItem)) {
			throw new BindingConfigParseException(
					"item '"
							+ item.getName()
							+ "' is of type '"
							+ item.getClass().getSimpleName()
							+ "', only Switch-, Number- and ContactItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		String[] parts = bindingConfig.trim().split(":");
		if (parts.length == 2 && this.devices != null) {
			SmaDevice device = this.devices.get(parts[0]);
			if (device != null) {
				List<LRIDefinition> validLRIDefinitions = device.getValidLRIDefinitions();
				
				boolean matches = false;
				String lRIName = parts[1].toUpperCase();
				for (LRIDefinition lriDefinition : validLRIDefinitions) {
					if (matches = lRIName.equals(lriDefinition.getCode()))
						break;
				}
				if (!matches) {
					String msg = "given item type '" + parts[1] + "' of '"
							+ parts[0]
							+ "' does not follow the expected pattern.";
					throw new BindingConfigParseException(msg);
				}

				addBindingConfig(item, new SmaBindingConfig(item.getClass(),
						parts[0], LRIDefinition.fromOrdinal(parts[1])));
			}

		} else {
			throw new BindingConfigParseException(
					"SMA items must have with <hostID>:<type>");
		}

	}
	
	@Override
	public Class<? extends Item> getItemType(String itemName) {
		SmaBindingConfig config = (SmaBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.getItemType() : null;
	}

	@Override
	public LRIDefinition getDefinition(String itemName) {
		SmaBindingConfig config = (SmaBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.getLRIDefinition() : null;
	}

	@Override
	public void setDevices(Map<String, SmaDevice> devices) {
		this.devices = devices;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SmaBindingConfig getDeviceConfig(String itemName) {
		return (SmaBindingConfig) bindingConfigs.get(itemName);
	}

}
