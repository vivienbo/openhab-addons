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
package org.openhab.binding.tradfri.internal.handler;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tradfri.internal.TradfriCoapClient;
import org.openhab.binding.tradfri.internal.model.TradfriAirPurifierData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriAirPurifierHandler} is responsible for handling commands and status updates
 * for Starkvind Air Purifiers.
 *
 * @author Vivien Boistuaud - Initial contribution
 */
@NonNullByDefault
public class TradfriAirPurifierHandler extends TradfriThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriAirPurifierHandler.class);

    public TradfriAirPurifierHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (active) {
            if (command instanceof RefreshType) {
                TradfriCoapClient coapClient = this.coapClient;
                if (coapClient != null) {
                    logger.debug("Refreshing channel {}", channelUID);
                    coapClient.asyncGet(this);
                } else {
                    logger.debug("coapClient is null!");
                }
                return;
            }

            switch (channelUID.getId()) {
                case CHANNEL_FAN_MODE:
                    handleFanModeCommand(command);
                    break;
                case CHANNEL_DISABLE_LED:
                    handleDisableLed(command);
                    break;
                case CHANNEL_LOCK_BUTTON:
                    handleLockButton(command);
                    break;
                default:
                    logger.error("Unknown channel UID {}", channelUID);
            }
        }
    }

    private void handleFanModeCommand(Command command) {
        if (command instanceof DecimalType) {
            set(new TradfriAirPurifierData().setFanMode((DecimalType) command).getJsonString());
        } else if (command instanceof QuantityType) {
            DecimalType decimalCommand = new DecimalType(((QuantityType<?>) command));
            set(new TradfriAirPurifierData().setFanMode(decimalCommand).getJsonString());
        } else {
            logger.debug("Cannot handle command '{}' of type {} for channel '{}'", command, command.getClass(),
                    CHANNEL_FAN_MODE);
        }
    }

    private void handleDisableLed(Command command) {
        if (command instanceof OnOffType) {
            set(new TradfriAirPurifierData().setDisableLed((OnOffType) command).getJsonString());
        } else {
            logger.debug("Cannot handle command '{}' of type {} for channel '{}'", command, command.getClass(),
                    CHANNEL_DISABLE_LED);
        }
    }

    private void handleLockButton(Command command) {
        if (command instanceof OnOffType) {
            set(new TradfriAirPurifierData().setLockPhysicalButton((OnOffType) command).getJsonString());
        } else {
            logger.debug("Cannot handle command '{}' of type {} for channel '{}'", command, command.getClass(),
                    CHANNEL_DISABLE_LED);
        }
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            TradfriAirPurifierData state = new TradfriAirPurifierData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            DecimalType fanMode = state.getFanMode();
            if (fanMode != null) {
                updateState(CHANNEL_FAN_MODE, fanMode);
            }

            DecimalType fanSpeed = state.getFanSpeed();
            if (fanSpeed != null) {
                updateState(CHANNEL_FAN_SPEED, fanSpeed);
            }

            OnOffType disableLed = state.getDisableLed();
            if (disableLed != null) {
                updateState(CHANNEL_DISABLE_LED, disableLed);
            }

            OnOffType lockPhysicalButton = state.getLockPhysicalButton();
            if (lockPhysicalButton != null) {
                updateState(CHANNEL_LOCK_BUTTON, lockPhysicalButton);
            }

            State airQualityPm25 = state.getAirQualityPM25();
            if (airQualityPm25 != null) {
                updateState(CHANNEL_AIR_QUALITY_PM25, airQualityPm25);
            }

            State airQualityRating = state.getAirQualityRating();
            if (airQualityRating != null) {
                updateState(CHANNEL_AIR_QUALITY_RATING, airQualityRating);
            }

            logger.debug(
                    "Updating thing for airPurifierId {} to state {fanMode: {}, fanSpeed: {}, disableLed: {}, lockButton: {}, firmwareVersion: {}, modelId: {}, vendor: {}}",
                    state.getDeviceId(), state.getFanMode(), state.getFanSpeed(), state.getDisableLed(),
                    state.getLockPhysicalButton(), state.getFirmwareVersion(), state.getModelId(), state.getVendor());
        }
    }
}
