/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.backend.dao.schema;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Used for creating and evolving the database schema.
 * This class implementes the database schema for Subsonic Backend version 3.0.
 *
 * @author Sindre Mehus
 */
public class Schema30 extends Schema {
    private static final Logger LOG = Logger.getLogger(Schema30.class);

    public void execute(JdbcTemplate template) {

        if (!tableExists(template, "subscription")) {
            LOG.info("Database table 'subscription' not found.  Creating it.");
            template.execute("create cached table subscription (" +
                    "id identity," +
                    "subscr_id varchar not null," +  // PayPal subscription ID
                    "payer_id varchar not null," +  // PayPal payer ID
                    "btn_id varchar," +  // PayPal button ID
                    "email varchar_ignorecase," +
                    "first_name varchar," +
                    "last_name varchar," +
                    "country varchar," +
                    "period varchar," +
                    "amount double," +  // Total amount
                    "currency varchar," +
                    "valid_from datetime," +
                    "valid_to datetime," +
                    "processing_status varchar not null," +
                    "created datetime not null," +
                    "updated datetime not null)");
            template.execute("create index idx_subscription_subscr_id on subscription(subscr_id)");
            template.execute("create index idx_subscription_payer_id on subscription(payer_id)");
            template.execute("create index idx_subscription_created on subscription(created)");  // TODO: Needed?
            template.execute("create index idx_subscription_processing_status on subscription(processing_status)");
            template.execute("create index idx_subscription_email on subscription(email)");

            LOG.info("Database table 'subscription' was created successfully.");
        }


        if (!tableExists(template, "subscription_payment")) {
            LOG.info("Database table 'subscription_payment' not found.  Creating it.");
            template.execute("create cached table subscription_payment (" +
                    "id identity," +
                    "subscr_id varchar not null," +  // PayPal subscription ID
                    "payer_id varchar not null," +  // PayPal payer ID
                    "btn_id varchar," +  // PayPal button ID
                    "ipn_track_id varchar," +  // PayPal IPN track ID
                    "email varchar_ignorecase," +
                    "amount double," +
                    "currency varchar," +
                    "created datetime not null)");
            template.execute("create index idx_subscription_payment_email on subscription_payment(email)");

            LOG.info("Database table 'subscription_payment' was created successfully.");
        }

        if (!tableExists(template, "subscription_notification")) {
            LOG.info("Database table 'subscription_notification' not found.  Creating it.");
            template.execute("create cached table subscription_notification (" +
                    "id identity," +
                    "subscr_id varchar not null," +  // PayPal subscription ID
                    "payer_id varchar not null," +  // PayPal payer ID
                    "btn_id varchar," +  // PayPal button ID
                    "ipn_track_id varchar," +  // PayPal IPN track ID
                    "txn_type varchar not null," +
                    "email varchar_ignorecase," +
                    "created datetime not null)");
            template.execute("create index idx_subscription_notification_email on subscription_notification(email)");

            LOG.info("Database table 'subscription_notification' was created successfully.");
        }

    }
}