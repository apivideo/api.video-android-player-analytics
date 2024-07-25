object AndroidVersions {
    const val MIN_SDK = 21
    const val TARGET_SDK = 34
    const val COMPILE_SDK = 34
}

object Publication {
    object Repository {
        val username: String?
            get() = Property.get(Property.SonatypeUsername)
        val password: String?
            get() = Property.get(Property.SonatypePassword)
    }

    object Pom {
        const val PACKAGING = "aar"
        const val URL = "https://api.video"

        object Scm {
            const val CONNECTION = "scm:git:git@github.com:apivideo/api.video-android-player-analytics.git"
            const val DEVELOPER_CONNECTION =
                "scm:git:github.com:apivideo/api.video-android-player-analytics.git"
            const val URL = "https://github.com/apivideo/api.video-android-player-analytics/"
        }

        object License {
            const val NAME = "MIT Licence"
            const val URL = "https://opensource.org/licenses/mit-license.php"
            const val DISTRIBUTION = "repo"
        }

        object Developer {
            const val ID = "api.video"
            const val NAME = "Ecosystem Team"
        }
    }

    object Signing {
        val canSign: Boolean
            get() = keyId != null && password != null

        private val password: String?
            get() = Property.get(Property.GpgPassword)
        private val keyId: String?
            get() = Property.get(Property.GpgKeyId)
    }
}