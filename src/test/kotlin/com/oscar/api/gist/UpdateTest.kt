package com.oscar.api.gist

import com.oscar.api.gist.model.File
import com.oscar.api.gist.model.Gist
import com.oscar.api.gist.util.BaseTest
import io.restassured.RestAssured.given
import io.restassured.mapper.ObjectMapperType
import org.apache.http.HttpStatus
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateTest : BaseTest() {
    private lateinit var oldGist : Gist

    @Before
    fun setup() {
        deleteAllGists()
        createTestGist()
        oldGist = getGist(getFirstGistId())
    }

    @Test
    fun updateDescription() {
        val gist = oldGist.copy(
                description = "This is the new description",
                files = hashMapOf()
        )

        val updatedGist = updateGist(gist)

        Assert.assertEquals("ID of the updated Gist is different!", gist.id, updatedGist.id)
        Assert.assertEquals("Updated description different from expected!",
                gist.description, updatedGist.description)
        Assert.assertNotEquals("Description not updated!", oldGist.description, updatedGist.description)
        Assert.assertEquals("Number of files changed!", oldGist.files!!.count(), updatedGist.files!!.count())
    }

    @Test
    fun updateAddFiles() {
        val newFiles = hashMapOf(
                "new_file1.txt" to File(content = "This is the content of the new file 1"),
                "new_file2.txt" to File(content = "This is the content of the new file 2")
        )
        val gist = oldGist.copy(files = newFiles)


        val updatedGist = updateGist(gist)

        Assert.assertEquals("ID of the updated Gist is different!", gist.id, updatedGist.id)
        Assert.assertEquals("Description changed!", gist.description, updatedGist.description       )
        Assert.assertEquals("Description changed!", oldGist.description, updatedGist.description)
        Assert.assertEquals("Files not added as expected!",
                gist.files!!.count() + 2, updatedGist.files!!.count())
        Assert.assertEquals("File content different from expected!",
                newFiles["new_file1.txt"]!!.content, updatedGist.files["new_file1.txt"]!!.content)
        Assert.assertEquals("File content different from expected!",
                newFiles["new_file2.txt"]!!.content, updatedGist.files["new_file2.txt"]!!.content)
    }

    @Test
    fun updateChangeFileName() {
        val file = hashMapOf(
                "file1.txt" to File(
                        filename = "new_file_name1.txt"
                )
        )
        val gist = oldGist.copy(files = file)

        val updatedGist = updateGist(gist)

        Assert.assertEquals("ID of the updated Gist is different!", gist.id, updatedGist.id)
        Assert.assertEquals("Description changed!", gist.description, updatedGist.description)
        Assert.assertEquals("Description changed!", oldGist.description, updatedGist.description)
        Assert.assertEquals("Number of files changed!",2, updatedGist.files!!.count())
        Assert.assertNotNull("File was not renamed!", updatedGist.files["new_file_name1.txt"])
        Assert.assertNotNull("File2 changed!", updatedGist.files["file2.txt"])
        Assert.assertNull("File1 was not renamed!", updatedGist.files["file1.txt"])
    }

    @Test
    fun updateFileContent() {
        val file = hashMapOf(
                "file1.txt" to File(
                        content = "This is the new content."
                )
        )
        val gist = oldGist.copy(files = file)

        val updatedGist = updateGist(gist)

        Assert.assertEquals("ID of the updated Gist is different!", gist.id, updatedGist.id)
        Assert.assertEquals("Description changed!", gist.description, updatedGist.description)
        Assert.assertEquals("Description changed!", oldGist.description, updatedGist.description)
        Assert.assertEquals("Number of files changed!",2, updatedGist.files!!.count())
        Assert.assertEquals("File1 content was not updated!",
                gist.files!!["file1.txt"]!!.content, updatedGist.files["file1.txt"]!!.content)
        Assert.assertNotEquals("File1 content was not updated!",
                oldGist.files!!["file1.txt"]!!.content, updatedGist.files["file1.txt"]!!.content)
        Assert.assertEquals("File2 was updated!",
                oldGist.files!!["file2.txt"]!!.content, updatedGist.files["file2.txt"]!!.content)
    }

    @Test
    fun updateDeleteFile() {
        val file = hashMapOf(
                "file1.txt" to File(
                        filename = null
                )
        )
        val gist = oldGist.copy(files = file)

        val updatedGist = updateGist(gist)

        Assert.assertEquals("ID of the updated Gist is different!", gist.id, updatedGist.id)
        Assert.assertEquals("Description changed!", gist.description, updatedGist.description)
        Assert.assertEquals("Description changed!", oldGist.description, updatedGist.description)
        Assert.assertEquals("File1 was not deleted!",1, updatedGist.files!!.count())
        Assert.assertNull("File1 was not deleted!", updatedGist.files["file1.txt"])
        Assert.assertEquals("File2 was updated!",
                oldGist.files!!["file2.txt"]!!.content, updatedGist.files["file2.txt"]!!.content)
    }

    @Test
    fun canNotUpdateGistWithoutAuthorization() {
        given().
                spec(specsWithoutAuthorization).
        `when`().
                patch(oldGist.id).
        then().
                statusCode(HttpStatus.SC_NOT_FOUND)
    }

    /**
     * Updates some Gist
     *
     * 'PATCH /gists/:gist_id'
     *
     * @param gist Object that represents the Gist to be updated
     *
     * @return The gist returned by the update API
     */
    private fun updateGist(gist: Gist) : Gist {
        return given().
                spec(specsWithAuthorization).
                    body(gist, ObjectMapperType.GSON).
                `when`().
                    patch(gist.id).
                then().
                    statusCode(HttpStatus.SC_OK).
                extract().
                    `as`(Gist::class.java)
    }
}