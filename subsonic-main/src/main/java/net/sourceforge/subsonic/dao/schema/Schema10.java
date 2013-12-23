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

 Copyright 2013 (C) Jan Riechers
 */
package net.sourceforge.subsonic.dao.schema;

import net.sourceforge.subsonic.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Used for creating and evolving the database schema.
 * This class implements the database schema for Subsonic version 4.0.
 *
 * @author Sindre Mehus
 */
public class Schema10 extends Schema {

    private static final Logger LOG = Logger.getLogger(Schema10.class);

    @Override
    public void execute(JdbcTemplate template) {

        // Reset stream byte count since they have been wrong in earlier releases.
        template.execute("CHECKPOINT DEFRAG");
        LOG.info("Defrag database.");
    }
}
