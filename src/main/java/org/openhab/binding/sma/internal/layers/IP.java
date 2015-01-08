/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal.layers;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IP extends AbstractPhysicalLayer {

	private static final Logger logger = LoggerFactory
			.getLogger(IP.class);
	
	protected static final int L2SIGNATURE = 0x65601000;
	
	protected InetAddress address;

	public IP(InetAddress address) {
		this.address = address;
	}
	

	@Override
	public void open() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(byte v) {
		buffer[packetposition++] = v;
	}

	@Override
	public void writePacket(byte longwords, byte ctrl, short ctrl2,
			short dstSUSyID, int dstSerial) {
		write(L2SIGNATURE);

		write(longwords);
		write(ctrl);
		write(dstSUSyID);
		write(dstSerial);
		write(ctrl2);
		write(AppSUSyID);
		write(AppSerial);
		write(ctrl2);
		write((short) 0);
		write((short) 0);
		write(pcktID | 0x8000);
	}

	@Override
	public void writePacketTrailer() {
		write(0x0);
	}

	@Override
	public void writePacketHeader(int control) {
        write(0x00414D53);  // SMA\0
        write(0xA0020400);
        write(0x01000000);
        write((byte) 0);
        write((byte) 0); 	// Placeholder for packet length
    }


	@Override
	public byte[] receive(int i) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void writePacketLength() {
		// TODO Auto-generated method stub
		
	}
}
