/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gemantic.search.factory;

import com.gemantic.search.support.elastic.IndexInfo;
import com.gemantic.search.utils.ESUtils;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetSocketAddress;
import java.util.Map;


public class ElasticTransportClient extends ElasticClientFactory<Client> {


	private String clusterName = "elasticsearch";

	@Override
	protected Client buildClient() throws Exception {
		Settings settings = Settings.builder()
		        .put("cluster.name", this.clusterName).build();
		PreBuiltTransportClient preBuiltClient = new PreBuiltTransportClient(settings);
		for (String node:nodes) {
			preBuiltClient.addTransportAddress(toAddress(node));
		}


		return preBuiltClient;
	}

	@Override
	protected void initAliases(Map<String, String> aliases) throws Exception {
		LOG.info("not support initAliases");
		return;
	}

	@Override
	protected void initIndices(Map<String, IndexInfo> indices) throws Exception {
		if (MapUtils.isEmpty(indices)) {
			return;
		}
		for (Map.Entry<String, IndexInfo> indexEntry : indices.entrySet()) {
			IndexInfo index = indexEntry.getValue();
			try {
				boolean isExist = ESUtils.isIndexExist(client,index.getIndex());
				if (isExist) {
					if (mergeSettings) {
						boolean isMerge = ESUtils.mergeSetting(client,index);
						if (!isMerge) {
							LOG.error("index [{}] settings error {}", index.getIndex(), index.getSetting());
						}
					}
				} else {
					boolean isCreate = ESUtils.createIndex(client,index);
					if (!isCreate) {
						LOG.error("index [{}] create error {}", index.getIndex(), index.getSetting());
					}
				}
				if(mergeMapping) {
					boolean isMapping = ESUtils.putMapping(client, index);
					if (!isMapping) {
						LOG.error("index [{}] mapping error {}", index.getIndex(), index.getMapping());
					}
				}
			} catch (Exception e) {
				LOG.error("init index [{}] error {} {}",index.getIndex(),index,e);
			}
		}
	}

	@Override
	protected void initTemplates(Map<String, String> templates) throws Exception {
		LOG.info("not support initTemplates");
		return;
	}


	private TransportAddress toAddress(String address) {
		if (address == null) {
			return null;
		}
		
		String[] splitted = address.split(":");
		int port = 9300;
		if (splitted.length > 1) {
			port = Integer.parseInt(splitted[1]);
		}
		
		return new TransportAddress(new InetSocketAddress(splitted[0], port));
	}


	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	
	
}
