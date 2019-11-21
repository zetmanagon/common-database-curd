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
import com.gemantic.search.utils.ESRestUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;


public class ElasticRestClient extends ElasticClientFactory<RestHighLevelClient> {

	@Override
	protected RestHighLevelClient buildClient() throws Exception {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)){
			credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(userName, password));  //es账号密码
		}
		List<HttpHost> httpHosts= Lists.newArrayList();
		for (String node: nodes) {
			String arr[]=node.split(":");
			httpHosts.add(new HttpHost(arr[0],Integer.valueOf(arr[1])));
		}
		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
				RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]))
						.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
							public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
								httpClientBuilder.disableAuthCaching();
								return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
							}
						})
		);
		return restHighLevelClient;
	}

    /**
     * Init templates if needed.
     * <p>
     * Note that you can force to recreate template using
     *
     * @throws Exception
     */
    protected void initTemplates(Map<String, String> templates) throws Exception {
        if (MapUtils.isEmpty(templates)) {
            return;
        }
        for (Map.Entry<String, String> tplEntry : templates.entrySet()) {
            String templateName = tplEntry.getKey();
            String templateSource = tplEntry.getValue();
            Assert.hasText(templateSource, "Can not read template [" + templateName + "]. Check that templates is not empty.");
            try {
                boolean isExist = ESRestUtils.isTemplateExist(client,templateName);
                if (forceTemplate && isExist) {
                    boolean isDelete = ESRestUtils.deleteTemplate(client,templateName);
                    if (!isDelete) {
                        LOG.error("delete template [{}] error", templateName);
                        continue;
                    }
                }
                boolean isPut = ESRestUtils.putTemplate(client,templateName, templateSource);
                if (!isPut) {
                    LOG.error("put template [{}] error {}", templateName, templateSource);
                }
            } catch (Exception e) {
                LOG.error("initTemplates [{}] source {} error {}", templateName, templateSource, e);
            }


        }

    }

    protected void initIndices(Map<String, IndexInfo> indices) throws Exception {
        if (MapUtils.isEmpty(indices)) {
            return;
        }
        for (Map.Entry<String, IndexInfo> indexEntry : indices.entrySet()) {
            IndexInfo index = indexEntry.getValue();
            try {
                boolean isExist = ESRestUtils.isIndexExist(client,index.getIndex());
                if (isExist) {
                    if (mergeSettings) {
                        boolean isMerge = ESRestUtils.mergeSetting(client,index);
                        if (!isMerge) {
                            LOG.error("index [{}] settings error {}", index.getIndex(), index.getSetting());
                        }
                    }
                } else {
                    boolean isCreate = ESRestUtils.createIndex(client,index);
                    if (!isCreate) {
                        LOG.error("index [{}] create error {}", index.getIndex(), index.getSetting());
                    }
                }
                if(mergeMapping) {
                    boolean isMapping = ESRestUtils.putMapping(client, index);
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
    protected void initAliases(Map<String, String> aliases) throws Exception {
            ESRestUtils.initAliases(client,aliases);
    }


}
