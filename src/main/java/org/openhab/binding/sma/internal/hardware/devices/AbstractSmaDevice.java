package org.openhab.binding.sma.internal.hardware.devices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.core.events.EventPublisher;

public abstract class AbstractSmaDevice implements SmaDevice {

	/**
	 * defines valid LRIs for that kind of device
	 */
	private static final List<LRIDefinition> validLRIDefinition = new ArrayList<LRIDefinition>();
	
	EventPublisher eventPublisher = null;

	@Override
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;;
	}

	@Override
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<LRIDefinition> getValidLRIDefinitions() {
		return AbstractSmaDevice.validLRIDefinition;
	}
}
