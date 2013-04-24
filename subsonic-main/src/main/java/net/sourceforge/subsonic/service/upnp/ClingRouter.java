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
package net.sourceforge.subsonic.service.upnp;

import net.sourceforge.subsonic.service.UPnPService;

/**
 * @author Sindre Mehus
 */
public class ClingRouter implements Router {

    private final UPnPService upnpService;

    // TODO: Remove
    public static void main(String[] args) throws Exception {

        UPnPService upnpService = new UPnPService();
        upnpService.init();
        System.out.println("Created UPnP service");
        Thread.sleep(3000);

        ClingRouter router = new ClingRouter(upnpService);

        router.addPortMapping(4040, 4040, 0);
        System.out.println("Added port mapping");

        router.deletePortMapping(4040, 4040);
        System.out.println("Deleted port mapping");

        Thread.sleep(2000);
    }


    public ClingRouter(UPnPService upnpService) {
        this.upnpService = upnpService;
    }

    public void addPortMapping(int externalPort, int internalPort, int leaseDuration) throws Exception {
        upnpService.addPortMapping(internalPort);
    }

    public void deletePortMapping(int externalPort, int internalPort) throws Exception {
        upnpService.deletePortMapping(internalPort);
    }
}
