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
package net.sourceforge.subsonic.androidapp.billing;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public enum PurchaseMode {
    UNKNOWN(true, true, false),
    NOT_SUPPORTED(false, false, false),
    PURCHASED(false, false, false),
    NOT_PURCHASED(true, false, true);

    private final boolean shouldPurchaseButtonBeVisible;
    private final boolean shouldRestoreTransactions;
    private final boolean shouldDisplayAd;

    private PurchaseMode(boolean shouldPurchaseButtonBeVisible, boolean shouldRestoreTransactions, boolean shouldDisplayAd) {
        this.shouldPurchaseButtonBeVisible = shouldPurchaseButtonBeVisible;
        this.shouldRestoreTransactions = shouldRestoreTransactions;
        this.shouldDisplayAd = shouldDisplayAd;
    }

    public boolean shouldPurchaseButtonBeVisible() {
        return shouldPurchaseButtonBeVisible;
    }

    public boolean shouldRestoreTransactions() {
        return shouldRestoreTransactions;
    }

    public boolean shouldDisplayAd() {
        return shouldDisplayAd;
    }
}
