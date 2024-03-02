package com.bobek.oss.licenses.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.EOFException

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
    fun parseMetadataThrowsExceptionWhenOffsetInvalid() {
        val exception = assertThrows<IllegalArgumentException> {
            "123456789123456789123456789:13 libraryName".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }

        assertEquals("Metadata offset invalid: 123456789123456789123456789:13 libraryName", exception.message)
    }

    @Test
    fun parseMetadataThrowsExceptionWhenLengthInvalid() {
        val exception = assertThrows<IllegalArgumentException> {
            "0:123456789123456789123456789 libraryName".byteInputStream().use { metadata ->
                OssLicensesParser.parseMetadata(metadata)
            }
        }

        assertEquals("Metadata length invalid: 0:123456789123456789123456789 libraryName", exception.message)
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
