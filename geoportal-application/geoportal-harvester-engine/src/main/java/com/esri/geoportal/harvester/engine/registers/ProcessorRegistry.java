/*
 * Copyright 2016 Esri, Inc.
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
package com.esri.geoportal.harvester.engine.registers;

import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessor;
import java.util.HashMap;

/**
 * Processor registry.
 */
public class ProcessorRegistry extends HashMap<String,Processor>{
  private final Processor defaultProcessor = new DefaultProcessor();
  
  public Processor getDefaultProcessor() {
    return defaultProcessor;
  }
}
