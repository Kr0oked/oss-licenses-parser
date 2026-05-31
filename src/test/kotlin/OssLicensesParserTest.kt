/*
 * This file is part of OssLicensesParser.
 * Copyright (C) 2026 Philipp Bobek <philipp.bobek@mailbox.org>
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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.EOFException
import java.io.InputStream

class OssLicensesParserTest {

    private val metadataText = "0:16 libraryName A\n17:16 libraryName B"
    private val licensesContent = "licenseContent A\nlicenseContent B"
    private val licenseMetadataA = ThirdPartyLicenseMetadata("libraryName A", 0L, 16)
    private val licenseMetadataB = ThirdPartyLicenseMetadata("libraryName B", 17L, 16)
    private val licenseA = ThirdPartyLicense("libraryName A", "licenseContent A")
    private val licenseB = ThirdPartyLicense("libraryName B", "licenseContent B")

    @Test
    fun parseAllLicenses() {
        val actual = metadataText.byteInputStream().use { metadata ->
            licensesContent.byteInputStream().use { licenses ->
                OssLicensesParser.parseAllLicenses(metadata, licenses)
            }
        }

        val expected = listOf(licenseA, licenseB)
        assertIterableEquals(expected, actual)
    }

    @Test
    fun parseMetadata() {
        val actual = metadataText.byteInputStream().use { metadata ->
            OssLicensesParser.parseMetadata(metadata)
        }

        val expected = listOf(licenseMetadataA, licenseMetadataB)
        assertIterableEquals(expected, actual)
    }

    @Test
    fun parseMetadataThrowsExceptionWhenLineInvalid() {
        val exception = assertThrows<IllegalArgumentException> {
            "invalid".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }

        assertEquals("Metadata line invalid: invalid", exception.message)
    }

    @Test
    fun parseMetadataThrowsExceptionWhenOffsetOverflows() {
        val exception = assertThrows<IllegalArgumentException> {
            "123456789123456789123456789:13 libraryName".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }

        assertEquals("Metadata offset overflow: 123456789123456789123456789:13 libraryName", exception.message)
    }

    @Test
    fun parseMetadataThrowsExceptionWhenLengthOverflows() {
        val exception = assertThrows<IllegalArgumentException> {
            "0:123456789123456789123456789 libraryName".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }

        assertEquals("Metadata length overflow: 0:123456789123456789123456789 libraryName", exception.message)
    }

    @Test
    fun parseMetadataThrowsExceptionWhenLengthExceedsMaximum() {
        assertThrows<IllegalArgumentException> {
            "0:1048577 libraryName".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }
    }

    @Test
    fun parseLicenseA() {
        val actual = licensesContent.byteInputStream().use { licenses ->
            OssLicensesParser.parseLicense(licenseMetadataA, licenses)
        }

        assertEquals(licenseA, actual)
    }

    @Test
    fun parseLicenseB() {
        val actual = licensesContent.byteInputStream().use { licenses ->
            OssLicensesParser.parseLicense(licenseMetadataB, licenses)
        }

        assertEquals(licenseB, actual)
    }

    @Test
    fun parseLicenseWithPartialSkipStream() {
        val actual = PartialSkipInputStream(licensesContent.byteInputStream()).use { licenses ->
            OssLicensesParser.parseLicense(licenseMetadataB, licenses)
        }

        assertEquals(licenseB, actual)
    }

    @Test
    fun parseLicenseWithPartialReadStream() {
        val actual = PartialReadInputStream(licensesContent.byteInputStream()).use { licenses ->
            OssLicensesParser.parseLicense(licenseMetadataB, licenses)
        }

        assertEquals(licenseB, actual)
    }

    @Test
    fun parseLicenseThrowsExceptionWhenEndOfFileReachedBeforeOffset() {
        assertThrows<EOFException> {
            "123".byteInputStream().use { licenses ->
                val metadata = ThirdPartyLicenseMetadata("libraryName", 4L, 0)
                OssLicensesParser.parseLicense(metadata, licenses)
            }
        }
    }

    @Test
    fun parseLicenseThrowsExceptionWhenEndOfFileReachedBeforeAllBytesRead() {
        assertThrows<EOFException> {
            "123".byteInputStream().use { licenses ->
                val metadata = ThirdPartyLicenseMetadata("libraryName", 0L, 4)
                OssLicensesParser.parseLicense(metadata, licenses)
            }
        }
    }
}

/** Wraps a stream so that skip() advances at most 1 byte per call, simulating a chunked stream. */
private class PartialSkipInputStream(private val delegate: InputStream) : InputStream() {
    override fun read(): Int = delegate.read()
    override fun skip(n: Long): Long = delegate.skip(minOf(n, 1))
}

/** Wraps a stream so that read() returns at most 1 byte per call, simulating a chunked stream. */
private class PartialReadInputStream(private val delegate: InputStream) : InputStream() {
    override fun read(): Int = delegate.read()
    override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, minOf(len, 1))
}
