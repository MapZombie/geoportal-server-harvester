/*
 * Copyright 2016 Esri, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.harvester.impl;

import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsConfigImpl;
import com.esri.geoportal.commons.robots.BotsMode;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Bots attributes adaptor.
 */
public class BotsAttributesAdaptor extends AbstractMap<String,String> {

  protected static final String P_BOTS_AGENT = "bots.config.agent";
  protected static final String P_BOTS_ENABLED = "bots.config.enabled";
  protected static final String P_BOTS_OVERRIDE = "bots.config.override";
  protected static final String P_BOTS_MODE = "bots.mode";

  protected final Map<String, String> attributes;

  /**
   * Creates instance of the adaptor
   *
   * @param attributes attributes
   */
  public BotsAttributesAdaptor(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public Set<Map.Entry<String, String>> entrySet() {
    return attributes.entrySet();
  }

  /**
   * Gets bots mode.
   *
   * @return bots mode
   */
  public BotsMode getBotsMode() {
    String sBootMode = attributes.get(P_BOTS_MODE);
    return BotsMode.parseMode(sBootMode);
  }

  /**
   * Sets bots mode.
   *
   * @param botsMode bots mode
   */
  public void setBotsMode(BotsMode botsMode) {
    attributes.put(P_BOTS_MODE, botsMode.name());
  }

  /**
   * Gets bots config.
   *
   * @return bots config
   */
  public BotsConfig getBotsConfig() {
    return new BotsConfigImpl(
            StringUtils.defaultIfBlank(attributes.get(P_BOTS_AGENT), BotsConfig.DEFAULT.getUserAgent()), 
            BooleanUtils.toBoolean(StringUtils.defaultIfBlank(attributes.get(P_BOTS_ENABLED), Boolean.toString(BotsConfig.DEFAULT.isEnabled()))), 
            BooleanUtils.toBoolean(StringUtils.defaultIfBlank(attributes.get(P_BOTS_OVERRIDE), Boolean.toString(BotsConfig.DEFAULT.isOverride())))
    );
  }

  /**
   * Sets bots config.
   *
   * @param botsConfig bots config
   */
  public void setBotsConfig(BotsConfig botsConfig) {
    attributes.put(P_BOTS_AGENT, botsConfig.getUserAgent());
    attributes.put(P_BOTS_ENABLED, Boolean.toString(botsConfig.isEnabled()));
    attributes.put(P_BOTS_OVERRIDE, Boolean.toString(botsConfig.isOverride()));
  }
}