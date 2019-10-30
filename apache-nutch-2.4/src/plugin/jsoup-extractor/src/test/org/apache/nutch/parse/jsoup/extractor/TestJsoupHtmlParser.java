/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.parse.jsoup.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.core.jsoup.extractor.JsoupExtractorConstants;
import org.apache.nutch.parse.ParseException;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NutchConfiguration;
import org.junit.Test;

public class TestJsoupHtmlParser {
  
  private static final String SAMPLE_CONF_FILE = "jsoup-extractor-example.xml";
  private static final String SAMPLE_URL = "https://www.youtube.com/watch?v=pzMpwW4ppRM";
  private static final String TITLE = "Large scale crawling with Apache Nutch";
  private static final String PUBLISHER = "LuceneSolrRevolution";
  
  @Test
  public void testJsoupHtmlParser() {
    Configuration conf = NutchConfiguration.create();
    conf.set(JsoupExtractorConstants.JSOUP_DOC_PROPERTY_FILE, SAMPLE_CONF_FILE);
    InputStream inputStream = null;
    try {
      URL url = new URL(SAMPLE_URL);
      inputStream = url.openStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      
      WebPage page = WebPage.newBuilder().build();
      page.setBaseUrl(new Utf8(SAMPLE_URL));
      page.setContent(ByteBuffer.wrap(sb.toString().getBytes()));
      page.setContentType(new Utf8("text/html"));
      
      ParseUtil parser = new ParseUtil(conf);
      parser.parse(SAMPLE_URL, page);
      
      for(Entry<CharSequence, ByteBuffer> entry: page.getMetadata().entrySet()) {
         System.out.println(entry.getKey().toString() + " => " + new String(entry.getValue().array(), StandardCharsets.UTF_8));
      }
      
      assertEquals(new String(page.getMetadata().get(new Utf8("title")).array(), StandardCharsets.UTF_8).replaceAll("\\t", ""), TITLE);
      assertEquals(new String(page.getMetadata().get(new Utf8("publisherName")).array(), StandardCharsets.UTF_8).replaceAll("\\t", ""), PUBLISHER);
      
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
      fail(ex.toString());
    } catch(IOException ex) {
      ex.printStackTrace();
      fail(ex.toString());
    } 
    catch (ParseException ex) {
      ex.printStackTrace();
      fail(ex.toString());
    }
    finally {
        try {
          if(inputStream != null) {
            inputStream.close();
          }
        } catch (IOException ex) {
          ex.printStackTrace();
          fail(ex.toString());
        }
    }
  }
  
}
