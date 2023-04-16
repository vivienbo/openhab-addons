/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriAirPurifierData} class is a Java wrapper for the raw JSON data about the air purifier state.
 *
 * @author Vivien Boistuaud - Initial contribution
 */
@NonNullByDefault
public class TradfriAirPurifierData extends TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriAirPurifierData.class);

    public TradfriAirPurifierData() {
        super(AIR_PURIFIER);
    }

    public TradfriAirPurifierData(JsonElement json) {
        super(AIR_PURIFIER, json);
    }

    public String getJsonString() {
        return root.toString();
    }

    public @Nullable DecimalType getFanMode() {
        JsonElement fanMode = attributes.get(FAN_MODE);
        if (fanMode != null) {
            int modeValue = fanMode.getAsInt();
            if (AIR_PURIFIER_FANMODE.contains(modeValue)) {
                return new DecimalType(modeValue);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public TradfriAirPurifierData setFanMode(DecimalType speedValue) {
        int speed = speedValue.intValue();
        if (AIR_PURIFIER_FANMODE.contains(speed)) {
            attributes.add(FAN_MODE, new JsonPrimitive(speed));
        } else {
            logger.debug("Could not set speedMode to '{}': unknown value", speed);
        }
        return this;
    }

    public @Nullable DecimalType getFanSpeed() {
        JsonElement fanSpeed = attributes.get(FAN_SPEED);
        if (fanSpeed != null) {
            int speedValue = fanSpeed.getAsInt();
            return new DecimalType(speedValue);
        } else {
            return null;
        }
    }

    public @Nullable OnOffType getDisableLed() {
        JsonElement ledOnOff = attributes.get(LED_DISABLE);
        if (ledOnOff != null) {
            boolean ledStatus = ledOnOff.getAsInt() != 0;
            return OnOffType.from(ledStatus);
        } else {
            return null;
        }
    }

    public TradfriAirPurifierData setDisableLed(OnOffType disableOnOff) {
        attributes.add(LED_DISABLE, new JsonPrimitive(OnOffType.ON.equals(disableOnOff) ? 1 : 0));
        return this;
    }
}
