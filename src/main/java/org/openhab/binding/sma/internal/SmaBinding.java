/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

import org.openhab.binding.sma.SmaBindingProvider;
import org.openhab.binding.sma.internal.hardware.devices.BluetoothSolarInverter;
import org.openhab.binding.sma.internal.hardware.devices.BluetoothSolarInverterPlant;
import org.openhab.binding.sma.internal.hardware.devices.EthernetSolarInverter;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Volker Richert
 * @since 1.5.0
 */
public class SmaBinding extends AbstractActiveBinding<SmaBindingProvider>
		implements ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(SmaBinding.class);

	private static final Pattern DEVICES_PATTERN = Pattern
			.compile("^(.*?)\\.(plant|ip|bt|login|password|retry)$");

	protected Map<String, SmaDevice> deviceCache = new HashMap<String, SmaDevice>();

	/**
	 * the refresh interval which is used to poll values from the Sma server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	public SmaBinding() {
	}

	public void activate() {
		try {
			// load the bluetooth stack
			LocalDevice.getLocalDevice();

			for (SmaBindingProvider currentProvider : providers) {
				for (String currentItem : currentProvider.getItemNames()) {
					logger.debug("item: {}", currentItem);
				}
			}
		} catch (BluetoothStateException e) {
		}
	}

	public void deactivate() {
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "Sma Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
		for (SmaBindingProvider currentProvider : providers) {

			for (String currentItem : currentProvider.getItemNames()) {
				logger.debug("item: {}", currentItem);
				SmaBindingConfig currentConfig = currentProvider
						.getDeviceConfig(currentItem);
				
				if (!deviceCache.containsKey(currentConfig.getDeviceId()))
					continue;
				
				SmaDevice dev = deviceCache.get(currentConfig.getDeviceId());
				try {
					dev.init();
					
					String value = dev.getValueAsString(currentConfig.getLRIDefinition());
					
					if (value != null) {
						Class<? extends Item> itemType = currentProvider.getItemType(currentItem);

						org.openhab.core.types.State state = null;

						if (itemType.isAssignableFrom(SwitchItem.class)) {
							if (value.equals("1"))
								state = OnOffType.ON;
							else
								state = OnOffType.OFF;
						} else if (itemType.isAssignableFrom(NumberItem.class)) {
							state = new DecimalType(value);
						} else if (itemType.isAssignableFrom(StringItem.class)) {
							state = new StringType(value);
						}

						if (state != null)
							eventPublisher.postUpdate(currentItem, state);						
					} else {
						logger.error("unable to get value for dev {}", dev.toString());
					}
				} catch (IOException e) {
					logger.error("unable to init dev {}:\n {}", dev.toString(), e.getMessage());
				}
			}
		}

	}

	@Override
	public void setEventPublisher(EventPublisher eventPublisher) {
		super.setEventPublisher(eventPublisher);
		for (SmaDevice currentDeviceData : deviceCache.values()) {
			currentDeviceData.setEventPublisher(eventPublisher);
		}
	}

	@Override
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		super.unsetEventPublisher(eventPublisher);
		for (SmaDevice currentDeviceData : deviceCache.values()) {
			currentDeviceData.unsetEventPublisher(eventPublisher);
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		if (config != null) {
			Map<String, Device> configStore = new HashMap<String, Device>();

			// Based on fritzAHA parsing mechanism
			Enumeration<String> keys = config.keys();

			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				// to override the default refresh interval one has to add a
				// parameter to openhab.cfg like
				// <bindingName>:refresh=<intervalInMs>
				if ("refresh".equals(key)) {
					refreshInterval = Long.parseLong((String) config
							.get("refresh"));
					continue;
				}

				Matcher matcher = DEVICES_PATTERN.matcher(key);

				if (!matcher.matches()) {
					logger.debug("given config key '"
							+ key
							+ "' does not follow the expected pattern '<id>.<plant|bt|ip|login|password|retry>'");
					continue;
				}

				matcher.reset();
				matcher.find();

				String devId = matcher.group(1);

				Device dev = configStore.get(devId);

				if (dev == null) {
					dev = new Device(devId);

					dev.eventPublisher = eventPublisher;
					configStore.put(devId, dev);
					logger.debug("Created new SMA configuration " + devId);
				}

				String configKey = matcher.group(2);
				String value = (String) config.get(key);

				if ("plant".equals(configKey)) {
					dev.setPlant(value);
				} else if ("bt".equals(configKey)) {
					dev.setBTAdress(value);
				} else if ("ip".equals(configKey)) {
					try {
						dev.setAddress(InetAddress.getByName(value));
					} catch (UnknownHostException e) {
						logger.error("Unable to get IP-Adress of host '{}'",
								value);
					}
				} else if ("login".equals(configKey)) {
					dev.setLoginAsInstaller("installer".equals(value
							.toLowerCase()));
				} else if ("password".equals(configKey)) {
					dev.setPassword(value);
				} else if ("retry".equals(configKey)) {
					dev.setRetry(Integer.parseInt((String) value));
				} else {
					throw new ConfigurationException(configKey,
							"the given configKey '" + configKey
									+ "' is unknown");
				}
			}

			deviceCache.clear();
			for (Device entry : configStore.values()) {
				logger.debug("Creating config for devide {}", entry);
				SmaDevice device = entry.createSmaDevice();
				if (device != null) {
					deviceCache.put(entry.deviceId, device);
				} else {
					logger.error("unable to create SMA device {}",
							entry.toString());
				}
			}

			for (SmaBindingProvider currentProvider : providers) {
				currentProvider.setDevices(deviceCache);
			}
			setProperlyConfigured(true);
			logger.debug("SMA Binding configured");
		}
	}

	/**
	 * Internal data structure which carries the connection details of one
	 * device (there could be several)
	 */
	public static class Device {
		private boolean loginAsInstaller = false;
		private int retry = 10;
		private String password = "0000";
		private InetAddress address;
		private String bt;
		private String plant;

		public boolean isLoginAsInstaller() {
			return loginAsInstaller;
		}

		public SmaDevice createSmaDevice() {
			SmaDevice dev;
			if (this.address != null) {
				dev = new EthernetSolarInverter(this);
			} else if (this.bt != null) {
				dev = new BluetoothSolarInverter(this);
			} else if (this.plant != null) {
				dev = new BluetoothSolarInverterPlant(this);
			} else {
				return null;
			}

			return dev;
		}

		public void setLoginAsInstaller(boolean loginAsInstaller) {
			this.loginAsInstaller = loginAsInstaller;
		}

		public int getRetry() {
			return retry;
		}

		public void setRetry(int retry) {
			this.retry = retry;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public InetAddress getAddress() {
			return address;
		}

		public void setAddress(InetAddress address) {
			this.address = address;
			this.bt = null;
			this.plant = null;
		}

		public String getBTAdress() {
			return bt;
		}

		public void setBTAdress(String bt) {
			this.bt = bt;
			this.address = null;
			this.plant = null;
		}

		public String getPlant() {
			return plant;
		}

		public void setPlant(String plant) {
			this.plant = plant;
			this.bt = null;
			this.address = null;
		}

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public EventPublisher getEventPublisher() {
			return eventPublisher;
		}

		public void setEventPublisher(EventPublisher eventPublisher) {
			this.eventPublisher = eventPublisher;
		}

		String deviceId;
		EventPublisher eventPublisher;

		public Device(String deviceId) {
			this.deviceId = deviceId;
		}

		public String toString() {
			if (this.address != null)
				return "Device " + deviceId + " [IP=" + address + ", login as "
						+ (loginAsInstaller ? "installer" : "user") + "]";
			if (this.bt != null)
				return "Device " + deviceId + " [BT=" + bt + ", login as "
						+ (loginAsInstaller ? "installer" : "user") + "]";
			return "Device " + deviceId + " [Plant=" + plant + ", login as "
					+ (loginAsInstaller ? "installer" : "user") + "]";
		}

	}
}
