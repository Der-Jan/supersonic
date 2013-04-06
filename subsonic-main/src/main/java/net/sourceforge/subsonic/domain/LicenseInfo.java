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
package net.sourceforge.subsonic.domain;

import java.util.Date;

/**
 * Controller for the "Podcast receiver" page.
 *
 * @author Sindre Mehus
 */
public class LicenseInfo {

    private final boolean licenseValid;
    private final Date trialExpires;
    private final Date licenseExpires;

    public LicenseInfo(boolean licenseValid, Date trialExpires, Date licenseExpires) {
        this.licenseValid = licenseValid;
        this.trialExpires = trialExpires;
        this.licenseExpires = licenseExpires;
    }

    public boolean isLicenseValid() {
        return licenseValid;
    }

    public boolean isTrial() {
        return trialExpires != null && !licenseValid;
    }

    public boolean isTrialExpired() {
        return trialExpires != null && trialExpires.before(new Date());
    }

    public Date getTrialExpires() {
        return trialExpires;
    }

    public Date getLicenseExpires() {
        return licenseExpires;
    }
}
