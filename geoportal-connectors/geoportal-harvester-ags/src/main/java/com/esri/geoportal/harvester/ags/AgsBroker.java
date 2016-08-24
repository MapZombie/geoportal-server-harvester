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
package com.esri.geoportal.harvester.ags;

import com.esri.geoportal.commons.ags.client.AgsClient;
import com.esri.geoportal.commons.ags.client.ContentResponse;
import com.esri.geoportal.commons.ags.client.ServerResponse;
import com.esri.geoportal.commons.ags.client.ServiceInfo;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MetaHandler;
import com.esri.geoportal.commons.meta.ObjectAttribute;
import com.esri.geoportal.commons.meta.StringAttribute;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.mime.MimeType;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

/**
 * Ags broker.
 */
/*package*/ class AgsBroker implements InputBroker {

  private final AgsConnector connector;
  private final AgsBrokerDefinitionAdaptor definition;
  private final MetaHandler metaHandler;
  private AgsClient client;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   * @param metaHandler meta handler
   */
  public AgsBroker(AgsConnector connector, AgsBrokerDefinitionAdaptor definition, MetaHandler metaHandler) {
    this.connector = connector;
    this.definition = definition;
    this.metaHandler = metaHandler;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    // nothing to initialize
  }

  @Override
  public void terminate() {
    // nothing to terminate
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("AGS", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public Iterator iterator(Map<String, Object> attributes) throws DataInputException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private List<ServerResponse> listResponses(String rootFolder) throws URISyntaxException, IOException {
    ArrayList<ServerResponse> responses = new ArrayList<>();

    ContentResponse content = client.listContent(rootFolder);
    if (content.services != null) {
      for (ServiceInfo si : content.services) {
        ServerResponse response = client.readServiceInformation(rootFolder, si);
        responses.add(response);
      }
    }
    if (content.folders != null) {
      for (String f : content.folders) {
        String subFolder = (rootFolder != null ? rootFolder + "/" : "") + f;
        List<ServerResponse> subResponses = listResponses(subFolder);
        responses.addAll(subResponses);
      }
    }

    return responses;
  }

  private class AgsIterator implements InputBroker.Iterator {

    private final java.util.Iterator<ServerResponse> iterator;

    public AgsIterator(java.util.Iterator<ServerResponse> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      return iterator.hasNext();
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        ServerResponse next = iterator.next();
        HashMap<String, Attribute> attributes = new HashMap<>();
        attributes.put("title", new StringAttribute(StringUtils.defaultString(next.mapName, next.name)));
        attributes.put("description", new StringAttribute(StringUtils.defaultString(next.description, next.serviceDescription)));
        attributes.put("resource.url", new StringAttribute(next.url));

        Document document = metaHandler.create(new ObjectAttribute(attributes));

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        
        byte [] bytes = writer.toString().getBytes("UTF-8");
        
        return new SimpleDataReference(getBrokerUri(), next.url, null, URI.create(next.url), bytes, MimeType.APPLICATION_JSON);
      } catch (TransformerException|TransformerFactoryConfigurationError|IOException|URISyntaxException ex) {
        throw new DataInputException(AgsBroker.this, String.format("Error creating data reference for ArcGIS Server service"), ex);
      }
    }

  }
}
