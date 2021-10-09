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

@XStreamAlias("Device")
public class DeviceQuery {
    @XStreamAlias("Components")
    private Components components;

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }
}
