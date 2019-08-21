/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/

package de.dentrassi.iot.kafka;

import org.influxdb.InfluxDB;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.influx.InfluxDbAutoConfiguration;
import org.springframework.boot.autoconfigure.influx.InfluxDbOkHttpClientBuilderProvider;
import org.springframework.boot.autoconfigure.influx.InfluxDbProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient.Builder;

@Configuration
public class CustomInfluxDbAutoConfiguration extends InfluxDbAutoConfiguration {

	public CustomInfluxDbAutoConfiguration(InfluxDbProperties properties,
			ObjectProvider<InfluxDbOkHttpClientBuilderProvider> builder, ObjectProvider<Builder> deprecatedBuilder) {
		super(properties, builder, deprecatedBuilder);
	}

	@Bean("influxdbConnection")
	@Override
	public InfluxDB influxDb() {
		return super.influxDb();
	}

}
