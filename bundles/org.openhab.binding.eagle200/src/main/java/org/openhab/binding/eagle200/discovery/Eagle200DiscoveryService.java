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
package org.openhab.binding.eagle200.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.eagle200.handler.Eagle200BridgeHandler;
import org.openhab.binding.eagle200.internal.Eagle200BindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 *
 */

public class Eagle200DiscoveryService extends AbstractDiscoveryService {

    private static final int TIMEOUT = 20;
    private static final int REFRESH = 15;
    private Eagle200BridgeHandler bridgeHandler;
    private ScheduledFuture<?> discoveryJob;
    private Runnable discoveryRunnable;
    private boolean scanInProgress = false;
    private int scancount = 0;
    private static final Logger logger = LoggerFactory.getLogger(Eagle200DiscoveryService.class);

    /**
     * @param timeout
     * @throws IllegalArgumentException
     */
    public Eagle200DiscoveryService(Eagle200BridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(Collections.singleton(Eagle200BindingConstants.THING_TYPE_EAGLE200_METER), TIMEOUT, true);
        this.bridgeHandler = bridgeHandler;
        this.discoveryRunnable = new Runnable() {

            @Override
            public void run() {
                Eagle200DiscoveryService.this.startScan();
            }
        };
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (this.discoveryJob == null || this.discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this.discoveryRunnable, 0, REFRESH, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (this.discoveryJob != null && !this.discoveryJob.isCancelled()) {
            this.discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startScan()
     */
    @Override
    protected void startScan() {
        if (this.scanInProgress) {
            return;
        }
        this.scanInProgress = true;
        try {
            if (this.bridgeHandler.getConfiguration().isComplete()) {
                this.scanForEnergyMeterThings();
                // TODO: eagle200 supports other devices, scan for them next...
            } else {
                logger.warn("connection to Eagle failed, leaving device scan. Fix configuration");
                return;
            }
        } finally {
            this.scanInProgress = false;
        }
    }

    private void scanForEnergyMeterThings() {
        List<String> meterAddresses;
        try {
            meterAddresses = bridgeHandler.getConnection().getMeterHWAddress();
        } catch (IOException | IllegalStateException e) {
            logger.warn("connection to Eagle failed, stopping device scan. Fix network or configuration: {}",
                    e.getMessage());
            return;
        }

        ThingUID bridgeID = this.bridgeHandler.getThing().getUID();
        // for each address add a device
        for (String meterAddress : meterAddresses) {
            ThingTypeUID theThingTypeUid = Eagle200BindingConstants.THING_TYPE_EAGLE200_METER;
            String thingID = meterAddress;
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeID, thingID);
            // ThingUID thingUID = new ThingUID(theThingTypeUid, thingID);

            Map<String, Object> properties = new HashMap<>(0);
            properties.put(Eagle200BindingConstants.THING_CONFIG_HWADDRESS, meterAddress);
            properties.put(Eagle200BindingConstants.THING_CONFIG_REFRESHINTERVAL, 60);
            this.scancount++;
            logger.debug("Bridge {} for device {} is in state {}, scan count {}", bridgeID, thingUID,
                    this.bridgeHandler.getThing().getStatus(), this.scancount);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeID)
                    .withProperties(properties).withRepresentationProperty("hwaddress")
                    .withLabel("Electricity Meter " + meterAddress).build();
            thingDiscovered(discoveryResult);
        }
    }
}
