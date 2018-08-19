# api_testing

Tests with REST-assured and Kotlin for the Gist part of the [Github API](https://developer.github.com/v3/gists/).

All tests can be found on folder `src/test/kotlin/com/oscar/api/gist/`

## Important - User Authorization

To avoid security breaches the user authorization token is not stored on the source code.

To run the tests, you will need to create a System Environment Variable called `GIST_TOKEN`.

If you want to use a different name for the token env variable, please update the reference on the class `src/test/kotlin/com/oscar/api/gist/util/Config`