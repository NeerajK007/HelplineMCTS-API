/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mcts.configure;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.iemr.mcts.utils.config.ConfigProperties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  entityManagerFactoryRef = "entityManagerFactory",
  basePackages = { "com.iemr.mcts.repository","com.iemr.mcts.repositroy", "com.iemr.mcts.data.*", "com.iemr.mcts.data.mapper.*"}
)
public class PrimaryDBConfig {
	@Primary
	  @Bean(name = "dataSource")
	  @ConfigurationProperties(prefix = "spring.datasource")
	  public DataSource dataSource() {
		 PoolConfiguration p = new PoolProperties();//change wrt to common
		  p.setMaxActive(30);
		  p.setMaxIdle(15);
		  p.setMinIdle(5);
		  p.setInitialSize(5);
		  p.setMaxWait(10000);
		  p.setMinEvictableIdleTimeMillis(15000);
		  p.setRemoveAbandoned(true);
		  p.setLogAbandoned(true);
		  p.setRemoveAbandonedTimeout(600);
		  p.setTestOnBorrow(true);
		  p.setValidationQuery("SELECT 1");
		  org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();//use one data source by removing package
		  datasource.setPoolProperties(p);
		  
		  StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setAlgorithm("PBEWithMD5AndDES");

			encryptor.setPassword("dev-env-secret");
			datasource.setUsername(encryptor.decrypt(ConfigProperties.getPropertyByName("encDbUserName")));
			datasource.setPassword(encryptor.decrypt(ConfigProperties.getPropertyByName("encDbPassword")));
	    return datasource;
	  }
	  
	  @Primary
	  @Bean(name = "entityManagerFactory")
	  public LocalContainerEntityManagerFactoryBean 
	  entityManagerFactory(
	    EntityManagerFactoryBuilder builder,
	    @Qualifier("dataSource") DataSource dataSource
	  ) {
	    return builder
	      .dataSource(dataSource)
	      .packages("com.iemr.mcts.data", "com.iemr.mcts.data.*", "com.iemr.mcts.data.mapper.*")
	      .persistenceUnit("db_iemr")
	      .build();
	  }
	    
	  @Primary
	  @Bean(name = "transactionManager")
	  public PlatformTransactionManager transactionManager(
	    @Qualifier("entityManagerFactory") EntityManagerFactory 
	    entityManagerFactory
	  ) {
	    return new JpaTransactionManager(entityManagerFactory);
	  }
}
