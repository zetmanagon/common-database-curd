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

import com.alibaba.fastjson.JSON;
import com.gemantic.search.support.elastic.IndexInfo;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Properties;


public abstract class ElasticClientFactory<T extends Closeable> implements FactoryBean, InitializingBean, DisposableBean {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

	protected String[] nodes = {"localhost:9200"};

	protected String userName;
	protected String password;

	protected boolean forceTemplate = Boolean.FALSE;

	protected boolean mergeMapping = Boolean.TRUE;

	protected boolean mergeSettings = Boolean.TRUE;

	protected boolean autoscan = Boolean.TRUE;

	protected T client;

	public static final String CLASSPATH_ROOT = "/es";

	public static final String JSON_FILE_EXTENSION = ".json";

	public static final String INDEX_SETTINGS_FILE = "_settings.json";

	public static final String INDEX_MAPPINGS_FILE = "_mappings.json";

	public static final String TEMPLATE_DIR = "_template";

	public static final String ALIASES_FILE = "_index_aliases.json";

	protected abstract T buildClient() throws Exception;



	public void afterPropertiesSet() throws Exception {
		LOG.info("Starting ElasticSearch client [{}]",getTClass().getSimpleName());
		Map<String, IndexInfo> indices = Maps.newHashMap();
		Map<String, String> aliases = Maps.newHashMap();
		Map<String, String> templates = Maps.newHashMap();
		client = buildClient();
		if (autoscan) {
			initConfig(indices, aliases, templates);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("indices {}", indices);
			LOG.debug("aliases {}", aliases);
			LOG.debug("templates {}", templates);
		}
		checkClient();
		initTemplates(templates);
		initIndices(indices);
		initAliases(aliases);

	}

	public T getObject() throws Exception {
		return client;
	}

	public Class<T> getObjectType() {
		return getTClass();
	}


	protected abstract void initTemplates(Map<String, String> templates) throws Exception;

	protected abstract void initIndices(Map<String, IndexInfo> indices) throws Exception;

	protected abstract void initAliases(Map<String, String> aliases) throws Exception;

	public void checkClient() throws Exception {
		if (client == null) {
			throw new Exception(getTClass().getSimpleName()+" doesn't exist. Your factory is not properly initialized.");
		}
	}

	public void destroy() throws Exception {
		try {
			LOG.info("Closing ElasticSearch client [{}]",getTClass().getSimpleName());
			if (client != null) {
				client.close();
			}
		} catch (final Exception e) {
			LOG.error("Error closing ElasticSearch client [{}]: ",getTClass().getSimpleName(), e);
		}
	}


	protected void initConfig(Map<String, IndexInfo> indices, Map<String, String> aliases, Map<String, String> templates) {
		LOG.info("Automatic discovery is activated. Looking for definition files in classpath under " + CLASSPATH_ROOT + ".");
		PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
		Resource resourceRoot = pathResolver.getResource(CLASSPATH_ROOT);
		try {
			Resource[] resources = pathResolver.getResources("classpath:" + CLASSPATH_ROOT + "/**/*" + JSON_FILE_EXTENSION);
			for (Resource resource : resources) {
				String relPath = resource.getURI().toString().substring(resourceRoot.getURI().toString().length());
				String json = readFileInClasspath(CLASSPATH_ROOT + relPath);
				if (relPath.startsWith("/")) {
					relPath = relPath.substring(1);
				}
				if (relPath.startsWith(TEMPLATE_DIR)) {
					String tplName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(relPath, "/"), ".");
					templates.put(tplName, json);
				} else if (relPath.endsWith(ALIASES_FILE)) {
					Map<String, Object> aliaseMap = JSON.parseObject(json).getInnerMap();
					for (Map.Entry<String, Object> aliaseEntry : aliaseMap.entrySet()) {
						aliases.put(aliaseEntry.getKey(), aliaseEntry.getValue().toString());
					}
				} else {
					String index = StringUtils.substringBeforeLast(relPath, "/");
					IndexInfo indexInfo = indices.get(index);
					if (null == indexInfo) {
						indexInfo = new IndexInfo();
						indexInfo.setIndex(index);
					}
					if (relPath.endsWith(INDEX_MAPPINGS_FILE)) {
						indexInfo.setMapping(json);
					} else if (relPath.endsWith(INDEX_SETTINGS_FILE)) {
						indexInfo.setSetting(json);
					}
					indices.put(index, indexInfo);
				}
			}
		} catch (Exception e) {
			LOG.error("Automatic discovery does not succeed for finding json files in classpath under " + CLASSPATH_ROOT + ". {}", e);

		}

	}


	public  String readFileInClasspath(String url) throws Exception {
		StringBuilder bufferJSON = new StringBuilder();

		try {
			InputStream ips = getTClass().getResourceAsStream(url);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			while ((line = br.readLine()) != null) {
				bufferJSON.append(line);
			}
			br.close();
		} catch (Exception e) {
			return null;
		}

		return bufferJSON.toString();
	}


	public Class<T> getTClass() {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		return entityClass;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setForceTemplate(boolean forceTemplate) {
		this.forceTemplate = forceTemplate;
	}

	public void setMergeMapping(boolean mergeMapping) {
		this.mergeMapping = mergeMapping;
	}

	public void setMergeSettings(boolean mergeSettings) {
		this.mergeSettings = mergeSettings;
	}

	public void setAutoscan(boolean autoscan) {
		this.autoscan = autoscan;
	}
}
