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

import java.security.Security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.dentrassi.crypto.pem.PemKeyStoreProvider;

@SpringBootApplication
public class Application {

	public static void main(final String[] args) {
		Security.addProvider(new PemKeyStoreProvider());

		SpringApplication.run(Application.class, args);
	}

}
