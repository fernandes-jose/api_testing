package com.oscar.api.gist.util

/**
 * Class with default configurations
 */
class Config {
    companion object {
        /** The environment of the API that should be used during test execution */
        val env: Environments = Environments.PROD
        /** The token of the authorized user that is stored as a System Environment variable */
        val gistToken: String = System.getenv("GIST_TOKEN")
    }
}