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
package org.openhab.binding.eagle200.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eagle200.internal.Eagle200Configuration;
import org.openhab.binding.eagle200.protocol.Component;
import org.openhab.binding.eagle200.protocol.Components;
import org.openhab.binding.eagle200.protocol.Device;
import org.openhab.binding.eagle200.protocol.DeviceList;
import org.openhab.binding.eagle200.protocol.DeviceQuery;
import org.openhab.binding.eagle200.protocol.Variable;
import org.openhab.binding.eagle200.protocol.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 *
 */

@NonNullByDefault
public class Eagle200Connection {

    private static final String MODELID_ELEC_METER = "electric_meter";

    private static final String DEVICELIST_CMD = "<Command><Name>device_list</Name></Command>";
    private static final String QUERY_CMD_1 = "<Command><Name>device_query</Name><DeviceDetails><HardwareAddress>";
    private static final String QUERY_CMD_2 = "</HardwareAddress></DeviceDetails><Components><All>Y</All></Components></Command>";

    @SuppressWarnings("rawtypes")
    private static final Class[] DeviceListModel = new Class[] { DeviceList.class, Device.class };

    @SuppressWarnings("rawtypes")
    private static final Class[] QueryModel = new Class[] { DeviceQuery.class, Components.class, Component.class,
            Variables.class, Variable.class };

    @SuppressWarnings("unused")
    private Eagle200BridgeHandler bridge;
    @Nullable
    private Eagle200Configuration configuration;
    private XStream xStream;
    private final Logger logger = LoggerFactory.getLogger(Eagle200Connection.class);

    public Eagle200Connection(Eagle200BridgeHandler bridge) {
        this.bridge = bridge;
        this.xStream = new XStream(new StaxDriver());
        this.xStream.ignoreUnknownElements();
        this.xStream.setClassLoader(Eagle200Connection.class.getClassLoader());
    }

    void updateConfiguration(@Nullable Eagle200Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("null")
    private String getBaseURL() {
        if (this.configuration == null || !this.configuration.isComplete()) {
            throw new IllegalStateException(
                    "getBaseURL(): Connection is not configured: configuration = " + this.configuration);
        }
        return "http://" + this.configuration.hostname + "/cgi-bin/post_manager";
    }

    @SuppressWarnings("null")
    private String doPost(String msg) throws IOException {
        if (this.configuration == null || !this.configuration.isComplete()) {
            throw new IllegalStateException(
                    "doPost(): Connection is not configured: configuration = " + this.configuration);
        }
        URL url = new URL(this.getBaseURL());
        String auth = this.configuration.cloudid + ":" + this.configuration.installcode;
        byte[] bytes = auth.getBytes("UTF-8");
        String encoding = Base64.getEncoder().encodeToString(bytes);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-type", "text/xml");
        connection.setRequestProperty("Content-Length", Integer.toString(msg.length()));
        connection.setRequestProperty("Authorization", "Basic " + encoding);

        connection.getOutputStream().write(msg.getBytes("UTF8"));
        InputStream content = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        String lines = "";
        String line;
        while ((line = in.readLine()) != null) {
            lines += line;
        }
        return lines;
    }

    public List<String> getMeterHWAddress() throws IOException {
        List<String> result = new ArrayList<String>();
        String xml = this.doPost(DEVICELIST_CMD);
        logger.trace("Eagle200 meter HW address: {}", xml);
        this.xStream.allowTypes(DeviceListModel);
        this.xStream.processAnnotations(DeviceListModel);
        DeviceList reply = (DeviceList) xStream.fromXML(xml);
        for (Device device : reply.getDevices()) {
            if (MODELID_ELEC_METER.equalsIgnoreCase(device.getModelID())) {
                result.add(device.getHwAddress());
            }
        }
        return result;
    }

    Map<String, String> queryMeter(String meterAddr) throws IOException {
        String cmd = QUERY_CMD_1 + meterAddr + QUERY_CMD_2;
        String xml = this.doPost(cmd);
        logger.trace("Eagle200 meter query {} : {}", meterAddr, xml);
        xml = xml.replaceAll(" & ", " &amp; ");
        this.xStream.allowTypes(QueryModel);
        this.xStream.processAnnotations(QueryModel);
        DeviceQuery reply = (DeviceQuery) xStream.fromXML(xml);
        List<Variable> vars = reply.getComponents().getComponents().get(0).getVariables().getVariables();
        Map<String, String> result = new HashMap<String, String>();
        for (Variable var : vars) {
            result.put(var.getName(), var.getValue());
        }
        return result;
    }
}
