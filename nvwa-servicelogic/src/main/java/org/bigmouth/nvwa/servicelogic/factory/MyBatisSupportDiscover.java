/*
 * Copyright 2016 mopote.com
 *
 * The Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.bigmouth.nvwa.servicelogic.factory;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bigmouth.nvwa.dpl.hotswap.PlugInClassLoader;
import org.bigmouth.nvwa.dpl.hotswap.ResourceFilter;
import org.bigmouth.nvwa.dpl.plugin.PlugIn;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * MyBatis discover
 * @since 1.0
 * @author Allen Hu - (big-mouth.cn)
 */
public class MyBatisSupportDiscover extends ServiceLogicPlugInDiscover {

    @Override
    public PlugIn discover(PlugInClassLoader classloader) {
        return super.discover(classloader);
    }

    @Override
    protected void extraDiscover(PlugInClassLoader classloader, AbstractApplicationContext plugInContext) {
        try {
            loadMyBatisMapperFiles(classloader, plugInContext);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadMyBatisMapperFiles(PlugInClassLoader classloader, AbstractApplicationContext plugInContext)
            throws IOException {
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) plugInContext.getBean("sqlSessionFactory");
        if (null == sqlSessionFactory)
            return;
        Configuration configuration = sqlSessionFactory.getConfiguration();
        List<String> mappers = findPlugInMapperXmlFiles(classloader);
        if (CollectionUtils.isNotEmpty(mappers)) {
            for (String mapper : mappers) {
                ClassPathResource r = new ClassPathResource(mapper);
                XMLMapperBuilder builder = new XMLMapperBuilder(r.getInputStream(), configuration, r.toString(),
                        configuration.getSqlFragments());
                builder.parse();
            }
        }
    }
    
    private List<String> findPlugInMapperXmlFiles(PlugInClassLoader classLoader) {
        return getSearchSupport().searchResources(classLoader, new ResourceFilter() {
            
            @Override
            public boolean accept(String name) {
                return isMapperXmlFile(name);
            }
        }, false);
    }
    
    private boolean isMapperXmlFile(String name) {
        return name.endsWith(".mapper.xml");
    }
}