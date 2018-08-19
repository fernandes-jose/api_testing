package com.oscar.api.gist

import com.oscar.api.gist.model.Gist
import com.oscar.api.gist.util.BaseTest
import io.restassured.RestAssured.given
import org.apache.http.HttpStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ReadTest : BaseTest() {

    @Before
    fun setup() {
        deleteAllGists()
    }

    @Test
    fun shouldReadEmptyAccount() {
        val gists = getAllGists()
        Assert.assertEquals("Authorized account should be empty!",0, gists.count())
    }

    @Test
    fun canReadAllGists() {
        createGistsIfNeeded(3)
        val gists = getAllGists()
        Assert.assertEquals(3, gists.count())

        Assert.assertNull("Get all gists should not return file content!", gists[0].files!!["file1.txt"]!!.content)
        Assert.assertNull("Get all gists should not return file content!", gists[0].files!!["file2.txt"]!!.content)
    }

    @Test
    fun canReadSingleGist() {
        createTestGist()
        val gist = getGist(getFirstGistId())
        Assert.assertEquals(2, gist.files!!.count())

        // When you read a single gist, you can get file content
        Assert.assertNotNull("Get single gist should returns file content!", gist.files["file1.txt"]!!.content)
        Assert.assertNotNull("Get single gist should returns file content!", gist.files["file2.txt"]!!.content)
        Assert.assertEquals("Content of the file of the fetched Gist different than expected!",
                testGist.files!!["file1.txt"]!!.content, gist.files["file1.txt"]!!.content)
        Assert.assertEquals("Content of the file of the fetched Gist different than expected!",
                testGist.files["file2.txt"]!!.content, gist.files["file2.txt"]!!.content)
    }

    @Test
    fun readPageWith30PublicGists() {
        given().
                spec(specsWithAuthorization).
        `when`().
                get("public").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", equalTo(30))
    }

    @Test
    fun perPageMaximumIs100() {
        given().
                spec(specsWithAuthorization).
                param("per_page", 200).
        `when`().
                get("public").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", equalTo(100))
    }

    @Test
    fun perPageMinimumIs1() {
        given().
                spec(specsWithAuthorization).
                param("per_page", 1).
        `when`().
                get("public").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", equalTo(1))
    }

    @Test
    fun perPageZeroIsEqualToDefault() {
        given().
                spec(specsWithAuthorization).
                param("per_page", 0).
        `when`().
                get("public").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", equalTo(30))
    }

    @Test
    fun listSpecificUserGists() {
        given().
                spec(specsWithAuthorization).
                basePath("users").
        `when`().
                get("oscartanner/gists").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", not(equalTo(0)))

        given().
                spec(specsWithoutAuthorization).
                basePath("users").
        `when`().
                get("oscartanner/gists").
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", not(equalTo(0)))
    }

    @Test
    fun listStaredGists() {
        val gist = createTestGist()
        starGist(gist.id)

        val starredGists =
                given().
                        spec(specsWithAuthorization).
                `when`().
                        get("starred").
                then().
                        statusCode(HttpStatus.SC_OK).
                extract().
                        `as`(Array<Gist>::class.java)


        Assert.assertEquals("More then 1 Gist starred!", 1, starredGists.count())
        Assert.assertEquals("Starred Gist different than expected!",
                gist.description, starredGists[0].description)
    }

    @Test
    fun readWithoutAuthorizationReturnsPublicGists() {
        given().
                spec(specsWithoutAuthorization).
        `when`().
                get().
        then().
                statusCode(HttpStatus.SC_OK).
                body("size", equalTo(30))
    }

    @Test
    fun canNotReadStarredGistsWithoutAuthorization() {
        given().
                spec(specsWithoutAuthorization).
        `when`().
                get("starred").
        then().
                statusCode(HttpStatus.SC_UNAUTHORIZED)
    }
}