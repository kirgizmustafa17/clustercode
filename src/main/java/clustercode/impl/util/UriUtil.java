package clustercode.impl.util;

import java.net.URI;

public class UriUtil {

    private UriUtil(){}

    public static String stripCredentialFromUri(URI uri) {
        return uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
    }

}
