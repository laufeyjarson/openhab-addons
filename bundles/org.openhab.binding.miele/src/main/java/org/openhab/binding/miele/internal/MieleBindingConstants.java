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
package org.openhab.binding.miele.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MieleBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - added constants for support of WiFi devices & protocol
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
@NonNullByDefault
public class MieleBindingConstants {

    public static final String BINDING_ID = "miele";
    public static final String APPLIANCE_ID = "uid";
    public static final String DEVICE_CLASS = "dc";
    public static final String PROTOCOL_PROPERTY_NAME = "protocol";
    public static final String SERIAL_NUMBER_PROPERTY_NAME = "serialNumber";
    public static final String EXTENDED_DEVICE_STATE_PROPERTY_NAME = "extendedDeviceState";
    public static final String STATE_PROPERTY_NAME = "state";

    // Shared Channel ID's
    public static final String SUPERCOOL_CHANNEL_ID = "supercool";
    public static final String SUPERFREEZE_CHANNEL_ID = "superfreeze";
    public static final String POWER_CONSUMPTION_CHANNEL_ID = "powerConsumption";
    public static final String WATER_CONSUMPTION_CHANNEL_ID = "waterConsumption";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_XGW3000 = new ThingTypeUID(BINDING_ID, "xgw3000");
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID, "dishwasher");
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");
    public static final ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID, "fridge");
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, "tumbledryer");
    public static final ThingTypeUID THING_TYPE_HOB = new ThingTypeUID(BINDING_ID, "hob");
    public static final ThingTypeUID THING_TYPE_FRIDGEFREEZER = new ThingTypeUID(BINDING_ID, "fridgefreezer");
    public static final ThingTypeUID THING_TYPE_HOOD = new ThingTypeUID(BINDING_ID, "hood");
    public static final ThingTypeUID THING_TYPE_WASHINGMACHINE = new ThingTypeUID(BINDING_ID, "washingmachine");
    public static final ThingTypeUID THING_TYPE_COFFEEMACHINE = new ThingTypeUID(BINDING_ID, "coffeemachine");

    // Miele devices classes
    public static final String MIELE_DEVICE_CLASS_COFFEE_SYSTEM = "CoffeeSystem";
    public static final String MIELE_DEVICE_CLASS_FRIDGE = "Fridge";
    public static final String MIELE_DEVICE_CLASS_FRIDGE_FREEZER = "FridgeFreezer";

    // Miele appliance states
    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_OFF = 1;
    public static final int STATE_STAND_BY = 2;
    public static final int STATE_PROGRAMMED = 3;
    public static final int STATE_WAITING_TO_START = 4;
    public static final int STATE_RUNNING = 5;
    public static final int STATE_PAUSED = 6;
    public static final int STATE_END = 7;
    public static final int STATE_FAILURE = 8;
    public static final int STATE_ABORT = 9;
    public static final int STATE_IDLE = 10;
    public static final int STATE_RINSE_HOLD = 11;
    public static final int STATE_SERVICE = 12;
    public static final int STATE_SUPER_FREEZING = 13;
    public static final int STATE_SUPER_COOLING = 14;
    public static final int STATE_SUPER_HEATING = 15;
    public static final int STATE_LOCKED = 145;
    public static final int STATE_NOT_CONNECTED = 255;

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String INTERFACE = "interface";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
}
