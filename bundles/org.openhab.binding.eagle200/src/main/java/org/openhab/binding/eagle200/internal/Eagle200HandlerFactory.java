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
package org.openhab.binding.eagle200.internal;

import static org.openhab.binding.eagle200.internal.Eagle200BindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eagle200.discovery.Eagle200DiscoveryService;
import org.openhab.binding.eagle200.handler.Eagle200BridgeHandler;
import org.openhab.binding.eagle200.handler.Eagle200MeterHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 */
@NonNullByDefault
@Component(configurationPid = "binding.eagle200", service = ThingHandlerFactory.class)
public class Eagle200HandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<ThingUID, ServiceRegistration<?>>();

    private static final Logger logger = LoggerFactory.getLogger(Eagle200HandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Eagle200BindingConstants.THING_TYPE_EAGLE200_BRIDGE.equals(thingTypeUID)) {
            Eagle200BridgeHandler handler = new Eagle200BridgeHandler((Bridge) thing);
            this.registerBridgeDiscoveryService(handler);
            return handler;

        }
        if (Eagle200BindingConstants.THING_TYPE_EAGLE200_METER.equals(thingTypeUID)) {
            return new Eagle200MeterHandler(thing);
        }

        return null;
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerBridgeDiscoveryService(Eagle200BridgeHandler bridgeHandler) {
        logger.debug("Eagle discovery service activating");

        Eagle200DiscoveryService discoveryService = new Eagle200DiscoveryService(bridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> registration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>());

        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), registration);

        logger.debug("registerBridgeDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                bridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Eagle200BridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegistrations
                    .get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                Eagle200DiscoveryService service = (Eagle200DiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
            }
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
            logger.debug("Eagle discovery service removed");
        }
    }
}
