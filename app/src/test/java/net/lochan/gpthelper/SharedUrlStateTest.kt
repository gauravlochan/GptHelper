package net.lochan.gpthelper

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.net.URL

class SharedUrlStateTest {
    @Before
    fun setup() {
        // Clear the state before each test
        SharedUrlState.clear()
    }

    @Test
    fun `addUrl adds URL to the beginning of the list`() {
        // Arrange
        val url1 = "https://example.com/1"
        val url2 = "https://example.com/2"

        // Act
        SharedUrlState.addUrl(url1)
        SharedUrlState.addUrl(url2)

        // Assert
        val urls = SharedUrlState.urls
        assertEquals(2, urls.size)
        assertEquals(url2, urls[0]) // Most recent URL should be first
        assertEquals(url1, urls[1])
    }

    @Test
    fun `urls list is immutable`() {
        // Arrange
        SharedUrlState.addUrl("https://example.com")

        // Act & Assert
        val urls = SharedUrlState.urls
        assertThrows(UnsupportedOperationException::class.java) {
            (urls as MutableList<String>).add("https://another.com")
        }
    }

    @Test
    fun `clear removes all URLs`() {
        // Arrange
        SharedUrlState.addUrl("https://example.com/1")
        SharedUrlState.addUrl("https://example.com/2")

        // Act
        SharedUrlState.clear()

        // Assert
        assertTrue(SharedUrlState.urls.isEmpty())
    }

    @Test
    fun `urls list is empty by default`() {
        // Assert
        assertTrue(SharedUrlState.urls.isEmpty())
    }

    @Test
    fun `addUrl handles duplicate URLs`() {
        // Arrange
        val url = "https://example.com"

        // Act
        SharedUrlState.addUrl(url)
        SharedUrlState.addUrl(url)

        // Assert
        val urls = SharedUrlState.urls
        assertEquals(2, urls.size)
        assertEquals(url, urls[0])
        assertEquals(url, urls[1])
    }

    @Test
    fun `addUrl handles empty URL`() {
        // Act
        SharedUrlState.addUrl("")

        // Assert
        val urls = SharedUrlState.urls
        assertEquals(1, urls.size)
        assertEquals("", urls[0])
    }

    @Test
    fun `addUrl validates URL format`() {
        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            SharedUrlState.addUrl("not-a-valid-url")
        }
    }

    @Test
    fun `addUrl accepts valid URLs`() {
        // Arrange
        val validUrls = listOf(
            "https://example.com",
            "http://example.com",
            "https://example.com/path",
            "https://example.com/path?query=value",
            "https://example.com:8080/path"
        )

        // Act & Assert
        validUrls.forEach { url ->
            try {
                SharedUrlState.addUrl(url)
                // If we get here, the URL was accepted
                assertTrue(true)
            } catch (e: Exception) {
                fail("Valid URL was rejected: $url")
            }
        }
    }
} 