package com.oscar.api.gist

import com.oscar.api.gist.model.File
import com.oscar.api.gist.model.Gist
import com.oscar.api.gist.util.BaseTest
import io.restassured.RestAssured.given
import io.restassured.mapper.ObjectMapperType
import org.apache.http.HttpStatus
import org.junit.Assert
import org.junit.Test


class CreateTest : BaseTest() {

    @Test
    fun createGistWithSingleFile() {
        val fileName = "single_file.txt"
        val file = File(
                content = "Just a simple content for the Single File of the createGistWithSingleFile Test."
        )

        val gist = Gist(
                description = "createGistWithSingleFile Description",
                public = true,
                files = hashMapOf(fileName to file)
        )

        val gistCount = getAllGists().count()

        val createdGist = createGist(gist)
        Assert.assertEquals("More than on Gist created!",
                gistCount + 1, getAllGists().count())
        Assert.assertEquals("Created gist description different from expected!",
                gist.description, createdGist.description)
        Assert.assertEquals("Created gist public attribute different from expected!",
                gist.public, createdGist.public)
        Assert.assertEquals("Created gist file content different from expected!",
                gist.files!![fileName]!!.content, createdGist.files!![fileName]!!.content)
    }

    @Test
    fun createGistWithMultipleFiles() {
        val file = File(
                content = "Just a simple content for the Multiple Files of the createGistWithMultipleFiles Test."
        )

        val gist = Gist(
                description = "createGistWithSingleFile Description",
                public = true,
                files = hashMapOf(
                        "multiple_files1.txt" to file,
                        "multiple_files2.txt" to file,
                        "multiple_files3.txt" to file,
                        "multiple_files4.txt" to file,
                        "multiple_files5.txt" to file
                )
        )

        val gistCount = getAllGists().count()

        val createdGist = createGist(gist)
        Assert.assertEquals("More than on Gist created!",gistCount + 1, getAllGists().count())
        Assert.assertEquals("Number of created files different from expected!",
                gist.files!!.count(), createdGist.files!!.count())
        Assert.assertEquals("Created gist description different from expected!",
                gist.description, createdGist.description)
        Assert.assertEquals("Created gist file content different from expected!",
                gist.public, createdGist.public)

        for (i in 1..5) {
            Assert.assertEquals("Created gist file content different from expected!",
                    gist.files["multiple_files$i.txt"]!!.content, createdGist.files["multiple_files$i.txt"]!!.content)
        }
    }

    @Test
    fun createGistStartAndUnStarIt() {
        deleteAllGists()
        val gist = createTestGist()

        starGist(gist.id)

        // Check that gist is starred
        given().
                spec(specsWithAuthorization).
        `when`().
                get("${gist.id}/star").
        then().
                statusCode(HttpStatus.SC_NO_CONTENT)

        // Unstar a gist
        given().
                spec(specsWithAuthorization).
        `when`().
                delete("${gist.id}/star").
        then().
                statusCode(HttpStatus.SC_NO_CONTENT)


        // Check that gist is not starred
        given().
                spec(specsWithAuthorization).
        `when`().
                get("${gist.id}/star").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND)
    }

    @Test
    fun canNotStarGistWithoutAuthorization() {
        deleteAllGists()
        val gist = createTestGist()

        given().
                spec(specsWithoutAuthorization).
        `when`().
                put("${gist.id}/star").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND)
    }

    @Test
    fun canNotCreateGistWithoutAuthorization() {
        given().
                spec(specsWithoutAuthorization).
                body(testGist, ObjectMapperType.GSON).
        `when`().
                post().
        then().
                statusCode(HttpStatus.SC_UNAUTHORIZED)
    }

    @Test
    fun gistDescriptionHasLimit() {
        val gist = testGist.copy(
                description = "This is the repeted description".repeat(100000)
        )

        given().
                spec(specsWithAuthorization).
                body(gist, ObjectMapperType.GSON).
        `when`().
                post().
        then().
                statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)

    }

}