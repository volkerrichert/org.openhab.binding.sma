/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal.layers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.openhab.binding.sma.internal.hardware.devices.SmaBluetoothAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bluetooth extends AbstractPhysicalLayer {

	private static final Logger logger = LoggerFactory
			.getLogger(Bluetooth.class);

	// length of package header
	public static final int HEADERLENGTH = 18;
	
	protected static final int L2SIGNATURE = 0x656003FF;
	

	// stores address in low endian
	public SmaBluetoothAddress localAddress = new SmaBluetoothAddress();
	public SmaBluetoothAddress destAddress;

	protected short FCSChecksum = (short) 0xffff;

	protected static StreamConnection connection;
	protected static DataOutputStream out;
	protected static DataInputStream in;

	public Bluetooth(SmaBluetoothAddress destAdress) {
		super();

		this.destAddress = destAdress;
	}

	public Bluetooth(String destAd) {
		this(destAd, 1);
	}

	public Bluetooth(String destAdr, int port) {
		super();

		this.destAddress = new SmaBluetoothAddress(destAdr, port);
	}

	@Override
	public void open() throws IOException {
		close();
		// TODO Auto-generated method stub
		if (connection == null) {
			connection = (StreamConnection) Connector.open(destAddress
					.getConnectorString());

			out = connection.openDataOutputStream();
			in = connection.openDataInputStream();
		}

	}

	@Override
	public void close() {
	}

	@Override
	public void write(byte v) {
		// Keep a rolling checksum over the payload
		FCSChecksum = (short) (((FCSChecksum & 0xff00) >>> 8) ^ fcstab[(FCSChecksum ^ v) & 0xff]);

		if (v == 0x7d || v == 0x7e || v == 0x11 || v == 0x12 || v == 0x13) {
			buffer[packetposition++] = 0x7d;
			buffer[packetposition++] = (byte) (v ^ 0x20);
		} else {
			buffer[packetposition++] = v;
		}
	}

	public void writePacket(byte longwords, byte ctrl, short ctrl2,
			short dstSUSyID, int dstSerial) {
		buffer[packetposition++] = 0x7E; // Not included in checksum
		logger.debug("Checksum {}", Bluetooth.toHex(FCSChecksum));
		write(L2SIGNATURE);
		logger.debug("Checksum {}", Bluetooth.toHex(FCSChecksum));
		write(longwords);
		logger.debug("Checksum {}", Bluetooth.toHex(FCSChecksum));
		write(ctrl);
		write(dstSUSyID);
		write(dstSerial);
		write(ctrl2);
		write(AppSUSyID);
		write(AppSerial);
		write(ctrl2);
		write((short) 0);
		write((short) 0);
		write((short) (pcktID | 0x8000));
	}

	public void writePacketTrailer() {
		FCSChecksum ^= 0xFFFF;
		buffer[packetposition++] = (byte) (FCSChecksum & 0x00FF);
		buffer[packetposition++] = (byte) (((FCSChecksum & 0xFF00) >>> 8) & 0x00FF);
		buffer[packetposition++] = 0x7E; // Trailing byte
	}

	@Override
	public void writePacketHeader(int control) {
		this.writePacketHeader(control, this.destAddress);
	}

	public void writePacketHeader(int control, SmaBluetoothAddress destaddress) {
		packetposition = 0;
		FCSChecksum = (short) 0xFFFF;

		buffer[packetposition++] = 0x7E;
		buffer[packetposition++] = 0; // placeholder for len1
		buffer[packetposition++] = 0; // placeholder for len2
		buffer[packetposition++] = 0; // placeholder for checksum

		int i;
		for (i = 0; i < 6; i++)
			buffer[packetposition++] = localAddress.get(i);

		for (i = 0; i < 6; i++)
			buffer[packetposition++] = destaddress.get(i);

		buffer[packetposition++] = (byte) (control & 0xFF);
		buffer[packetposition++] = (byte) (control >>> 8);
	}

	@Override
	public void writePacketLength() {
		buffer[1] = (byte) (packetposition & 0xFF); // Lo-Byte
		buffer[2] = (byte) ((packetposition >>> 8) & 0xFF); // Hi-Byte
		buffer[3] = (byte) (buffer[0] ^ buffer[1] ^ buffer[2]); // checksum
	}

	@Override
	public void send() throws IOException {
		writePacketLength();
		logger.debug("Sending {} bytes:\n{}", packetposition,
				bytesToHex(buffer, packetposition, ' '));
		out.write(buffer, 0, packetposition);
	}

	@Override
	public byte[] receive(int wait4Command) throws IOException {
		return receive(destAddress, wait4Command);
	}

	public byte[] receiveAll(int wait4Command) throws IOException {
		return receive(SmaBluetoothAddress.BROADCAST, wait4Command);
	}

	protected byte[] receive(SmaBluetoothAddress destAddress, int wait4Command)
			throws IOException {
		SmaBluetoothAddress sourceAddr = new SmaBluetoothAddress();
		SmaBluetoothAddress destinationAddr = new SmaBluetoothAddress();
		byte[] data = null;

		logger.debug("receive({})\n", wait4Command);

		int index = 0;
		int hasL2pckt = 0;

		int rc = 0;
		int command = 0;

		do {
			data = new byte[520];
			in.read(data, 0, HEADERLENGTH);

			// int SOP = data[0];
			// data are in litle endian. getUnsignedShort exact big endian
			int pkLength = data[1] + (data[2] << 8);
			// int pkChecksum = data[3];

			sourceAddr.setAddress(data, 4);
			destinationAddr.setAddress(data, 10);

			command = data[16] + (data[17] << 8);

			if (pkLength > HEADERLENGTH) {
				// data = new byte[pkLength - HEADERLENGTH];
				in.read(data, 18, pkLength - HEADERLENGTH);

				logger.debug("data received: \n{}", bytesToHex(data, pkLength));
				// Check if data is coming from the right inverter
				if (destAddress.equals(sourceAddr)) {
					rc = 0;
					logger.debug("source: {}", sourceAddr.toString());
					logger.debug("destination: {}", destinationAddr.toString());

					logger.debug("receiving cmd {}", command);

					if ((hasL2pckt == 0) && data[18] == (byte) 0x7E
							&& data[19] == (byte) 0xff
							&& data[20] == (byte) 0x03
							&& data[21] == (byte) 0x60
							&& data[22] == (byte) 0x65) // 0x656003FF7E
					{
						hasL2pckt = 1;
					}

					if (hasL2pckt == 1) {
						// Copy CommBuf to packetbuffer
						boolean escNext = false;
						byte dummy[] = new byte[data.length];

						logger.debug("PacketLength={}", pkLength);

						for (int i = HEADERLENGTH; i < pkLength; i++) {
							dummy[index] = data[i];
							// Keep 1st byte raw unescaped 0x7E
							if (escNext == true) {
								dummy[index] ^= 0x20;
								escNext = false;
								index++;
							} else {
								if (dummy[index] == 0x7D)
									escNext = true; // Throw away the 0x7d byte
								else
									index++;
							}
							if (index >= 520) {
								logger.warn(
										"Warning: pcktBuf buffer overflow! ({})\n",
										index);
								throw new ArrayIndexOutOfBoundsException();
							}
						}

						byte[] result = new byte[index + HEADERLENGTH];
						System.arraycopy(data, 0, result, 0, HEADERLENGTH);
						System.arraycopy(dummy, 0, result, HEADERLENGTH, index);

						data = result;
						
						logger.debug("data decoded: \n{}", bytesToHex(data, data.length));
					}
				} // isValidSender()
				else {
					rc = -1; // E_RETRY;
					logger.debug("Wrong sender: {}", sourceAddr);
				}

			}
		} while (((command != wait4Command) || (rc == -1/* E_RETRY */))
				&& (0xFF != wait4Command));

		return data;
	}

	public boolean isCrcValid() {
		byte lb = buffer[packetposition - 3], hb = buffer[packetposition - 2];

		return !((lb == 0x7E) || (hb == 0x7E) || (lb == 0x7D) || (hb == 0x7D));
	}

	public boolean isValidChecksum() {
		FCSChecksum = (short) 0xffff;
		// Skip over 0x7e at start and end of packet
		int i;
		for (i = 1; i <= packetposition - 4; i++) {
			FCSChecksum = (short) ((FCSChecksum >> 8) ^ fcstab[(FCSChecksum ^ buffer[i]) & 0xff]);
		}

		FCSChecksum ^= 0xffff;

		if (getShort(buffer, packetposition - 3) == (short) FCSChecksum)
			return true;
		else {
			logger.debug("Invalid chk {} - Found 0x{}{}", toHex(FCSChecksum),
					toHex(buffer[2]), toHex(buffer[3]));
			return false;
		}
	}

}
