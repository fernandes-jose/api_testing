package com.oscar.api.gist.util

import com.oscar.api.gist.model.File
import com.oscar.api.gist.model.Gist
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.mapper.ObjectMapperType
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.junit.Assert

/**
 * This class is the parent of all Test classes.
 * It is responsible for setting up default configurations for all API calls and
 * declare objects and functions that will be used for more then one of
 * its children Test classes
 */
open class BaseTest {

    /** Stores the user authorization string that will be used on API call headers */
    private val authorization: String = "token ${Config.gistToken}"
    /** The default request specifications that will be used for all API calls for authorized user */
    val specsWithAuthorization: RequestSpecification
    /** The default request specifications that will be used for all API calls for not authorized user */
    val specsWithoutAuthorization: RequestSpecification
    /** A default object of the Gist class that will be used as test data */
    protected val testGist: Gist


    init {
        // Building the default specsWithAuthorization
        val specsBuilder = RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept("application/vnd.github.v3+json")

        val specsBuilderNot = RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept("application/vnd.github.v3+json")

        when(Config.env) {
            Environments.PROD -> {
                specsBuilder.setBaseUri("https://api.github.com")
                specsBuilder.setBasePath("/gists")
                specsBuilder.setPort(443)

                specsBuilderNot.setBaseUri("https://api.github.com")
                specsBuilderNot.setBasePath("/gists")
                specsBuilderNot.setPort(443)
            }
            Environments.LOCAL -> TODO()
            Environments.DEV -> TODO()
            Environments.STAGE -> TODO()
        }

        specsWithoutAuthorization = specsBuilderNot.build()

        specsBuilder.addHeader("Authorization", authorization)
        specsWithAuthorization = specsBuilder.build()

        // Creating the gist object with test data
        val defaultFile = File(
                content = "Just a simple content for a File."
        )
        testGist = Gist(
                description = "createGistWithSingleFile Description",
                public = true,
                files = hashMapOf(
                        "file1.txt" to defaultFile,
                        "file2.txt" to defaultFile
                )
        )
    }

    /**
     * Gets all Gists for the authorized user
     *
     * 'GET /gists'
     *
     * @return An array of Gist objects
     */
    fun getAllGists() : Array<Gist> {
        return given().
                    spec(specsWithAuthorization).
                `when`().
                    get().
                then().
                    statusCode(HttpStatus.SC_OK).
                extract().
                    `as`(Array<Gist>::class.java)
    }

    /**
     * Get a single Gist
     *
     * 'GET /gists/:gist_id'
     *
     * @return A single Gist object
     */
    fun getGist(id: String) : Gist {
        return given().
                    spec(specsWithAuthorization).
                `when`().
                    get(id).
                then().
                    statusCode(HttpStatus.SC_OK).
                extract().
                    `as`(Gist::class.java)
    }

    /**
     * Get the ID of the first gist returned by the getAllGists() function
     * Only get the Gists of the authorized user
     *
     * @return A string with the Gist ID
     */
    fun getFirstGistId() : String {
        if (getAllGists().count() <= 0) {
            throw Throwable("Problem to get ID! There are no Gists.")
        } else {
            return getAllGists()[0].id
        }
    }

    /**
     * Returns the ID the all Gists returned by getAllGists() function
     * Only get the Gists of the authorized user
     *
     * @return An array of Strings with all Gists ID
     */
    fun getAllGistsId() : List<String> {
        return getAllGists().map { it.id }
    }

    /**
     * Creates a single Gist
     *
     * 'POST /gists'
     *
     * @param create The Gist object that should be created
     *
     * @return Gist object of the created object
     */
    fun createGist(create: Gist) : Gist {
        return given().
                    spec(specsWithAuthorization).
                    body(create, ObjectMapperType.GSON).
                `when`().
                    post().
                then().
                    statusCode(HttpStatus.SC_CREATED).
                extract().
                    `as`(Gist::class.java)
    }

    /**
     * Creates the Test Gist calling the createGist function passing the
     * testGist object as parameter
     *
     * @return Gist object of the created object
     */
    fun createTestGist() : Gist {
        return createGist(testGist)
    }

    /**
     * Creates a specific number of Test Gists to reach some target count
     *
     * @param targetCount Target number of Gists that the user should have
     */
    fun createGistsIfNeeded(targetCount: Int) {
        val actualCount = getAllGists().count()
        if ( actualCount < targetCount) {
            for (i in actualCount until targetCount)
                createGist(testGist)
        }
    }

    /**
     * Start specific Gist
     *
     * @param id ID of the Gist to be starred
     */
    fun starGist(id: String) {
        given().
                spec(specsWithAuthorization).
        `when`().
                put("$id/star").
        then().
                statusCode(HttpStatus.SC_NO_CONTENT)
    }

    /**
     * Deletes a Gist by ID
     *
     * 'DELETE /gists/:gist_id'
     *
     * @param id The ID of the Gist to be deleted
     */
    fun deleteGist(id: String) {
        given().
                spec(specsWithAuthorization).
        `when`().
                delete(id).
        then().
                statusCode(HttpStatus.SC_NO_CONTENT)
    }

    /**
     * Deletes all Gists of the authorized user
     */
    fun deleteAllGists() {
        getAllGistsId().forEach { deleteGist(it) }
        Assert.assertEquals(0, getAllGists().count())
    }
}