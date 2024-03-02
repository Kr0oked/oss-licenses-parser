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

package com.bobek.oss.licenses.parser

import java.io.InputStream

object OssLicensesParser {

    private val licenseMetadataLineRegex = """^(?<offset>\d+):(?<length>\d+) (?<libraryName>.+)$""".toRegex()

    @JvmStatic
    fun parseAllLicenses(
        thirdPartyLicensesMetadataFile: InputStream,
        thirdPartyLicensesFile: InputStream
    ): List<ThirdPartyLicense> {
        val licensesBytes = thirdPartyLicensesFile.readBytes()

        return parseMetadata(thirdPartyLicensesMetadataFile)
            .map { metadata ->
                licensesBytes.inputStream()
                    .use { licensesFile -> parseLicense(metadata, licensesFile) }
            }
    }

    @JvmStatic
    fun parseMetadata(thirdPartyLicensesMetadataFile: InputStream): List<ThirdPartyLicenseMetadata> =
        thirdPartyLicensesMetadataFile.reader()
            .useLines { licenseMetadataLines ->
                licenseMetadataLines
                    .map(::getLicenseMetadata)
                    .toList()
            }

    @JvmStatic
    private fun getLicenseMetadata(metadataLine: String): ThirdPartyLicenseMetadata {
        val matchResult = licenseMetadataLineRegex.find(metadataLine)
            ?: throw IllegalArgumentException("Metadata line invalid: $metadataLine")

        val libraryName = matchResult.groups["libraryName"]?.value
            ?: throw IllegalArgumentException("Metadata library name invalid: $metadataLine")

        val offset = matchResult.groups["offset"]?.value?.toLongOrNull()
            ?: throw IllegalArgumentException("Metadata offset invalid: $metadataLine")

        val length = matchResult.groups["length"]?.value?.toIntOrNull()
            ?: throw IllegalArgumentException("Metadata length invalid: $metadataLine")

        return ThirdPartyLicenseMetadata(libraryName, offset, length)
    }

    @JvmStatic
    fun parseLicense(metadata: ThirdPartyLicenseMetadata, thirdPartyLicensesFile: InputStream): ThirdPartyLicense {
        thirdPartyLicensesFile.skipNBytes(metadata.offset)
        val bytes = thirdPartyLicensesFile.readNBytes(metadata.length)
        if (bytes.size != metadata.length) throw EOFException()
        return ThirdPartyLicense(metadata.libraryName, bytes.decodeToString())
    }
}
