/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.eagle200.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 *
 */

public class Device {

    @XStreamAlias("HardwareAddress")
    private String hwAddress;

    @XStreamAlias("Manufacturer")
    private String manufacturer;

    @XStreamAlias("ModelId")
    private String modelID;

    @XStreamAlias("Protocol")
    private String protocol;

    @XStreamAlias("LastContact")
    private String lastContact;

    @XStreamAlias("ConnectionStatus")
    private String connectionStatus;

    @XStreamAlias("NetworkAddress")
    private String networkAddress;

    public Device() {
    }

    public String getHwAddress() {
        return hwAddress;
    }

    public void setHwAddress(String hwAddress) {
        this.hwAddress = hwAddress;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLastContact() {
        return lastContact;
    }

    public void setLastContact(String lastContact) {
        this.lastContact = lastContact;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }
}
