package org.entando.kubernetes;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class DigitalExchangeTestUtils {

    private static String DE_PUBLIC_KEY;
    private static String DE_PRIVATE_KEY;

    public static String getTestPublicKey() {
        if (Strings.isEmpty(DE_PUBLIC_KEY)) {
            try {
                final Path publicKeyPath = Paths.get(DigitalExchangeTestUtils.class.getResource("/de_test_public_key.txt").toURI());
                DE_PUBLIC_KEY = new String(Files.readAllBytes(publicKeyPath));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return DE_PUBLIC_KEY;
    }

    public static String getTestPrivateKey() {
        if (Strings.isEmpty(DE_PRIVATE_KEY)) {
            try {
                final Path publicKeyPath = Paths.get(DigitalExchangeTestUtils.class.getResource("/de_test_private_key.txt").toURI());
                DE_PRIVATE_KEY = new String(Files.readAllBytes(publicKeyPath));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return DE_PRIVATE_KEY;
    }

    public static String readFileAsBase64(String filePath) throws Exception {
        final Path path = Paths.get(DigitalExchangeTestUtils.class.getResource(filePath).toURI());
        return Base64.getEncoder().encodeToString(Files.readAllBytes(path));
    }

    public static String readFile(String filePath) throws Exception {
        final Path path = Paths.get(DigitalExchangeTestUtils.class.getResource(filePath).toURI());
        return new String(Files.readAllBytes(path));
    }

    public static RequestChecker checkRequest(final LoggedRequest loggedRequest) {
        System.out.println(new String(loggedRequest.getBody()));
        final DocumentContext context = JsonPath.parse(new String(loggedRequest.getBody()));
        return new RequestChecker(context);
    }

    public static class RequestChecker {
        private final DocumentContext context;

        private RequestChecker(final DocumentContext context) {
            this.context = context;
        }

        public RequestChecker expectEqual(final String jsonPath, final Object value) {
            Assertions.assertThat(context.read(jsonPath, value.getClass())).isEqualTo(value);
            return this;
        }
    }

}
