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

import java.nio.ByteBuffer;

import org.apache.camel.Exchange;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.dentrassi.iot.cayenne.lpp.Message;
import de.dentrassi.iot.cayenne.lpp.Parser;
import de.dentrassi.iot.cayenne.lpp.types.Luminosity;

@Component
public class RouteBuilder extends org.apache.camel.builder.RouteBuilder {

    private @Value("${bridge.influxdb.database}") String database;
    private @Value("${bridge.influxdb.retention-policy}") String retentionPolicy;

    @Override
    public void configure() throws Exception {
        from("kafka:{{kafka.topic}}?brokers={{kafka.brokers}}&groupId={{kafka.group-id}}")

                .log("Before: ${body} - ${headers}")

                // decode base64
                .unmarshal().base64()

                // convert to influxdb
                .process(x -> {

                    final Message msg = Parser.parseMessage(x.getIn().getBody(ByteBuffer.class));

                    final Luminosity wifi = msg.getEntry(15, Luminosity.class);
                    if (wifi == null) {
                        x.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                        return;
                    }

                    final Point.Builder p = Point
                            .measurement("pax")
                            .tag("device", x.getIn().getHeader("kafka.KEY", String.class));

                    p.addField("wifi", wifi.getValue());

                    x.getIn().setBody(p.build());

                })

                .log("Processed: ${body}")

                // send to influxdb
                .to("influxdb:influxdbConnection?databaseName=" + this.database + "&retentionPolicy="
                        + this.retentionPolicy);
    }

}
