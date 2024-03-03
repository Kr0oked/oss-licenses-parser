/*
 * This file is part of OssLicensesParser.
 * Copyright (C) 2024 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.philipp_bobek.oss_licenses_parser

/**
 * Holds information of a license obtained from the third_party_licenses file.
 *
 * @param libraryName The name of the library.
 * @param licenseContent The content of the license.
 */
data class ThirdPartyLicense(val libraryName: String, val licenseContent: String)
