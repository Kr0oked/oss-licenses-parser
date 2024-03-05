/*
 * This file is part of OssLicensesParser.
 * Copyright (C) 2024 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OssLicensesParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.philipp_bobek.oss_licenses_parser

/**
 * Holds license metadata information obtained from the third_party_licenses_metadata file.
 * This information is needed to extract the license content from the third_party_licenses file.
 *
 * @param libraryName The name of the library.
 * @param offset The offset of bytes at which the license content starts.
 * @param length The length of bytes that the license content has.
 */
data class ThirdPartyLicenseMetadata(val libraryName: String, val offset: Long, val length: Int)
