package video.api.player.analytics.exoplayer.extensions

import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
import androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED
import androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED
import androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
import video.api.player.analytics.core.payload.ErrorCode

/**
 * Gets the corresponding analytics [ErrorCode]
 */
internal val PlaybackException.playerAnalyticsErrorCode: ErrorCode
    get() {
        return when (errorCode) {
            ERROR_CODE_IO_BAD_HTTP_STATUS, ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE, ERROR_CODE_IO_NETWORK_CONNECTION_FAILED, ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                ErrorCode.NETWORK
            }

            ERROR_CODE_DECODER_INIT_FAILED, ERROR_CODE_DECODER_QUERY_FAILED, ERROR_CODE_DECODING_FAILED, ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> {
                ErrorCode.DECODING
            }

            else -> {  // ERROR_CODE_DECODING_FORMAT_UNSUPPORTED, ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED, ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED
                ErrorCode.NO_SUPPORT
            }
        }
    }