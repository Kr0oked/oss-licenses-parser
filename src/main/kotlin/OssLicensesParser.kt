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

import java.io.EOFException
import java.io.InputStream

/**
 * Parser for the text files generated by the OSS Licenses Gradle Plugin.
 * @see <a href="https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin">
 *     OSS Licenses Gradle Plugin</a>
 */
object OssLicensesParser {

    private val licenseMetadataLineRegex = """^(?<offset>\d+):(?<length>\d+) (?<libraryName>.+)$""".toRegex()

    /**
     * Parses all licenses contained in the third_party_licenses_metadata and third_party_licenses files.
     *
     * Generally it is advised to use parseMetadata instead of this method if you only want to display a list of all
     * libraries.
     * Later on the parseLicense method can be used to display the license content of a specific library.
     * This approach can help saving resources.
     *
     * @param thirdPartyLicensesMetadataFile Content of the third_party_licenses_metadata file.
     * @param thirdPartyLicensesFile Content of the third_party_licenses file.
     * @return List of all licenses.
     * @throws java.io.IOException If an I/O error occurs.
     * @throws IllegalArgumentException If content is invalid.
     */
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

    /**
     * Parses the licenses metadata contained in the third_party_licenses_metadata file.
     *
     * Later on the parseLicense method can be used to display the license content of a specific library.
     *
     * @param thirdPartyLicensesMetadataFile Content of the third_party_licenses_metadata file.
     * @return List of license metadata.
     * @throws java.io.IOException If an I/O error occurs.
     * @throws IllegalArgumentException If content is invalid.
     */
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

    /**
     * Parses a license contained in the third_party_licenses file.
     *
     * @param metadata License metadata obtained from the parseMetadata method.
     * @param thirdPartyLicensesFile Content of the third_party_licenses file.
     * @return The license.
     * @throws java.io.IOException If an I/O error occurs.
     */
    @JvmStatic
    fun parseLicense(metadata: ThirdPartyLicenseMetadata, thirdPartyLicensesFile: InputStream): ThirdPartyLicense {
        thirdPartyLicensesFile.skipNBytes(metadata.offset)
        val bytes = thirdPartyLicensesFile.readNBytes(metadata.length)
        if (bytes.size != metadata.length) throw EOFException()
        return ThirdPartyLicense(metadata.libraryName, bytes.decodeToString())
    }
}
