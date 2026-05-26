package com.example.flickfind.data

import com.example.flickfind.data.model.Video
import com.example.flickfind.data.model.VideoResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoParsingTest {

    @Test
    fun `test parse video response json`() {
        val json = """
            {
              "id": 550,
              "results": [
                {
                  "iso_639_1": "en",
                  "iso_3166_1": "US",
                  "name": "Fight Club - Theatrical Trailer Remastered in HD",
                  "key": "6JnN1DmbqoU",
                  "site": "YouTube",
                  "size": 1080,
                  "type": "Trailer",
                  "official": false,
                  "published_at": "2015-02-26T03:19:25.000Z",
                  "id": "5e382d1b4ca20000160d9e0b"
                }
              ]
            }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(json, VideoResponse::class.java)

        assertEquals(550, response.id)
        assertEquals(1, response.results.size)
        
        val video = response.results[0]
        assertEquals("6JnN1DmbqoU", video.key)
        assertEquals("YouTube", video.site)
        assertEquals("Trailer", video.type)
        assertTrue(video.isYouTube)
        assertTrue(video.isTrailerOrTeaser)
        assertEquals("https://img.youtube.com/vi/6JnN1DmbqoU/hqdefault.jpg", video.thumbnailUrl)
    }
}
