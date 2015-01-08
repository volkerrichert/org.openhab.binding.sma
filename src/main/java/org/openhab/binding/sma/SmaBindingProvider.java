/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma;

import java.util.Map;

import org.openhab.binding.sma.internal.SmaBindingConfig;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.LRIDefinition;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.Item;

/**
 * @author Volker Richert
 * @since 1.5.0
 */
public interface SmaBindingProvider extends BindingProvider {
	/**
	 * Gets device config corresponding to the item specified.
	 * 
	 * @param itemName
	 *            Name of the item for which to get the device configuration
	 * @return Device configuration corresponding to item
	 */
	public SmaBindingConfig getDeviceConfig(String itemName);

	/**
	 * sets device config corresponding to the item specified
	 * 
	 * @param deviceCache reference to configured devices to cross check
	 * and validate configuration
	 */
	public void setDevices(Map<String, SmaDevice> deviceCache);


	/**
	 * Returns the Type of the Item identified by {@code itemName}
	 * 
	 * @param itemName
	 *            the name of the item to find the type for
	 * @return the type of the Item identified by {@code itemName}
	 */
	Class<? extends Item> getItemType(String itemName);

	/**
	 * Returns the binding type for an item name
	 * 
	 * @param itemName
	 *            the name of the item
	 * @return the items binding type
	 */
	LRIDefinition getDefinition(String itemName);
	
}
