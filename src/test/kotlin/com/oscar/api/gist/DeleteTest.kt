package com.oscar.api.gist

import com.oscar.api.gist.util.BaseTest
import io.restassured.RestAssured.given
import org.apache.http.HttpStatus
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeleteTest : BaseTest() {

    @Before
    fun setup() {
        createGistsIfNeeded(3)
    }

    @Test
    fun deleteOnlyFirstGist() {
        val gistCount = getAllGists().count()
        deleteGist(getFirstGistId())
        Assert.assertEquals("Problem to delete just one Gist!",
                gistCount - 1, getAllGists().count())
    }

    @Test
    fun canDeleteAllGists() {
        Assert.assertTrue(getAllGistsId().count() > 0)
        deleteAllGists()
        Assert.assertEquals("Not all Gists deleted!",
                0, getAllGists().count())
    }

    @Test
    fun canNotDeleteGistWithoutAuthentication() {
        given().
                spec(specsWithoutAuthorization).
        `when`().
                delete(getFirstGistId()).
        then().
                statusCode(HttpStatus.SC_NOT_FOUND)
    }
}