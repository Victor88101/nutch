/**
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

package org.apache.nutch.indexer.enterprise;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.TableUtil;
import org.apache.solr.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

/**
 * Adds basic searchable fields to a document. The fields are: host - add host
 * as un-stored, indexed and tokenized url - url is both stored and indexed, so
 * it's both searchable and returned. This is also a required field. content -
 * content is indexed, so that it's searchable, but not stored in index title -
 * title is stored and indexed cache - add cached content/summary display
 * policy, if available tstamp - add timestamp when fetched, for deduplication
 */
public class EnterpriseIndexingFilter implements IndexingFilter {
	public static final Logger LOG = LoggerFactory.getLogger(EnterpriseIndexingFilter.class);

	// private int MAX_TITLE_LENGTH;
	private Configuration conf;

	private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

	static {
		FIELDS.add(WebPage.Field.TITLE);
		FIELDS.add(WebPage.Field.TEXT);
		FIELDS.add(WebPage.Field.FETCH_TIME);
	}

	/**
	 * The {@link BasicIndexingFilter} filter object which supports boolean
	 * configurable value for length of characters permitted within the
	 * title @see {@code indexer.max.title.length} in nutch-default.xml
	 * 
	 * @param doc
	 *            The {@link NutchDocument} object
	 * @param url
	 *            URL to be filtered for anchor text
	 * @param page
	 *            {@link WebPage} object relative to the URL
	 * @return filtered NutchDocument
	 */
	public NutchDocument filter(NutchDocument doc, String url, WebPage page) throws IndexingException {

		String reprUrl = null;
		// if (page.isReadable(WebPage.Field.REPR_URL.getIndex()))
		// {
		reprUrl = TableUtil.toString(page.getReprUrl());
		// }

		String host = null;
		Connection conn = null; // 定义一个MYSQL链接对象
		try {

			URL u;
			if (reprUrl != null) {
				u = new URL(reprUrl);
			} else {
				u = new URL(url);
			}
			host = u.getHost();

			Properties prop = new Properties();
			InputStream path = Thread.currentThread().getContextClassLoader().getResourceAsStream("EnterpriseIndexing.properties");
			prop.load(path);

			Class.forName("com.mysql.jdbc.Driver").newInstance(); // MYSQL驱动
			conn = DriverManager.getConnection(prop.getProperty("DBUrl"), prop.getProperty("DBUser"),
					prop.getProperty("DBPassword")); // 链接本地MYSQL

			CallableStatement cstmt = conn.prepareCall("{CALL TTROS_GroupMng.usp_SOLR_GetInfoForIndex(?)}");
			cstmt.setString(1, host);

			ResultSet rs = cstmt.executeQuery();

			if (rs.next()) {
				doc.add("EnterpriseID", rs.getString("EnterpriseID"));
				doc.add("EnterpriseName", rs.getString("EnterpriseName"));				
				doc.add("NetMediaID", rs.getString("NetMediaID"));
				doc.add("MediaType", rs.getString("MediaType"));
				
				 if (rs.getString("ETradeAreaIDs") != null && rs.getString("ETradeAreaNames") != null) {
				      String[] ETradeAreaIDs = rs.getString("ETradeAreaIDs").split(",");
				      String[] ETradeAreaNames = rs.getString("ETradeAreaNames").split(",");
				      for (int i = 0; i < ETradeAreaIDs.length; i++) {
							doc.add("ETradeAreaIDs", ETradeAreaIDs[i]);
							doc.add("ETradeAreaNames", ETradeAreaNames[i]);
				      }
					}
				 if (rs.getString("CategoryIDs") != null) {
				      String[] CategoryIDs = rs.getString("CategoryIDs").split(",");
				      for (int i = 0; i < CategoryIDs.length; i++) {
							doc.add("CategoryIDs", CategoryIDs[i]);
				      }
					}
			}
			conn.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	}

	public void addIndexBackendOptions(Configuration conf) {
	}

	/**
	 * Set the {@link Configuration} object
	 */
	public void setConf(Configuration conf) {
		this.conf = conf;
		/*
		 * this.MAX_TITLE_LENGTH = conf.getInt("indexer.max.title.length", 100);
		 * LOG.info("Maximum title length for indexing set to: " +
		 * this.MAX_TITLE_LENGTH);
		 */
	}

	/**
	 * Get the {@link Configuration} object
	 */
	public Configuration getConf() {
		return this.conf;
	}

	/**
	 * Gets all the fields for a given {@link WebPage} Many datastores need to
	 * setup the mapreduce job by specifying the fields needed. All extensions
	 * that work on WebPage are able to specify what fields they need.
	 */
	@Override
	public Collection<WebPage.Field> getFields() {
		return FIELDS;
	}

}
