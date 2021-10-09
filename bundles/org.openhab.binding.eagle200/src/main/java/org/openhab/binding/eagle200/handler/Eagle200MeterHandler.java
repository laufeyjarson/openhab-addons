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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eagle200.internal.Eagle200BindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200MeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 *
 */
@NonNullByDefault
public class Eagle200MeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Eagle200MeterHandler.class);
    @Nullable
    private Runnable scraper;
    private Map<String, String> lastupdates = new HashMap<String, String>();
    @Nullable
    private ScheduledFuture<?> poller;
    private int scanrate = 60;

    public Eagle200MeterHandler(Thing thing) {
        super(thing);
        this.scraper = new Runnable() {

            @Override
            public void run() {
                Configuration config = Eagle200MeterHandler.this.getConfig();
                String addr = (String) config.get(Eagle200BindingConstants.THING_CONFIG_HWADDRESS);
                try {
                    Bridge bridge = getBridge();
                    if (bridge != null && bridge.getHandler() != null) {
                        @SuppressWarnings("null")
                        Map<String, String> update = ((Eagle200BridgeHandler) bridge.getHandler()).getConnection()
                                .queryMeter(addr);
                        if (!Eagle200MeterHandler.this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                            Eagle200MeterHandler.this.updateStatus(ThingStatus.ONLINE);
                        }
                        Eagle200MeterHandler.this.updateChannels(update);
                        logger.debug("MeterHandler::Run: called updateChannels {}", update);
                    } else {
                        logger.debug("MeterHandler::Run: no bridge set, can't call for data");
                    }
                } catch (Exception e) {
                    logger.warn("connection to Eagle200 caused error", e);
                    Eagle200MeterHandler.this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        };
    }

    @Override
    public void initialize() {
        this.initChannels();
        Configuration config = Eagle200MeterHandler.this.getConfig();
        BigDecimal freq = (BigDecimal) config.get(Eagle200BindingConstants.THING_CONFIG_REFRESHINTERVAL);
        if (freq == null) {
            freq = new BigDecimal(60);
        }
        this.scanrate = freq.intValue();
        this.poller = this.scheduler.scheduleWithFixedDelay(this.scraper, 1, this.scanrate, TimeUnit.SECONDS);
    }

    @SuppressWarnings("null")
    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> update) {
        super.handleConfigurationUpdate(update);

        boolean changed = false;
        if (update.containsKey(Eagle200BindingConstants.THING_CONFIG_REFRESHINTERVAL)) {
            BigDecimal freq = (BigDecimal) update.get(Eagle200BindingConstants.THING_CONFIG_REFRESHINTERVAL);
            this.scanrate = freq.intValue();
            changed = true;
        }

        if (changed) {
            if (this.poller != null && !this.poller.isDone()) {
                this.poller.cancel(true);
            }
            this.poller = this.scheduler.scheduleWithFixedDelay(this.scraper, 1, this.scanrate, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands, updates come from poller via updateChannels.
        logger.debug("Eagle200MeterHandler->handleCommand called");
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        if (this.poller != null && !this.poller.isDone()) {
            this.poller.cancel(true);
        }
        super.dispose();
    }

    @SuppressWarnings("null")
    private void initChannels() {
        Bridge bridge = getBridge();
        Configuration config = this.getConfig();
        String addr = (String) config.get(Eagle200BindingConstants.THING_CONFIG_HWADDRESS);

        if (bridge == null || bridge.getHandler() == null) {
            logger.warn("eagle200 utility meter has no bridge yet");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        Map<String, String> update;
        try {
            update = ((Eagle200BridgeHandler) bridge.getHandler()).getConnection().queryMeter(addr);
        } catch (Exception e) {
            logger.warn("connection to eagle200 caused IO error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        ThingBuilder builder = this.editThing();
        SortedMap<String, String> sorted = new TreeMap<String, String>(update);
        List<Channel> channels = new ArrayList<Channel>();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String tag = this.getChannelName(entry.getKey());

            if (this.getThing().getChannel(tag) == null) {
                boolean numeric = this.isNumeric(entry.getValue());
                ChannelUID uid = this.getChannelUID(entry.getKey());
                if (numeric) {
                    Channel channel = ChannelBuilder.create(uid, "Number").withLabel(tag)
                            .withType(Eagle200BindingConstants.CHANNEL_ELECTRIC_METERNUMBER_TYPEUID).build();
                    channels.add(channel);
                    logger.debug("Adding numeric channel {}", uid);
                } else {
                    Channel channel = ChannelBuilder.create(uid, "String").withLabel(tag)
                            .withType(Eagle200BindingConstants.CHANNEL_ELECTRIC_METER_TYPEUID).build();
                    channels.add(channel);
                    logger.debug("Adding string channel {}", uid);
                }
            } else {
                logger.debug("Channel {} already exists", tag);
            }
        }
        builder.withChannels(channels);
        this.updateThing(builder.build());
        updateStatus(ThingStatus.ONLINE);
    }

    private ChannelUID getChannelUID(String key) {
        return new ChannelUID(this.getThing().getUID(), this.getChannelName(key));
    }

    private String getChannelName(String key) {
        return key.replace("zigbee:", "");
    }

    @SuppressWarnings({ "null" })
    private void updateChannels(Map<String, String> updates) {

        for (Map.Entry<String, String> update : updates.entrySet()) {
            String lastvalue = this.lastupdates.get(update.getKey());
            ChannelUID key = this.getChannelUID(update.getKey());
            boolean numeric = this.isNumeric(update.getValue());
            if (lastvalue == null) {
                if (numeric) {
                    BigDecimal value = new BigDecimal(update.getValue());
                    logger.debug("Setting {} to numeric value {}", key, value);
                    this.updateState(key, new DecimalType(value));
                } else {
                    logger.debug("Setting {} to string value {}", key, update.getValue());
                    this.updateState(key, new StringType(update.getValue()));
                }
            } else if (update.getValue() != null && !update.getValue().equals(this.lastupdates.get(update.getKey()))) {
                if (numeric) {
                    BigDecimal value = new BigDecimal(update.getValue());
                    logger.debug("Updating {} to numeric value {}", key, value);
                    this.updateState(key, new DecimalType(value));
                } else {
                    logger.debug("Updating {} to string value {}", key, update.getValue());
                    this.updateState(key, new StringType(update.getValue()));
                }
            }
        }
        this.lastupdates = updates;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }
}
