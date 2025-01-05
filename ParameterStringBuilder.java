package sd.rest1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility class to build parameterized query strings.
 */
public class ParameterStringBuilder {

    /**
     * Converts a map of parameters into a URL-encoded query string.
     *
     * @param params the map containing query parameters.
     * @return a URL-encoded query string.
     * @throws UnsupportedEncodingException if the encoding is unsupported.
     */
    public static String buildQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(encode(entry.getKey()))
                       .append("=")
                       .append(encode(entry.getValue()))
                       .append("&");
        }

        // Remove the trailing '&' if it exists
        return queryString.substring(0, queryString.length() - 1);
    }

    /**
     * URL-encodes a value using UTF-8.
     *
     * @param value the value to encode.
     * @return the encoded value.
     * @throws UnsupportedEncodingException if the encoding is unsupported.
     */
    private static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}
