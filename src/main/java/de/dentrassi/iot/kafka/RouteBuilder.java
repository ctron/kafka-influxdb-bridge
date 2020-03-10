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
import de.dentrassi.iot.cayenne.lpp.types.BarometricPressure;
import de.dentrassi.iot.cayenne.lpp.types.Luminosity;
import de.dentrassi.iot.cayenne.lpp.types.RelativeHumidity;
import de.dentrassi.iot.cayenne.lpp.types.Temperature;

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

                    final Luminosity wifi = msg.getEntry(21, Luminosity.class);

                    final BarometricPressure pressure = msg.getEntry(30, BarometricPressure.class);
                    final RelativeHumidity humidity = msg.getEntry(29, RelativeHumidity.class);
                    final Luminosity airq = msg.getEntry(31, Luminosity.class);
                    final Temperature temp = msg.getEntry(26, Temperature.class);

                    if (wifi != null) {
                        processWifi(x, wifi);
                    } else if (airq != null && temp != null && pressure != null && humidity != null) {
                        processAir(x, airq, temp, pressure, humidity);
                    } else {
                        x.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                    }

                })

                .log("Processed: ${body}")

                // send to influxdb
                .to("influxdb:influxdbConnection?databaseName=" + this.database + "&retentionPolicy="
                        + this.retentionPolicy);
    }

    private void processAir(final Exchange x, final Luminosity airq, final Temperature temp, final BarometricPressure pressure, final RelativeHumidity humidity) {
        final Point.Builder p = Point
                .measurement("air")
                .tag("device", x.getIn().getHeader("kafka.KEY", String.class));

        p.addField("airq", airq.getValue());
        p.addField("temp", temp.getValue());
        p.addField("pressure", pressure.getValue());
        p.addField("humidity", humidity.getValue());

        x.getIn().setBody(p.build());
    }

    private void processWifi(final Exchange x, final Luminosity wifi) {
        final Point.Builder p = Point
                .measurement("pax")
                .tag("device", x.getIn().getHeader("kafka.KEY", String.class));

        p.addField("wifi", wifi.getValue());

        x.getIn().setBody(p.build());
    }

}
