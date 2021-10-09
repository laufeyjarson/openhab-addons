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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eagle200.discovery.Eagle200DiscoveryService;
import org.openhab.binding.eagle200.internal.Eagle200BindingConstants;
import org.openhab.binding.eagle200.internal.Eagle200Configuration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 *
 */
@NonNullByDefault
public class Eagle200BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(Eagle200BridgeHandler.class);

    @Nullable
    private Eagle200Configuration config;
    private Eagle200Connection connection;
    @Nullable
    private Eagle200DiscoveryService discoveryService;

    public Eagle200BridgeHandler(Bridge bridge) {
        super(bridge);
        this.connection = new Eagle200Connection(this);
        logger.debug("Bridge Handler created");
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(Eagle200Configuration.class);
        if (!config.isComplete()) {
            logger.debug("Config incomplete, setting offline");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        this.connection.updateConfiguration(config);
        updateStatus(ThingStatus.ONLINE);
        if (this.discoveryService != null) {
            logger.debug("Starting discovery service");
            this.discoveryService.activate();
        }
    }

    public Eagle200Configuration getConfiguration() {
        return getConfigAs(Eagle200Configuration.class);
    }

    public Eagle200Connection getConnection() {
        return this.connection;
    }

    @Override
    public void handleConfigurationUpdate(Map<@NonNull String, Object> update) {

        super.handleConfigurationUpdate(update);

        Configuration config = this.editConfiguration();
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME));
        }
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID));
        }
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE));
        }
        ThingStatus currentStatus = this.getThing().getStatus();
        this.updateConfiguration(config);
        if (!this.getConfigAs(Eagle200Configuration.class).isComplete()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        if (!currentStatus.equals(ThingStatus.ONLINE)) {
            // TODO Does this.connection.updateConfiguration need to be here, or before this if?
            // What happens if we update the config and are already online?
            this.connection.updateConfiguration(this.getConfigAs(Eagle200Configuration.class));
            this.updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Our commands are read-only, so this isn't needed.
        logger.debug("eagle200 bridge handleCommand called");
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void registerDiscoveryService(Eagle200DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    // @SuppressWarnings("null")
    public void unregisterDiscoveryService() {
        this.discoveryService = null;
    }
}
