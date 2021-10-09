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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Eagle200Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Louis Erickson - Ported to OH3.x
 */
@NonNullByDefault
public class Eagle200Configuration {

    @Nullable
    public String hostname;
    @Nullable
    public String cloudid;
    @Nullable
    public String installcode;

    @SuppressWarnings("null")
    public boolean isComplete() {

        if (hostname == null || hostname.isEmpty()) {
            return false;
        }
        if (cloudid == null || cloudid.isEmpty()) {
            return false;
        }
        if (installcode == null || installcode.isEmpty()) {
            return false;
        }
        return true;
    }
}
